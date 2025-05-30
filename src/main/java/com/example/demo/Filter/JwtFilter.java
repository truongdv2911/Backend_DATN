package com.example.demo.Filter;


import com.example.demo.Component.JwtTokenUntil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
    private Boolean isPassToken(HttpServletRequest request){
//        final List<Pair<String, String>> passTokens = Arrays.asList(
//                Pair.of("api/lego-store/user/login", "POST"),
//
//                Pair.of("api/lego-store/user/register", "POST"),
//                Pair.of("api/lego-store/user/register", "POST"),
//                Pair.of("/api/anhsp", "GET"),
//                Pair.of("/api/sanpham", "GET"),
//                Pair.of("/api/sanpham", "POST"),
//                Pair.of("/api/sanpham", "PUT"),
//                Pair.of("/api/sanpham", "DELETE"),
//                Pair.of("/api/anhsp/create", "POST"),
//                Pair.of("/api/bosuutap", "GET"),
//                Pair.of("/api/bosuutap/Create", "POST"),
//                Pair.of("/api/bosuutap/Update", "PUT"),
//                Pair.of("/api/bosuutap", "DELETE"),
//                Pair.of("/api/danhmuc", "GET"),
//                Pair.of("/api/danhmuc/Create", "POST"),
//                Pair.of("/api/danhmuc", "PUT"),
//                Pair.of("/api/danhmuc", "DELETE"),
//                Pair.of("/api/phieugiamgia", "GET"),
//                Pair.of("/api/phieugiamgia", "POST"),
//                Pair.of("/api/phieugiamgia", "PUT"),
//                Pair.of("/api/phieugiamgia", "DELETE"),
//                Pair.of("/api/giohang", "GET"),
//                Pair.of("/api/giohang", "POST"),
//                Pair.of("/api/giohang", "PUT"),
//                Pair.of("/api/giohang", "DELETE"),
//
//                Pair.of("api/lego-store/hoa-don/create", "POST"),
//                Pair.of("api/lego-store/hoa-don/user/**", "GET"),
//                Pair.of("api/lego-store/hoa-don/**", "GET")
//
//        );
//        for (Pair<String, String> pair:
//                passTokens) {
//            if (request.getServletPath().contains(pair.getLeft()) &&
//                    request.getMethod().equals(pair.getRight()))
//            {
//                return true;
//            }
//        }
        return true;
    }
}