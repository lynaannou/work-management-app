CREATE OR REPLACE FUNCTION creation_auto_todo_for_inserted_user()
RETURNS TRIGGER AS $$
DECLARE
    v_todo_id INT;
BEGIN
    -- 1) Créer le TODO pour ce user
    INSERT INTO todo (user_id)
    VALUES (NEW.user_id)
    RETURNING todo_id INTO v_todo_id;

    -- 2) Créer un premier item "Welcome"
    INSERT INTO todo_item (todo_id, title, description, due_date)
    VALUES (
        v_todo_id,
        'Welcome Task',
        'This is your first task! Feel free to edit or delete it.',
        CURRENT_DATE + INTERVAL '7 days'
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE TRIGGER trg_creation_auto_todo_for_inserted_user
AFTER INSERT ON app_user
FOR EACH ROW
EXECUTE FUNCTION creation_auto_todo_for_inserted_user();
