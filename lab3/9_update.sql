-- Увеличивает стоимость всех участков второго маршрута на 1000
UPDATE route_sections
SET cost = cost + 1000
WHERE route_id = 2;