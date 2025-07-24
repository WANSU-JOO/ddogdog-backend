package com.ddogdog.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String userId;
    private String username;
    private String email;
    private String status;
    private Long kakaoId;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.kakaoId = user.getKakaoId();
    }
}
