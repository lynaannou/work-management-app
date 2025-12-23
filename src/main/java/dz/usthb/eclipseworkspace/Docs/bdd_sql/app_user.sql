CREATE TABLE app_user (
  user_id        SERIAL PRIMARY KEY,
  email          TEXT NOT NULL UNIQUE,
  username       TEXT  NOT NULL UNIQUE,     -- sera auto depuis email
  first_name     TEXT  NOT NULL,
  last_name      TEXT  NOT NULL,
  phone          TEXT,
  password_hash  TEXT NOT NULL,
  created_at     DATE NOT NULL DEFAULT CURRENT_DATE
);

INSERT INTO app_user(email, first_name, last_name, phone, password_hash, created_at) VALUES ('lynaannou@gmail.com', 'Lyna', 'Annou', 06789999, 'passwording to test');
