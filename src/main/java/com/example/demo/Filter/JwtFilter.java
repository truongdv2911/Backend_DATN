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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException
    {
        try {
            if (isPassToken(request)){
                filterChain.doFilter(request,response);
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (authHeader.startsWith("Bearer ")){
                final String token = authHeader.substring(7);
                final String sdt =jwtTokenUntil.extractEmail(token);
                if (sdt != null && SecurityContextHolder.getContext().getAuthentication() ==null){
                    User user =(User) userDetailsService.loadUserByUsername(sdt);
                    if (jwtTokenUntil.validateToken(token, user)){
                        UsernamePasswordAuthenticationToken usernamePassword = new
                                UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
                        usernamePassword.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePassword);
                    }
                }
            }
            filterChain.doFilter(request,response);
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String body = "{\"status\":401,\"message\":\"Bạn chưa đăng nhập hoặc token không hợp lệ\"}" + e.getMessage();
            response.getWriter().write(body);
        }

    }
    private Boolean isPassToken(HttpServletRequest request){

        final List<Pair<String, String>> passTokens = Arrays.asList(
                Pair.of("/api/lego-store/user/login", "POST"),
                Pair.of("/api/lego-store/user/register", "POST"),
                Pair.of("/api/lego-store/user/forgot-password", "POST"),
                Pair.of("/api/lego-store/user/verify-otp", "POST"),
                Pair.of("/api/lego-store/user/reset-password", "POST"),
                Pair.of("/api/sanpham/", "GET"),
                Pair.of("/api/danhmuc/", "GET"),
                Pair.of("/api/bosuutap/", "GET"),
                Pair.of("/api/giohang/", "GET"),
                Pair.of("/api/anhsp/", "GET")
        );
        for (Pair<String, String> pair:
                passTokens) {
            if (request.getServletPath().contains(pair.getLeft()) &&
                    request.getMethod().equals(pair.getRight()))
            {
                return true;
            }
        }
        return true;
    }
}