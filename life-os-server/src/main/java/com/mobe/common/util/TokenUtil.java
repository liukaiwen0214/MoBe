package com.mobe.common.util;

import java.util.UUID;

public class TokenUtil {

    private TokenUtil() {
    }

    public static String generateSessionToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}