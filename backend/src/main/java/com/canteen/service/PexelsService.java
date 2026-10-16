package com.canteen.service;

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
public class PexelsService {

    @Value("${pexels.api.key:}")
    private String pexelsApiKey;

    @Value("${pexels.api.url:https://api.pexels.com/v1/search}")
    private String pexelsApiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String fetchImageUrl(String query) {
        if (pexelsApiKey == null || pexelsApiKey.isBlank() || query == null || query.isBlank()) {
            return null;
        }
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = pexelsApiUrl + "?query=" + encodedQuery + "&per_page=1";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", pexelsApiKey)
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode photos = root.get("photos");
                if (photos != null && photos.isArray() && photos.size() > 0) {
                    JsonNode src = photos.get(0).get("src");
                    if (src != null) {
                        JsonNode small = src.get("small");
                        if (small != null) {
                            return small.asText();
                        }
                        JsonNode thumb = src.get("thumb");
                        if (thumb != null) {
                            return thumb.asText();
                        }
                        JsonNode medium = src.get("medium");
                        if (medium != null) {
                            return medium.asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
