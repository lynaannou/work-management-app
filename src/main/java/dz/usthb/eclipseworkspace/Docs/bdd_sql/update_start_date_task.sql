-- 1) Make sure the column exists (only if you haven’t already done it)
-- If start_date is already there, you can skip this.
ALTER TABLE task
ADD COLUMN IF NOT EXISTS start_date DATE;

-- 2) Function: auto set start_date when status = IN_PROGRESS
CREATE OR REPLACE FUNCTION if_status_inprogress_startdate_update()
RETURNS TRIGGER AS $$
BEGIN
    -- When we insert or update and set status to IN_PROGRESS
    -- and start_date is still NULL, we fill it automatically.
    IF NEW.status = 'IN_PROGRESS'
       AND NEW.start_date IS NULL THEN
        NEW.start_date := CURRENT_DATE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3) Trigger: call this function on INSERT/UPDATE of status
CREATE OR REPLACE TRIGGER trg_task_auto_start_date
BEFORE INSERT OR UPDATE OF status ON task
FOR EACH ROW
EXECUTE FUNCTION if_status_inprogress_startdate_update();
