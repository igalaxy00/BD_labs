-- Используя ХП для вычисления расстояний между станциями, сохраняет в представление топ-5 самых длинных маршрутов.
CREATE OR REPLACE VIEW "5 longest routes" AS
SELECT r.name as route_name,
    sum(dist) as total_distance
FROM (
        SELECT sections.name,
            sections.route_id,
            station_distance(
                sections.departure_station_id,
                sections.destination_station_id
            ) as dist
        FROM (
                routes
                INNER JOIN route_sections ON (
                    routes.id = route_sections.route_id
                )
            ) as sections
    ) as r
GROUP BY r.name
ORDER BY total_distance DESC
LIMIT 5;