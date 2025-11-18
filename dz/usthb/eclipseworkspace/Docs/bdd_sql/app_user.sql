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
