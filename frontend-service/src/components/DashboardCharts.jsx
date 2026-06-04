import React, { useMemo } from 'react';
import { Card, Title, DonutChart, BarChart } from '@tremor/react';

const DashboardCharts = ({ routes = [] }) => {
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
    </div>
  );
};

export default DashboardCharts;