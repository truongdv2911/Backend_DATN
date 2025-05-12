package com.example.demo.Component;

import com.example.demo.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
@Component
public class JwtTokenUntil {
    private final long expirationTime = 2592000;
    private String secretKey="ThisIsA256BitLongSecretKeyForJWTTokenjhvuugcy";
    private Key key(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }
    public String generationToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setExpiration(new Date(System.currentTimeMillis() + (expirationTime*1000)))
                    .signWith(key(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
            System.out.printf("Cannot create token: "+e.getMessage());
            return null;
        }
    }
    private Claims claims(String token){
        return Jwts.parser()
                .setSigningKey(key())
                .build().parseClaimsJws(token)
                .getBody();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = claims(token);
        return claimsResolver.apply(claims);
    }
    public boolean isTokenExpired(String token){
        Date expirationDate = this.extractClaim(token,Claims::getExpiration);
        return expirationDate.before(new Date());
    }
    public String extractEmail(String token){
        return extractClaim(token,Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails){
        String email = extractEmail(token);
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}
