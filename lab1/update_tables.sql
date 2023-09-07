CREATE TABLE IF NOT EXISTS public.stations (
    id serial PRIMARY KEY,
    name varchar(50) NOT NULL CHECK (name != ''),
    latitude real NOT NULL CHECK (
        latitude >= -90
        AND latitude <= 90
    ),
    longitude real NOT NULL CHECK (
        longitude >= -180
        AND longitude <= 180
    )
);
ALTER TABLE public.tickets DROP COLUMN departure_station,
    DROP COLUMN destination_station,
    ADD COLUMN departure_station_id integer REFERENCES public.stations NOT NULL,
    ADD COLUMN destination_station_id integer REFERENCES public.stations NOT NULL,
    ADD CONSTRAINT valid_transfer CHECK (
        departure_station_id != destination_station_id
        AND departure_time < destination_time
    );
ALTER TABLE public.routes DROP COLUMN first_station,
    DROP COLUMN last_station,
    ADD COLUMN first_station_id integer REFERENCES public.stations NOT NULL,
    ADD COLUMN last_station_id integer REFERENCES public.stations NOT NULL,
    ADD CONSTRAINT valid_route CHECK (first_station_id != last_station_id);
ALTER TABLE public.route_sections DROP COLUMN departure_station,
    DROP COLUMN destination_station,
    ADD COLUMN departure_station_id integer REFERENCES public.stations NOT NULL,
    ADD COLUMN destination_station_id integer REFERENCES public.stations NOT NULL,
    ADD CONSTRAINT valid_transfer CHECK (
        departure_station_id != destination_station_id
        AND departure_time < destination_time
    );