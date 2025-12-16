CREATE OR REPLACE FUNCTION set_username_from_email()
RETURNS trigger AS $$
BEGIN
  -- si username est vide ou NULL, on le calcule à partir de l'email
  IF NEW.username IS NULL OR NEW.username = '' THEN
    NEW.username := split_part(NEW.email, '@', 1);  -- tout ce qui est avant @
  END IF;

  RETURN NEW;  -- obligatoire dans un BEFORE trigger
END;
$$ LANGUAGE plpgsql;

-- 2) Le trigger
CREATE TRIGGER trg_username_from_email
BEFORE INSERT ON app_user
FOR EACH ROW
EXECUTE FUNCTION set_username_from_email();