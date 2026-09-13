package com.aptitudeapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Thin wrapper around the Supabase Storage REST API (no official Java SDK exists,
 * so we talk to https://{project}.supabase.co/storage/v1/... directly).
 *
 * Uses the project's service_role key, which bypasses Storage RLS entirely - so
 * access control for end users happens in our own controllers (JWT auth + role
 * checks), and this service never hands back the service key or a permanent
 * public URL, only short-lived signed URLs generated on demand.
 */
@Service
public class SupabaseStorageService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-key}")
    private String serviceKey;

    @Value("${app.supabase.bucket}")
    private String bucket;

    private void assertConfigured() {
        if (isBlank(supabaseUrl) || isBlank(serviceKey)) {
            throw new IllegalStateException(
                    "Supabase Storage is not configured. Set SUPABASE_URL and SUPABASE_SERVICE_KEY env vars " +
                            "(see backend/src/main/resources/application.yml -> app.supabase.*).");
        }
    }

    /**
     * Uploads a file under a namespaced folder (e.g. "notes/{topicId}") with a random,
     * collision-proof name, and returns the resulting object path inside the bucket.
     * The object path - NOT a public URL - is what should be persisted in the DB.
     */
    public String upload(MultipartFile file, String folder) {
        assertConfigured();
        try {
            String safeName = UUID.randomUUID() + "-" + sanitizeFilename(file.getOriginalFilename());
            String objectPath = folder + "/" + safeName;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(objectPath)))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .header("x-upsert", "true")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Supabase upload failed (" + response.statusCode() + "): " + response.body());
            }
            return objectPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file to Supabase Storage", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Upload to Supabase Storage was interrupted", e);
        }
    }

    /** Generates a time-limited signed URL for a previously uploaded object path. */
    public String createSignedUrl(String objectPath, int expiresInSeconds) {
        assertConfigured();
        try {
            String body = objectMapper.writeValueAsString(Map.of("expiresIn", expiresInSeconds));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + encodePath(objectPath)))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Supabase sign-URL request failed (" + response.statusCode() + "): " + response.body());
            }
            JsonNode node = objectMapper.readTree(response.body());
            String signedPath = node.get("signedURL").asText();
            return supabaseUrl + "/storage/v1" + signedPath;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create signed URL for " + objectPath, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Signed URL request was interrupted", e);
        }
    }

    /** Best-effort delete, e.g. when a Note is removed or a partial upload needs cleanup. */
    public void delete(String objectPath) {
        if (isBlank(objectPath) || isBlank(supabaseUrl) || isBlank(serviceKey)) return;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + encodePath(objectPath)))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String sanitizeFilename(String name) {
        if (isBlank(name)) return "file";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String encodePath(String path) {
        StringBuilder sb = new StringBuilder();
        for (String segment : path.split("/")) {
            if (sb.length() > 0) sb.append("/");
            sb.append(URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return sb.toString();
    }
}
