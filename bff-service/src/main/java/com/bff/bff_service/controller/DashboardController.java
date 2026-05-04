package com.bff.bff_service.controller;

import com.bff.bff_service.dto.DashboardResponse;
import com.bff.bff_service.dto.RouteSummaryDTO;
import com.bff.bff_service.dto.UserSummaryDTO;
import com.bff.bff_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final RestTemplate restTemplate;
    private final AuthService authService;

    @Value("${USER_SERVICE_URL:http://localhost:8080}")
    private String userServiceUrl;

    @Value("${ROUTE_SERVICE_URL:http://localhost:8081}")
    private String routeServiceUrl;

    @Value("${TELEMETRY_SERVICE_URL:http://localhost:8083}")
    private String telemetryServiceUrl;

    // Simulacion: 1 minuto real = 1 hora simulada (velocidad 60x)
    private static final double TIME_MULTIPLIER = 60.0;
    private static final double AVG_SPEED_KMH = 80.0;

    @Autowired
    public DashboardController(RestTemplate restTemplate, AuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    // ===================== DASHBOARD PRINCIPAL =====================

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
                        HttpMethod.GET, entity,
                        new ParameterizedTypeReference<List<UserSummaryDTO>>() {}
                );
                if (userResponse.getBody() != null) {
                    dashboardResponse.setUsers(userResponse.getBody());
                }
            } catch (Exception e) {
                System.err.println("Error al obtener usuarios: " + e.getMessage());
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
                        HttpMethod.GET, entity,
                        new ParameterizedTypeReference<List<RouteSummaryDTO>>() {}
                );
                if (routeResponse.getBody() != null) {
                    List<RouteSummaryDTO> routes = routeResponse.getBody();
                    for (RouteSummaryDTO route : routes) {
                        enrichWithSimulation(route);
                    }
                    dashboardResponse.setRoutes(routes);
                }
            } catch (Exception e) {
                System.err.println("Error al obtener rutas: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(dashboardResponse);
    }

    // ===================== SIMULACION DE PROGRESO =====================

    /**
     * Calcula el progreso simulado de cada ruta en base al tiempo transcurrido.
     *
     * Para rutas "En Transito": interpola entre 0-100% usando un multiplicador
     * de tiempo (60x). Un viaje de 1030 km a 80 km/h (~13h) se completa en ~13 min reales.
     *
     * Para "Completada": fijado en 100%. Para otros estados: fijado en 0%.
     */
    private void enrichWithSimulation(RouteSummaryDTO route) {
        String estado = route.getEstado();
        if (estado == null) return;

        if ("Completada".equalsIgnoreCase(estado.trim())) {
            route.setProgressPercent(100.0);
            route.setKmRecorridos(route.getDistanciaEstimadaKm() != null
                    ? route.getDistanciaEstimadaKm().doubleValue() : 0.0);
            route.setVelocidadSimulada(0.0);
            return;
        }

        if (!"En Transito".equalsIgnoreCase(estado.trim())) {
            route.setProgressPercent(0.0);
            route.setKmRecorridos(0.0);
            route.setVelocidadSimulada(0.0);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Usa fechaSalidaReal si existe, sino fechaCreacion como respaldo
        LocalDateTime departure = route.getFechaSalidaReal();
        if (departure == null) departure = route.getFechaCreacion();
        if (departure == null) departure = now.minusMinutes(5);

        double distKm = route.getDistanciaEstimadaKm() != null
                ? route.getDistanciaEstimadaKm() : 500.0;

        // Segundos reales necesarios para completar el viaje simulado
        double totalSimHours = distKm / AVG_SPEED_KMH;
        double totalRealSeconds = (totalSimHours * 3600.0) / TIME_MULTIPLIER;

        long elapsedRealSeconds = Duration.between(departure, now).getSeconds();
        double progress = totalRealSeconds > 0
                ? (double) elapsedRealSeconds / totalRealSeconds : 0.0;

        // Limitar entre 0% y 99% (aun en transito, no completada)
        progress = Math.max(0.0, Math.min(0.99, progress));

        route.setProgressPercent(Math.round(progress * 1000.0) / 10.0);
        route.setKmRecorridos(Math.round(distKm * progress * 10.0) / 10.0);

        // Velocidad simulada: promedio +/- variacion aleatoria
        double jitter = (Math.random() * 20.0) - 10.0;
        route.setVelocidadSimulada(Math.round((AVG_SPEED_KMH + jitter) * 10.0) / 10.0);
    }

    // ===================== PROXY CRUD GENERICO =====================

    private ResponseEntity<Object> proxyToRoute(String path, HttpMethod method, Object body) {
        return proxyToService(routeServiceUrl + path, method, body, authService::getRouteServiceToken);
    }

    private ResponseEntity<Object> proxyToUser(String path, HttpMethod method, Object body) {
        return proxyToService(userServiceUrl + path, method, body, authService::getUserServiceToken);
    }

    private ResponseEntity<Object> proxyToService(String url, HttpMethod method, Object body,
                                                   Supplier<String> tokenSupplier) {
        try {
            String jwt = tokenSupplier.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwt);

            // Solo setear Content-Type cuando hay body (POST/PUT)
            if (body != null) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }

            HttpEntity<Object> request = body != null
                    ? new HttpEntity<>(body, headers) : new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(url, method, request, Object.class);
            return new ResponseEntity<>(response.getBody(), response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Error en proxy (" + method + " " + url + "): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- Rutas ---
    @PostMapping("/proxy/routes")
    public ResponseEntity<Object> createRoute(@RequestBody Object payload) {
        return proxyToRoute("/api/routes", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/routes/{id}")
    public ResponseEntity<Object> updateRoute(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToRoute("/api/routes/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/routes/{id}")
    public ResponseEntity<Object> deleteRoute(@PathVariable Integer id) {
        return proxyToRoute("/api/routes/" + id, HttpMethod.DELETE, null);
    }

    // --- Camiones ---
    @GetMapping("/proxy/camiones")
    public ResponseEntity<Object> getTrucks() {
        return proxyToRoute("/api/camiones", HttpMethod.GET, null);
    }

    @PostMapping("/proxy/camiones")
    public ResponseEntity<Object> createTruck(@RequestBody Object payload) {
        return proxyToRoute("/api/camiones", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/camiones/{id}")
    public ResponseEntity<Object> updateTruck(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToRoute("/api/camiones/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/camiones/{id}")
    public ResponseEntity<Object> deleteTruck(@PathVariable Integer id) {
        return proxyToRoute("/api/camiones/" + id, HttpMethod.DELETE, null);
    }

    // --- Usuarios ---
    @PostMapping("/proxy/users")
    public ResponseEntity<Object> createUser(@RequestBody Object payload) {
        return proxyToUser("/api/users", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToUser("/api/users/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
        return proxyToUser("/api/users/" + id, HttpMethod.DELETE, null);
    }

    // --- Cargamentos ---
    @GetMapping("/proxy/cargo")
    public ResponseEntity<Object> getCargo() {
        return proxyToRoute("/api/cargo", HttpMethod.GET, null);
    }

    @PostMapping("/proxy/cargo")
    public ResponseEntity<Object> createCargo(@RequestBody Object payload) {
        return proxyToRoute("/api/cargo", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/cargo/{id}")
    public ResponseEntity<Object> updateCargo(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToRoute("/api/cargo/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/cargo/{id}")
    public ResponseEntity<Object> deleteCargo(@PathVariable Integer id) {
        return proxyToRoute("/api/cargo/" + id, HttpMethod.DELETE, null);
    }

    // --- Clientes ---
    @GetMapping("/proxy/clients")
    public ResponseEntity<Object> getClients() {
        return proxyToRoute("/api/clients", HttpMethod.GET, null);
    }

    @PostMapping("/proxy/clients")
    public ResponseEntity<Object> createClient(@RequestBody Object payload) {
        return proxyToRoute("/api/clients", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/clients/{id}")
    public ResponseEntity<Object> updateClient(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToRoute("/api/clients/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/clients/{id}")
    public ResponseEntity<Object> deleteClient(@PathVariable Integer id) {
        return proxyToRoute("/api/clients/" + id, HttpMethod.DELETE, null);
    }

    // --- Facturas ---
    @GetMapping("/proxy/invoices")
    public ResponseEntity<Object> getInvoices() {
        return proxyToRoute("/api/invoices", HttpMethod.GET, null);
    }

    @PostMapping("/proxy/invoices")
    public ResponseEntity<Object> createInvoice(@RequestBody Object payload) {
        return proxyToRoute("/api/invoices", HttpMethod.POST, payload);
    }

    @PutMapping("/proxy/invoices/{id}")
    public ResponseEntity<Object> updateInvoice(@PathVariable Integer id, @RequestBody Object payload) {
        return proxyToRoute("/api/invoices/" + id, HttpMethod.PUT, payload);
    }

    @DeleteMapping("/proxy/invoices/{id}")
    public ResponseEntity<Object> deleteInvoice(@PathVariable Integer id) {
        return proxyToRoute("/api/invoices/" + id, HttpMethod.DELETE, null);
    }

    // --- Telemetria ---
    private ResponseEntity<Object> proxyToTelemetry(String path, HttpMethod method, Object body) {
        return proxyToService(telemetryServiceUrl + path, method, body, authService::getTelemetryServiceToken);
    }

    @GetMapping("/proxy/telemetry")
    public ResponseEntity<Object> getTelemetry() {
        return proxyToTelemetry("/api/telemetry", HttpMethod.GET, null);
    }

    @GetMapping("/proxy/telemetry/route/{routeId}")
    public ResponseEntity<Object> getTelemetryByRoute(@PathVariable Integer routeId) {
        return proxyToTelemetry("/api/telemetry/route/" + routeId, HttpMethod.GET, null);
    }

    @GetMapping("/proxy/alerts")
    public ResponseEntity<Object> getAlerts() {
        return proxyToTelemetry("/api/alerts", HttpMethod.GET, null);
    }

    @GetMapping("/proxy/alerts/active")
    public ResponseEntity<Object> getActiveAlerts() {
        return proxyToTelemetry("/api/alerts/active", HttpMethod.GET, null);
    }
}