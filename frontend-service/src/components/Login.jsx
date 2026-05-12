import { useState } from 'react';
import { useRole } from '../context/RoleContext';

const Login = () => {

  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("")
  const { login } = useRole();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await fetch("http://localhost:8082/api/dashboard/proxy/user-login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ correo: correo, password: password })
      });

      if (!response.ok) {
        setError("Correo o contraseña incorrectos");
        return;
      }

      const data = await response.json();
      login(data);
    } catch (err) {
      setError("No se pudo conectar con el servidor");
    }
  };

  return (
    <div className="dashboard-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
      <div className="form-card" style={{ width: '100%', maxWidth: '400px' }}>
        <h3 style={{ textAlign: 'center', fontSize: '1.5rem', marginBottom: '24px' }}>Logística Andina</h3>

        {error && <div className="status-message error" style={{ padding: '10px', marginTop: '0', marginBottom: '15px' }}>{error}</div>}

        <form onSubmit={handleSubmit} className="crud-form">
          <div className="field-group">
            <label style={{ fontWeight: 600, fontSize: '0.9rem', color: '#555', marginBottom: '4px' }}>Correo Electrónico:</label>
            <input
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              placeholder="ejemplo@transandina.cl"
              required
            />
          </div>
          <div className="field-group" style={{ marginBottom: '10px' }}>
            <label style={{ fontWeight: 600, fontSize: '0.9rem', color: '#555', marginBottom: '4px' }}>Contraseña:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          <button type="submit" className="btn-primary" style={{ width: '100%', padding: '12px' }}>
            Iniciar Sesión
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;