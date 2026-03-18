package com.mobe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSessionResponse {

    private String sessionId;
    private String deviceType;
    private String clientIp;
    private String userAgent;
    private Integer rememberMe;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime createdAt;
    private Boolean current;
}