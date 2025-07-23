package com.ddogdog.dto;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String username;
    private String password;
    private String email;
    private String status;
    private Long kakaoId;
}
