-- Status global du TODO + date de complétion
ALTER TABLE todo
ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'EMPTY'
    CHECK (status IN ('EMPTY','ACTIVE','ALL_DONE'));

ALTER TABLE todo
ADD COLUMN IF NOT EXISTS completed_at DATE;
CREATE OR REPLACE FUNCTION update_todo_status_from_items()
RETURNS TRIGGER AS $$
DECLARE
    v_todo_id    INT;
    v_total      INT;
    v_done       INT;
BEGIN
    -- Récupérer le bon todo_id
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        v_todo_id := NEW.todo_id;
    ELSIF TG_OP = 'DELETE' THEN
        v_todo_id := OLD.todo_id;
    END IF;

    -- Compter les items de ce TODO
    SELECT
        COUNT(*) AS total_count,
        COUNT(*) FILTER (WHERE status = 'DONE') AS done_count
    INTO v_total, v_done
    FROM todo_item
    WHERE todo_id = v_todo_id;

    -- Déterminer le status
    IF v_total = 0 THEN
        -- Aucun item
        UPDATE todo
        SET status = 'EMPTY',
            completed_at = NULL
        WHERE todo_id = v_todo_id;

    ELSIF v_done = v_total THEN
        -- Tous les items sont DONE
        UPDATE todo
        SET status = 'ALL_DONE',
            completed_at = CURRENT_DATE
        WHERE todo_id = v_todo_id;

    ELSE
        -- Mix OPEN / DONE / CANCELLED → actif
        UPDATE todo
        SET status = 'ACTIVE',
            completed_at = NULL
        WHERE todo_id = v_todo_id;
    END IF;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_update_todo_status_from_items
AFTER INSERT OR UPDATE OF status OR DELETE ON todo_item
FOR EACH ROW
EXECUTE FUNCTION update_todo_status_from_items();
ALTER TABLE todo_item
ADD COLUMN IF NOT EXISTS priority TEXT NOT NULL DEFAULT 'MEDIUM'
    CHECK (priority IN ('LOW','MEDIUM','HIGH'));
CREATE OR REPLACE FUNCTION normalize_todo_item()
RETURNS TRIGGER AS $$
BEGIN
    -- Nettoyage du titre
    IF NEW.title IS NOT NULL THEN
        NEW.title := trim(NEW.title);

        -- Limiter à 200 caractères par exemple
        IF char_length(NEW.title) > 200 THEN
            NEW.title := substring(NEW.title FROM 1 FOR 200);
        END IF;
    END IF;

    -- Définir due_date par défaut si NULL
    IF NEW.due_date IS NULL THEN
        IF NEW.priority = 'HIGH' THEN
            NEW.due_date := CURRENT_DATE + INTERVAL '2 days';
        ELSIF NEW.priority = 'MEDIUM' THEN
            NEW.due_date := CURRENT_DATE + INTERVAL '7 days';
        ELSIF NEW.priority = 'LOW' THEN
            NEW.due_date := CURRENT_DATE + INTERVAL '14 days';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_normalize_todo_item
BEFORE INSERT OR UPDATE OF title, priority, due_date ON todo_item
FOR EACH ROW
EXECUTE FUNCTION normalize_todo_item();
CREATE TABLE IF NOT EXISTS audit_log (
    audit_id    BIGSERIAL PRIMARY KEY,
    table_name  TEXT NOT NULL,
    op          TEXT NOT NULL CHECK (op IN ('INSERT','UPDATE','DELETE')),
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    db_user     TEXT NOT NULL DEFAULT CURRENT_USER,
    row_pk      JSONB,   -- optionnel : pour plus tard
    old_row     JSONB,
    new_row     JSONB
);
CREATE OR REPLACE FUNCTION audit_changes()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_log (table_name, op, row_pk, old_row, new_row)
    VALUES (
        TG_TABLE_NAME,
        TG_OP,
        NULL,  -- tu pourras plus tard y mettre un JSON avec les PK
        CASE WHEN TG_OP IN ('UPDATE','DELETE') THEN to_jsonb(OLD) ELSE NULL END,
        CASE WHEN TG_OP IN ('INSERT','UPDATE') THEN to_jsonb(NEW) ELSE NULL END
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
-- Exemple : app_user
CREATE OR REPLACE TRIGGER audit_app_user
AFTER INSERT OR UPDATE OR DELETE ON app_user
FOR EACH ROW
EXECUTE FUNCTION audit_changes();

-- team
CREATE OR REPLACE TRIGGER audit_team
AFTER INSERT OR UPDATE OR DELETE ON team
FOR EACH ROW
EXECUTE FUNCTION audit_changes();

-- task
CREATE OR REPLACE TRIGGER audit_task
AFTER INSERT OR UPDATE OR DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION audit_changes();

-- todo
CREATE OR REPLACE TRIGGER audit_todo
AFTER INSERT OR UPDATE OR DELETE ON todo
FOR EACH ROW
EXECUTE FUNCTION audit_changes();

-- todo_item
CREATE OR REPLACE TRIGGER audit_todo_item
AFTER INSERT OR UPDATE OR DELETE ON todo_item
FOR EACH ROW
EXECUTE FUNCTION audit_changes();
SELECT * FROM audit_log ORDER BY audit_id DESC;
ALTER TABLE task
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE task
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
CREATE OR REPLACE FUNCTION soft_delete_task()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE task
    SET is_deleted = TRUE,
        deleted_at = NOW()
    WHERE task_id = OLD.task_id;

    -- Empêcher le DELETE réel
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_soft_delete_task
BEFORE DELETE ON task
FOR EACH ROW
EXECUTE FUNCTION soft_delete_task();
