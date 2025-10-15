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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
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

import java.nio.charset.StandardCharsets;
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
                        // Cho phép preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public auth endpoints + toàn bộ /api (giữ như bạn đang có)
                        .requestMatchers(
                                "/api/lego-store/user/register",
                                "/api/lego-store/user/login",
                                "/api/lego-store/user/loginBasic",
                                "/api/lego-store/user/forgot-password",
                                "/api/lego-store/user/verify-otp",
                                "/api/lego-store/user/reset-password",
                                "/api/**"
                        ).permitAll()

                        .requestMatchers("/ws-game/**").permitAll()

                        // San pham
                        .requestMatchers(HttpMethod.GET, "/api/sanpham/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sanpham/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/sanpham/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/sanpham/**").hasRole(admin)

                        // Khuyen mai
                        .requestMatchers(HttpMethod.GET, "/api/khuyenmai/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.POST, "/api/khuyenmai/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/khuyenmai/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/khuyenmai/**").hasRole(admin)

                        // Danh muc
                        .requestMatchers(HttpMethod.GET, "/api/danhmuc/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/danhmuc/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/danhmuc/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/danhmuc/**").hasRole(admin)

                        // Ảnh SP
                        .requestMatchers(HttpMethod.GET, "/api/anhsp/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/anhsp/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/anhsp/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/anhsp/**").hasRole(admin)

                        // Bộ sưu tập
                        .requestMatchers(HttpMethod.GET, "/api/bosuutap/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bosuutap/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/bosuutap/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/bosuutap/**").hasRole(admin)

                        // Phiếu giảm giá
                        .requestMatchers(HttpMethod.GET, "/api/phieugiamgia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/phieugiamgia/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/phieugiamgia/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/phieugiamgia/**").hasRole(admin)

                        // Giỏ hàng
                        .requestMatchers(HttpMethod.GET, "/api/giohang/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/giohang/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/giohang/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/giohang/**").permitAll()

                        // Hóa đơn (đã thêm dấu /)
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/hoa-don/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/hoa-don/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/hoa-don/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/hoa-don/**").hasRole(admin)

                        // Hóa đơn chi tiết
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/hoa-don-chi-tiet/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/hoa-don-chi-tiet/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/hoa-don-chi-tiet/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/hoa-don-chi-tiet/**").hasRole(admin)

                        // Thông tin người nhận
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/thong-tin-nguoi-nhan/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/thong-tin-nguoi-nhan/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/thong-tin-nguoi-nhan/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/thong-tin-nguoi-nhan/**").hasRole(user)

                        // User
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/user/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/user/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/user/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/user/**").hasRole(admin)

                        // Thương hiệu
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/thuong-hieu/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/thuong-hieu/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/thuong-hieu/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/thuong-hieu/**").hasRole(admin)

                        // Xuất xứ
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/xuatXu/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/xuatXu/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/xuatXu/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/xuatXu/**").hasRole(admin)

                        // Đánh giá (fix double-slash)
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/danh-gia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/danh-gia/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/danh-gia/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/danh-gia/**").hasAnyRole(admin, user, staff)

                        // Hoàn hàng
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/hoan-hang/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/hoan-hang/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/hoan-hang/**").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/hoan-hang/**").hasRole(admin)

                        // Lịch sử log
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/lich-su-log/**").hasAnyRole(admin, staff)
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/lich-su-log/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/lich-su-log/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/lich-su-log/**").hasRole(admin)

                        // Yêu thích
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/san-pham-yeu-thich/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/san-pham-yeu-thich/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/san-pham-yeu-thich/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/san-pham-yeu-thich/**").hasAnyRole(admin, user)

                        // Thống kê
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/thong-ke/**").hasAnyRole(admin)

                        // Hàng thanh lý
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/hang-thanh-ly").hasAnyRole(admin)

                        // Ví phiếu giảm
                        .requestMatchers(HttpMethod.GET, "/api/lego-store/vi-phieu-giam-gia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lego-store/vi-phieu-giam-gia/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.PUT, "/api/lego-store/vi-phieu-giam-gia/**").hasAnyRole(admin, staff, user)
                        .requestMatchers(HttpMethod.DELETE, "/api/lego-store/vi-phieu-giam-gia/**").hasRole(admin)

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    // ✅ Cấu hình CORS cho phép truy cập từ các origin khác (frontend...)
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
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

    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}