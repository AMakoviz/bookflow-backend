CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE SCHEMA resource;
CREATE SCHEMA booking;

CREATE TABLE resource.rooms (
    id uuid NOT NULL,
    name varchar(120) NOT NULL,
    location varchar(160) NOT NULL,
    capacity integer NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT rooms_pk PRIMARY KEY (id),
    CONSTRAINT rooms_name_uk UNIQUE (name),
    CONSTRAINT rooms_capacity_positive_ck CHECK (capacity > 0),
    CONSTRAINT rooms_status_ck CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE booking.employees (
    id uuid NOT NULL,
    display_name varchar(160) NOT NULL,
    email varchar(254) NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT employees_pk PRIMARY KEY (id),
    CONSTRAINT employees_email_uk UNIQUE (email)
);

CREATE TABLE booking.bookings (
    id uuid NOT NULL,
    room_id uuid NOT NULL,
    employee_id uuid NOT NULL,
    start_at timestamptz NOT NULL,
    end_at timestamptz NOT NULL,
    participant_count integer NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    CONSTRAINT bookings_pk PRIMARY KEY (id),
    CONSTRAINT bookings_room_fk FOREIGN KEY (room_id) REFERENCES resource.rooms (id),
    CONSTRAINT bookings_employee_fk FOREIGN KEY (employee_id) REFERENCES booking.employees (id),
    CONSTRAINT bookings_interval_ck CHECK (end_at > start_at),
    CONSTRAINT bookings_participant_count_positive_ck CHECK (participant_count > 0),
    CONSTRAINT bookings_status_ck CHECK (status IN ('CONFIRMED')),
    CONSTRAINT booking_no_room_overlap EXCLUDE USING gist (
        room_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
    ) WHERE (status = 'CONFIRMED')
);

CREATE INDEX bookings_room_interval_idx
    ON booking.bookings (room_id, start_at, end_at);

CREATE INDEX bookings_employee_start_idx
    ON booking.bookings (employee_id, start_at);

INSERT INTO resource.rooms (id, name, location, capacity, status, created_at, updated_at)
VALUES
    ('8a8d73f1-1ef0-4e49-aef3-d84ee901a5b5', 'Focus', '2nd floor', 4, 'ACTIVE', now(), now()),
    ('a11f1c07-ff68-4454-9851-9f4df096b25e', 'Team', '3rd floor', 8, 'ACTIVE', now(), now()),
    ('77dfbf77-64cb-4486-9c52-899dfba240a3', 'Forum', '1st floor', 12, 'ACTIVE', now(), now());

INSERT INTO booking.employees (id, display_name, email, created_at)
VALUES (
    '3ceebbd9-84e9-40df-8123-b93128f79ac5',
    'Demo Employee',
    'demo@bookflow.local',
    now()
);
