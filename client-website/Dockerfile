# Usa una imagen ligera de Nginx
FROM nginx:alpine

# Opcional: Elimina el contenido por defecto de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia tu sitio web estático a la carpeta pública de Nginx
COPY ./ /usr/share/nginx/html/

# Exponer el puerto 80 dentro del contenedor
EXPOSE 80

# El comando por defecto de Nginx arranca el servidor
CMD ["nginx", "-g", "daemon off;"]