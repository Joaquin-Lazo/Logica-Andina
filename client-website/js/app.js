document.addEventListener('DOMContentLoaded', () => {
    // 1. Cargar Camiones desde la API
    const bffUrl = 'http://localhost:8082'; 
    
    fetch(`${bffUrl}/api/dashboard`)
        .then(response => {
            if (!response.ok) throw new Error("Error en la red");
            return response.json();
        })
        .then(data => {
            const container = document.getElementById('fleet-container');
            container.innerHTML = ''; 

            const rutasEnTransito = data.routes?.filter(r => r.estado === 'En Transito') || [];

            if (rutasEnTransito.length === 0) {
                container.innerHTML = '<div class="col-12 text-center text-muted">No hay camiones en ruta actualmente.</div>';
                return;
            }

            rutasEnTransito.forEach(ruta => {
                const progress = ruta.progressPercent || 0;
                const card = document.createElement('div');
                card.className = 'col-md-6 col-lg-4';
                card.innerHTML = `
                    <div class="card h-100 border-success border-opacity-25 shadow-sm p-3">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h5 class="fw-bold mb-0 text-success"><i class="bi bi-truck"></i> Camión #${ruta.camionId || 'N/A'}</h5>
                            <span class="badge bg-success bg-opacity-10 text-success">En Ruta</span>
                        </div>
                        <p class="text-muted small mb-3">
                            <strong>Origen:</strong> ${ruta.origen} <br>
                            <strong>Destino:</strong> ${ruta.destino}
                        </p>
                        <div>
                            <div class="progress" style="height: 10px;">
                                <div class="progress-bar bg-success progress-bar-striped progress-bar-animated" role="progressbar" style="width: ${progress}%;" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                            <div class="d-flex justify-content-between mt-1">
                                <small class="text-muted">${ruta.velocidadSimulada || 0} km/h</small>
                                <small class="fw-bold text-success">${progress}% completado</small>
                            </div>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error fetching fleet data:', error);
            document.getElementById('fleet-container').innerHTML = `
                <div class="col-12 text-center text-danger">
                    <i class="bi bi-exclamation-triangle"></i> Error al conectar con el servidor. Verifica que bff-service esté corriendo.
                </div>
            `;
        });

    // 2. Manejar Formulario de Contacto
    const contactForm = document.getElementById('contactForm');
    const alertBox = document.getElementById('contact-alert');
    const btnSubmit = document.getElementById('btn-submit');

    contactForm.addEventListener('submit', (e) => {
        e.preventDefault();
        
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Enviando...';

        const payload = {
            nombre: document.getElementById('nombre').value,
            email: document.getElementById('email').value,
            mensaje: document.getElementById('mensaje').value
        };

        fetch(`${bffUrl}/api/dashboard/proxy/contact`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(response => {
            if(response.ok || response.status === 404) { 
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
});