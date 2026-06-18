package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.config.RabbitConfig;
import com.telemetry.telemetry_service.dto.RouteEventDTO;
import com.telemetry.telemetry_service.dto.TelemetryLogDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private RabbitTemplate rabbitTemplate;

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

        // Publicar log inicial
        publishLog(event.idRuta(), currentPos[0], currentPos[1], 0.0f);

        Runnable gpsTask = () -> {
            try {
                currentPos[0] += latStep;
                currentPos[1] += lngStep;
                float simulatedSpeed = 80.0f + (float) (Math.random() * 25.0f - 10.0f); // 70 a 95 km/h

                publishLog(event.idRuta(), currentPos[0], currentPos[1], simulatedSpeed);
                System.out.println("Ping GPS Ruta #" + event.idRuta() + " publicado en cola de Telemetría (Vel: " + simulatedSpeed + " km/h)");
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
        // Publicar log final
        publishLog(event.idRuta(), event.latDestino(), event.lngDestino(), 0.0f);
        System.out.println("Simulador GPS detenido para Ruta #" + event.idRuta());
    }

    private void publishLog(Integer routeId, Double lat, Double lng, Float speed) {
        TelemetryLogDTO logDTO = new TelemetryLogDTO(
                routeId,
                lat != null ? lat : -33.4569,
                lng != null ? lng : -70.6482,
                speed,
                java.time.LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.TELEMETRY_EXCHANGE, RabbitConfig.TELEMETRY_ROUTING_KEY, logDTO);
    }
}