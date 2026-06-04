import React, { useMemo } from 'react';
import { Card, Title, DonutChart, BarChart } from '@tremor/react';

const DashboardCharts = ({ routes = [], trucks = [], users = [] }) => {

  // Chart 1: Donut — Route Status Distribution
  const routeStatusData = useMemo(() => {
    const counts = routes.reduce((acc, r) => {
      const status = r.estado || "Desconocido";
      acc[status] = (acc[status] || 0) + 1;
      return acc;
    }, {});
    return Object.keys(counts).map(key => ({ name: key, cantidad: counts[key] }));
  }, [routes]);

  // Chart 2: Bar — Routes grouped by distance range
  const distanceData = useMemo(() => {
    const buckets = { "0–200 km": 0, "200–500 km": 0, "500–1000 km": 0, "1000+ km": 0 };
    routes.forEach(r => {
      const km = r.distanciaEstimadaKm || 0;
      if (km <= 200) buckets["0–200 km"]++;
      else if (km <= 500) buckets["200–500 km"]++;
      else if (km <= 1000) buckets["500–1000 km"]++;
      else buckets["1000+ km"]++;
    });
    return Object.entries(buckets).map(([name, count]) => ({ name, "Rutas": count }));
  }, [routes]);

  // Chart 3: Donut — Truck Operational Status
  const truckStatusData = useMemo(() => {
    const counts = trucks.reduce((acc, t) => {
      const status = t.estadoOperativo || "Desconocido";
      acc[status] = (acc[status] || 0) + 1;
      return acc;
    }, {});
    return Object.keys(counts).map(key => ({ name: key, cantidad: counts[key] }));
  }, [trucks]);

  // Chart 4: Bar — Users by Role
  const usersByRole = useMemo(() => {
    const counts = {};
    users.forEach(u => {
      const role = u.rol?.nombreRol?.replace("ROLE_", "") || "Sin Rol";
      counts[role] = (counts[role] || 0) + 1;
    });
    return Object.entries(counts)
      .map(([name, count]) => ({ name, "Usuarios": count }))
      .sort((a, b) => b["Usuarios"] - a["Usuarios"]);
  }, [users]);

  return (
    <div className="charts-grid">
      <Card>
        <Title>Distribución de Rutas</Title>
        <DonutChart
          className="mt-4 h-52"
          data={routeStatusData}
          category="cantidad"
          index="name"
          colors={["blue", "emerald", "amber", "gray"]}
        />
      </Card>

      <Card>
        <Title>Rutas por Distancia</Title>
        <BarChart
          className="mt-4 h-52"
          data={distanceData}
          index="name"
          categories={["Rutas"]}
          colors={["cyan"]}
          yAxisWidth={32}
        />
      </Card>

      <Card>
        <Title>Estado de Flota</Title>
        <DonutChart
          className="mt-4 h-52"
          data={truckStatusData}
          category="cantidad"
          index="name"
          colors={["rose", "emerald", "amber"]}
        />
      </Card>

      <Card>
        <Title>Usuarios por Rol</Title>
        <BarChart
          className="mt-4 h-52"
          data={usersByRole}
          index="name"
          categories={["Usuarios"]}
          colors={["violet"]}
          yAxisWidth={32}
        />
      </Card>
    </div>
  );
};

export default DashboardCharts;