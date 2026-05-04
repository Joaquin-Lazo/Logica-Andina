import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082/api/dashboard";
const REFRESH_MS = 10000;

const Telemetry = () => {
  const [logs, setLogs] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [selectedRoute, setSelectedRoute] = useState("");
  const [isLive, setIsLive] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchData = useCallback(() => {
    const logsUrl = selectedRoute
      ? `${BFF}/proxy/telemetry/route/${selectedRoute}`
      : `${BFF}/proxy/telemetry`;
    fetch(logsUrl).then(r => r.json()).then(d => setLogs(Array.isArray(d) ? d : [])).catch(() => {});
    fetch(`${BFF}/proxy/alerts`).then(r => r.json()).then(d => setAlerts(Array.isArray(d) ? d : [])).catch(() => {});
    setLastUpdated(new Date());
  }, [selectedRoute]);

  useEffect(() => { fetchData(); }, [fetchData]);

  useEffect(() => {
    if (!isLive) return;
    const interval = setInterval(fetchData, REFRESH_MS);
    return () => clearInterval(interval);
  }, [isLive, fetchData]);

  const formatTime = (ts) => {
    if (!ts) return "—";
    try { return new Date(ts).toLocaleTimeString("es-CL"); } catch { return "—"; }
  };

  const getSeverityClass = (s) => {
    if (!s) return "";
    if (s.toLowerCase().includes("crit") || s.toLowerCase().includes("alta")) return "en-transito";
    if (s.toLowerCase().includes("media")) return "pendiente";
    return "completada";
  };

  return (
    <main className="dashboard-container">
      <div className="live-bar">
        <div className="live-bar-left">
          <button className={`live-toggle ${isLive ? "active" : ""}`} onClick={() => setIsLive(!isLive)}>
            <span className={`live-dot ${isLive ? "pulsing" : ""}`}></span>
            {isLive ? "EN VIVO" : "PAUSADO"}
          </button>
          {lastUpdated && <span className="last-updated">Actualizado: {lastUpdated.toLocaleTimeString("es-CL")}</span>}
          <select className="conductor-select" value={selectedRoute} onChange={(e) => setSelectedRoute(e.target.value)}>
            <option value="">Todas las rutas</option>
            {[...new Set(logs.map(l => l.idRutaRef))].sort((a,b) => a-b).map(id =>
              <option key={id} value={id}>Ruta #{id}</option>
            )}
          </select>
        </div>
        <button className="refresh-btn" onClick={fetchData} title="Refrescar ahora">↻</button>
      </div>

      <div className="tables-wrapper-vertical">
        <section className="data-section full-width">
          <h2>Logs GPS <span className="badge-count">{logs.length}</span></h2>
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th><th>Ruta</th><th>Latitud</th><th>Longitud</th>
                  <th>Velocidad</th><th>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {logs.length === 0 ? <tr><td colSpan="6" className="empty-cell">Sin datos de telemetria</td></tr> :
                  logs.slice(0, 50).map(l => (
                    <tr key={l.idLog} className={l.velocidadKmh > 90 ? "row-live" : ""}>
                      <td className="cell-id">{l.idLog}</td>
                      <td className="cell-id">#{l.idRutaRef}</td>
                      <td className="cell-mono">{l.latitud?.toFixed(6)}</td>
                      <td className="cell-mono">{l.longitud?.toFixed(6)}</td>
                      <td className="cell-number">
                        {l.velocidadKmh?.toFixed(1)} km/h
                        {l.velocidadKmh > 90 && <span className="status-badge en-transito" style={{marginLeft: 6, fontSize: '0.7rem'}}>ALTA</span>}
                      </td>
                      <td className="cell-date">{formatTime(l.timestampEvento)}</td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        </section>

        <section className="data-section full-width">
          <h2>Alertas <span className="badge-count">{alerts.length}</span></h2>
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th><th>Tipo</th><th>Severidad</th><th>Resuelta</th><th>Comentarios</th>
                </tr>
              </thead>
              <tbody>
                {alerts.length === 0 ? <tr><td colSpan="5" className="empty-cell">Sin alertas</td></tr> :
                  alerts.map(a => (
                    <tr key={a.idAlerta}>
                      <td className="cell-id">{a.idAlerta}</td>
                      <td>{a.tipoAlerta}</td>
                      <td><span className={`status-badge ${getSeverityClass(a.nivelSeveridad)}`}>{a.nivelSeveridad}</span></td>
                      <td>
                        <span className={`status-dot ${a.resuelta ? "active" : "inactive"}`}>
                          {a.resuelta ? "Si" : "No"}
                        </span>
                      </td>
                      <td style={{maxWidth: 300, whiteSpace: 'normal'}}>{a.comentariosDespachador || "—"}</td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </main>
  );
};

export default Telemetry;
