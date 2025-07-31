package com.example.demo.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleAccessTokenProvider {
    @Value("${google.drive.client-id}")
    private String clientId;

    @Value("${google.drive.client-secret}")
    private String clientSecret;

    @Value("${google.drive.refresh-token}")
    private String refreshToken;

    public String getAccessToken() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                                    "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                                    "&grant_type=refresh_token"))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.body());
            if (jsonNode.has("access_token")) {
                return jsonNode.get("access_token").asText();
            } else {
                System.err.println("Failed to retrieve access token. Response: " + response.body());
                throw new RuntimeException("Access token not found in response: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get access token", e);
        }
    }
}
