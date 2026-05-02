package com.bff.bff_service.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserServiceToken() {
        // Replace with your actual User Service login URL
        String authUrl = "http://localhost:8080/api/auth/login"; 
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "admin"); // Adjust key if your API expects 'correo' or 'rut'
        credentials.put("password", "admin123");

        return executeAuthRequest(authUrl, credentials);
    }

    public String getRouteServiceToken() {
        // Replace with your actual Route Service login URL
        String authUrl = "http://localhost:8081/api/auth/login"; 
        
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
            
            // Using ParameterizedTypeReference eliminates the raw type warning
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, String>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // No casting required now because Java knows the value is a String
                return response.getBody().get("token"); 
            }
        } catch (Exception e) {
            System.err.println("Authentication failed for URL: " + url + " - " + e.getMessage());
        }
        return null;
    }
}