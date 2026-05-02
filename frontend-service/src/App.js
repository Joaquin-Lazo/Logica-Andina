import React, { useState, useEffect } from "react";
import "./App.css";

const App = () => {
  const [dashboardData, setDashboardData] = useState({ users: [], routes: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8082/api/dashboard")
      .then((response) => {
        if (!response.ok) {
          throw new Error("No se pudo conectar con el BFF");
        }
        return response.json();
      })
      .then((data) => {
        setDashboardData(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
        setError("Esperando conexión con el Backend For Frontend...");
        setLoading(false);
      });
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>Logística Andina - Panel de Control</h1>
        <p>Centro de integración de microservicios</p>
      </header>
      
      <main className="dashboard-container">
        {loading ? (
          <div className="status-message loading">Cargando datos del ecosistema...</div>
        ) : error ? (
          <div className="status-message error">{error}</div>
        ) : (
          <div className="tables-wrapper">
            <section className="data-section">
              <h2>Usuarios Activos</h2>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Rol</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.users.length === 0 ? (
                    <tr><td colSpan="3">Sin datos de usuarios</td></tr>
                  ) : (
                    dashboardData.users.map((user) => (
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.nombre}</td>
                        <td>{user.rol}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </section>

            <section className="data-section">
              <h2>Rutas Operativas</h2>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Ruta ID</th>
                    <th>Camión Asignado</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.routes.length === 0 ? (
                    <tr><td colSpan="3">Sin datos de rutas</td></tr>
                  ) : (
                    dashboardData.routes.map((route) => (
                      <tr key={route.id}>
                        <td>{route.id}</td>
                        <td>Camión #{route.camion_id}</td>
                        <td>
                          <span className={`status-badge ${route.estado.toLowerCase().replace(" ", "-")}`}>
                            {route.estado}
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </section>
          </div>
        )}
      </main>
    </div>
  );
};

export default App;