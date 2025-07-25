package com.ddogdog.service;

import com.ddogdog.dto.LoginRequest;
import com.ddogdog.dto.LoginResponse;
import com.ddogdog.dto.User;
import com.ddogdog.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
        try {
            // 1. 카카오 액세스 토큰 얻기
            String accessToken = getKakaoAccessToken(code);
            
            // 2. 카카오 사용자 정보 얻기
            Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
            
            // 3. 사용자 정보로 회원가입/로그인 처리
            Long kakaoId = ((Number) userInfo.get("id")).longValue();
            Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            
            if (properties == null || kakaoAccount == null) {
                throw new RuntimeException("필수 카카오 계정 정보를 가져올 수 없습니다.");
            }
            
            String nickname = (String) properties.get("nickname");
            String email = (String) kakaoAccount.get("email");
            
            if (nickname == null || email == null) {
                throw new RuntimeException("카카오 계정의 닉네임 또는 이메일 정보가 없습니다.");
            }
            
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
            return new LoginResponse(token, user);
        } catch (Exception e) {
            System.err.println("카카오 로그인 처리 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("카카오 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user != null && request.getPassword().equals(user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserId());
            return new LoginResponse(token, user);
        }
        throw new RuntimeException("Invalid credentials");
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            jwtUtil.addToBlacklist(jwt);
        }
    }

    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:5173/oauth/kakao/callback");
        
        System.out.println("Kakao Token Request Params: " + params);
        
        try {
            Map<String, Object> response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(error -> {
                                        System.err.println("Kakao API Error Response: " + error);
                                        return new RuntimeException(error);
                                    }))
                    .bodyToMono(Map.class)
                    .block();
            
            System.out.println("Kakao Token Response: " + response);
            return (String) response.get("access_token");
        } catch (Exception e) {
            System.err.println("Kakao Token Error: " + e.getMessage());
            throw e;
        }
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        try {
            System.out.println("Requesting Kakao user info with token: " + accessToken);
            Map<String, Object> userInfo = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            System.out.println("Kakao User Info Response: " + userInfo);
            return userInfo;
        } catch (Exception e) {
            System.err.println("Error getting Kakao user info: " + e.getMessage());
            throw e;
        }
    }
}
