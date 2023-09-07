-- Выбирает билеты в удобном порядке
SELECT *
FROM tickets
ORDER BY train_id,
    wagon_number,
    seat;