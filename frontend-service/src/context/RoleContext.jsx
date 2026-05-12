import React, { createContext, useState, useContext } from 'react';

const RoleContext = createContext();

export const ROLES = {
  ADMIN: 'ROLE_ADMINISTRADOR',
  DESPACHADOR: 'ROLE_DESPACHADOR',
  CONDUCTOR: 'ROLE_CONDUCTOR'
};

export const RoleProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("user");
    return saved ? JSON.parse(saved) : null;
  });

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
  };

  const role = user ? user.rol : null;

  return (
    <RoleContext.Provider value={{ role, user, login, logout }}>
      {children}
    </RoleContext.Provider>
  );
}
export const useRole = () => useContext(RoleContext);
