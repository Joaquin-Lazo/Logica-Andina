import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082/api/dashboard";

const ManageInvoices = () => {
  const [invoices, setInvoices] = useState([]);

  const fetchData = useCallback(() => {
    fetch(`${BFF}/proxy/invoices`)
      .then(r => r.json())
      .then(d => {
        console.log("Invoices response:", d);
        setInvoices(Array.isArray(d) ? d : []);
      })
      .catch(() => {});
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const formatMoney = (n) => n != null ? `$${Number(n).toLocaleString("es-CL")}` : "—";

  const getPayClass = (s) => {
    if (!s) return "";
    if (s === "Pagada") return "completada";
    return "pendiente";
  };

  return (
    <main className="dashboard-container">
      <div className="page-header"><h2>Facturas</h2></div>
      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Ruta</th><th>Cliente</th><th>Monto Neto</th>
                <th>IVA</th><th>Total</th><th>Estado Pago</th>
              </tr>
            </thead>
            <tbody>
              {invoices.length === 0 ? <tr><td colSpan="7" className="empty-cell">Sin facturas</td></tr> : invoices.map(f => (
                <tr key={f.idFactura}>
                  <td className="cell-id">{f.idFactura}</td>
                  <td className="cell-id">#{f.route?.idRuta || "—"}</td>
                  <td>{f.client?.razonSocial || "—"}</td>
                  <td className="cell-number">{formatMoney(f.montoNeto)}</td>
                  <td className="cell-number">{formatMoney(f.impuestos)}</td>
                  <td className="cell-number" style={{fontWeight: 600}}>{formatMoney(f.totalPagar)}</td>
                  <td><span className={`status-badge ${getPayClass(f.estadoPago)}`}>{f.estadoPago}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
};

export default ManageInvoices;
