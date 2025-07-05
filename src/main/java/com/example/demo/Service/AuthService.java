package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Google OAuth2 Configuration
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    
    // Facebook OAuth2 Configuration
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri:http://localhost:4300/auth/facebook/callback}")
    private String facebookRedirectUri;
    
    public String generateAuthUrl(String loginType) {
        loginType = loginType.trim().toLowerCase();
        
        switch (loginType) {
            case "google":
                return generateGoogleAuthUrl();
            case "facebook":
                return generateFacebookAuthUrl();
            default:
                throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
    }
    
    private String generateGoogleAuthUrl() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth";
        String scope = "openid+email+profile";
        String responseType = "code";
        String accessType = "offline";
        
        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=%s&access_type=%s",
                googleAuthUrl, googleClientId, googleRedirectUri, scope, responseType, accessType);
    }
    
    private String generateFacebookAuthUrl() {
        String facebookAuthUrl = "https://www.facebook.com/v18.0/dialog/oauth";
        String scope = "email,public_profile";
        String responseType = "code";

        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=%s",
                facebookAuthUrl, facebookClientId, facebookRedirectUri, scope, responseType);
    }
    
    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType) {
        loginType = loginType.trim().toLowerCase();

        switch (loginType) {
            case "google":
                return authenticateGoogle(code);
//            case "facebook":
//                return authenticateFacebook(code);
            default:
                throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
    }
    
    private Map<String, Object> authenticateGoogle(String code) {
        try {
            // Step 1: Exchange authorization code for access token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("client_id", googleClientId);
            tokenRequest.add("client_secret", googleClientSecret);
            tokenRequest.add("code", code);
            tokenRequest.add("grant_type", "authorization_code");
            tokenRequest.add("redirect_uri", googleRedirectUri);
            
            HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(tokenRequest, headers);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenEntity, String.class);
            
            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();
            
            // Step 2: Fetch user profile using access token
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> userInfoEntity = new HttpEntity<>(headers);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, userInfoEntity, String.class);
            
            JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", userInfoJson.get("sub").asText());
            userInfo.put("name", userInfoJson.get("name").asText());
            userInfo.put("email", userInfoJson.get("email").asText());

            return userInfo;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Map<String, Object> authenticateFacebook(String code) {
        try {
            // Step 1: Exchange authorization code for access token
            String tokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("client_id", facebookClientId);
            tokenRequest.add("client_secret", facebookClientSecret);
            tokenRequest.add("code", code);
            tokenRequest.add("redirect_uri", facebookRedirectUri);

            HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(tokenRequest, headers);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenEntity, String.class);

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            // Step 2: Fetch user profile using access token
            String userInfoUrl = "https://graph.facebook.com/v18.0/me?fields=id,name,email,picture";
            headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> userInfoEntity = new HttpEntity<>(headers);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, userInfoEntity, String.class);

            JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", userInfoJson.get("id").asText());
            userInfo.put("name", userInfoJson.get("name").asText());
            userInfo.put("email", userInfoJson.has("email") ? userInfoJson.get("email").asText() : null);
            userInfo.put("picture", userInfoJson.has("picture") ?
                    userInfoJson.get("picture").get("data").get("url").asText() : null);

            return userInfo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
