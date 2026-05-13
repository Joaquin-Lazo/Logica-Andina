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
    <div className="dashboard-container login-wrapper">
      <div className="form-card login-card">
        <h3>Logística Andina</h3>
        {error && <div className="status-message error">{error}</div>}
        <form onSubmit={handleSubmit} className="crud-form">
          <div className="field-group">
            <label>Correo Electrónico:</label>
            <input
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              placeholder="ejemplo@transandina.cl"
              required
            />
          </div>
          <div className="field-group">
            <label>Contraseña:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          <button type="submit" className="btn-primary">
            Iniciar Sesión
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;