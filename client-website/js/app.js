document.addEventListener('DOMContentLoaded', () => {
    const bffUrl = 'http://localhost:8082';
    const REFRESH_INTERVAL = 5000; // 5 seconds

    // ===================== FLEET TRACKING =====================
    function loadFleetData() {
        fetch(bffUrl + '/api/dashboard')
            .then(response => {
                if (!response.ok) throw new Error("Error en la red");
                return response.json();
            })
            .then(data => {
                const container = document.getElementById('fleet-container');
                if (!container) return;

                const routes = data.routes || [];
                const rutasEnTransito = routes.filter(r => r.estado === 'En Transito');

                if (rutasEnTransito.length === 0) {
                    container.innerHTML = '<div class="col-12 text-center"><p class="text-light opacity-75"><i class="bi bi-inbox"></i> No hay camiones en ruta actualmente.</p></div>';
                    return;
                }

                let html = '';
                rutasEnTransito.forEach(ruta => {
                    const progress = ruta.progressPercent || 0;
                    // Handle truck data - could be nested object or null
                    const truckPatente = (ruta.truck && ruta.truck.patente) ? ruta.truck.patente : 'Sin Asignar';
                    const truckModelo = (ruta.truck && ruta.truck.marcaModelo) ? ruta.truck.marcaModelo : '';
                    const origen = ruta.origenDireccion || 'No especificado';
                    const destino = ruta.destinoDireccion || 'No especificado';
                    const velocidad = ruta.velocidadSimulada || 0;

                    html += '<div class="col-md-6 col-lg-4">'
                        + '<div class="card fleet-card h-100 p-3">'
                        + '<div class="d-flex justify-content-between align-items-center mb-2">'
                        + '<h5 class="fw-bold mb-0"><i class="bi bi-truck"></i> ' + truckPatente + ' <small class="opacity-75">' + truckModelo + '</small></h5>'
                        + '<span class="badge bg-success">En Ruta</span>'
                        + '</div>'
                        + '<p class="small opacity-75 mb-3">'
                        + '<strong>Origen:</strong> ' + origen + '<br>'
                        + '<strong>Destino:</strong> ' + destino
                        + '</p>'
                        + '<div>'
                        + '<div class="progress" style="height: 8px; background: rgba(255,255,255,0.1);">'
                        + '<div class="progress-bar bg-success progress-bar-striped progress-bar-animated" role="progressbar" style="width: ' + progress + '%;" aria-valuenow="' + progress + '" aria-valuemin="0" aria-valuemax="100"></div>'
                        + '</div>'
                        + '<div class="d-flex justify-content-between mt-1">'
                        + '<small class="opacity-75">' + velocidad + ' km/h</small>'
                        + '<small class="fw-bold text-success">' + progress + '% completado</small>'
                        + '</div>'
                        + '</div>'
                        + '</div>'
                        + '</div>';
                });
                container.innerHTML = html;
            })
            .catch(error => {
                console.error('Error fetching fleet data:', error);
                const container = document.getElementById('fleet-container');
                if (container) {
                    container.innerHTML = '<div class="col-12 text-center text-danger">'
                        + '<i class="bi bi-exclamation-triangle"></i> Error al conectar con el servidor. Verifica que bff-service esté corriendo.'
                        + '</div>';
                }
            });
    }

    // Initial load + auto-refresh
    loadFleetData();
    setInterval(loadFleetData, REFRESH_INTERVAL);

    // ===================== CONTACT FORM =====================
    const contactForm = document.getElementById('contactForm');
    const alertBox = document.getElementById('contact-alert');
    const btnSubmit = document.getElementById('btn-submit');

    if (contactForm) {
        contactForm.addEventListener('submit', (e) => {
            e.preventDefault();

            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Enviando...';

            const payload = {
                nombre: document.getElementById('nombre').value,
                email: document.getElementById('email').value,
                mensaje: document.getElementById('mensaje').value
            };

            fetch(bffUrl + '/api/dashboard/proxy/contact', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(response => {
                if (response.ok) {
                    alertBox.className = 'alert alert-success';
                    alertBox.innerHTML = '<i class="bi bi-check-circle"></i> ¡Petición enviada! Te contactaremos pronto.';
                    contactForm.reset();
                } else {
                    throw new Error("Fallo en backend");
                }
            })
            .catch(err => {
                alertBox.className = 'alert alert-danger';
                alertBox.innerHTML = '<i class="bi bi-exclamation-triangle"></i> Hubo un error al enviar tu solicitud. Intenta más tarde.';
            })
            .finally(() => {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Enviar Petición';
                alertBox.classList.remove('d-none');
            });
        });
    }

    // ===================== LIVE MAP =====================
    const map = L.map('live-map').setView([-33.4569, -70.6482], 7); // Santiago, zoom out to see routes

    // Dark-themed tile layer
    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; <a href="https://carto.com/">CARTO</a>',
        maxZoom: 19
    }).addTo(map);

    // Custom green truck icon
    const truckIcon = L.divIcon({
        html: '<i class="bi bi-truck" style="font-size:24px;color:#198754;text-shadow:0 0 6px rgba(25,135,84,0.6);"></i>',
        className: 'truck-marker',
        iconSize: [30, 30],
        iconAnchor: [15, 15]
    });

    let truckMarkers = {};

    function updateMapMarkers() {
        fetch(bffUrl + '/api/dashboard')
            .then(r => r.json())
            .then(data => {
                const routes = (data.routes || []).filter(r => r.estado === 'En Transito');

                // Remove markers for routes no longer in transit
                Object.keys(truckMarkers).forEach(id => {
                    if (!routes.find(r => r.idRuta == id)) {
                        map.removeLayer(truckMarkers[id]);
                        delete truckMarkers[id];
                    }
                });

                routes.forEach(ruta => {
                    // Use lat/lng from the route's destination, offset by progress
                    // Since we don't have real GPS from telemetry yet, simulate a position
                    const progress = (ruta.progressPercent || 0) / 100;
                    const startLat = -33.4569; // Santiago
                    const startLng = -70.6482;
                    const endLat = ruta.latDestino || startLat;
                    const endLng = ruta.lngDestino || startLng;
                    const curLat = startLat + (endLat - startLat) * progress;
                    const curLng = startLng + (endLng - startLng) * progress;

                    const patente = (ruta.truck && ruta.truck.patente) ? ruta.truck.patente : 'N/A';
                    const destino = ruta.destinoDireccion || 'Desconocido';

                    if (truckMarkers[ruta.idRuta]) {
                        truckMarkers[ruta.idRuta].setLatLng([curLat, curLng]);
                    } else {
                        const marker = L.marker([curLat, curLng], { icon: truckIcon })
                            .addTo(map)
                            .bindPopup('<b>' + patente + '</b><br>Destino: ' + destino + '<br>Progreso: ' + (ruta.progressPercent || 0) + '%');
                        truckMarkers[ruta.idRuta] = marker;
                    }
                });
            })
            .catch(err => console.error('Error updating map:', err));
    }

    updateMapMarkers();
    setInterval(updateMapMarkers, REFRESH_INTERVAL);

    // ===================== NAVBAR SCROLL EFFECT =====================
    window.addEventListener('scroll', () => {
        const navbar = document.getElementById('navbar-main');
        if (navbar) {
            navbar.classList.toggle('scrolled', window.scrollY > 50);
        }
    });
});