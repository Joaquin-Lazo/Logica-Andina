import React, { useState, useEffect, useCallback } from "react";
import { validateRut, validateEmail, validateNotEmpty } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = { rutEmpresa: "", razonSocial: "", direccionFacturacion: "", correoContacto: "" };

const ManageClients = () => {
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  const fetchClients = useCallback(() => {
    fetch(`${BFF}/proxy/clients`)
      .then(r => r.json())
      .then(data => {
        console.log("Clients response:", data);
        setClients(Array.isArray(data) ? data : []);
      })
      .catch((err) => console.error("Clients fetch error:", err));
  }, []);

  useEffect(() => { fetchClients(); }, [fetchClients]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const resetForm = () => { setForm(EMPTY_FORM); setEditingId(null); setShowForm(false); setMsg(null); setErrors({}); };

  const validate = () => {
    const e = {};
    e.rutEmpresa = validateRut(form.rutEmpresa);
    e.razonSocial = validateNotEmpty(form.razonSocial, 'Razon social');
    e.direccionFacturacion = validateNotEmpty(form.direccionFacturacion, 'Direccion');
    e.correoContacto = validateEmail(form.correoContacto);
    const filtered = Object.fromEntries(Object.entries(e).filter(([, v]) => v));
    setErrors(filtered);
    return Object.keys(filtered).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      const url = editingId ? `${BFF}/proxy/clients/${editingId}` : `${BFF}/proxy/clients`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(form) });
      if (res.ok) { setMsg(editingId ? "Cliente actualizado" : "Cliente creado"); resetForm(); fetchClients(); }
      else { setMsg("Error: " + res.status); }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (c) => {
    setForm({ rutEmpresa: c.rutEmpresa || "", razonSocial: c.razonSocial || "", direccionFacturacion: c.direccionFacturacion || "", correoContacto: c.correoContacto || "" });
    setEditingId(c.idCliente); setShowForm(true);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try { await fetch(`${BFF}/proxy/clients/${deleteConfirmId}`, { method: "DELETE" }); fetchClients(); } catch {}
    setDeleteConfirmId(null);
  };

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Gestion de Clientes</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nuevo Cliente"}
        </button>
      </div>
      {msg && <div className="status-message loading" style={{marginBottom: 16}}>{msg}</div>}
      {deleteConfirmId && (
        <div className="confirm-panel">
          <p>Confirmar eliminacion de cliente #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}
      {showForm && (
        <div className="form-card">
          <h3>{editingId ? "Editar Cliente #" + editingId : "Nuevo Cliente"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <div className="field-group">
                <input name="rutEmpresa" value={form.rutEmpresa} onChange={handleChange} placeholder="RUT Empresa (ej: 77777777-K)" />
                {errors.rutEmpresa && <span className="field-error">{errors.rutEmpresa}</span>}
              </div>
              <div className="field-group">
                <input name="razonSocial" value={form.razonSocial} onChange={handleChange} placeholder="Razon Social" />
                {errors.razonSocial && <span className="field-error">{errors.razonSocial}</span>}
              </div>
            </div>
            <div className="form-row">
              <div className="field-group">
                <input name="direccionFacturacion" value={form.direccionFacturacion} onChange={handleChange} placeholder="Direccion de Facturacion" />
                {errors.direccionFacturacion && <span className="field-error">{errors.direccionFacturacion}</span>}
              </div>
              <div className="field-group">
                <input name="correoContacto" value={form.correoContacto} onChange={handleChange} placeholder="Correo de Contacto" />
                {errors.correoContacto && <span className="field-error">{errors.correoContacto}</span>}
              </div>
            </div>
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Cliente"}</button>
          </form>
        </div>
      )}
      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead><tr><th>ID</th><th>RUT</th><th>Razon Social</th><th>Direccion</th><th>Correo</th><th>Acciones</th></tr></thead>
            <tbody>
              {clients.length === 0 ? <tr><td colSpan="6" className="empty-cell">Sin clientes</td></tr> : clients.map(c => (
                <tr key={c.idCliente}>
                  <td className="cell-id">{c.idCliente}</td>
                  <td className="cell-mono">{c.rutEmpresa}</td>
                  <td>{c.razonSocial}</td>
                  <td>{c.direccionFacturacion}</td>
                  <td className="cell-email">{c.correoContacto}</td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(c)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => setDeleteConfirmId(c.idCliente)}>Eliminar</button>
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

export default ManageClients;
