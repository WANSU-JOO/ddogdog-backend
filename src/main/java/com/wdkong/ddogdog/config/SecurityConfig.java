package com.wdkong.ddogdog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.wdkong.ddogdog.jwt.JwtAuthenticationFilter;
import com.wdkong.ddogdog.jwt.JwtTokenProvider;
/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * JWT Token Provider
     */

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Spring Security의 핵심 필터 체인을 정의합니다.
     * 이 Bean을 통해 HTTP 요청에 대한 보안 설정을 구성합니다.
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain Bean
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF 보호 비활성화 (Stateless한 REST API에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Form 로그인 및 HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 4. 세션 관리를 Stateless로 설정 (세션을 사용하지 않음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. 요청 경로에 대한 인가 규칙 설정
                .authorizeHttpRequests(auth -> auth
                        // 아래 경로들은 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/auth/**", // 로그인, 회원가입 등 인증 관련 API
                                "/swagger-ui/**", // Swagger UI
                                "/v3/api-docs/**"  // OpenAPI 명세
                        ).permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                // 6. JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 넣는다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) 설정을 정의합니다.
     * 프론트엔드 애플리케이션과의 통신을 위해 필요합니다.
     *
     * @return CorsConfigurationSource Bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 애플리케이션의 주소를 허용합니다.
        // 개발 환경에서는 localhost, 운영 환경에서는 실제 도메인을 추가해야 합니다.
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드를 설정합니다.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 HTTP 헤더를 설정합니다.
        configuration.setAllowedHeaders(List.of("*"));

        // 자격 증명(쿠키, 인증 헤더 등)을 허용합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }
}