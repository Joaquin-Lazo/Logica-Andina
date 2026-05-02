package com.bff.bff_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate;

    @Value("${USER_SERVICE_URL:http://localhost:8080}")
    private String userServiceUrl;

    @Value("${ROUTE_SERVICE_URL:http://localhost:8081}")
    private String routeServiceUrl;

    @Autowired
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserServiceToken() {
        String authUrl = userServiceUrl + "/api/auth/login"; 
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin"); 
        credentials.put("password", "admin123");

        return executeAuthRequest(authUrl, credentials);
    }

    public String getRouteServiceToken() {
        String authUrl = routeServiceUrl + "/api/auth/login"; 
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "route"); 
        credentials.put("password", "route123");

        return executeAuthRequest(authUrl, credentials);
    }
    
    private String executeAuthRequest(String url, Map<String, String> credentials) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(credentials, headers);
            
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, String>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().get("token"); 
            }
        } catch (Exception e) {
            System.err.println("Authentication failed for URL: " + url + " - " + e.getMessage());
        }
        return null;
    }
}