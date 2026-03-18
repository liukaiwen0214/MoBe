package com.mobe.user.dto;

import lombok.Data;

@Data
public class UserPreferencesResponse {

    private String theme;
    private String language;
    private String defaultHomepage;
    private String dateFormat;
    private String timeFormat;
    private String weekStartDay;
    private Integer notificationEnabled;
    private Integer emailNotificationEnabled;
}