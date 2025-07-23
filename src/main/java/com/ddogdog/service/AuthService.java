package com.ddogdog.service;

import com.ddogdog.dto.LoginRequest;
import com.ddogdog.dto.LoginResponse;
import com.ddogdog.dto.User;
import com.ddogdog.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AuthService {

    @Value("${kakao.client.id}")
    private String clientId;

    @Value("${kakao.client.secret}")
    private String clientSecret;

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    private final WebClient webClient;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthService(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.webClient = WebClient.builder()
                .baseUrl(kakaoApiUrl)
                .build();
    }

    public LoginResponse kakaoLogin(String code) {
        // 1. 카카오 액세스 토큰 얻기
        String accessToken = getKakaoAccessToken(code);
        
        // 2. 카카오 사용자 정보 얻기
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
        
        // 3. 사용자 정보로 회원가입/로그인 처리
        Long kakaoId = ((Number) userInfo.get("id")).longValue();
        Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
        String nickname = (String) properties.get("nickname");
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        
        // 기존 회원인지 확인하고 없으면 회원가입
        User user = userService.findByKakaoId(kakaoId);
        if (user == null) {
            user = new User();
            user.setUserId("K" + kakaoId); // 카카오 회원은 K prefix
            user.setUsername(nickname);
            user.setEmail(email);
            user.setPassword(""); // 카카오 로그인은 비밀번호 불필요
            user.setStatus("A");
            user.setKakaoId(kakaoId);
            userService.register(user);
        }
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getUserId());
        return new LoginResponse(token);
    }

    public String authenticate(String username, String password) {
        User user = userService.findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            return jwtUtil.generateToken(user.getUserId());
        }
        throw new RuntimeException("Invalid credentials");
    }

    private String getKakaoAccessToken(String code) {
        Map<String, Object> response = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .bodyValue(Map.of(
                        "grant_type", "authorization_code",
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
