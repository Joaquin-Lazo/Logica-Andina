import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const CreateRoute = () => {
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState({
        idConductorRef: '',
        idDespachadorRef: '',
        idCamion: '',
        origenDireccion: '',
        destinoDireccion: '',
        latDestino: '',
        lngDestino: '',
        distanciaEstimadaKm: ''
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const payload = {
            idConductorRef: parseInt(formData.idConductorRef),
            idDespachadorRef: parseInt(formData.idDespachadorRef),
            truck: { idCamion: parseInt(formData.idCamion) },
            origenDireccion: formData.origenDireccion,
            destinoDireccion: formData.destinoDireccion,
            latDestino: parseFloat(formData.latDestino),
            lngDestino: parseFloat(formData.lngDestino),
            distanciaEstimadaKm: parseFloat(formData.distanciaEstimadaKm),
            estado: "Pendiente"
        };

        try {
            const response = await fetch("http://localhost:8082/api/dashboard/proxy/routes", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                navigate('/');
            } else {
                console.error("BFF Error:", response.status);
            }
        } catch (error) {
            console.error("Network error:", error);
        }
    };

    return (
        <div className="dashboard-container" style={{ padding: '20px', maxWidth: '600px', margin: '0 auto' }}>
            <h2 style={{ color: 'white', marginBottom: '20px' }}>Registrar Nueva Ruta</h2>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                
                <div style={{ display: 'flex', gap: '10px' }}>
                    <input type="text" name="origenDireccion" placeholder="Origen" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                    <input type="text" name="destinoDireccion" placeholder="Destino" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                    <input type="number" step="any" name="latDestino" placeholder="Latitud" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                    <input type="number" step="any" name="lngDestino" placeholder="Longitud" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                </div>

                <div style={{ display: 'flex', gap: '10px' }}>
                    <input type="number" name="idConductorRef" placeholder="ID Conductor" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                    <input type="number" name="idDespachadorRef" placeholder="ID Despachador" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                    <input type="number" name="idCamion" placeholder="ID Camión" onChange={handleInputChange} required style={{ flex: 1, padding: '10px' }}/>
                </div>

                <input type="number" step="any" name="distanciaEstimadaKm" placeholder="Distancia Estimada (Km)" onChange={handleInputChange} required style={{ padding: '10px' }}/>

                <button type="submit" style={{ padding: '12px', backgroundColor: '#0056b3', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                    Crear Ruta
                </button>
            </form>
        </div>
    );
};

export default CreateRoute;