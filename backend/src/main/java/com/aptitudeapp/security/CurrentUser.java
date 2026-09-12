package com.aptitudeapp.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

// Helper to pull the authenticated user's id out of the JWT-backed SecurityContext.
public class CurrentUser {
    public static Long id() {
        var auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getDetails();
    }

    public static String email() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
