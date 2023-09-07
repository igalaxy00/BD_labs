-- Написать хранимую процедуру/функцию, которая вычисляет расстояние между двумя станциями (идентификаторы станций должны быть параметрами ХП).
CREATE OR REPLACE FUNCTION station_distance(first_station_id int, second_station_id int) RETURNS float AS $dist$
DECLARE 
    lat1 float;
    lon1 float;
    lat2 float;
    lon2 float;

    dist float = 0;
    radlat1 float;
    radlat2 float;
    theta float;
    radtheta float;
BEGIN
    SELECT INTO lat1, lon1 latitude, longitude
    FROM stations 
    WHERE id = first_station_id;
    SELECT INTO lat2, lon2 latitude, longitude
    FROM stations 
    WHERE id = second_station_id;

    IF lat1 = lat2 AND lon1 = lon2
        THEN RETURN dist;
    ELSE
        radlat1 = pi() * lat1 / 180;
        radlat2 = pi() * lat2 / 180;
        theta = lon1 - lon2;
        radtheta = pi() * theta / 180;
        dist = sin(radlat1) * sin(radlat2) + cos(radlat1) * cos(radlat2) * cos(radtheta);

        IF dist > 1 THEN dist = 1; END IF;

        dist = acos(dist);
        dist = dist * 180 / pi();
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        RETURN dist;
    END IF;
END;
$dist$ 
LANGUAGE plpgsql
IMMUTABLE;
-- Формулы я взял отсюда https://www.geodatasource.com/developers/postgresql