package com.ddogdog.controller;

import com.ddogdog.dto.LoginRequest;
import com.ddogdog.dto.LoginResponse;
import com.ddogdog.service.AuthService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/kakao")
    public LoginResponse kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        return authService.kakaoLogin(code);
    }
}
