CREATE OR REPLACE FUNCTION updating_done_taks_points()
RETURNS TRIGGER AS $$
BEGIN
IF NEW.status = 'done' THEN
    NEW.progress_pct := 100;
    NEW.completed_at := CURRENT_DATE;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_update_done_tasks
AFTER UPDATE ON task
FOR EACH ROW
EXECUTE FUNCTION updating_done_taks_points();

-------------------
CREATE OR REPLACE FUNCTION updating_points_taks_done()
RETURNS TRIGGER AS $$
BEGIN
IF NEW.progress_pct = 100 THEN
    NEW.status := 'done';
    NEW.completed_at := CURRENT_DATE;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_update_points_tasks_done
AFTER UPDATE ON task
FOR EACH ROW
EXECUTE FUNCTION updating_done_taks_points();

