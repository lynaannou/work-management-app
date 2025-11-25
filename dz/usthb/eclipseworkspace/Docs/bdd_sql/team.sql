
CREATE DOMAIN status_domain AS TEXT 
CHECK status (VALUE IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));
ALTER TABLE team
ADD COLUMN IF NOT EXISTS total_tasks_count INT DEFAULT 0;

ALTER TABLE team
ADD CONSTRAINT chck_total_tasks_nonneg CHECK (total_tasks_count >= 0);
 DROP TRIGGER IF EXISTS trg_update_team_task_counters ON task;
DROP FUNCTION IF EXISTS update_team_task_counters CASCADE;

CREATE OR REPLACE FUNCTION update_team_task_counters()
RETURNS TRIGGER AS $$
DECLARE
    v_team_id_old INT;
    v_team_id_new INT;
BEGIN
    -- Récupération des team_id impliqués
    IF TG_OP = 'INSERT' THEN
        v_team_id_new := NEW.team_id;

    ELSIF TG_OP = 'UPDATE' THEN
        v_team_id_new := NEW.team_id;
        v_team_id_old := OLD.team_id;

    ELSIF TG_OP = 'DELETE' THEN
        v_team_id_old := OLD.team_id;
    END IF;

    --------------------------------------------------------
    -- Fonction interne pour recalculer TOUS les compteurs
    --------------------------------------------------------
    PERFORM recalc_team(v_team_id_new);
    IF v_team_id_old IS NOT NULL AND v_team_id_old <> v_team_id_new THEN
        PERFORM recalc_team(v_team_id_old);
    END IF;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION recalc_team(p_team_id INT)
RETURNS VOID AS $$
DECLARE
    v_open INT;
    v_done INT;
    v_total INT;
BEGIN
    -- Recalcul
    SELECT
        COUNT(*) FILTER (WHERE status IN ('TODO','IN_PROGRESS')),
        COUNT(*) FILTER (WHERE status = 'DONE'),
        COUNT(*)
    INTO v_open, v_done, v_total
    FROM task
    WHERE team_id = p_team_id;

    -- Mise à jour des compteurs
    UPDATE team
    SET
        open_tasks_count  = v_open,
        done_tasks_count  = v_done,
        total_tasks_count = v_total,
        -- Mise à jour du statut si toutes les tâches sont finies
        status = CASE
                    WHEN v_total > 0 AND v_done = v_total THEN 'ARCHIVED'
                    ELSE status
                 END
    WHERE team_id = p_team_id;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_update_team_task_counters
AFTER INSERT OR UPDATE OF status, team_id OR DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION update_team_task_counters();
CREATE OR REPLACE FUNCTION recalc_team_total_tasks(p_team_id INT)
RETURNS VOID AS $$
DECLARE
    v_total INT;
BEGIN
    SELECT COALESCE(SUM(task_count), 0)
    INTO v_total
    FROM team_member
    WHERE team_id = p_team_id;

    UPDATE team
    SET total_tasks_count = v_total
    WHERE team_id = p_team_id;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION recalc_team_total_tasks(p_team_id INT)
RETURNS VOID AS $$
DECLARE
    v_total INT;
BEGIN
    SELECT COALESCE(SUM(task_count), 0)
    INTO v_total
    FROM team_member
    WHERE team_id = p_team_id;

    UPDATE team
    SET total_tasks_count = v_total
    WHERE team_id = p_team_id;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION recalc_team_total_tasks_trigger()
RETURNS TRIGGER AS $$
DECLARE
    v_team_id_old INT;
    v_team_id_new INT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        v_team_id_new := NEW.team_id;

    ELSIF TG_OP = 'UPDATE' THEN
        v_team_id_new := NEW.team_id;
        v_team_id_old := OLD.team_id;

    ELSIF TG_OP = 'DELETE' THEN
        v_team_id_old := OLD.team_id;
    END IF;

    -- Recalc new team
    IF v_team_id_new IS NOT NULL THEN
        PERFORM recalc_team_total_tasks(v_team_id_new);
    END IF;

    -- Recalc old team if changed
    IF v_team_id_old IS NOT NULL AND v_team_id_old <> v_team_id_new THEN
        PERFORM recalc_team_total_tasks(v_team_id_old);
    END IF;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
DROP TRIGGER IF EXISTS trg_recalc_team_total_tasks ON task;

CREATE TRIGGER trg_recalc_team_total_tasks
AFTER INSERT OR UPDATE OF team_member_id, team_id, status OR DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION recalc_team_total_tasks_trigger();
DROP TRIGGER IF EXISTS trg_recalc_total_on_team_member ON team_member;

CREATE OR REPLACE FUNCTION recalc_team_total_tasks_trigger_members()
RETURNS TRIGGER AS $$
DECLARE
    v_team_id INT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        v_team_id := NEW.team_id;
    ELSE
        v_team_id := OLD.team_id;
    END IF;

    PERFORM recalc_team_total_tasks(v_team_id);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recalc_total_on_team_member
AFTER INSERT OR DELETE ON team_member
FOR EACH ROW
EXECUTE FUNCTION recalc_team_total_tasks_trigger_members();
