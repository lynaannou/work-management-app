ALTER TABLE team
ADD COLUMN IF NOT EXISTS open_tasks_count INT DEFAULT 0;

ALTER TABLE team
ADD COLUMN IF NOT EXISTS done_tasks_count INT DEFAULT 0;

-- (optionnel mais propre : s'assurer qu'on ne descend jamais en dessous de 0)
ALTER TABLE team
ADD CONSTRAINT chck_open_tasks_nonneg
CHECK (open_tasks_count >= 0);

ALTER TABLE team
ADD CONSTRAINT chck_done_tasks_nonneg
CHECK (done_tasks_count >= 0);
CREATE OR REPLACE FUNCTION update_team_task_counters()
RETURNS TRIGGER AS $$
DECLARE
    v_team_id INT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- nouvelle tâche → on met à jour la team de NEW
        v_team_id := NEW.team_id;

        UPDATE team t
        SET
            open_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status IN ('TODO','IN_PROGRESS')
            ),
            done_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status = 'DONE'
            )
        WHERE t.team_id = v_team_id;

    ELSIF TG_OP = 'DELETE' THEN
        -- suppression de tâche → on met à jour la team de OLD
        v_team_id := OLD.team_id;

        UPDATE team t
        SET
            open_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status IN ('TODO','IN_PROGRESS')
            ),
            done_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status = 'DONE'
            )
        WHERE t.team_id = v_team_id;

    ELSIF TG_OP = 'UPDATE' THEN
        -- 1) Toujours recalculer pour la nouvelle team
        v_team_id := NEW.team_id;

        UPDATE team t
        SET
            open_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status IN ('TODO','IN_PROGRESS')
            ),
            done_tasks_count = (
                SELECT COUNT(*)
                FROM task
                WHERE team_id = v_team_id
                  AND status = 'DONE'
            )
        WHERE t.team_id = v_team_id;

        -- 2) Si la tâche a changé de team, il faut aussi mettre à jour l’ancienne
        IF NEW.team_id IS DISTINCT FROM OLD.team_id THEN
            v_team_id := OLD.team_id;

            UPDATE team t
            SET
                open_tasks_count = (
                    SELECT COUNT(*)
                    FROM task
                    WHERE team_id = v_team_id
                      AND status IN ('TODO','IN_PROGRESS')
                ),
                done_tasks_count = (
                    SELECT COUNT(*)
                    FROM task
                    WHERE team_id = v_team_id
                      AND status = 'DONE'
                )
            WHERE t.team_id = v_team_id;
        END IF;
    END IF;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_update_team_task_counters
AFTER INSERT
OR UPDATE OF status, team_id
OR DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION update_team_task_counters();
