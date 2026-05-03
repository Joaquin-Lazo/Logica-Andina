package com.bff.bff_service.controller;

import com.bff.bff_service.dto.DashboardResponse;
import com.bff.bff_service.dto.RouteSummaryDTO;
import com.bff.bff_service.dto.UserSummaryDTO;
import com.bff.bff_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
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

    @Value("${USER_SERVICE_URL:http://localhost:8080}")
    private String userServiceUrl;

    @Value("${ROUTE_SERVICE_URL:http://localhost:8081}")
    private String routeServiceUrl;

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
                        userServiceUrl + "/api/users",
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
                        routeServiceUrl + "/api/routes",
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

    @PostMapping("/proxy/routes")
    public ResponseEntity<Object> createRouteSecurely(@RequestBody Object newRoutePayload) {
        try {
            String jwtToken = authService.getRouteServiceToken(); 
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);
            
            HttpEntity<Object> secureRequest = new HttpEntity<>(newRoutePayload, headers);
            
            String internalRouteUrl = routeServiceUrl + "/api/routes"; 
            ResponseEntity<Object> downstreamResponse = restTemplate.exchange(
                    internalRouteUrl,
                    HttpMethod.POST,
                    secureRequest,
                    Object.class
            );
            
            return new ResponseEntity<>(downstreamResponse.getBody(), downstreamResponse.getStatusCode());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/proxy/users")
    public ResponseEntity<Object> createUserSecurely(@RequestBody Object newUserPayload) {
        try {
            String jwtToken = authService.getUserServiceToken(); 
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);
            
            HttpEntity<Object> secureRequest = new HttpEntity<>(newUserPayload, headers);
            
            String internalUserUrl = userServiceUrl + "/api/users"; 
            ResponseEntity<Object> downstreamResponse = restTemplate.exchange(
                    internalUserUrl,
                    HttpMethod.POST,
                    secureRequest,
                    Object.class
            );
            
            return new ResponseEntity<>(downstreamResponse.getBody(), downstreamResponse.getStatusCode());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}