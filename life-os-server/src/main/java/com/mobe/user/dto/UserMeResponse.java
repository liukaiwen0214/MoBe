package com.mobe.user.dto;

import lombok.Data;

@Data
public class UserMeResponse {

    private String id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String status;
}