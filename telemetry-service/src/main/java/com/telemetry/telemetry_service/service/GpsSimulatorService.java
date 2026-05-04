package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servicio de simulacion GPS.
 *
 * Cada 30 segundos consulta al route-service para obtener las rutas "En Transito",
 * luego genera un nuevo log de telemetria para cada una, desplazando las coordenadas
 * ligeramente para simular movimiento vehicular.
 *
 * La consulta dinamica permite que rutas creadas desde el frontend tambien
 * reciban datos de telemetria automaticamente al cambiar su estado a "En Transito".
 */
@Service
public class GpsSimulatorService {

    @Autowired
    private TelemetryLogRepository logRepository;

    @Value("${ROUTE_SERVICE_URL:http://localhost:8081}")
    private String routeServiceUrl;

    @Value("${ROUTE_SERVICE_USER:route}")
    private String routeUser;

    @Value("${ROUTE_SERVICE_PASS:route123}")
    private String routePass;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 30000)
    public void simulateGpsPings() {
        List<Integer> activeRouteIds = fetchActiveRouteIds();
        if (activeRouteIds.isEmpty()) return;

        for (int routeId : activeRouteIds) {
            List<TelemetryLog> recent = logRepository.findByIdRutaRefOrderByTimestampEventoDesc(routeId);

            double lat, lng;
            if (!recent.isEmpty()) {
                TelemetryLog last = recent.get(0);
                lat = last.getLatitud() - (0.01 + Math.random() * 0.02);
                lng = last.getLongitud() + (Math.random() * 0.01 - 0.005);
            } else {
                lat = -33.4569 + (Math.random() * 0.1);
                lng = -70.6482 + (Math.random() * 0.1);
            }

            float speed = (float) (60 + Math.random() * 40);

            TelemetryLog newLog = new TelemetryLog();
            newLog.setIdRutaRef(routeId);
            newLog.setLatitud(lat);
            newLog.setLongitud(lng);
            newLog.setVelocidadKmh(speed);
            logRepository.save(newLog);
        }
    }

    /**
     * Obtiene los IDs de rutas "En Transito" desde el route-service.
     * Primero se autentica via /api/auth/login para obtener un JWT,
     * luego consulta /api/routes y filtra por estado.
     */
    private List<Integer> fetchActiveRouteIds() {
        try {
            String token = authenticateWithRouteService();
            if (token == null) return List.of();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    routeServiceUrl + "/api/routes",
                    HttpMethod.GET, entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() == null) return List.of();

            return response.getBody().stream()
                    .filter(r -> "En Transito".equalsIgnoreCase(String.valueOf(r.get("estado"))))
                    .map(r -> ((Number) r.get("idRuta")).intValue())
                    .toList();
        } catch (Exception e) {
            System.err.println("Error al consultar rutas activas: " + e.getMessage());
            return List.of();
        }
    }

    private String authenticateWithRouteService() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> creds = Map.of("username", routeUser, "password", routePass);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(creds, headers);

            ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                    routeServiceUrl + "/api/auth/login",
                    HttpMethod.POST, request,
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody() != null ? response.getBody().get("token") : null;
        } catch (Exception e) {
            System.err.println("Error de autenticacion con route-service: " + e.getMessage());
            return null;
        }
    }
}
