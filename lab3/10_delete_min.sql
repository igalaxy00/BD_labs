-- Удаляет билеты с наименьшей стоимостью
DELETE FROM tickets
WHERE price_paid = (
        SELECT min(price_paid)
        FROM tickets
    );