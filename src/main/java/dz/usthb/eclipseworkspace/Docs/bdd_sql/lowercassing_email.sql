-- 1) Fonction de normalisation de l'email
CREATE OR REPLACE FUNCTION lowercasing_email()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.email IS NOT NULL THEN
        -- optionnel : trim des espaces + mise en minuscule
        NEW.email := LOWER(TRIM(NEW.email));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2) Trigger qui appelle la fonction
CREATE OR REPLACE TRIGGER trg_lowercasing_email
BEFORE INSERT OR UPDATE OF email ON app_user
FOR EACH ROW
EXECUTE FUNCTION lowercasing_email();
