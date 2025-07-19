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
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/kakao")
    public LoginResponse kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        System.err.println(code);
        return new LoginResponse("kakao_token");
    }
}
