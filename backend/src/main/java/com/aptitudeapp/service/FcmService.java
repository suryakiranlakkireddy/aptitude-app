package com.aptitudeapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Talks to Firebase Cloud Messaging's HTTP v1 API directly (no firebase-admin SDK
 * dependency - that pulls in gRPC/Guava versions that fight with Spring Boot 3.3's
 * own, so we do the two HTTP calls it takes ourselves):
 *
 *  1. Exchange the Firebase service-account key for a short-lived OAuth2 access
 *     token (self-signed JWT assertion -> Google's token endpoint). Reuses the
 *     jjwt library already in pom.xml for JWT auth, which supports RS256 signing
 *     the same way it already signs this app's own HS256 login tokens.
 *  2. POST the actual push to https://fcm.googleapis.com/v1/projects/{id}/messages:send
 *
 * Requires FIREBASE_SERVICE_ACCOUNT_JSON to be set to the *full contents* of a
 * Firebase service account key file (Firebase Console -> Project Settings ->
 * Service Accounts -> Generate new private key). Until that's set, every method
 * here throws IllegalStateException - callers (NotificationService) catch that
 * and skip sending rather than crash a request or the scheduled job.
 */
@Service
public class FcmService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.firebase.service-account-json:}")
    private String serviceAccountJson;

    private volatile String cachedAccessToken;
    private volatile Instant cachedAccessTokenExpiry = Instant.EPOCH;

    public enum SendResult { SENT, INVALID_TOKEN, ERROR }

    private JsonNode serviceAccount() {
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            throw new IllegalStateException(
                    "Push notifications are not configured. Set FIREBASE_SERVICE_ACCOUNT_JSON " +
                            "to the contents of a Firebase service account key file.");
        }
        try {
            return objectMapper.readTree(serviceAccountJson);
        } catch (Exception e) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_JSON is not valid JSON", e);
        }
    }

    private synchronized String accessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(cachedAccessTokenExpiry)) {
            return cachedAccessToken;
        }
        JsonNode sa = serviceAccount();
        String clientEmail = sa.get("client_email").asText();
        String tokenUri = sa.has("token_uri") ? sa.get("token_uri").asText() : "https://oauth2.googleapis.com/token";
        PrivateKey privateKey = parsePrivateKey(sa.get("private_key").asText());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 3600_000L);
        String assertion = Jwts.builder()
                .issuer(clientEmail)
                .subject(clientEmail)
                .claim("aud", tokenUri)
                .claim("scope", "https://www.googleapis.com/auth/firebase.messaging")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();

        try {
            String form = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8)
                    + "&assertion=" + URLEncoder.encode(assertion, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUri))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Google OAuth2 token exchange failed (" + response.statusCode() + "): " + response.body());
            }
            JsonNode body = objectMapper.readTree(response.body());
            cachedAccessToken = body.get("access_token").asText();
            int expiresInSeconds = body.has("expires_in") ? body.get("expires_in").asInt() : 3600;
            cachedAccessTokenExpiry = Instant.now().plusSeconds(Math.max(expiresInSeconds - 60, 60));
            return cachedAccessToken;
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to obtain FCM access token", e);
        }
    }

    /** Sends a push notification to a single FCM registration token. */
    public SendResult sendToToken(String deviceToken, String title, String body) {
        JsonNode sa = serviceAccount();
        String projectId = sa.get("project_id").asText();
        String accessToken = accessToken();

        try {
            Map<String, Object> notification = Map.of("title", title, "body", body);
            Map<String, Object> message = Map.of("token", deviceToken, "notification", notification);
            String payload = objectMapper.writeValueAsString(Map.of("message", message));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 == 2) {
                return SendResult.SENT;
            }
            String responseBody = response.body() != null ? response.body() : "";
            if (response.statusCode() == 404 || responseBody.contains("UNREGISTERED") || responseBody.contains("NOT_FOUND")) {
                return SendResult.INVALID_TOKEN;
            }
            return SendResult.ERROR;
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return SendResult.ERROR;
        }
    }

    private PrivateKey parsePrivateKey(String pem) {
        try {
            String cleaned = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(cleaned);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse private_key from FIREBASE_SERVICE_ACCOUNT_JSON", e);
        }
    }
}
