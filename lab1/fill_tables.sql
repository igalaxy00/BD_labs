INSERT INTO public.passengers (
        first_name,
        last_name,
        gender,
        document,
        phone_number
    )
VALUES ('Among', 'Us', '-', 'sus', '69'),
    ('Vlad', 'Dalv', 'M', 'PASSPORT 4390', '11111111'),
    ('Ada', 'Dado', 'F', 'PASSPORT 2442', NULL);
INSERT INTO public.orders (creation_time, email, phone_number)
VALUES ('2011-01-08 04:05:06', 'example@mail.ru', NULL),
    ('2011-01-13 10:23:54', 'test@gmail.com', '666');
INSERT INTO public.routes (name, first_station, last_station)
VALUES ('A-C', 'A', 'C'),
    ('K-L', 'K', 'L');
INSERT INTO public.route_sections (
        route_id,
        departure_station,
        departure_time,
        destination_station,
        destination_time,
        cost
    )
VALUES (1, 'A', '12:00:00', 'B', '15:20:00', 100.5),
    (1, 'B', '15:40:00', 'C', '20:32:00', 4200),
    (2, 'K', '01:05:30', 'L', '02:25:10', 999.33);
INSERT INTO public.wagon_classes (name, capacity, cost_multiplier)
VALUES ('common', 32, 1.0),
    ('lux', 16, 2.5),
    ('ultra', 4, 20),
    ('sit', 48, 0.7);
INSERT INTO public.trains (route_id, length, departure_date)
VALUES (1, 3, '2011-01-30'),
    (1, 1, '2011-01-31');
INSERT INTO public.train_wagons (train_id, position_in_train, wagon_class_id)
VALUES (1, 1, 3),
    (1, 2, 1),
    (1, 3, 4),
    (2, 1, 3);
INSERT INTO public.tickets (
        train_id,
        order_id,
        passenger_id,
        departure_station,
        departure_time,
        destination_station,
        destination_time,
        wagon_number,
        seat,
        price_paid
    )
VALUES (
        1,
        1,
        1,
        'A',
        '2011-01-30 12:00:00',
        'B',
        '2011-01-30 15:20:00',
        3,
        47,
        70
    ),
    (
        1,
        1,
        1,
        'A',
        '2011-01-30 12:00:00',
        'B',
        '2011-01-30 15:20:00',
        3,
        48,
        70
    ),
    (
        2,
        2,
        2,
        'B',
        '2011-01-31 15:40:00',
        'C',
        '2011-01-31 20:32:00',
        1,
        4,
        84000
    ),
    (
        2,
        2,
        3,
        'A',
        '2011-01-31 12:00:00',
        'C',
        '2011-01-31 20:32:00',
        2,
        10,
        4300.5
    );