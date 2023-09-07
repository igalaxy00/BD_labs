-- Выводит номер поезда и название маршрута
SELECT trains.id as train_id,
    routes.name as route_name
    FROM trains
    INNER JOIN routes ON trains.route_id = routes.id;