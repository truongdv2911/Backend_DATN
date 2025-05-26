package com.example.demo.Configuration;

<<<<<<< HEAD
<<<<<<< HEAD
import com.example.demo.Service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
=======
import com.example.demo.Filter.JwtFilter;
>>>>>>> be_ky
=======
import com.example.demo.Filter.JwtFilter;
import com.example.demo.Service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
<<<<<<< HEAD
<<<<<<< HEAD
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
=======
>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
<<<<<<< HEAD
=======
>>>>>>> be_ky
=======
>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

<<<<<<< HEAD
<<<<<<< HEAD
    import java.util.Arrays;
    import java.util.List;
=======
import java.util.List;

>>>>>>> be_ky
=======
    import java.util.Arrays;
    import java.util.List;
>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
@Configuration
@EnableWebMvc
@EnableWebSecurity
@RequiredArgsConstructor
public class WebConfig {
<<<<<<< HEAD
<<<<<<< HEAD
=======

    private final JwtFilter jwtFilter;

    public WebConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> {
                    request
                            .requestMatchers(
                                    ("/api/lego-store/user/register"),
                                    ("/api/lego-store/user/login"),
                                    ("/api/lego-store/user/loginBasic")
                            ).permitAll()
                    .anyRequest().authenticated();
                })
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService())
                        )
                        //.successHandler(oAuth2SuccessHandler())
                )
                .formLogin(form-> form.disable())
                .httpBasic(basic-> basic.disable());
        http.cors(new Customizer<CorsConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
                CorsConfiguration corsConfigurer = new CorsConfiguration();
                corsConfigurer.setAllowedOrigins(List.of("*"));
                corsConfigurer.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
                corsConfigurer.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
                corsConfigurer.setExposedHeaders(List.of("x-auth-token"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfigurer);
                httpSecurityCorsConfigurer.configurationSource(source);
            }
        });
        return http.build();
    }

<<<<<<< HEAD
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return customOAuth2UserService; // bạn định nghĩa service này
    }
}
=======
    private final JwtFilter jwtFilter;

    public WebConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/api/lego-store/user/register",
                                "/api/lego-store/user/login",
                                "/api/lego-store/user/loginBasic"
                        ).permitAll()
                        .requestMatchers(
                                "/api/sanpham/**",
                                "/api/khuyenmai/**",
                                "/api/danhmuc/**",
                                "/api/anhsp/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // ✅ Thêm JWT Filter trước UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
=======
>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482

    // ✅ Cấu hình CORS cho phép truy cập từ các origin khác (frontend...)
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*")); // hoặc thay * bằng domain cụ thể cho an toàn
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Auth-Token"));
        corsConfiguration.setExposedHeaders(List.of("X-Auth-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    // ✅ Khai báo AuthenticationManager (nếu bạn cần dùng để login controller)




    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return customOAuth2UserService; // bạn định nghĩa service này
    }
}
<<<<<<< HEAD
>>>>>>> be_ky
=======

>>>>>>> bc8ddfb1a9850f1a7a5081b1ea2d04921ba99482
