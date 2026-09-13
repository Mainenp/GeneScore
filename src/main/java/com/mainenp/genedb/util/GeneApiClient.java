package com.mainenp.genedb.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Universal HTTP Request Utility
 * Handles GET/POST requests, parameter splicing, headers, and retry logic
 */
@Slf4j
@Component
public class GeneApiClient {
    
    private static final int TIMEOUT_SECONDS = 10; // Reduced from 30 to 10 for faster response
    private static final int MAX_RETRIES = 1; // Reduced from 3 to 1
    private static final int RETRY_DELAY_MS = 1000;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final HttpClient httpClient;
    
    public GeneApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }
    
    /**
     * Perform GET request with retry logic
     * @param url the request URL
     * @param headers optional request headers
     * @return response body as string
     * @throws Exception if request fails after retries
     */
    public String get(String url, Map<String, String> headers) throws Exception {
        return executeWithRetry(url, headers, null, "GET");
    }
    
    /**
     * Perform GET request without headers
     * @param url the request URL
     * @return response body as string
     * @throws Exception if request fails
     */
    public String get(String url) throws Exception {
        return get(url, new HashMap<>());
    }
    
    /**
     * Perform POST request
     * @param url the request URL
     * @param body request body
     * @param headers optional request headers
     * @return response body as string
     * @throws Exception if request fails
     */
    public String post(String url, String body, Map<String, String> headers) throws Exception {
        return executeWithRetry(url, headers, body, "POST");
    }
    
    /**
     * Execute request with automatic retry on failure
     * @param url the request URL
     * @param headers request headers
     * @param body request body (null for GET)
     * @param method HTTP method (GET or POST)
     * @return response body as string
     * @throws Exception if all retries fail
     */
    private String executeWithRetry(String url, Map<String, String> headers, 
                                   String body, String method) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return executeRequest(url, headers, body, method);
            } catch (Exception e) {
                lastException = e;
                log.warn("API request failed (attempt {}/{}): {} - {}", 
                        attempt, MAX_RETRIES, url, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                }
            }
        }
        
        throw new Exception("API request failed after " + MAX_RETRIES + " retries: " + url, lastException);
    }
    
    /**
     * Execute single HTTP request
     * @param url the request URL
     * @param headers request headers
     * @param body request body
     * @param method HTTP method
     * @return response body as string
     * @throws IOException if request fails
     * @throws InterruptedException if request is interrupted
     */
    private String executeRequest(String url, Map<String, String> headers, 
                                 String body, String method) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        
        // Add default headers
        requestBuilder.header("User-Agent", "GeneScore/1.0");
        requestBuilder.header("Accept", "application/json");
        
        // Add custom headers
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
        
        // Set method and body
        if ("POST".equalsIgnoreCase(method)) {
            if (body != null) {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
            } else {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            }
        } else {
            requestBuilder.GET();
        }
        
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
        
        // Check if response is JSON - but don't return empty JSON for non-JSON responses
        // Let the caller handle different response formats
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!contentType.contains("application/json")) {
            log.warn("Non-JSON response received: {}", contentType);
            // Return the actual response body, let the caller handle it
        }
        
        return response.body();
    }
    
    /**
     * Build URL with query parameters
     * @param baseUrl base URL
     * @param params query parameters
     * @return complete URL with parameters
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");
        
        params.forEach((key, value) -> {
            try {
                urlBuilder.append(key).append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
                        .append("&");
            } catch (Exception e) {
                log.error("Error encoding parameter: {} = {}", key, value, e);
            }
        });
        
        // Remove trailing &
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * Parse JSON response
     * @param jsonString JSON string
     * @return JsonNode object
     * @throws IOException if parsing fails
     */
    public static JsonNode parseJson(String jsonString) throws IOException {
        return objectMapper.readTree(jsonString);
    }
    
    /**
     * Convert object to JSON string
     * @param object object to convert
     * @return JSON string
     * @throws Exception if conversion fails
     */
    public static String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
