import React, { useMemo } from 'react';
import { Card, Metric, Text, Flex, ProgressBar, Grid, BadgeDelta } from '@tremor/react';

const DashboardKPIs = ({ routes = [], trucks = []}) => {
  const activeRoutes = useMemo(() => routes.filter(r => r.estado === 'En Transito'), [routes]);
  const activePercent = routes.length > 0 ? (activeRoutes.length / routes.length) * 100 : 0;
  const completedRoutes = useMemo(() => routes.filter(r => r.estado === 'Completada'), [routes]);
  const completedPercent = routes.length > 0 ? (completedRoutes.length / routes.length) * 100 : 0;

  const pendingRoutes = useMemo(() => routes.filter(r => r.estado === 'Pendiente'), [routes]);

  const totalTrucks = trucks.length > 0 ? trucks.length : 15; 
  const availableTrucks = totalTrucks - activeRoutes.length;
  const fleetPercent = (activeRoutes.length / totalTrucks) * 100;

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

      <Card decoration="top" decorationColor="cyan">
        <Text>Rutas Completadas</Text>
        <Metric>{completedRoutes.length}</Metric>
        <Flex className="mt-4">
          <Text>{completedPercent.toFixed(1)}% tasa de cumplimiento</Text>
        </Flex>
        <ProgressBar value={completedPercent} color="cyan" className="mt-2" />
      </Card>
    </Grid>
  );
};

export default DashboardKPIs;