package com.aptitudeapp.dto;

import lombok.Data;

@Data
public class RegisterDeviceTokenRequest {
    private String token;
    private String platform; // ANDROID | IOS | WEB
}
