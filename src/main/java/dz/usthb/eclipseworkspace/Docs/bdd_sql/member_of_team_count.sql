-- 1) colonne + contrainte (ok)
ALTER TABLE team
ADD COLUMN IF NOT EXISTS member_count INT DEFAULT 0;

ALTER TABLE team
ADD CONSTRAINT chck_member_count_max7
CHECK (member_count <= 7);

-- 2) fonction de mise à jour du member_count
CREATE OR REPLACE FUNCTION update_member_count()
RETURNS TRIGGER AS $$
DECLARE
    v_team_id INT;
BEGIN
    -- On récupère le team_id selon l'opération
    IF TG_OP = 'INSERT' THEN
        v_team_id := NEW.team_id;
    ELSIF TG_OP = 'DELETE' THEN
        v_team_id := OLD.team_id;
    END IF;

    UPDATE team t
    SET member_count = (
        SELECT COUNT(*) FROM team_member tm
        WHERE tm.team_id = v_team_id
    )
    WHERE t.team_id = v_team_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- 3) trigger
CREATE OR REPLACE TRIGGER trg_update_member_count
AFTER INSERT OR DELETE ON team_member
FOR EACH ROW
EXECUTE FUNCTION update_member_count();

--- tests 
-- Nettoyage minimal (optionnel, à adapter à ta base)
DELETE FROM team_member;
DELETE FROM team;


-- 1) Créer quelques users de test
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES
  ('alpha@usthb.dz',  'Alpha', 'User', '0550000001', 'alpha123'),
  ('bravo@gmail.com', 'Bravo', 'User', '0550000002', 'bravo123'),
  ('charlie@yahoo.fr','Charlie','User','0550000003', 'charlie123'),
  ('delta@usthb.dz',  'Delta', 'User', '0550000004', 'delta123');

-- Vérifier
SELECT user_id, email, username
FROM app_user
ORDER BY user_id;


-- Créer 2 teams
INSERT INTO team (team_id, name, lead_user_id, status)
VALUES
  (
    1,
    'Team Alpha',
    (SELECT user_id FROM app_user WHERE email = 'alpha@usthb.dz'),
    'ACTIVE'    -- ou 'DRAFT' / 'ARCHIVED' selon ton domain status_domain
  ),
  (
    2,
    'Team Beta',
    (SELECT user_id FROM app_user WHERE email = 'bravo@gmail.com'),
    'ACTIVE'
  );

-- Vérifier les member_count initiaux (doivent être 0)
SELECT team_id, name, member_count
FROM team
ORDER BY team_id;

-- 3.1) Associer 2 users à la Team 1
INSERT INTO team_member (team_id, user_id)
VALUES
  (1, (SELECT user_id FROM app_user WHERE email = 'alpha@usthb.dz')),
  (1, (SELECT user_id FROM app_user WHERE email = 'bravo@gmail.com'));

-- 3.2) Associer 1 user à la Team 2
INSERT INTO team_member (team_id, user_id)
VALUES
  (2, (SELECT user_id FROM app_user WHERE email = 'charlie@yahoo.fr'));

-- Vérifier les member_count après ces insertions
SELECT team_id, name, member_count
FROM team
ORDER BY team_id;

SELECT tm.team_member_id, tm.team_id, t.name, tm.user_id, u.email
FROM team_member tm
JOIN team t ON tm.team_id = t.team_id
JOIN app_user u ON tm.user_id = u.user_id
ORDER BY tm.team_member_id;

-- Créer quelques users de plus pour tester la limite
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES
  ('user3@usthb.dz', 'User3', 'Test', '0550000005', 'u3'),
  ('user4@usthb.dz', 'User4', 'Test', '0550000006', 'u4'),
  ('user5@usthb.dz', 'User5', 'Test', '0550000007', 'u5'),
  ('user6@usthb.dz', 'User6', 'Test', '0550000008', 'u6'),
  ('user7@usthb.dz', 'User7', 'Test', '0550000009', 'u7');

-- Ajouter ces 5 nouveaux users à la Team 1
INSERT INTO team_member (team_id, user_id)
SELECT 1, user_id
FROM app_user
WHERE email IN (
  'user3@usthb.dz',
  'user4@usthb.dz',
  'user5@usthb.dz',
  'user6@usthb.dz',
  'user7@usthb.dz'
);

-- Vérifier member_count
SELECT team_id, name, member_count
FROM team
WHERE team_id = 1;

-- Créer un 8ème user
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('user8@usthb.dz', 'User8', 'Test', '0550000010', 'u8');

-- Essayer de l'ajouter à la Team 1
INSERT INTO team_member (team_id, user_id)
VALUES (
  1,
  (SELECT user_id FROM app_user WHERE email = 'user8@usthb.dz')
);

SELECT team_id, name, member_count
FROM team
WHERE team_id = 1;

SELECT tm.team_member_id, u.email
FROM team_member tm
JOIN app_user u ON tm.user_id = u.user_id
WHERE tm.team_id = 1
ORDER BY tm.team_member_id;

-- Supprimer un membre (par exemple user7)
DELETE FROM team_member
WHERE team_id = 1
  AND user_id = (SELECT user_id FROM app_user WHERE email = 'user7@usthb.dz');

-- Vérifier member_count
SELECT team_id, name, member_count
FROM team
WHERE team_id = 1;

-- Ajouter d'autres users dans la Team 2
INSERT INTO team_member (team_id, user_id)
SELECT 2, user_id
FROM app_user
WHERE email IN ('delta@usthb.dz', 'user3@usthb.dz');

-- Vérifier les deux teams
SELECT team_id, name, member_count
FROM team
ORDER BY team_id;
