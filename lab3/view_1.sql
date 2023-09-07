-- Сохраняет в представление количество пассажиров, перевезенных вагонами каждого класса, за последний месяц и общую сумму, вырученную с билетов, которые были куплены этими группами пассажиров.
CREATE OR REPLACE VIEW "Last month stats" AS
SELECT r.wagon_class_id,
    count(r.id) as passengers,
    sum(r.price_paid) as total_price
FROM (
        (
            SELECT *
            FROM tickets
            WHERE (
                    departure_time > (CURRENT_DATE - INTERVAL '1 month')
                    AND departure_time < CURRENT_DATE
                )
        ) as last_month_tickets
        INNER JOIN train_wagons ON (
            last_month_tickets.train_id = train_wagons.train_id
            AND last_month_tickets.wagon_number = train_wagons.position_in_train
        )
    ) as r
GROUP BY r.wagon_class_id;