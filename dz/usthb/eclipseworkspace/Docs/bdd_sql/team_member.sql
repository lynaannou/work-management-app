CREATE TABLE team_member (
  team_member_id SERIAL PRIMARY KEY,
  team_id        INTEGER NOT NULL REFERENCES team(team_id) ON DELETE CASCADE,
  user_id        INTEGER NOT NULL REFERENCES app_user(user_id),
  role           TEXT DEFAULT 'MEMBER' CHECK (role IN ('LEAD','MEMBER')),
  added_at       DATE NOT NULL DEFAULT CURRENT_DATE

);

ALTER TABLE team_member
ADD CONSTRAINT uq_team_member_unique
UNIQUE (team_id, user_id);
