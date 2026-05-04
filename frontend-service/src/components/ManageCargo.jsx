import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082/api/dashboard";

const ManageCargo = () => {
  const [cargos, setCargos] = useState([]);

  const fetchData = useCallback(() => {
    fetch(`${BFF}/proxy/cargo`)
      .then(r => r.json())
      .then(d => {
        console.log("Cargo response:", d);
        setCargos(Array.isArray(d) ? d : []);
      })
      .catch((err) => console.error("Cargo fetch error:", err));
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const getStatusClass = (s) => {
    if (!s) return "";
    const lower = s.toLowerCase();
    if (lower.includes("entregado")) return "completada";
    if (lower.includes("transito")) return "en-transito";
    return "pendiente";
  };

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Cargamentos (Carga)</h2>
      </div>
      
      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Ruta</th>
                <th>Cliente</th>
                <th>Descripción</th>
                <th>Tipo</th>
                <th>Peso (Ton)</th>
                <th>Volumen (m3)</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {cargos.length === 0 ? (
                <tr><td colSpan="8" className="empty-cell">Sin cargamentos</td></tr>
              ) : (
                cargos.map(c => (
                  <tr key={c.idCargamento}>
                    <td className="cell-id">{c.idCargamento}</td>
                    <td className="cell-id">#{c.route?.idRuta || "—"}</td>
                    <td>{c.client?.razonSocial || "—"}</td>
                    <td>{c.descripcionProductos}</td>
                    <td>{c.tipoCarga}</td>
                    <td className="cell-number">{c.pesoToneladas?.toFixed(2)}</td>
                    <td className="cell-number">{c.volumenM3?.toFixed(2)}</td>
                    <td>
                      <span className={`status-badge ${getStatusClass(c.estadoEntrega)}`}>
                        {c.estadoEntrega || "Pendiente"}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
};

export default ManageCargo;
