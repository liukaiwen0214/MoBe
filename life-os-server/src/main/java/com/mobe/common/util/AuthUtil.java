package com.mobe.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class AuthUtil {

    private AuthUtil() {
    }

    public static String getBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7).trim();
    }
}