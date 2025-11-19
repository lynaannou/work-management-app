-- 1) Une seule fois dans ta base
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2) Fonction trigger
CREATE OR REPLACE FUNCTION crypting_passwords()
RETURNS TRIGGER AS $$
BEGIN   
    -- On crypte le mot de passe avant insertion ou mise à jour
    NEW.password_hash := crypt(NEW.password_hash, gen_salt('bf'));
    RETURN NEW;  -- obligatoire dans un BEFORE trigger
END;
$$ LANGUAGE plpgsql;

-- 3) Trigger
CREATE OR REPLACE TRIGGER trg_crypting_passwords
BEFORE INSERT OR UPDATE ON app_user
FOR EACH ROW
EXECUTE FUNCTION crypting_passwords();
