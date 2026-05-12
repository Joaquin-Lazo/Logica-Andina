import React from "react";
import { BrowserRouter as Router, Routes, Route, NavLink } from "react-router-dom";
import { RoleProvider, useRole, ROLES } from "./context/RoleContext";
import Dashboard from "./components/Dashboard";
import ManageRoutes from "./components/ManageRoutes";
import ManageTrucks from "./components/ManageTrucks";
import ManageUsers from "./components/ManageUsers";
import ManageClients from "./components/ManageClients";
import ManageInvoices from "./components/ManageInvoices";
import ManageCargo from "./components/ManageCargo";
import Telemetry from "./components/Telemetry";
import Login from "./components/Login";
import "./App.css";

const UserBar = () => {
  const { user, logout } = useRole();
  return (
    <div className="role-selector" style={{ padding: '6px 12px', background: 'rgba(0,0,0,0.2)' }}>
      <span style={{ fontSize: '0.9rem', fontWeight: 500, marginRight: '10px' }}>
        Hola, {user.nombres}
      </span>
      <button
        onClick={logout}
        className="btn-sm btn-delete"
        style={{ padding: '6px 12px', letterSpacing: '0.5px' }}
      >
        Cerrar Sesión
      </button>
    </div>
  );
};


const AppNav = () => {
  const { role } = useRole();
  return (
    <nav className="main-nav">
      <NavLink to="/" end className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
        Panel General
      </NavLink>
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/rutas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Rutas
        </NavLink>
      )}
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/camiones" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Flota
        </NavLink>
      )}
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/clientes" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Clientes
        </NavLink>
      )}
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/facturas" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Facturas
        </NavLink>
      )}
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/carga" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Carga
        </NavLink>
      )}
      {(role === ROLES.ADMIN || role === ROLES.DESPACHADOR) && (
        <NavLink to="/telemetria" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Telemetria
        </NavLink>
      )}
      {role === ROLES.ADMIN && (
        <NavLink to="/usuarios" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
          Usuarios
        </NavLink>
      )}
    </nav>
  );
};

const AppContent = () => {
  const { user } = useRole();

  if (!user) {
    return <Login />
  }

  return (
    <Router>
      <div className="App">
        <header className="App-header">
          <div className="header-top">
            <div>
              <h1>Logistica Andina - Panel de Control</h1>
              <p>Centro de integracion de microservicios</p>
            </div>
            <UserBar />
          </div>
        </header>
        <AppNav />
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/rutas" element={<ManageRoutes />} />
          <Route path="/camiones" element={<ManageTrucks />} />
          <Route path="/clientes" element={<ManageClients />} />
          <Route path="/facturas" element={<ManageInvoices />} />
          <Route path="/carga" element={<ManageCargo />} />
          <Route path="/telemetria" element={<Telemetry />} />
          <Route path="/usuarios" element={<ManageUsers />} />
        </Routes>
      </div>
    </Router>
  );
};

const App = () => {
  return (
    <RoleProvider>
      <AppContent />
    </RoleProvider>
  );
};

export default App;