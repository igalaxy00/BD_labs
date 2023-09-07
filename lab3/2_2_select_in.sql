-- Выбирает билеты с местами в 1, 2 и 8 вагонах
SELECT *
FROM tickets
WHERE wagon_number IN (1, 2, 8);