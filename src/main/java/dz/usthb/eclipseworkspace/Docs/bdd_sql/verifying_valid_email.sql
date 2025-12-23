CREATE OR REPLACE FUNCTION email_valid()
RETURNS TRIGGER AS $$
BEGIN
    -- Check not null
    IF NEW.email IS NOT NULL THEN

        -- General email format check
        IF NEW.email !~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' THEN
            RAISE EXCEPTION 'Invalid email format: %', NEW.email;
        END IF;

        -- Allowed domains only
        IF NOT (
            NEW.email ~* '^[^@]+@usthb\.dz$' OR
            NEW.email ~* '^[^@]+@gmail\.com$' OR
            NEW.email ~* '^[^@]+@yahoo\.fr$' OR
            NEW.email ~* '^[^@]+@hotmail\.com$' OR
            NEW.email ~* '^[^@]+@outlook\.com$'
        ) THEN
            RAISE EXCEPTION 'Email domain not allowed: %', NEW.email;
        END IF;

    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
