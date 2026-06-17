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

    // ===================== NAVBAR SCROLL EFFECT =====================
    window.addEventListener('scroll', () => {
        const navbar = document.getElementById('navbar-main');
        if (navbar) {
            navbar.classList.toggle('scrolled', window.scrollY > 50);
        }
    });
});