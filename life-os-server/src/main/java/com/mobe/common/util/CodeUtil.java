package com.mobe.common.util;

import java.util.Random;

public class CodeUtil {

    private static final Random RANDOM = new Random();

    private CodeUtil() {
    }

    public static String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}