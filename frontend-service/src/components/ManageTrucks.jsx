import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = { patente: "", marcaModelo: "", capacidadMaxToneladas: "", estadoOperativo: "Disponible" };
const ESTADOS = ["Disponible", "En Mantenimiento", "Fuera de Servicio"];

const ManageTrucks = () => {
  const [trucks, setTrucks] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);

  const fetchTrucks = useCallback(() => {
    fetch(`${BFF}/proxy/camiones`)
      .then(r => r.json())
      .then(data => setTrucks(Array.isArray(data) ? data : []))
      .catch(() => {});
  }, []);

  useEffect(() => { fetchTrucks(); }, [fetchTrucks]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const resetForm = () => { setForm(EMPTY_FORM); setEditingId(null); setShowForm(false); setMsg(null); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      patente: form.patente,
      marcaModelo: form.marcaModelo,
      capacidadMaxToneladas: parseFloat(form.capacidadMaxToneladas),
      estadoOperativo: form.estadoOperativo
    };
    try {
      const url = editingId ? `${BFF}/proxy/camiones/${editingId}` : `${BFF}/proxy/camiones`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        setMsg(editingId ? "Camion actualizado" : "Camion creado");
        resetForm(); fetchTrucks();
      } else { setMsg("Error: " + res.status); }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (t) => {
    setForm({
      patente: t.patente || "",
      marcaModelo: t.marcaModelo || "",
      capacidadMaxToneladas: t.capacidadMaxToneladas || "",
      estadoOperativo: t.estadoOperativo || "Disponible"
    });
    setEditingId(t.idCamion);
    setShowForm(true);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/camiones/${deleteConfirmId}`, { method: "DELETE" });
      fetchTrucks();
    } catch {}
    setDeleteConfirmId(null);
  };

  const getStatusClass = (s) => s ? s.toLowerCase().replace(/\s+/g, "-") : "";

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Gestion de Flota</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nuevo Camion"}
        </button>
      </div>

      {msg && <div className="status-message loading" style={{marginBottom: 16}}>{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel">
          <p>Confirmar eliminacion de camion #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card">
          <h3>{editingId ? "Editar Camion #" + editingId : "Nuevo Camion"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <input name="patente" value={form.patente} onChange={handleChange} placeholder="Patente (ej: AB-CD-12)" required />
              <input name="marcaModelo" value={form.marcaModelo} onChange={handleChange} placeholder="Marca y Modelo" required />
            </div>
            <div className="form-row">
              <input type="number" step="0.1" name="capacidadMaxToneladas" value={form.capacidadMaxToneladas} onChange={handleChange} placeholder="Capacidad (toneladas)" required />
              <select name="estadoOperativo" value={form.estadoOperativo} onChange={handleChange}>
                {ESTADOS.map(e => <option key={e} value={e}>{e}</option>)}
              </select>
            </div>
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Camion"}</button>
          </form>
        </div>
      )}

      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Patente</th><th>Marca / Modelo</th>
                <th>Capacidad (ton)</th><th>Estado</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {trucks.length === 0 ? (
                <tr><td colSpan="6" className="empty-cell">Sin camiones</td></tr>
              ) : trucks.map(t => (
                <tr key={t.idCamion}>
                  <td className="cell-id">{t.idCamion}</td>
                  <td className="cell-mono">{t.patente}</td>
                  <td>{t.marcaModelo}</td>
                  <td className="cell-number">{t.capacidadMaxToneladas} ton</td>
                  <td><span className={`status-badge ${getStatusClass(t.estadoOperativo)}`}>{t.estadoOperativo}</span></td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(t)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => setDeleteConfirmId(t.idCamion)}>Eliminar</button>
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

export default ManageTrucks;
