package com.mobe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SendCodeResponse {

    private String codeId;
    private String code;
    private LocalDateTime expiresAt;
}