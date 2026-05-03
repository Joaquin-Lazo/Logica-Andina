import React, { createContext, useState, useContext } from 'react';

const RoleContext = createContext();

export const ROLES = {
  ADMIN: 'ROLE_ADMINISTRADOR',
  DESPACHADOR: 'ROLE_DESPACHADOR',
  CONDUCTOR: 'ROLE_CONDUCTOR'
};

export const RoleProvider = ({ children }) => {
  const [role, setRole] = useState(ROLES.ADMIN);
  return (
    <RoleContext.Provider value={{ role, setRole }}>
      {children}
    </RoleContext.Provider>
  );
};

export const useRole = () => useContext(RoleContext);
