import React, { useState, useEffect, useCallback, useRef } from "react";
import { validatePositiveNumber } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = {
  idRuta: "", idCliente: "", montoNeto: "",
  impuestos: "", totalPagar: "", estadoPago: "Pendiente"
};

const ManageInvoices = () => {
  const [invoices, setInvoices] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const formRef = useRef(null);
  const confirmRef = useRef(null);

  const fetchData = useCallback(() => {
    fetch(`${BFF}/proxy/invoices`)
      .then(r => r.json())
      .then(d => setInvoices(Array.isArray(d) ? d : []))
      .catch(() => {});
      
    fetch(BFF)
      .then(r => r.json())
      .then(d => setRoutes(d.routes || []))
      .catch(() => {});

    fetch(`${BFF}/proxy/clients`)
      .then(r => r.json())
      .then(d => setClients(d || []))
      .catch(() => {});
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    let newForm = { ...form, [name]: value };

    // Auto calculate totals if amounts change
    if (name === "montoNeto") {
       const neto = parseFloat(value) || 0;
       const iva = neto * 0.19; // 19% IVA en Chile
       newForm.impuestos = iva.toFixed(2);
       newForm.totalPagar = (neto + iva).toFixed(2);
    }
    setForm(newForm);
  };

  const resetForm = () => {
    setForm(EMPTY_FORM);
    setEditingId(null);
    setShowForm(false);
    setMsg(null);
    setErrors({});
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    errs.neto = validatePositiveNumber(form.montoNeto, 'Monto Neto');
    errs.imp = validatePositiveNumber(form.impuestos, 'Impuestos');
    errs.tot = validatePositiveNumber(form.totalPagar, 'Total Pagar');
    
    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v));
    setErrors(filtered);
    if (Object.keys(filtered).length > 0) return;

    const payload = {
      route: { idRuta: parseInt(form.idRuta) },
      cliente: { idCliente: parseInt(form.idCliente) },
      montoNeto: parseFloat(form.montoNeto),
      impuestos: parseFloat(form.impuestos),
      totalPagar: parseFloat(form.totalPagar),
      estadoPago: form.estadoPago
    };

    try {
      const url = editingId ? `${BFF}/proxy/invoices/${editingId}` : `${BFF}/proxy/invoices`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        setMsg(editingId ? "Factura actualizada" : "Factura creada exitosamente");
        resetForm();
        fetchData();
      } else {
        setMsg("Error: " + res.status);
      }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (f) => {
    setForm({
      idRuta: f.route?.idRuta || "",
      idCliente: f.cliente?.idCliente || f.client?.idCliente || "",
      montoNeto: f.montoNeto || "",
      impuestos: f.impuestos || "",
      totalPagar: f.totalPagar || "",
      estadoPago: f.estadoPago || "Pendiente"
    });
    setEditingId(f.idFactura);
    setShowForm(true);
    setTimeout(() => formRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/invoices/${deleteConfirmId}`, { method: "DELETE" });
      fetchData();
    } catch { }
    setDeleteConfirmId(null);
  };

  const formatMoney = (n) => n != null ? `$${Number(n).toLocaleString("es-CL")}` : "—";
  const getPayClass = (s) => (!s || s !== "Pagada") ? "pendiente" : "completada";

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Facturas</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nueva Factura"}
        </button>
      </div>

      {msg && <div className="status-message loading">{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel" ref={confirmRef}>
          <p>Confirmar eliminacion de factura #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card" ref={formRef}>
          <h3>{editingId ? "Editar Factura #" + editingId : "Nueva Factura"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <select name="idRuta" value={form.idRuta} onChange={handleChange} required>
                <option value="">-- Seleccionar Ruta --</option>
                {routes.map(r => (
                  <option key={r.idRuta} value={r.idRuta}>Ruta #{r.idRuta} ({r.origenDireccion} - {r.destinoDireccion})</option>
                ))}
              </select>
              <select name="idCliente" value={form.idCliente} onChange={handleChange} required>
                <option value="">-- Seleccionar Cliente --</option>
                {clients.map(c => (
                  <option key={c.idCliente} value={c.idCliente}>{c.razonSocial} ({c.rutEmpresa})</option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <input type="number" step="any" name="montoNeto" value={form.montoNeto} onChange={handleChange} placeholder="Monto Neto ($)" required />
              <input type="number" step="any" name="impuestos" value={form.impuestos} onChange={handleChange} placeholder="IVA (Calculado aut.)" required />
            </div>
            <div className="form-row">
              <input type="number" step="any" name="totalPagar" value={form.totalPagar} onChange={handleChange} placeholder="Total a Pagar" required />
              <select name="estadoPago" value={form.estadoPago} onChange={handleChange} required>
                <option value="Pendiente">Pendiente</option>
                <option value="Pagada">Pagada</option>
              </select>
            </div>
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Factura"}</button>
          </form>
        </div>
      )}

      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Ruta</th><th>Cliente</th><th>Monto Neto</th>
                <th>IVA</th><th>Total</th><th>Estado Pago</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {invoices.length === 0 ? <tr><td colSpan="8" className="empty-cell">Sin facturas</td></tr> : invoices.map(f => (
                <tr key={f.idFactura}>
                  <td className="cell-id">{f.idFactura}</td>
                  <td className="cell-id">#{f.route?.idRuta || "—"}</td>
                  <td>{f.client?.razonSocial || f.cliente?.razonSocial || "—"}</td>
                  <td className="cell-number">{formatMoney(f.montoNeto)}</td>
                  <td className="cell-number">{formatMoney(f.impuestos)}</td>
                  <td className="cell-number cell-bold">{formatMoney(f.totalPagar)}</td>
                  <td><span className={`status-badge ${getPayClass(f.estadoPago)}`}>{f.estadoPago}</span></td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(f)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => { setDeleteConfirmId(f.idFactura); setTimeout(() => confirmRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50); }}>Eliminar</button>
                  </td>
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
