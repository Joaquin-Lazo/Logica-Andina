import React, { useMemo } from 'react';
import { Card, Title, DonutChart, BarChart, Grid } from '@tremor/react';

const DashboardCharts = ({ routes = [], alerts = [] }) => {
  // Chart 1: Donut Chart data for Route Status
  const donutData = useMemo(() => {
    const counts = routes.reduce((acc, route) => {
      acc[route.estado] = (acc[route.estado] || 0) + 1;
      return acc;
    }, {});
    return Object.keys(counts).map(key => ({ name: key, cantidad: counts[key] }));
  }, [routes]);

  // Chart 2: Bar Chart data for Alerts
  const barData = useMemo(() => {
    const counts = alerts.reduce((acc, alert) => {
      acc[alert.severidad] = (acc[alert.severidad] || 0) + 1;
      return acc;
    }, {});
    // Force specific order so it looks good on the bar chart
    return [
      { name: "ALTA", "Total": counts["ALTA"] || 0 },
      { name: "MEDIA", "Total": counts["MEDIA"] || 0 },
      { name: "BAJA", "Total": counts["BAJA"] || 0 }
    ];
  }, [alerts]);

  return (
    <Grid numItemsSm={1} numItemsLg={2} className="gap-6 mb-8">
      <Card>
        <Title>Distribución de Rutas</Title>
        <DonutChart
          className="mt-6 h-64"
          data={donutData}
          category="cantidad"
          index="name"
          colors={["blue", "amber", "emerald", "gray"]}
        />
      </Card>
      
      <Card>
        <Title>Alertas por Severidad</Title>
        <BarChart
          className="mt-6 h-64"
          data={barData}
          index="name"
          categories={["Total"]}
          colors={["rose"]}
          yAxisWidth={48}
        />
      </Card>
    </Grid>
  );
};

export default DashboardCharts;