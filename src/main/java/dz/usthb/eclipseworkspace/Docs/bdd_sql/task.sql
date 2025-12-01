CREATE TABLE task (
  task_id        SERIAL PRIMARY KEY,
  team_id        INTEGER NOT NULL REFERENCES team(team_id) ON DELETE CASCADE,
  team_member_id INTEGER REFERENCES team_member(team_member_id), 
  title          TEXT NOT NULL,
  description    TEXT,
  status         TEXT DEFAULT 'TODO' CHECK (status IN ('TODO','IN_PROGRESS','DONE','CANCELLED')),
  start_date     DATE,
  due_date       DATE CHECK (due_date IS NULL OR start_date IS NULL OR due_date >= start_date),
  progress_pct   INTEGER DEFAULT 0 CHECK (progress_pct BETWEEN 0 AND 100),
  created_at     DATE DEFAULT CURRENT_DATE,
  completed_at   DATE
);