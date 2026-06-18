import React, { useState, useEffect, useCallback, useRef } from "react";
import { validateRut, validateEmail, validateNotEmpty } from "../utils/validators";
import { MapContainer, TileLayer, Marker, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import iconRetinaUrl from "leaflet/dist/images/marker-icon-2x.png";
import shadowUrl from "leaflet/dist/images/marker-shadow.png";

// Fix Leaflet icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});

const LocationMarker = ({ setForm, position }) => {
  useMapEvents({
    click(e) {
      setForm((prev) => ({ ...prev, latitud: e.latlng.lat, longitud: e.latlng.lng }));
    },
  });
  return position.lat && position.lng ? <Marker position={[position.lat, position.lng]} /> : null;
};

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = { rutEmpresa: "", razonSocial: "", direccionFacturacion: "", correoContacto: "", latitud: "", longitud: "" };

const ManageClients = () => {
  const [clients, setClients] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const formRef = useRef(null);
  const confirmRef = useRef(null);

  const fetchClients = useCallback(() => {
    fetch(`${BFF}/proxy/clients`)
      .then(r => r.json())
      .then(data => setClients(Array.isArray(data) ? data : []))
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
    
    // Validate coordinates
    e.latitud = validateNotEmpty(form.latitud, 'Latitud');
    if (form.latitud && isNaN(parseFloat(form.latitud))) e.latitud = "Debe ser numero";
    
    e.longitud = validateNotEmpty(form.longitud, 'Longitud');
    if (form.longitud && isNaN(parseFloat(form.longitud))) e.longitud = "Debe ser numero";

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
    setForm({ 
      rutEmpresa: c.rutEmpresa || "", 
      razonSocial: c.razonSocial || "", 
      direccionFacturacion: c.direccionFacturacion || "", 
      correoContacto: c.correoContacto || "",
      latitud: c.latitud || "",
      longitud: c.longitud || ""
    });
    setEditingId(c.idCliente);
    setShowForm(true);
    setTimeout(() => formRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
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
      {msg && <div className="status-message loading">{msg}</div>}
      {deleteConfirmId && (
        <div className="confirm-panel" ref={confirmRef}>
          <p>Confirmar eliminacion de cliente #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}
      {showForm && (
        <div className="form-card" ref={formRef}>
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
            <div className="form-row">
              <div className="field-group">
                <input type="number" step="any" name="latitud" value={form.latitud} onChange={handleChange} placeholder="Latitud" required />
                {errors.latitud && <span className="field-error">{errors.latitud}</span>}
              </div>
              <div className="field-group">
                <input type="number" step="any" name="longitud" value={form.longitud} onChange={handleChange} placeholder="Longitud" required />
                {errors.longitud && <span className="field-error">{errors.longitud}</span>}
              </div>
            </div>
            
            <div style={{ width: "100%", marginBottom: "1rem", marginTop: "1rem" }}>
              <label className="form-label" style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>Seleccionar Coordenadas en el Mapa</label>
              <div style={{ height: "250px", width: "100%", border: "1px solid #ccc", borderRadius: "8px", overflow: "hidden" }}>
                <MapContainer 
                  center={form.latitud && form.longitud ? [parseFloat(form.latitud), parseFloat(form.longitud)] : [-33.4489, -70.6693]} 
                  zoom={10} 
                  style={{ height: "100%", width: "100%", zIndex: 1 }}
                >
                  <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                  <LocationMarker setForm={setForm} position={{ lat: parseFloat(form.latitud), lng: parseFloat(form.longitud) }} />
                </MapContainer>
              </div>
            </div>
            
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Cliente"}</button>
          </form>
        </div>
      )}
      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead><tr><th>ID</th><th>RUT</th><th>Razon Social</th><th>Direccion</th><th>Coordenadas</th><th>Acciones</th></tr></thead>
            <tbody>
              {clients.length === 0 ? <tr><td colSpan="6" className="empty-cell">Sin clientes</td></tr> : clients.map(c => (
                <tr key={c.idCliente}>
                  <td className="cell-id">{c.idCliente}</td>
                  <td className="cell-mono">{c.rutEmpresa}</td>
                  <td>{c.razonSocial}</td>
                  <td>{c.direccionFacturacion}</td>
                  <td className="cell-mono">
                    {c.latitud && c.longitud ? `${c.latitud}, ${c.longitud}` : "N/A"}
                  </td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(c)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => { setDeleteConfirmId(c.idCliente); setTimeout(() => confirmRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50); }}>Eliminar</button>
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
