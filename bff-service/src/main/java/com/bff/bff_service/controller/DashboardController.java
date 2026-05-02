package com.bff.bff_service.controller;

import com.bff.bff_service.dto.DashboardResponse;
import com.bff.bff_service.dto.RouteSummaryDTO;
import com.bff.bff_service.dto.UserSummaryDTO;
import com.bff.bff_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") 
public class DashboardController {

    private final RestTemplate restTemplate;
    private final AuthService authService;

    @Autowired
    public DashboardController(RestTemplate restTemplate, AuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData() {
        DashboardResponse dashboardResponse = new DashboardResponse();
        dashboardResponse.setUsers(new ArrayList<>());
        dashboardResponse.setRoutes(new ArrayList<>());

        String userToken = authService.getUserServiceToken();
        if (userToken != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + userToken);
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<List<UserSummaryDTO>> userResponse = restTemplate.exchange(
                        "http://localhost:8080/api/users",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<UserSummaryDTO>>() {}
                );
                
                if (userResponse.getBody() != null) {
                    dashboardResponse.setUsers(userResponse.getBody());
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch users: " + e.getMessage());
            }
        }

        String routeToken = authService.getRouteServiceToken();
        if (routeToken != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + routeToken);
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<List<RouteSummaryDTO>> routeResponse = restTemplate.exchange(
                        "http://localhost:8081/api/routes",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<RouteSummaryDTO>>() {}
                );
                
                if (routeResponse.getBody() != null) {
                    dashboardResponse.setRoutes(routeResponse.getBody());
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch routes: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(dashboardResponse);
    }
}