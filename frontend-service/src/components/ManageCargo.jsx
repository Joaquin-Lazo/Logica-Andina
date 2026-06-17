import React, { useState, useEffect, useCallback, useRef } from "react";
import { validateNotEmpty, validatePositiveNumber } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = {
  idRuta: "", idCliente: "", descripcionProductos: "",
  tipoCarga: "", pesoToneladas: "", volumenM3: "", estadoEntrega: "Pendiente"
};

const ManageCargo = () => {
  const [cargos, setCargos] = useState([]);
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
    fetch(`${BFF}/proxy/cargo`)
      .then(r => r.json())
      .then(d => setCargos(Array.isArray(d) ? d : []))
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
    setForm({ ...form, [e.target.name]: e.target.value });
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
    errs.desc = validateNotEmpty(form.descripcionProductos, 'Descripción');
    errs.peso = validatePositiveNumber(form.pesoToneladas, 'Peso');
    errs.vol = validatePositiveNumber(form.volumenM3, 'Volumen');
    
    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v));
    setErrors(filtered);
    if (Object.keys(filtered).length > 0) return;

    const payload = {
      route: { idRuta: parseInt(form.idRuta) },
      cliente: { idCliente: parseInt(form.idCliente) },
      descripcionProductos: form.descripcionProductos,
      tipoCarga: form.tipoCarga,
      pesoToneladas: parseFloat(form.pesoToneladas),
      volumenM3: parseFloat(form.volumenM3),
      estadoEntrega: form.estadoEntrega
    };

    try {
      const url = editingId ? `${BFF}/proxy/cargo/${editingId}` : `${BFF}/proxy/cargo`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        setMsg(editingId ? "Cargamento actualizado" : "Cargamento creado exitosamente");
        resetForm();
        fetchData();
      } else {
        setMsg("Error: " + res.status);
      }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (c) => {
    setForm({
      idRuta: c.route?.idRuta || "",
      idCliente: c.cliente?.idCliente || c.client?.idCliente || "",
      descripcionProductos: c.descripcionProductos || "",
      tipoCarga: c.tipoCarga || "",
      pesoToneladas: c.pesoToneladas || "",
      volumenM3: c.volumenM3 || "",
      estadoEntrega: c.estadoEntrega || "Pendiente"
    });
    setEditingId(c.idCargamento);
    setShowForm(true);
    setTimeout(() => formRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/cargo/${deleteConfirmId}`, { method: "DELETE" });
      fetchData();
    } catch { }
    setDeleteConfirmId(null);
  };

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
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nuevo Cargamento"}
        </button>
      </div>
      
      {msg && <div className="status-message loading">{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel" ref={confirmRef}>
          <p>Confirmar eliminacion de cargamento #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card" ref={formRef}>
          <h3>{editingId ? "Editar Cargamento #" + editingId : "Nuevo Cargamento"}</h3>
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
              <input name="descripcionProductos" value={form.descripcionProductos} onChange={handleChange} placeholder="Descripción de Productos" required />
              <input name="tipoCarga" value={form.tipoCarga} onChange={handleChange} placeholder="Tipo de Carga (ej. General, Refrigerada)" required />
            </div>
            <div className="form-row">
              <input type="number" step="any" name="pesoToneladas" value={form.pesoToneladas} onChange={handleChange} placeholder="Peso (Toneladas)" required />
              <input type="number" step="any" name="volumenM3" value={form.volumenM3} onChange={handleChange} placeholder="Volumen (m3)" required />
              <select name="estadoEntrega" value={form.estadoEntrega} onChange={handleChange} required>
                <option value="Pendiente">Pendiente</option>
                <option value="En Transito">En Transito</option>
                <option value="Entregado">Entregado</option>
              </select>
            </div>
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Cargamento"}</button>
          </form>
        </div>
      )}

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
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cargos.length === 0 ? (
                <tr><td colSpan="9" className="empty-cell">Sin cargamentos</td></tr>
              ) : (
                cargos.map(c => (
                  <tr key={c.idCargamento}>
                    <td className="cell-id">{c.idCargamento}</td>
                    <td className="cell-id">#{c.route?.idRuta || "—"}</td>
                    <td>{c.client?.razonSocial || c.cliente?.razonSocial || "—"}</td>
                    <td>{c.descripcionProductos}</td>
                    <td>{c.tipoCarga}</td>
                    <td className="cell-number">{c.pesoToneladas?.toFixed(2)}</td>
                    <td className="cell-number">{c.volumenM3?.toFixed(2)}</td>
                    <td>
                      <span className={`status-badge ${getStatusClass(c.estadoEntrega)}`}>
                        {c.estadoEntrega || "Pendiente"}
                      </span>
                    </td>
                    <td className="cell-actions">
                      <button className="btn-sm btn-edit" onClick={() => handleEdit(c)}>Editar</button>
                      <button className="btn-sm btn-delete" onClick={() => { setDeleteConfirmId(c.idCargamento); setTimeout(() => confirmRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50); }}>Eliminar</button>
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
