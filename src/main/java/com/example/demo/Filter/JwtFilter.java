package com.example.demo.Filter;

<<<<<<< HEAD
<<<<<<< HEAD
=======
import com.example.demo.Entity.User;
import com.nimbusds.jose.util.Pair;
>>>>>>> e7549f567e9b08e96c00c2c1242f6fbb6b9d3dc7
=======
import com.example.demo.Component.JwtTokenUtil;
>>>>>>> 754502382729bc327cdba87746de83b49a824e5a
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
=======
>>>>>>> e7549f567e9b08e96c00c2c1242f6fbb6b9d3dc7
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
<<<<<<< HEAD
    private final UserDetailsService userDetailsService;
<<<<<<< HEAD
    private final JwtTokenUntil jwtTokenUntil;
=======

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
>>>>>>> e7549f567e9b08e96c00c2c1242f6fbb6b9d3dc7
=======
    private final JwtTokenUtil jwtTokenUntil;

>>>>>>> 754502382729bc327cdba87746de83b49a824e5a
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
<<<<<<< HEAD
                final String sdt =jwtTokenUntil.extractEmail(token);
                if (sdt != null && SecurityContextHolder.getContext().getAuthentication() ==null){
                    User user =(User) userDetailsService.loadUserByUsername(sdt);
                    if (jwtTokenUntil.validateToken(token, user)){
=======
                final String sdt =jwtTokenUtil.extractEmail(token);
                if (sdt != null && SecurityContextHolder.getContext().getAuthentication() ==null){
                    User user =(User) userDetailsService.loadUserByUsername(sdt);
                    if (jwtTokenUtil.validateToken(token, user)){
>>>>>>> e7549f567e9b08e96c00c2c1242f6fbb6b9d3dc7
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
        final List<Pair<String, String>> passTokens = Arrays.asList(
                Pair.of("api/lego-store/user/login", "POST"),
<<<<<<< HEAD
                Pair.of("api/lego-store/user/register", "POST")
        );
        for (Pair<String, String> pair:
                passTokens) {
            if (request.getServletPath().contains(pair.getLeft()) &&
                    request.getMethod().equals(pair.getRight()))
            {
=======
                Pair.of("api/lego-store/user/register", "POST"),
                Pair.of("api/sanpham", "GET") // Thêm dòng này
        );
        for (Pair<String, String> pair :
                passTokens) {
            if (request.getServletPath().contains(pair.getLeft()) &&
                    request.getMethod().equals(pair.getRight())) {
>>>>>>> e7549f567e9b08e96c00c2c1242f6fbb6b9d3dc7
                return true;
            }
        }
        return false;
    }
}
