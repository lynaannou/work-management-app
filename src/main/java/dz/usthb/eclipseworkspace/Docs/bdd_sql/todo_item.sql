CREATE TABLE todo_item (
  item_id     SERIAL PRIMARY KEY,
  todo_id     INTEGER NOT NULL REFERENCES todo(todo_id) ON DELETE CASCADE,
  title       TEXT NOT NULL,
  description TEXT,  -- au lieu de CLOB
  created_at  DATE NOT NULL DEFAULT CURRENT_DATE,
  status      TEXT NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','DONE','CANCELLED')),
  due_date    DATE
);
