import React, { useMemo } from 'react';
import { Card, Metric, Text, Flex, ProgressBar, Grid, BadgeDelta, Tracker } from '@tremor/react';

const DashboardKPIs = ({ routes = [], trucks = [], alerts = [] }) => {
  const activeRoutes = useMemo(() => routes.filter(r => r.estado === 'En Transito'), [routes]);
  const activePercent = routes.length > 0 ? (activeRoutes.length / routes.length) * 100 : 0;

  const pendingRoutes = useMemo(() => routes.filter(r => r.estado === 'Pendiente'), [routes]);

  const totalTrucks = trucks.length > 0 ? trucks.length : 15; 
  const availableTrucks = totalTrucks - activeRoutes.length;
  const fleetPercent = (activeRoutes.length / totalTrucks) * 100;

  const trackerData = useMemo(() => {
    if (!alerts || alerts.length === 0) {
      return Array(10).fill({ color: "emerald", tooltip: "Sin alertas recientes" });
    }
    return alerts.slice(0, 10).map(alert => ({
      color: alert.severidad === 'ALTA' ? 'rose' : alert.severidad === 'MEDIA' ? 'amber' : 'yellow',
      tooltip: alert.mensaje || "Alerta detectada"
    }));
  }, [alerts]);

  return (
    <Grid numItemsSm={2} numItemsLg={4} className="gap-6 mb-8 mt-4">
      <Card decoration="top" decorationColor="blue">
        <Text>Rutas Activas (En Tránsito)</Text>
        <Metric>{activeRoutes.length}</Metric>
        <Flex className="mt-4">
          <Text>{activePercent.toFixed(1)}% de todas las rutas</Text>
        </Flex>
        <ProgressBar value={activePercent} color="blue" className="mt-2" />
      </Card>

      <Card decoration="top" decorationColor="amber">
        <Text>Entregas Sin Iniciar</Text>
        <Flex className="items-baseline gap-2">
          <Metric>{pendingRoutes.length}</Metric>
          {pendingRoutes.length > 5 && <BadgeDelta deltaType="increase" isAllowedRatio={false}>Alta Carga</BadgeDelta>}
        </Flex>
        <Text className="mt-4 text-sm text-gray-500">Listas para despacho</Text>
      </Card>

      <Card decoration="top" decorationColor="emerald">
        <Text>Capacidad de Flota (Uso)</Text>
        <Metric>{activeRoutes.length} / {totalTrucks}</Metric>
        <Flex className="mt-4">
          <Text>{availableTrucks} camiones disponibles</Text>
        </Flex>
        <ProgressBar value={fleetPercent} color={fleetPercent > 80 ? "rose" : "emerald"} className="mt-2" />
      </Card>

      <Card decoration="top" decorationColor={alerts && alerts.length > 0 ? "rose" : "emerald"}>
        <Text>Alertas de Telemetría</Text>
        <Metric>{alerts ? alerts.length : 0}</Metric>
        <Text className="mt-4 mb-2">Últimos eventos:</Text>
        <Tracker data={trackerData} className="mt-2" />
      </Card>
    </Grid>
  );
};

export default DashboardKPIs;