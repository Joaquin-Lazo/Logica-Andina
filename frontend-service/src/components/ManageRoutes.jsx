import React, { useState, useEffect, useCallback } from "react";
import { validateCoord, validateNotEmpty, validatePositiveNumber } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = {
  idConductorRef: "", idDespachadorRef: "", idCamion: "",
  origenDireccion: "", destinoDireccion: "",
  latDestino: "", lngDestino: "", distanciaEstimadaKm: ""
};

const ManageRoutes = () => {
  const [routes, setRoutes] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  const fetchRoutes = useCallback(() => {
    fetch(BFF)
      .then(r => r.json())
      .then(data => setRoutes(data.routes || []))
      .catch(() => {});
  }, []);

  useEffect(() => { fetchRoutes(); }, [fetchRoutes]);

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
    errs.origenDireccion = validateNotEmpty(form.origenDireccion, 'Origen');
    errs.destinoDireccion = validateNotEmpty(form.destinoDireccion, 'Destino');
    errs.coords = validateCoord(form.latDestino, form.lngDestino);
    errs.distanciaEstimadaKm = validatePositiveNumber(form.distanciaEstimadaKm, 'Distancia');
    const filtered = Object.fromEntries(Object.entries(errs).filter(([,v]) => v));
    setErrors(filtered);
    if (Object.keys(filtered).length > 0) return;
    const payload = {
      idConductorRef: parseInt(form.idConductorRef),
      idDespachadorRef: parseInt(form.idDespachadorRef),
      truck: { idCamion: parseInt(form.idCamion) },
      origenDireccion: form.origenDireccion,
      destinoDireccion: form.destinoDireccion,
      latDestino: parseFloat(form.latDestino),
      lngDestino: parseFloat(form.lngDestino),
      distanciaEstimadaKm: parseFloat(form.distanciaEstimadaKm),
      estado: "Pendiente"
    };
    try {
      const url = editingId ? `${BFF}/proxy/routes/${editingId}` : `${BFF}/proxy/routes`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        setMsg(editingId ? "Ruta actualizada" : "Ruta creada");
        resetForm();
        fetchRoutes();
      } else {
        setMsg("Error: " + res.status);
      }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (r) => {
    setForm({
      idConductorRef: r.idConductorRef || "",
      idDespachadorRef: r.idDespachadorRef || "",
      idCamion: r.truck?.idCamion || "",
      origenDireccion: r.origenDireccion || "",
      destinoDireccion: r.destinoDireccion || "",
      latDestino: r.latDestino || "",
      lngDestino: r.lngDestino || "",
      distanciaEstimadaKm: r.distanciaEstimadaKm || ""
    });
    setEditingId(r.idRuta);
    setShowForm(true);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/routes/${deleteConfirmId}`, { method: "DELETE" });
      fetchRoutes();
    } catch {}
    setDeleteConfirmId(null);
  };

  const getStatusClass = (s) => s ? s.toLowerCase().replace(/\s+/g, "-") : "";

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Gestion de Rutas</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nueva Ruta"}
        </button>
      </div>

      {msg && <div className="status-message loading" style={{marginBottom: 16}}>{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel">
          <p>Confirmar eliminacion de ruta #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card">
          <h3>{editingId ? "Editar Ruta #" + editingId : "Nueva Ruta"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <input name="origenDireccion" value={form.origenDireccion} onChange={handleChange} placeholder="Origen" required />
              <input name="destinoDireccion" value={form.destinoDireccion} onChange={handleChange} placeholder="Destino" required />
            </div>
            <div className="form-row">
              <div className="field-group">
                <input type="number" step="any" name="latDestino" value={form.latDestino} onChange={handleChange} placeholder="Latitud destino" required />
                {errors.coords && <span className="field-error">{errors.coords}</span>}
              </div>
              <input type="number" step="any" name="lngDestino" value={form.lngDestino} onChange={handleChange} placeholder="Longitud destino" required />
            </div>
            <div className="form-row">
              <input type="number" name="idConductorRef" value={form.idConductorRef} onChange={handleChange} placeholder="ID Conductor" required />
              <input type="number" name="idDespachadorRef" value={form.idDespachadorRef} onChange={handleChange} placeholder="ID Despachador" required />
              <input type="number" name="idCamion" value={form.idCamion} onChange={handleChange} placeholder="ID Camion" required />
            </div>
            <input type="number" step="any" name="distanciaEstimadaKm" value={form.distanciaEstimadaKm} onChange={handleChange} placeholder="Distancia estimada (km)" required />
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Ruta"}</button>
          </form>
        </div>
      )}

      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Origen</th><th>Destino</th><th>Camion</th>
                <th>Distancia</th><th>Estado</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {routes.length === 0 ? (
                <tr><td colSpan="7" className="empty-cell">Sin rutas</td></tr>
              ) : routes.map(r => (
                <tr key={r.idRuta}>
                  <td className="cell-id">{r.idRuta}</td>
                  <td>{r.origenDireccion}</td>
                  <td>{r.destinoDireccion}</td>
                  <td className="cell-truck">
                    <span className="truck-patente">{r.truck?.patente}</span>
                    {r.truck?.marcaModelo && <span className="truck-model">{r.truck.marcaModelo}</span>}
                  </td>
                  <td className="cell-number">{r.distanciaEstimadaKm} km</td>
                  <td><span className={`status-badge ${getStatusClass(r.estado)}`}>{r.estado}</span></td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(r)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => setDeleteConfirmId(r.idRuta)}>Eliminar</button>
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

export default ManageRoutes;
