import React, { useState, useEffect } from "react";

const Dashboard = () => {
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
                    <tr key={user.idUsuario}>
                      <td>{user.idUsuario}</td>
                      <td>{user.nombres} {user.apellidos}</td>
                      <td>{user.rol.nombreRol.replace('ROLE_', '')}</td>
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
                    <tr key={route.idRuta}>
                      <td>{route.idRuta}</td>
                      <td>Camión #{route.truck.patente}</td>
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
  );
};

export default Dashboard;