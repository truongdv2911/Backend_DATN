package com.example.demo.Configuration;

import com.example.demo.Entity.Role;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.Filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@RequiredArgsConstructor
public class WebConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            String body = "{\"status\":403,\"message\":\"Bạn không có quyền truy cập tài nguyên này\"}";
            response.getWriter().write(body);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         String admin = roleRepository.findById(1).orElse(null).getName();
         String staff = roleRepository.findById(2).orElse(null).getName();
         String user = roleRepository.findById(3).orElse(null).getName();
        http.csrf(AbstractHttpConfigurer::disable)
         .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/api/lego-store/user/register",
                                "/api/lego-store/user/login",
                                "/api/lego-store/user/loginBasic",
                                "/api/**"
                        ).permitAll()
//                        .requestMatchers(HttpMethod.GET, ("/api/sanpham/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/sanpham/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/sanpham/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/sanpham/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/khuyenmai/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.POST, ("/api/khuyenmai/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/khuyenmai/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/khuyenmai/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/danhmuc/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/danhmuc/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/danhmuc/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/danhmuc/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/anhsp/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/anhsp/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/anhsp/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/anhsp/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/bosuutap/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/bosuutap/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/bosuutap/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/bosuutap/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/phieugiamgia/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/phieugiamgia/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.PUT, ("/api/phieugiamgia/**")).hasAnyRole(admin, staff)
//                        .requestMatchers(HttpMethod.DELETE, ("/api/phieugiamgia/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("/api/giohang/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("/api/giohang/**")).permitAll()
//                        .requestMatchers(HttpMethod.PUT, ("/api/giohang/**")).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, ("/api/giohang/**")).permitAll()
//
//                        .requestMatchers(HttpMethod.GET, ("api/lego-store/hoa-don/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("api/lego-store/hoa-don/**")).permitAll()
//                        .requestMatchers(HttpMethod.PUT, ("api/lego-store/hoa-don/**")).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, ("api/lego-store/hoa-don/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("api/lego-store/hoa-don-chi-tiet/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("api/lego-store/hoa-don-chi-tiet/**")).permitAll()
//                        .requestMatchers(HttpMethod.PUT, ("api/lego-store/hoa-don-chi-tiet/**")).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, ("api/lego-store/hoa-don-chi-tiet/**")).hasRole(admin)
//
//                        .requestMatchers(HttpMethod.GET, ("api/lego-store/thong-tin-nguoi-nhan/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("api/lego-store/thong-tin-nguoi-nhan/**")).permitAll()
//                        .requestMatchers(HttpMethod.PUT, ("api/lego-store/thong-tin-nguoi-nhan/**")).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, ("api/lego-store/thong-tin-nguoi-nhan/**")).hasRole(user)
//
//                        .requestMatchers(HttpMethod.GET, ("api/lego-store/user/**")).permitAll()
//                        .requestMatchers(HttpMethod.POST, ("api/lego-store/user/**")).permitAll()
//                        .requestMatchers(HttpMethod.PUT, ("api/lego-store/user/**")).permitAll()
//                        .requestMatchers(HttpMethod.DELETE, ("api/lego-store/user/**")).hasRole(admin)
                        .anyRequest().authenticated()
                )
        .exceptionHandling(exception -> exception
            .accessDeniedHandler(accessDeniedHandler())
        );
//                .oauth2Login(oauth -> oauth
//                    .defaultSuccessUrl("/api/lego-store/user/success", true)
//                    .failureUrl("/api/lego-store/user/login?error=true")
//                )
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // ✅ Cấu hình CORS cho phép truy cập từ các origin khác (frontend...)
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Auth-Token"));
        corsConfiguration.setExposedHeaders(List.of("X-Auth-Token"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return customOAuth2UserService;
    }
}