/*FONCTIONS POUR AFFICHER LES CHANGEMENTS OU INSERTIONS FAITES SUR LES TABLES ANY OF THE TABLES*/
-- Fonction unique pour logger les changements
CREATE OR REPLACE FUNCTION dbg_log_changes()
RETURNS trigger AS $$
DECLARE
    v_user text := current_user;
BEGIN
    IF TG_OP = 'INSERT' THEN
        RAISE NOTICE 'DBG %: INSERT on % by % → NEW = %',
            now(), TG_TABLE_NAME, v_user, row_to_json(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        RAISE NOTICE 'DBG %: UPDATE on % by % → OLD = %, NEW = %',
            now(), TG_TABLE_NAME, v_user, row_to_json(OLD), row_to_json(NEW);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
-- app_user (avec tes triggers existants sur username + password) 
CREATE OR REPLACE TRIGGER dbg_log_app_user
AFTER INSERT OR UPDATE ON app_user
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


-- team (ta table team existe déjà, utilisée par les FKs)
CREATE OR REPLACE TRIGGER dbg_log_team
AFTER INSERT OR UPDATE ON team
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


-- team_member :contentReference[oaicite:2]{index=2}
CREATE OR REPLACE TRIGGER dbg_log_team_member
AFTER INSERT OR UPDATE ON team_member
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


-- task :contentReference[oaicite:3]{index=3}
CREATE OR REPLACE TRIGGER dbg_log_task
AFTER INSERT OR UPDATE ON task
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


-- todo (la liste par user) :contentReference[oaicite:4]{index=4}
CREATE OR REPLACE TRIGGER dbg_log_todo
AFTER INSERT OR UPDATE ON todo
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


-- todo_item (les items dans chaque todo) :contentReference[oaicite:5]{index=5}
CREATE OR REPLACE TRIGGER dbg_log_todo_item
AFTER INSERT OR UPDATE ON todo_item
FOR EACH ROW
EXECUTE FUNCTION dbg_log_changes();


--- the tests i did :
INSERT INTO app_user (email, username, first_name, last_name, phone, password_hash)
VALUES ('test@example.com', '', 'Test', 'User', '0670000000', 'clearpassword');
