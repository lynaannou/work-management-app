CREATE OR REPLACE FUNCTION email_valid
()
RETURNS TRIGGER AS $$
BEGIN
    IF :NEW.email is NOT NULL THEN
        IF NOT (:NEW.email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$') THEN
            RAISE EXCEPTION 'Invalid email format: %', :NEW.email;
        END IF;
        IF NOT (:NEW.email ~* '^[^@]+@usthb\.dz$') OR NOT (:NEW.email ~* '^[^@]+@gmail\.com$') OR NOT (:NEW.email ~* '^[^@]+@yahoo\.fr$') OR NOT (:NEW.email ~* '^[^@]+@hotmail\.com$') OR NOT (:NEW.email ~* '^[^@]+@outlook\.com$') THEN
            RAISE EXCEPTION 'Email must belong to usthb.dz domain: %', :NEW.email;
        END IF;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_email_valid
BEFORE INSERT OR UPDATE ON app_user
FOR EACH ROW
EXECUTE FUNCTION email_valid();