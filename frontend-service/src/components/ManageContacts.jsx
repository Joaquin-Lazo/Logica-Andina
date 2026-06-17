import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082/api/dashboard";

const ManageContacts = () => {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchContacts = useCallback(() => {
    fetch(`${BFF}/proxy/contacts`)
      .then(r => r.json())
      .then(data => {
        setContacts(Array.isArray(data) ? data : []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  useEffect(() => { fetchContacts(); }, [fetchContacts]);

  return (
    <main className="dashboard-container">
      <div className="page-header">
        <h2>Peticiones de Contacto</h2>
      </div>

      {loading ? (
        <div className="status-message loading">Cargando contactos...</div>
      ) : (
        <section className="data-section full-width">
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Mensaje</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                {contacts.length === 0 ? (
                  <tr><td colSpan="5" className="empty-cell">Sin peticiones de contacto</td></tr>
                ) : (
                  contacts.map(c => (
                    <tr key={c.id}>
                      <td className="cell-id">{c.id}</td>
                      <td>{c.nombre}</td>
                      <td className="cell-email">{c.email}</td>
                      <td>{c.mensaje}</td>
                      <td className="cell-date">
                        {c.fechaCreacion ? new Date(c.fechaCreacion).toLocaleString("es-CL") : "—"}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </main>
  );
};

export default ManageContacts;
