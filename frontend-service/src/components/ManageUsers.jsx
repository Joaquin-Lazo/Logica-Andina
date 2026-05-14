import React, { useState, useEffect, useCallback, useRef } from "react";
import { validateRut, validateEmail, validateNotEmpty } from "../utils/validators";

const BFF = "http://localhost:8082/api/dashboard";

const EMPTY_FORM = {
  rut: "", nombres: "", apellidos: "", correo: "", telefono: "",
  passwordHash: "", idRol: "3", estadoActivo: true
};
const ROLES_MAP = { 1: "Administrador", 2: "Despachador", 3: "Conductor" };

const ManageUsers = () => {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [msg, setMsg] = useState(null);
  const [errors, setErrors] = useState({});
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const formRef = useRef(null);
  const confirmRef = useRef(null);

  const fetchUsers = useCallback(() => {
    fetch(BFF)
      .then(r => r.json())
      .then(data => setUsers(data.users || []))
      .catch(() => {});
  }, []);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleChange = (e) => {
    const val = e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setForm({ ...form, [e.target.name]: val });
  };

  const resetForm = () => { setForm(EMPTY_FORM); setEditingId(null); setShowForm(false); setMsg(null); setErrors({}); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = {};
    errs.rut = validateRut(form.rut);
    errs.nombres = validateNotEmpty(form.nombres, 'Nombres');
    errs.apellidos = validateNotEmpty(form.apellidos, 'Apellidos');
    errs.correo = validateEmail(form.correo);
    const filtered = Object.fromEntries(Object.entries(errs).filter(([,v]) => v));
    setErrors(filtered);
    if (Object.keys(filtered).length > 0) return;
    const payload = {
      rut: form.rut,
      nombres: form.nombres,
      apellidos: form.apellidos,
      correo: form.correo,
      telefono: form.telefono || null,
      passwordHash: form.passwordHash || "defaulthash",
      estadoActivo: form.estadoActivo,
      rol: { idRol: parseInt(form.idRol) }
    };
    try {
      const url = editingId ? `${BFF}/proxy/users/${editingId}` : `${BFF}/proxy/users`;
      const method = editingId ? "PUT" : "POST";
      const res = await fetch(url, {
        method, headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        setMsg(editingId ? "Usuario actualizado" : "Usuario creado");
        resetForm(); fetchUsers();
      } else { setMsg("Error: " + res.status); }
    } catch { setMsg("Error de conexion"); }
  };

  const handleEdit = (u) => {
    setForm({
      rut: u.rut || "",
      nombres: u.nombres || "",
      apellidos: u.apellidos || "",
      correo: u.correo || "",
      telefono: u.telefono || "",
      passwordHash: "",
      idRol: u.rol?.idRol?.toString() || "3",
      estadoActivo: u.estadoActivo !== false
    });
    setEditingId(u.idUsuario);
    setShowForm(true);
    setTimeout(() => formRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50);
  };

  const confirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      await fetch(`${BFF}/proxy/users/${deleteConfirmId}`, { method: "DELETE" });
      fetchUsers();
    } catch {}
    setDeleteConfirmId(null);
  };

  const getRoleLabel = (rol) => rol?.nombreRol?.replace("ROLE_", "") || "—";
  const getRoleClass = (rol) => {
    const name = rol?.nombreRol || "";
    if (name.includes("ADMINISTRADOR")) return "admin";
    if (name.includes("DESPACHADOR")) return "despachador";
    return "conductor";
  };

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Gestion de Usuarios</h2>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(!showForm); }}>
          {showForm ? "Cancelar" : "+ Nuevo Usuario"}
        </button>
      </div>

      {msg && <div className="status-message loading">{msg}</div>}

      {deleteConfirmId && (
        <div className="confirm-panel" ref={confirmRef}>
          <p>Confirmar eliminacion de usuario #{deleteConfirmId}?</p>
          <div className="confirm-actions">
            <button className="btn-sm btn-delete" onClick={confirmDelete}>Eliminar</button>
            <button className="btn-sm btn-edit" onClick={() => setDeleteConfirmId(null)}>Cancelar</button>
          </div>
        </div>
      )}

      {showForm && (
        <div className="form-card" ref={formRef}>
          <h3>{editingId ? "Editar Usuario #" + editingId : "Nuevo Usuario"}</h3>
          <form onSubmit={handleSubmit} className="crud-form">
            <div className="form-row">
              <input name="rut" value={form.rut} onChange={handleChange} placeholder="RUT (ej: 12345678-9)" required />
              <input name="nombres" value={form.nombres} onChange={handleChange} placeholder="Nombres" required />
              <input name="apellidos" value={form.apellidos} onChange={handleChange} placeholder="Apellidos" required />
            </div>
            <div className="form-row">
              <input type="email" name="correo" value={form.correo} onChange={handleChange} placeholder="Correo electronico" required />
              <input name="telefono" value={form.telefono} onChange={handleChange} placeholder="Telefono (opcional)" />
            </div>
            <div className="form-row">
              <select name="idRol" value={form.idRol} onChange={handleChange}>
                {Object.entries(ROLES_MAP).map(([id, label]) => (
                  <option key={id} value={id}>{label}</option>
                ))}
              </select>
              {!editingId && (
                <input type="password" name="passwordHash" value={form.passwordHash} onChange={handleChange} placeholder="Contrasena" required />
              )}
              <label className="checkbox-label">
                <input type="checkbox" name="estadoActivo" checked={form.estadoActivo} onChange={handleChange} />
                Activo
              </label>
            </div>
            <button type="submit" className="btn-primary">{editingId ? "Guardar Cambios" : "Crear Usuario"}</button>
          </form>
        </div>
      )}

      <section className="data-section full-width">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Nombre</th><th>RUT</th><th>Correo</th>
                <th>Rol</th><th>Estado</th><th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr><td colSpan="7" className="empty-cell">Sin usuarios</td></tr>
              ) : users.map(u => (
                <tr key={u.idUsuario}>
                  <td className="cell-id">{u.idUsuario}</td>
                  <td>{u.nombres} {u.apellidos}</td>
                  <td className="cell-mono">{u.rut}</td>
                  <td className="cell-email">{u.correo}</td>
                  <td><span className={`role-badge ${getRoleClass(u.rol)}`}>{getRoleLabel(u.rol)}</span></td>
                  <td>
                    <span className={`status-dot ${u.estadoActivo !== false ? "active" : "inactive"}`}>
                      {u.estadoActivo !== false ? "Activo" : "Inactivo"}
                    </span>
                  </td>
                  <td className="cell-actions">
                    <button className="btn-sm btn-edit" onClick={() => handleEdit(u)}>Editar</button>
                    <button className="btn-sm btn-delete" onClick={() => { setDeleteConfirmId(u.idUsuario); setTimeout(() => confirmRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50); }}>Eliminar</button>
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

export default ManageUsers;
