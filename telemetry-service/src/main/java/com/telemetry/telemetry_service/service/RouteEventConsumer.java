package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.config.RabbitConfig;
import com.telemetry.telemetry_service.dto.RouteEventDTO;
import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

@Service
public class RouteEventConsumer {

    @Autowired
    private TelemetryLogRepository logRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Integer, ScheduledFuture<?>> activeSimulators = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleRouteEvent(RouteEventDTO event) {
        System.out.println("Evento recibido: Ruta #" + event.idRuta() + " -> " + event.nuevoEstado());

        if ("En Transito".equalsIgnoreCase(event.nuevoEstado())) {
            startGpsSimulation(event);
        } else if ("Completada".equalsIgnoreCase(event.nuevoEstado())) {
            stopGpsSimulation(event);
        }
    }

    private void startGpsSimulation(RouteEventDTO event) {
        if (activeSimulators.containsKey(event.idRuta()))
            return; // Ya en curso

        // Coordenadas base (Santiago)
        double startLat = -33.4569;
        double startLng = -70.6482;
        double endLat = event.latDestino() != null ? event.latDestino() : startLat;
        double endLng = event.lngDestino() != null ? event.lngDestino() : startLng;

        double totalSteps = 60.0; // Asumimos que toma 60 iteraciones llegar al destino
        double latStep = (endLat - startLat) / totalSteps;
        double lngStep = (endLng - startLng) / totalSteps;

        // Estado inicial
        final double[] currentPos = { startLat, startLng };

        // Guardar log inicial
        saveLog(event.idRuta(), currentPos[0], currentPos[1], 0.0f);

        Runnable gpsTask = () -> {
            try {
                currentPos[0] += latStep;
                currentPos[1] += lngStep;
                float simulatedSpeed = 80.0f + (float) (Math.random() * 20.0f - 10.0f); // 70 a 90 km/h

                saveLog(event.idRuta(), currentPos[0], currentPos[1], simulatedSpeed);
                System.out.println("Ping GPS Ruta #" + event.idRuta() + " (Vel: " + simulatedSpeed + " km/h)");
            } catch (Exception e) {
                System.err.println("Error en simulador GPS ruta " + event.idRuta());
            }
        };

        // Ejecutar cada 10 segundos
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(gpsTask, 10, 10, TimeUnit.SECONDS);
        activeSimulators.put(event.idRuta(), future);
    }

    private void stopGpsSimulation(RouteEventDTO event) {
        ScheduledFuture<?> future = activeSimulators.remove(event.idRuta());
        if (future != null) {
            future.cancel(true);
        }
        // Guardar log final
        saveLog(event.idRuta(), event.latDestino(), event.lngDestino(), 0.0f);
        System.out.println("Simulador GPS detenido para Ruta #" + event.idRuta());
    }

    private void saveLog(Integer routeId, Double lat, Double lng, Float speed) {
        TelemetryLog log = new TelemetryLog();
        log.setIdRutaRef(routeId);
        log.setLatitud(lat != null ? lat : -33.4569);
        log.setLongitud(lng != null ? lng : -70.6482);
        log.setVelocidadKmh(speed);
        logRepository.save(log);
    }
}