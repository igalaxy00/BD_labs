-- Выбирает маршруты, где первая станция находится в северном полушарии
SELECT *
FROM routes
WHERE routes.first_station_id IN (
        SELECT stations.id
        FROM stations
        WHERE latitude > 0
    )