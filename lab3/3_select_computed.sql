-- Выводит список полных контактов заказа
SELECT concat(
        'Email: ',
        email,
        '    Phone Number: ',
        phone_number
    ) AS contacts
FROM orders;