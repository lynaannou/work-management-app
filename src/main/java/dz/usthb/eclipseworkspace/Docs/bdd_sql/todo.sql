CREATE TABLE todo (
  todo_id   SERIAL PRIMARY KEY,
  user_id   INTEGER NOT NULL UNIQUE REFERENCES app_user(user_id) ON DELETE CASCADE,                 -- enforce 1 list per user
  created_at DATE DEFAULT CURRENT_DATE
);
