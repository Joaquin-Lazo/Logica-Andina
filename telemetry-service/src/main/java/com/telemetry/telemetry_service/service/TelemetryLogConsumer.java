package com.telemetry.telemetry_service.service;

import com.telemetry.telemetry_service.config.RabbitConfig;
import com.telemetry.telemetry_service.dto.TelemetryLogDTO;
import com.telemetry.telemetry_service.model.TelemetryLog;
import com.telemetry.telemetry_service.model.Alert;
import com.telemetry.telemetry_service.repository.TelemetryLogRepository;
import com.telemetry.telemetry_service.repository.AlertRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelemetryLogConsumer {

    @Autowired
    private TelemetryLogRepository logRepository;

    @Autowired
    private AlertRepository alertRepository;

    @RabbitListener(queues = RabbitConfig.TELEMETRY_QUEUE)
    public void consumeTelemetryLog(TelemetryLogDTO logDTO) {
        TelemetryLog log = new TelemetryLog();
        log.setIdRutaRef(logDTO.idRutaRef());
        log.setLatitud(logDTO.latitud());
        log.setLongitud(logDTO.longitud());
        log.setVelocidadKmh(logDTO.velocidadKmh());
        log.setTimestampEvento(logDTO.timestampEvento());
        
        logRepository.save(log);
        
        // Auto-generar alerta si hay exceso de velocidad
        if (log.getVelocidadKmh() != null && log.getVelocidadKmh() > 90.0f) {
            Alert alert = new Alert();
            alert.setLogGps(log);
            alert.setTipoAlerta("Exceso de Velocidad");
            alert.setNivelSeveridad("Alta");
            alert.setResuelta(false);
            alert.setComentariosDespachador("Generada automáticamente por el sistema: " + log.getVelocidadKmh() + " km/h");
            alertRepository.save(alert);
        }

        // Descomentar para debug de base de datos
        // System.out.println("Guardado en BD Log Telemetría - Ruta #" + logDTO.idRutaRef());
    }
}
