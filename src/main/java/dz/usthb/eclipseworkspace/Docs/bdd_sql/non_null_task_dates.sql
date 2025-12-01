CREATE OR REPLACE FUNCTION non_null_dates()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.start_date IS NULL THEN
        RAISE EXCEPTION 'Start date of tasks must be non-null'
            USING ERRCODE = '23514';
    END IF;

    IF NEW.due_date IS NULL THEN
        RAISE EXCEPTION 'End date of tasks must be non-null'
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE TRIGGER trg_non_null_dates
BEFORE INSERT OR UPDATE ON task
FOR EACH ROW
EXECUTE FUNCTION non_null_dates();
