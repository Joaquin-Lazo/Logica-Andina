import React from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import Dashboard from "./components/Dashboard";
import CreateRoute from "./components/CreateRoute";
import "./App.css";

const App = () => {
  return (
    <Router>
      <div className="App">
        <header className="App-header">
          <h1>Logística Andina - Panel de Control</h1>
          <p>Centro de integración de microservicios</p>
        </header>

        <nav style={{ margin: '0 auto 30px auto', padding: '15px', backgroundColor: '#333', borderRadius: '8px', maxWidth: '800px', display: 'flex', justifyContent: 'center', gap: '20px' }}>
          <Link to="/" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold' }}>
            Dashboard Overview
          </Link>
          <Link to="/create-route" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold' }}>
            + Register New Route
          </Link>
        </nav>

        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/create-route" element={<CreateRoute />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;