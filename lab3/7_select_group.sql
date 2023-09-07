-- Выбирает поезда с совокупной стоимостью билетов больше заданной
SELECT sum(tickets.price_paid),
    tickets.train_id
FROM tickets
GROUP BY tickets.train_id
HAVING sum(tickets.price_paid) > 10000;