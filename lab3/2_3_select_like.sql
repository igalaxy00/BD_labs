-- Выбирает станции с пробелом в названии
SELECT *
FROM stations
WHERE name LIKE '% %';