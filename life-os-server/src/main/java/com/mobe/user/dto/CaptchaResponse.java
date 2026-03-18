package com.mobe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CaptchaResponse {

    private String captchaId;
    private String captchaCode;
    private LocalDateTime expiresAt;
}