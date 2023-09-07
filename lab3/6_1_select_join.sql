-- Выводит станции прибытия для маршрута
SELECT routes.name AS route,
    stations.name AS destination
FROM routes
    INNER JOIN route_sections ON routes.id = route_sections.route_id
    INNER JOIN stations ON route_sections.destination_station_id = stations.id
ORDER BY (routes.name, route_sections.departure_time);