package com.telemetry.telemetry_service.repository;

import com.telemetry.telemetry_service.model.TelemetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetryLogRepository extends JpaRepository<TelemetryLog, Long> {
    List<TelemetryLog> findByIdRutaRefOrderByTimestampEventoDesc(Integer idRutaRef);
}
