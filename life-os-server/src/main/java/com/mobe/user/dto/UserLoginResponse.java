package com.mobe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLoginResponse {

    private String userId;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;

    private String sessionToken;
    private LocalDateTime expiresAt;
}