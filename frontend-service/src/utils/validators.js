export const validateRut = (rut) => {
  const clean = rut.replace(/[.\s]/g, '').toUpperCase();
  if (!/^\d{7,8}-[\dK]$/.test(clean)) return 'Formato invalido (ej: 12345678-9)';
  return null;
};

export const validatePatente = (p) => {
  const clean = p.toUpperCase().trim();
  if (!/^[A-Z]{2}-[A-Z]{2}-\d{2}$/.test(clean) && !/^[A-Z]{4}-\d{2}$/.test(clean))
    return 'Formato invalido (ej: AB-CD-12)';
  return null;
};

export const validateEmail = (e) => {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) return 'Correo invalido';
  return null;
};

export const validateCoord = (lat, lng) => {
  const la = parseFloat(lat), lo = parseFloat(lng);
  if (isNaN(la) || la < -90 || la > 90) return 'Latitud debe estar entre -90 y 90';
  if (isNaN(lo) || lo < -180 || lo > 180) return 'Longitud debe estar entre -180 y 180';
  return null;
};

export const validateNotEmpty = (val, field) => {
  if (!val || !val.toString().trim()) return `${field} es requerido`;
  return null;
};

export const validatePositiveNumber = (val, field) => {
  const n = parseFloat(val);
  if (isNaN(n) || n <= 0) return `${field} debe ser un numero positivo`;
  return null;
};
