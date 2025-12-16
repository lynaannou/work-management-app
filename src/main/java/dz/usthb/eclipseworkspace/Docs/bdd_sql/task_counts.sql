ALTER TABLE team_member
ADD COLUMN task_count INT DEFAULT 0;

CREATE OR REPLACE FUNCTION update_member_counts()
RETURNS TRIGGER AS $$
DECLARE
    v_count           INT;
    v_team_member_id  INT;
    v_team_id         INT;
BEGIN
    -- 1) Récupérer le bon (team_member_id, team_id) selon l'opération
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        v_team_member_id := NEW.team_member_id;
        v_team_id        := NEW.team_id;
    ELSIF TG_OP = 'DELETE' THEN
        v_team_member_id := OLD.team_member_id;
        v_team_id        := OLD.team_id;
    END IF;

    -- Si pas de team_member (tâche non assignée), on ne fait rien
    IF v_team_member_id IS NULL THEN
        RETURN COALESCE(NEW, OLD);
    END IF;

    -- 2) Recompter le nombre de tâches pour ce (team_member, team)
    SELECT COUNT(*)
    INTO v_count
    FROM task
    WHERE team_member_id = v_team_member_id
      AND team_id        = v_team_id;

    -- 3) Vérifier la limite de 5 tâches
    IF v_count > 5 THEN
        RAISE EXCEPTION
            'A team member cannot have more than 5 tasks assigned in the same team. Current: %',
            v_count;
    END IF;

    -- 4) Mettre à jour le compteur dans team_member
    UPDATE team_member
    SET task_count = v_count
    WHERE team_member_id = v_team_member_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_update_member_task_count
AFTER INSERT OR UPDATE OF team_member_id, team_id OR DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION update_member_counts();
