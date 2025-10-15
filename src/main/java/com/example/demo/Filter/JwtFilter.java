package com.example.demo.Filter;


import com.example.demo.Component.JwtTokenUntil;
import com.example.demo.Entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenUntil jwtTokenUntil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Preflight
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            // Whitelist các route public
            if (isPassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Không có token -> để Spring Security quyết định theo rule (permitAll sẽ qua)
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authHeader.substring(7);
            final String sdt = jwtTokenUntil.extractEmail(token);
            if (sdt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = (User) userDetailsService.loadUserByUsername(sdt);
                if (jwtTokenUntil.validateToken(token, user)) {
                    UsernamePasswordAuthenticationToken usernamePassword =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    usernamePassword.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePassword);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Cho qua để Security xử lý, tránh trả 401 từ filter cho các route public
            filterChain.doFilter(request, response);
        }
    }

    private Boolean isPassToken(HttpServletRequest request) {
        final List<Pair<String, String>> passTokens = Arrays.asList(
                Pair.of("/api/lego-store/user/login", "POST"),
                Pair.of("/api/lego-store/user/register", "POST"),
                Pair.of("/api/lego-store/user/forgot-password", "POST"),
                Pair.of("/api/lego-store/user/verify-otp", "POST"),
                Pair.of("/api/lego-store/user/reset-password", "POST"),

                // Sản phẩm, danh mục, ảnh, bộ sưu tập, giỏ hàng
                Pair.of("/api/sanpham/", "GET"),
                Pair.of("/api/danhmuc/", "GET"),
                Pair.of("/api/bosuutap/", "GET"),
                Pair.of("/api/giohang/", "GET"),
                Pair.of("/api/anhsp/", "GET"),

                // Chat public
                Pair.of("/api/chat", "POST"),
                Pair.of("/api/chat", "GET"),

                // Lego-store public GET
                Pair.of("/api/lego-store/danh-gia", "GET"),
                Pair.of("/api/lego-store/thuong-hieu", "GET"),
                Pair.of("/api/lego-store/xuatXu", "GET"),
                Pair.of("/api/lego-store/payment", "GET"),
                Pair.of("/api/lego-store/user", "GET"),
                Pair.of("/api/lego-store/san-pham-yeu-thich", "GET"),

                // HÓA ĐƠN: thêm POST để tạo đơn không cần token
                Pair.of("/api/lego-store/hoa-don", "GET"),
                Pair.of("/api/lego-store/hoa-don", "POST")
        );

        String path = request.getServletPath();
        String method = request.getMethod();
        for (Pair<String, String> p : passTokens) {
            if (path.startsWith(p.getLeft()) && method.equalsIgnoreCase(p.getRight())) {
                return true;
            }
        }
        return false;
    }
}