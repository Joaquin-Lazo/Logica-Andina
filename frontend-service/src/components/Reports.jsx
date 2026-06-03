import React, { useState, useEffect, useCallback } from "react";

const BFF = "http://localhost:8082";

const Reports = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchReport = useCallback(() => {
        setLoading(true);
        fetch(`${BFF}/api/reports/summary`)
            .then(r => { if (!r.ok) throw new Error("Error"); return r.json(); })
            .then(d => { setData(d); setLoading(false); setError(null); })
            .catch(() => { setError("Error cargando datos"); setLoading(false); });
    }, []);

    useEffect(() => { fetchReport(); }, [fetchReport]);

    const downloadReport = async (format) => {
        try {
            const res = await fetch(`${BFF}/api/reports/${format}`);
            if (!res.ok) throw new Error("Error generando reporte");
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `reporte_logistica_andina.${format === 'excel' ? 'xlsx' : 'pdf'}`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            alert(err.message);
        }
    };

    const formatMoney = (n) => n != null ? `$${Number(n).toLocaleString("es-CL")}` : "$0";

    if (loading) return <main className="dashboard-container"><div className="status-message loading">Cargando reportes...</div></main>;
    if (error) return <main className="dashboard-container"><div className="status-message error">{error}</div></main>;

    const op = data.operational || {};
    const fin = data.financial || {};
    const clientRevenue = fin.ingresoPorCliente || {};

    return (
        <main className="dashboard-container">
            {/* HEADER + EXPORT BUTTONS */}
            <div className="page-header">
                <h2>Reportes</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className="btn-primary" onClick={() => downloadReport('excel')}>
                        Descargar Excel
                    </button>
                    <button className="btn-primary" onClick={() => downloadReport('pdf')}>
                        Descargar PDF
                    </button>
                    <button className="btn-sm btn-edit" onClick={fetchReport}>Refrescar</button>
                </div>
            </div>

            {/* ====== TOP-LEVEL KPI CARDS ====== */}
            <section className="report-kpi-grid">
                <div className="report-kpi-card kpi-blue">
                    <span className="kpi-label">Total Rutas</span>
                    <span className="kpi-value">{op.totalRutas || 0}</span>
                </div>
                <div className="report-kpi-card kpi-green">
                    <span className="kpi-label">Ingreso Total</span>
                    <span className="kpi-value">{formatMoney(fin.ingresoTotal)}</span>
                </div>
                <div className="report-kpi-card kpi-amber">
                    <span className="kpi-label">Por Cobrar</span>
                    <span className="kpi-value">{formatMoney(fin.montoPendienteCobro)}</span>
                    <span className="kpi-sub">{fin.facturasPorCobrar || 0} facturas</span>
                </div>
                <div className="report-kpi-card kpi-purple">
                    <span className="kpi-label">Flota</span>
                    <span className="kpi-value">{op.totalCamiones || 0}</span>
                    <span className="kpi-sub">{op.camionesActivos || 0} activos</span>
                </div>
            </section>

            {/* ====== TWO-COLUMN LAYOUT ====== */}
            <div className="report-columns">

                {/* --- LEFT: OPERATIONAL --- */}
                <section className="report-panel">
                    <h3 className="report-panel-title">Reporte Operacional</h3>

                    <div className="report-stat-grid">
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#fbbf24' }}>{op.pendientes || 0}</span>
                            <span className="stat-label">Pendientes</span>
                        </div>
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#38bdf8' }}>{op.enTransito || 0}</span>
                            <span className="stat-label">En Transito</span>
                        </div>
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#4ade80' }}>{op.completadas || 0}</span>
                            <span className="stat-label">Completadas</span>
                        </div>
                    </div>

                    <div className="report-detail-list">
                        <div className="report-detail-row">
                            <span>Km Totales Estimados</span>
                            <span className="detail-value">{op.totalKmEstimados?.toLocaleString("es-CL")} km</span>
                        </div>
                        <div className="report-detail-row">
                            <span>Toneladas Transportadas</span>
                            <span className="detail-value">{op.totalToneladasTransportadas?.toLocaleString("es-CL")} ton</span>
                        </div>
                        <div className="report-detail-row">
                            <span>Total Cargamentos</span>
                            <span className="detail-value">{op.totalCargamentos || 0}</span>
                        </div>
                        <div className="report-detail-row">
                            <span>Camiones Activos / Total</span>
                            <span className="detail-value">{op.camionesActivos || 0} / {op.totalCamiones || 0}</span>
                        </div>
                    </div>
                </section>

                {/* --- RIGHT: FINANCIAL --- */}
                <section className="report-panel">
                    <h3 className="report-panel-title">Reporte Financiero</h3>

                    <div className="report-stat-grid">
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#4ade80' }}>{formatMoney(fin.ingresoNeto)}</span>
                            <span className="stat-label">Ingreso Neto</span>
                        </div>
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#f87171' }}>{formatMoney(fin.totalImpuestos)}</span>
                            <span className="stat-label">IVA Total</span>
                        </div>
                        <div className="report-stat">
                            <span className="stat-number" style={{ color: '#38bdf8' }}>{formatMoney(fin.ingresoTotal)}</span>
                            <span className="stat-label">Ingreso Bruto</span>
                        </div>
                    </div>

                    <div className="report-detail-list">
                        <div className="report-detail-row">
                            <span>Facturas Pagadas</span>
                            <span className="detail-value" style={{ color: '#4ade80' }}>{fin.facturasPagadas || 0}</span>
                        </div>
                        <div className="report-detail-row">
                            <span>Facturas Pendientes</span>
                            <span className="detail-value" style={{ color: '#fbbf24' }}>{fin.facturasPorCobrar || 0}</span>
                        </div>
                        <div className="report-detail-row" style={{ borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '12px', marginTop: '8px' }}>
                            <span><strong>Monto Por Cobrar</strong></span>
                            <span className="detail-value" style={{ color: '#f87171', fontWeight: 'bold' }}>{formatMoney(fin.montoPendienteCobro)}</span>
                        </div>
                    </div>

                    {/* Revenue by Client */}
                    {Object.keys(clientRevenue).length > 0 && (
                        <>
                            <h4 style={{ margin: '20px 0 10px', color: 'rgba(255,255,255,0.7)', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '1px' }}>
                                Ingreso por Cliente
                            </h4>
                            <div className="report-detail-list">
                                {Object.entries(clientRevenue)
                                    .sort(([, a], [, b]) => b - a)
                                    .map(([client, amount]) => (
                                        <div className="report-detail-row" key={client}>
                                            <span>{client}</span>
                                            <span className="detail-value">{formatMoney(amount)}</span>
                                        </div>
                                    ))}
                            </div>
                        </>
                    )}
                </section>
            </div>
        </main>
    );
};

export default Reports;