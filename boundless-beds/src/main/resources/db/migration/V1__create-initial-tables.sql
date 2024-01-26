CREATE TYPE currency_type AS ENUM ('EUR', 'USD', 'GBP', 'JPY', 'CHF');
CREATE TYPE room_type AS ENUM ('Standard', 'Superior', 'Suite');

CREATE TABLE account
(
    id           UUID PRIMARY KEY,
    full_name    VARCHAR(255)  NOT NULL,
    email        VARCHAR(255)  NOT NULL UNIQUE,
    phone_number VARCHAR(255)  NOT NULL UNIQUE,
    balance      DECIMAL       NOT NULL,
    currency     currency_type NOT NULL
);

CREATE TABLE reservation
(
    id             UUID PRIMARY KEY,
    account_id     UUID      NOT NULL REFERENCES account (id),
    room_type      room_type NOT NULL,
    check_in_date  DATE      NOT NULL,
    check_out_date DATE      NOT NULL,
    total_price    DECIMAL   NOT NULL,
    is_confirmed   BOOLEAN DEFAULT FALSE,

    CONSTRAINT check_dates CHECK (check_out_date > check_in_date)
);