import React, { useState, useEffect, useCallback, useRef } from "react";
import { validateCoord, validateNotEmpty, validatePositiveNumber } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = {
  idConductorRef: "", idDespachadorRef: "", idCamion: "",
  origenDireccion: "", destinoDireccion: "",
  latDestino: "", lngDestino: "", distanciaEstimadaKm: "", idCliente: ""
};

const ManageRoutes = () => {
  const [routes, setRoutes] = useState([]);
  const [users, setUsers] = useState([]);
  const [trucks, setTrucks] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const formRef = useRef(null);
  const confirmRef = useRef(null);

  const [clients, setClients] = useState([]);

  const fetchRoutes = useCallback(() => {
    fetch(BFF)
      .then(r => r.json())
      .then(data => {
         setRoutes(data.routes || []);
         setUsers(data.users || []);
         setTrucks(data.trucks || []);
      })
      .catch(() => { });
  }, []);

  const fetchClients = useCallback(() => {
    fetch(`${BFF}/proxy/clients`)
      .then(r => r.json())
      .then(data => setClients(data || []))
      .catch(() => { });
  }, []);

  useEffect(() => { fetchRoutes(); fetchClients(); }, [fetchRoutes, fetchClients]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleClientChange = (e) => {
    const selectedId = e.target.value;
    if (!selectedId) {
       setForm({ ...form, idCliente: "", latDestino: "", lngDestino: "", destinoDireccion: "" });
       return;
    }
    const client = clients.find(c => c.idCliente == selectedId);
    if (client) {
       setForm({ 
         ...form, 
         idCliente: selectedId, 
         latDestino: client.latitud || "", 
         lngDestino: client.longitud || "",
         destinoDireccion: client.direccionFacturacion || ""
       });
    }
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
    const filtered = Object.fromEntries(Object.entries(errs).filter(([, v]) => v));
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
        const savedRoute = editingId ? null : await res.json();
        
        // Auto-create Cargo and Invoice if it's a new route and a client was selected
        if (!editingId && form.idCliente && savedRoute && savedRoute.idRuta) {
           try {
             // Create Cargo
             await fetch(`${BFF}/proxy/cargo`, {
                 method: "POST", headers: { "Content-Type": "application/json" },
                 body: JSON.stringify({
                     route: { idRuta: savedRoute.idRuta },
                     cliente: { idCliente: parseInt(form.idCliente) },
                     descripcionProductos: "Carga consolidada automática",
                     tipoCarga: "General",
                     pesoToneladas: 15.0,
                     volumenM3: 30.0,
                     estadoEntrega: "Pendiente"
                 })
             });
             // Create Invoice
             await fetch(`${BFF}/proxy/invoices`, {
                 method: "POST", headers: { "Content-Type": "application/json" },
                 body: JSON.stringify({
                     route: { idRuta: savedRoute.idRuta },
                     cliente: { idCliente: parseInt(form.idCliente) },
                     montoNeto: 1000000.0,
                     impuestos: 190000.0,
                     totalPagar: 1190000.0,
                     estadoPago: "Pendiente"
                 })
             });
           } catch (err) {
             console.error("Error auto-creating cargo/invoice", err);
           }
        }

        setMsg(editingId ? "Ruta actualizada" : "Ruta, Cargamento y Factura creados exitosamente");
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
      distanciaEstimadaKm: r.distanciaEstimadaKm || "",
      idCliente: "" // Editing doesn't re-select the client easily without looking up cargo
    });
    setEditingId(r.idRuta);
    setShowForm(true);
    setTimeout(() => formRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/routes/${deleteConfirmId}`, { method: "DELETE" });
      fetchRoutes();
    } catch { }
    setDeleteConfirmId(null);
  };

  const getStatusClass = (s) => s ? s.toLowerCase().replace(/\s+/g, "-") : "";

  const updateRouteStatus = async (id, newStatus) => {
    try {
      const res = await fetch(`${BFF}/proxy/routes/${id}/status`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ estado: newStatus })
      });
      if (res.ok) {
        setMsg("Estado actualizado");
        fetchRoutes();
      } else {
        setMsg("Error al actualizar estado");
      }
    } catch {
      setMsg("Error de conexion");
    }
  };

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Gestion de Rutas</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nueva Ruta"}
        </button>
      </div>

      {msg && <div className="status-message loading">{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel" ref={confirmRef}>
          <p>Confirmar eliminacion de ruta #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card" ref={formRef}>
          <h3>{editingId ? "Editar Ruta #" + editingId : "Nueva Ruta"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <input name="origenDireccion" value={form.origenDireccion} onChange={handleChange} placeholder="Origen" required />
              
              <select name="idCliente" value={form.idCliente} onChange={handleClientChange} required={!editingId}>
                <option value="">-- Seleccionar Cliente Destino --</option>
                {clients.map(c => (
                  <option key={c.idCliente} value={c.idCliente}>
                    {c.razonSocial} ({c.rutEmpresa})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <input name="destinoDireccion" value={form.destinoDireccion} onChange={handleChange} placeholder="Direccion Destino" required readOnly={!!form.idCliente} style={form.idCliente ? {backgroundColor: '#e9ecef'} : {}} />
              <input type="number" step="any" name="latDestino" value={form.latDestino} onChange={handleChange} placeholder="Latitud destino" required readOnly={!!form.idCliente} style={form.idCliente ? {backgroundColor: '#e9ecef'} : {}} />
              <input type="number" step="any" name="lngDestino" value={form.lngDestino} onChange={handleChange} placeholder="Longitud destino" required readOnly={!!form.idCliente} style={form.idCliente ? {backgroundColor: '#e9ecef'} : {}} />
            </div>
            <div className="form-row">
              <select name="idConductorRef" value={form.idConductorRef} onChange={handleChange} required>
                <option value="">-- Conductor --</option>
                {users.filter(u => u.rol && u.rol.nombreRol === 'ROLE_CONDUCTOR').map(u => (
                  <option key={u.idUsuario} value={u.idUsuario}>{u.nombres} {u.apellidos} (RUT: {u.rut})</option>
                ))}
              </select>
              <select name="idDespachadorRef" value={form.idDespachadorRef} onChange={handleChange} required>
                <option value="">-- Despachador --</option>
                {users.filter(u => u.rol && u.rol.nombreRol === 'ROLE_DESPACHADOR').map(u => (
                  <option key={u.idUsuario} value={u.idUsuario}>{u.nombres} {u.apellidos} (RUT: {u.rut})</option>
                ))}
              </select>
              <select name="idCamion" value={form.idCamion} onChange={handleChange} required>
                <option value="">-- Camion --</option>
                {trucks.map(t => (
                  <option key={t.idCamion} value={t.idCamion}>{t.patente} ({t.marcaModelo})</option>
                ))}
              </select>
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
                    {r.estado === 'Pendiente' && (
                      <button className="btn-sm btn-primary" onClick={() => updateRouteStatus(r.idRuta, 'En Transito')}>Iniciar</button>)}
                    {r.estado === 'En Transito' && (
                      <button className="btn-sm btn-primary" onClick={() => updateRouteStatus(r.idRuta, 'Completada')}>Completar</button>)}
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(r)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => { setDeleteConfirmId(r.idRuta); setTimeout(() => confirmRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50); }}>Eliminar</button>
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
