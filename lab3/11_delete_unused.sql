-- Удаляет все неиспользуемые станции
DELETE FROM stations
WHERE id NOT IN (
        SELECT (departure_station_id)
        FROM route_sections
        UNION
        SELECT (destination_station_id)
        FROM route_sections
        UNION
        SELECT (departure_station_id)
        FROM tickets
        UNION
        SELECT (destination_station_id)
        FROM tickets
        UNION
        SELECT (first_station_id)
        FROM routes
        UNION
        SELECT (last_station_id)
        FROM routes
    );