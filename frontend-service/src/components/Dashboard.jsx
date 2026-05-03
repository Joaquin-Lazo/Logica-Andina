import React, { useState, useEffect, useCallback } from "react";
import { useRole, ROLES } from "../context/RoleContext";

const REFRESH_INTERVAL_MS = 5000;

const Dashboard = () => {
  const { role } = useRole();
  const [dashboardData, setDashboardData] = useState({ users: [], routes: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [isLive, setIsLive] = useState(true);
  const [conductorId, setConductorId] = useState("");

  const fetchDashboard = useCallback(() => {
    fetch("http://localhost:8082/api/dashboard")
      .then((response) => {
        if (!response.ok) throw new Error("No se pudo conectar con el BFF");
        return response.json();
      })
      .then((data) => {
        setDashboardData(data);
        setLastUpdated(new Date());
        setLoading(false);
        setError(null);
      })
      .catch((err) => {
        console.error("Error:", err);
        setError("Esperando conexion con el Backend For Frontend...");
        setLoading(false);
      });
  }, []);

  useEffect(() => { fetchDashboard(); }, [fetchDashboard]);

  useEffect(() => {
    if (!isLive) return;
    const interval = setInterval(fetchDashboard, REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [isLive, fetchDashboard]);

  const getStatusClass = (s) => s ? s.toLowerCase().replace(/\s+/g, "-") : "";

  const formatDateTime = (dt) => {
    if (!dt) return "—";
    try {
      return new Date(dt).toLocaleString("es-CL", {
        day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit"
      });
    } catch { return "—"; }
  };

  // Filtrar rutas segun rol
  const filteredRoutes = role === ROLES.CONDUCTOR && conductorId
    ? dashboardData.routes.filter(r => String(r.idConductorRef) === conductorId)
    : dashboardData.routes;

  // Lista de conductores para el selector
  const conductores = dashboardData.users.filter(u =>
    u.rol?.nombreRol?.includes("CONDUCTOR")
  );

  return (
    <main className="dashboard-container">
      {/* Barra de estado en vivo */}
      <div className="live-bar">
        <div className="live-bar-left">
          <button className={`live-toggle ${isLive ? "active" : ""}`} onClick={() => setIsLive(!isLive)}>
            <span className={`live-dot ${isLive ? "pulsing" : ""}`}></span>
            {isLive ? "EN VIVO" : "PAUSADO"}
          </button>
          {lastUpdated && (
            <span className="last-updated">Actualizado: {lastUpdated.toLocaleTimeString("es-CL")}</span>
          )}
          {role === ROLES.CONDUCTOR && (
            <select className="conductor-select" value={conductorId}
              onChange={(e) => setConductorId(e.target.value)}>
              <option value="">-- Seleccionar conductor --</option>
              {conductores.map(c => (
                <option key={c.idUsuario} value={c.idUsuario}>
                  {c.nombres} {c.apellidos} (ID: {c.idUsuario})
                </option>
              ))}
            </select>
          )}
        </div>
        <button className="refresh-btn" onClick={fetchDashboard} title="Refrescar ahora">↻</button>
      </div>

      {loading ? (
        <div className="status-message loading">Cargando datos del ecosistema...</div>
      ) : error ? (
        <div className="status-message error">{error}</div>
      ) : (
        <div className="tables-wrapper-vertical">
          {/* RUTAS */}
          <section className="data-section full-width">
            <h2>
              Rutas Operativas
              <span className="badge-count">{filteredRoutes.length}</span>
            </h2>
            <div className="table-scroll">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th><th>Origen</th><th>Destino</th><th>Camion</th>
                    <th>Distancia</th><th>Progreso</th><th>Velocidad</th>
                    <th>Estado</th><th>ETA</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRoutes.length === 0 ? (
                    <tr><td colSpan="9" className="empty-cell">Sin datos de rutas</td></tr>
                  ) : filteredRoutes.map((route) => (
                    <tr key={route.idRuta} className={getStatusClass(route.estado) === "en-transito" ? "row-live" : ""}>
                      <td className="cell-id">{route.idRuta}</td>
                      <td className="cell-location">{route.origenDireccion || "—"}</td>
                      <td className="cell-location">{route.destinoDireccion || "—"}</td>
                      <td className="cell-truck">
                        <span className="truck-patente">{route.truck?.patente || "—"}</span>
                        {route.truck?.marcaModelo && <span className="truck-model">{route.truck.marcaModelo}</span>}
                      </td>
                      <td className="cell-number">{route.distanciaEstimadaKm ? `${route.distanciaEstimadaKm.toLocaleString("es-CL")} km` : "—"}</td>
                      <td className="cell-progress">
                        {route.progressPercent != null ? (
                          <div className="progress-wrapper">
                            <div className="progress-bar">
                              <div className={`progress-fill ${getStatusClass(route.estado)}`}
                                style={{ width: `${Math.min(route.progressPercent, 100)}%` }}></div>
                            </div>
                            <span className="progress-text">
                              {route.progressPercent}%
                              {route.kmRecorridos != null && <span className="km-detail"> ({route.kmRecorridos} km)</span>}
                            </span>
                          </div>
                        ) : "—"}
                      </td>
                      <td className="cell-number">{route.velocidadSimulada > 0 ? `${route.velocidadSimulada} km/h` : "—"}</td>
                      <td>
                        <span className={`status-badge ${getStatusClass(route.estado)}`}>
                          {getStatusClass(route.estado) === "en-transito" && <span className="live-dot pulsing mini"></span>}
                          {route.estado}
                        </span>
                      </td>
                      <td className="cell-date">{formatDateTime(route.etaCalculado)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          {/* USUARIOS - solo para Admin y Despachador */}
          {role !== ROLES.CONDUCTOR && (
            <section className="data-section full-width">
              <h2>
                Usuarios del Sistema
                <span className="badge-count">{dashboardData.users.length}</span>
              </h2>
              <div className="table-scroll">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>ID</th><th>Nombre Completo</th><th>RUT</th><th>Correo</th>
                      <th>Telefono</th><th>Rol</th><th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dashboardData.users.length === 0 ? (
                      <tr><td colSpan="7" className="empty-cell">Sin datos de usuarios</td></tr>
                    ) : dashboardData.users.map((user) => (
                      <tr key={user.idUsuario}>
                        <td className="cell-id">{user.idUsuario}</td>
                        <td>{user.nombres} {user.apellidos}</td>
                        <td className="cell-mono">{user.rut || "—"}</td>
                        <td className="cell-email">{user.correo || "—"}</td>
                        <td className="cell-mono">{user.telefono || "—"}</td>
                        <td>
                          <span className={`role-badge ${
                            user.rol?.nombreRol?.includes("ADMINISTRADOR") ? "admin" :
                            user.rol?.nombreRol?.includes("DESPACHADOR") ? "despachador" : "conductor"
                          }`}>{user.rol?.nombreRol?.replace("ROLE_", "") || "—"}</span>
                        </td>
                        <td>
                          <span className={`status-dot ${user.estadoActivo !== false ? "active" : "inactive"}`}>
                            {user.estadoActivo !== false ? "Activo" : "Inactivo"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          )}
        </div>
      )}
    </main>
  );
};

export default Dashboard;