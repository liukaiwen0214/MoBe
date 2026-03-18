package com.mobe.user.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String nickname;

    private String avatarUrl;
}