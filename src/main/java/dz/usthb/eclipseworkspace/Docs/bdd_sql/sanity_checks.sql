-- =====================================================================
-- ECLIPSEWORK – TRIGGER TEST SUITE (FIXED VERSION)
-- =====================================================================
-- This script tests all main triggers:
--   - app_user (email, password, username, auto-todo)
--   - todo / todo_item (normalization + status aggregation)
--   - task / team / team_member (counters, limits)
--   - audit + debug logs
--
-- IMPORTANT:
--   Run on a DEV DATABASE ONLY.
--   It will TRUNCATE core tables.
-- =====================================================================


-- =====================================================================
-- 0. GLOBAL RESET (OPTIONAL BUT RECOMMENDED)
-- =====================================================================

BEGIN;

TRUNCATE
    todo_item,
    todo,
    task,
    team_member,
    team,
    app_user,
    audit_log
RESTART IDENTITY CASCADE;

COMMIT;



-- =====================================================================
-- 1. USER MANAGEMENT – DIMA
-- =====================================================================
-- Tables: app_user, todo, todo_item
-- Triggers under test:
--   - email normalization (lowercase + trim)
--   - email format + domain validation
--   - password hashing (pgcrypto)
--   - username generated from email
--   - auto-creation of todo + "Welcome Task"
-- =====================================================================

-- -------------------------------------------------
-- 1.1 EMAIL NORMALIZATION (lowercase + trim)
-- -------------------------------------------------

INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('  Alpha@Usthb.DZ  ', 'Alpha', 'User', '0550000001', 'alpha123');

-- EXPECT:
--   - email = 'alpha@usthb.dz'
--   - username = 'alpha'
--   - password_hash != 'alpha123' (hashed)
--   - 1 todo + 1 welcome todo_item created via trigger
SELECT user_id, email, username, password_hash
FROM app_user
WHERE first_name = 'Alpha';

SELECT * FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE first_name = 'Alpha');
SELECT * FROM todo_item WHERE todo_id = (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE first_name = 'Alpha'));


-- -------------------------------------------------
-- 1.2 EMAIL FORMAT + DOMAIN VALIDATION
-- -------------------------------------------------

-- 1.2.a Valid email + allowed domain -> should pass
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('bravo@gmail.com', 'Bravo', 'User', '0550000002', 'bravo123');

-- 1.2.b Invalid email format -> should FAIL (UNCOMMENT TO TEST)
-- INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
-- VALUES ('invalid-email', 'Bad', 'Email', '0550000003', 'bad123');

-- 1.2.c Disallowed domain -> should FAIL (UNCOMMENT TO TEST)
-- INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
-- VALUES ('user@unknown-domain.xyz', 'Bad', 'Domain', '0550000004', 'test123');

SELECT user_id, email
FROM app_user
ORDER BY user_id;



-- -------------------------------------------------
-- 1.3 PASSWORD HASHING WITH PGCRYPTO
-- -------------------------------------------------

INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('charlie@yahoo.fr', 'Charlie', 'User', '0550000005', 'charlie123');

-- EXPECT: hashed password, not 'charlie123'
SELECT user_id, email, password_hash
FROM app_user
WHERE email = 'charlie@yahoo.fr';



-- -------------------------------------------------
-- 1.4 USERNAME FROM EMAIL
-- -------------------------------------------------

INSERT INTO app_user (email, first_name, last_name, phone, password_hash, username)
VALUES ('delta@usthb.dz', 'Delta', 'User', '0550000006', 'delta123', NULL);

-- EXPECT: username = 'delta'
SELECT user_id, email, username
FROM app_user
WHERE email = 'delta@usthb.dz';



-- -------------------------------------------------
-- 1.5 AUTO-CREATION OF TODO + "WELCOME TASK"
-- -------------------------------------------------

INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('echo@usthb.dz', 'Echo', 'User', '0550000007', 'echo123');

-- EXPECT: one todo + one "Welcome Task" todo_item
SELECT u.user_id, u.email, t.todo_id, t.status
FROM app_user u
JOIN todo t ON t.user_id = u.user_id
WHERE u.email = 'echo@usthb.dz';

SELECT ti.*
FROM todo_item ti
JOIN todo t ON ti.todo_id = t.todo_id
JOIN app_user u ON t.user_id = u.user_id
WHERE u.email = 'echo@usthb.dz';



-- =====================================================================
-- 2. TO-DO LIST MANAGEMENT – AMINA
-- =====================================================================
-- Tables: todo, todo_item
-- Triggers under test:
--   - todo_item normalization (title + priority -> due_date)
--   - todo status aggregation (EMPTY / ACTIVE / ALL_DONE)
-- =====================================================================

-- 2.0 Create a dedicated user for Amina's tests (todo auto-created by trigger)

INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('todo_owner@test.com', 'TodoOwner', 'User', '0550000008', 'owner123');

-- Find the todo created automatically for this user
SELECT * FROM todo
WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com');

-- We'll reuse that todo_id in next tests
--   SELECT todo_id FROM todo WHERE user_id = (...)



-- -------------------------------------------------
-- 2.1 TODO_ITEM NORMALIZATION (title + priority -> due_date)
-- -------------------------------------------------

-- HIGH priority, no due_date, messy title
INSERT INTO todo_item (todo_id, title, description, priority)
VALUES (
    (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com')),
    '   Finish report   ',
    'Important task with no due date set explicitly',
    'HIGH'
);

-- EXPECT:
--   - title trimmed: 'Finish report'
--   - due_date = today + 2 days (approx)
SELECT item_id, title, priority, created_at, due_date
FROM todo_item
WHERE description LIKE 'Important task%';


-- MEDIUM priority, no due_date
INSERT INTO todo_item (todo_id, title, description, priority)
VALUES (
    (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com')),
    'Medium priority item',
    'Test medium priority default due_date',
    'MEDIUM'
);

-- EXPECT: due_date = today + 7 days
SELECT item_id, title, priority, created_at, due_date
FROM todo_item
WHERE description LIKE 'Test medium priority%';


-- LOW priority, no due_date
INSERT INTO todo_item (todo_id, title, description, priority)
VALUES (
    (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com')),
    'Low priority item',
    'Test low priority default due_date',
    'LOW'
);

-- EXPECT: due_date = today + 14 days
SELECT item_id, title, priority, created_at, due_date
FROM todo_item
WHERE description LIKE 'Test low priority%';



-- -------------------------------------------------
-- 2.2 TODO STATUS AGGREGATION
-- -------------------------------------------------

-- Check current status of the todo for 'todo_owner'
SELECT todo_id, user_id, status, completed_at
FROM todo
WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com');
-- EXPECT: status = 'ACTIVE' (has items, not all DONE)

-- Mark all items as DONE
UPDATE todo_item
SET status = 'DONE'
WHERE todo_id = (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com'));

-- EXPECT:
--   - status = 'ALL_DONE'
--   - completed_at = today
SELECT todo_id, status, completed_at
FROM todo
WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com');

-- Delete all items to verify EMPTY status
DELETE FROM todo_item
WHERE todo_id = (SELECT todo_id FROM todo WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com'));

-- EXPECT:
--   - status = 'EMPTY'
--   - completed_at = NULL
SELECT todo_id, status, completed_at
FROM todo
WHERE user_id = (SELECT user_id FROM app_user WHERE email = 'todo_owner@test.com');



-- =====================================================================
-- 3. TASK MANAGEMENT – AÏDA
-- =====================================================================
-- Tables: task, team, team_member
-- Triggers / constraints under test:
--   - task state constraints (due_date/start_date/completed_at)
--   - per-team task counters (open_tasks_count, done_tasks_count)
--   - per-member task_count + max 5 tasks per member per team
-- =====================================================================

-- 3.0 Create users for tasks
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES
    ('worker1@usthb.dz', 'Worker1', 'User', '0550000010', 'w1'),
    ('worker2@usthb.dz', 'Worker2', 'User', '0550000011', 'w2');

-- 3.0 Create two teams
INSERT INTO team (team_id, name, lead_user_id, status)
VALUES
    (1, 'Team Alpha', (SELECT user_id FROM app_user WHERE email = 'worker1@usthb.dz'), 'ACTIVE'),
    (2, 'Team Beta',  (SELECT user_id FROM app_user WHERE email = 'worker2@usthb.dz'), 'ACTIVE');

-- 3.0 Create team members
INSERT INTO team_member (team_id, user_id, role)
VALUES
    (1, (SELECT user_id FROM app_user WHERE email = 'worker1@usthb.dz'), 'LEAD'),
    (1, (SELECT user_id FROM app_user WHERE email = 'worker2@usthb.dz'), 'MEMBER'),
    (2, (SELECT user_id FROM app_user WHERE email = 'worker2@usthb.dz'), 'LEAD');

SELECT * FROM team;
SELECT * FROM team_member;



-- -------------------------------------------------
-- 3.1 TASK STATE CONSTRAINTS – NEGATIVE TESTS (UNCOMMENT TO SEE ERRORS)
-- -------------------------------------------------

-- 3.1.a due_date < start_date -> should FAIL
-- INSERT INTO task (team_id, team_member_id, title, description, status, start_date, due_date)
-- VALUES (
--     1,
--     (SELECT team_member_id FROM team_member WHERE team_id = 1 LIMIT 1),
--     'Bad date task',
--     'due_date before start_date should fail',
--     'TODO',
--     DATE '2025-01-10',
--     DATE '2025-01-05'
-- );

-- 3.1.b completed_at before start_date -> should FAIL
-- INSERT INTO task (team_id, team_member_id, title, description, status, start_date, completed_at)
-- VALUES (
--     1,
--     (SELECT team_member_id FROM team_member WHERE team_id = 1 LIMIT 1),
--     'Bad completed_at task',
--     'completed_at before start_date should fail',
--     'DONE',
--     DATE '2025-01-10',
--     DATE '2025-01-09'
-- );

-- 3.1.c completed_at set but status != DONE -> should FAIL
-- INSERT INTO task (team_id, team_member_id, title, description, status, start_date, completed_at)
-- VALUES (
--     1,
--     (SELECT team_member_id FROM team_member WHERE team_id = 1 LIMIT 1),
--     'Bad status task',
--     'completed_at set while status != DONE',
--     'IN_PROGRESS',
--     DATE '2025-01-10',
--     DATE '2025-01-11'
-- );



-- -------------------------------------------------
-- 3.2 TEAM TASK COUNTERS (open_tasks_count / done_tasks_count)
-- -------------------------------------------------

-- Create tasks for Team Alpha
INSERT INTO task (team_id, team_member_id, title, description, status)
VALUES
    (1, (SELECT team_member_id FROM team_member WHERE team_id = 1 ORDER BY team_member_id LIMIT 1),
        'Task 1', 'Open task 1', 'TODO'),
    (1, (SELECT team_member_id FROM team_member WHERE team_id = 1 ORDER BY team_member_id LIMIT 1),
        'Task 2', 'Open task 2', 'IN_PROGRESS'),
    (1, (SELECT team_member_id FROM team_member WHERE team_id = 1 ORDER BY team_member_id DESC LIMIT 1),
        'Task 3', 'Done task', 'DONE');

-- EXPECT Team Alpha:
--   open_tasks_count = 2
--   done_tasks_count = 1
SELECT team_id, name, open_tasks_count, done_tasks_count
FROM team
WHERE team_id = 1;

-- Mark one open task as DONE
UPDATE task
SET status = 'DONE'
WHERE team_id = 1 AND title = 'Task 1';

-- EXPECT Team Alpha:
--   open_tasks_count = 1
--   done_tasks_count = 2
SELECT team_id, name, open_tasks_count, done_tasks_count
FROM team
WHERE team_id = 1;

-- Move Task 3 from Team Alpha to Team Beta
UPDATE task
SET team_id = 2
WHERE title = 'Task 3';

-- EXPECT:
--   - Team Alpha counters updated
--   - Team Beta counters updated
SELECT team_id, name, open_tasks_count, done_tasks_count
FROM team
ORDER BY team_id;



-- -------------------------------------------------
-- 3.3 PER-MEMBER TASK LIMIT (max 5 per member per team) + task_count
-- -------------------------------------------------

-- Choose a member in Team Alpha
SELECT team_member_id, team_id, user_id
FROM team_member
WHERE team_id = 1;

-- Insert up to 5 tasks for the same member in Team Alpha
INSERT INTO task (team_id, team_member_id, title, description, status)
SELECT
    1,
    (SELECT team_member_id FROM team_member WHERE team_id = 1 ORDER BY team_member_id LIMIT 1),
    CONCAT('MemberTask ', g),
    'Task for member limit test',
    'TODO'
FROM generate_series(1, 5) AS g;

-- EXPECT:
--   task_count = 5 for that member
SELECT tm.team_member_id, tm.team_id, tm.task_count
FROM team_member tm
WHERE team_id = 1
ORDER BY tm.team_member_id;

-- Now try to assign a 6th task to same member in same team -> should FAIL
-- INSERT INTO task (team_id, team_member_id, title, description, status)
-- VALUES (
--     1,
--     (SELECT team_member_id FROM team_member WHERE team_id = 1 ORDER BY team_member_id LIMIT 1),
--     'Too many tasks',
--     'This should violate the 5-task limit',
--     'TODO'
-- );



-- =====================================================================
-- 4. TEAM MANAGEMENT – WISSAM
-- =====================================================================
-- Tables: team, team_member
-- Triggers under test:
--   - member_count per team + max 7 members
-- =====================================================================

-- Optional: use a fresh team for this part
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('team_lead@test.com', 'TeamLead', 'User', '0550000012', 'lead123');

INSERT INTO team (team_id, name, lead_user_id, status)
VALUES (
    10,
    'TestTeam',
    (SELECT user_id FROM app_user WHERE email = 'team_lead@test.com'),
    'ACTIVE'
);

-- Create 7 users to join team 10
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
SELECT
    CONCAT('member', g, '@usthb.dz'),
    CONCAT('Member', g),
    'User',
    CONCAT('05500010', g),
    CONCAT('m', g)
FROM generate_series(1, 7) AS g;

-- Add 7 members to team 10
INSERT INTO team_member (team_id, user_id, role)
SELECT
    10,
    user_id,
    'MEMBER'
FROM app_user
WHERE email LIKE 'member%@usthb.dz';

-- EXPECT: member_count = 7
SELECT team_id, name, member_count
FROM team
WHERE team_id = 10;

-- Try to add an 8th member -> should FAIL
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('member8@usthb.dz', 'Member8', 'User', '055000108', 'm8');

-- Uncomment to see error:
-- INSERT INTO team_member (team_id, user_id, role)
-- VALUES (
--     10,
--     (SELECT user_id FROM app_user WHERE email = 'member8@usthb.dz'),
--     'MEMBER'
-- );

-- Remove one member and check member_count updates
DELETE FROM team_member
WHERE team_id = 10
  AND user_id = (SELECT user_id FROM app_user WHERE email = 'member7@usthb.dz');

-- EXPECT: member_count = 6
SELECT team_id, name, member_count
FROM team
WHERE team_id = 10;



-- =====================================================================
-- 5. WORKSPACE METRICS – LYNA
-- =====================================================================
-- These queries just SHOW the pre-aggregated data for dashboards.
-- =====================================================================

SELECT
    t.team_id,
    t.name,
    t.member_count,
    t.open_tasks_count,
    t.done_tasks_count
FROM team t
ORDER BY t.team_id;

SELECT
    u.user_id,
    u.email,
    td.todo_id,
    td.status AS todo_status,
    td.completed_at
FROM app_user u
LEFT JOIN todo td ON td.user_id = u.user_id
ORDER BY u.user_id;

SELECT
    tm.team_member_id,
    tm.team_id,
    tm.user_id,
    tm.task_count
FROM team_member tm
ORDER BY tm.team_member_id;



-- =====================================================================
-- 6. CROSS-CUTTING TRIGGERS – DEBUG + AUDIT
-- =====================================================================

-- 6.1 DEBUG LOGGING (RAISE NOTICE)
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('debug@test.com', 'Debug', 'User', '0550000999', 'debug123');

UPDATE app_user
SET first_name = 'DebugUpdated'
WHERE email = 'debug@test.com';
-- EXPECT: check Messages console for NOTICE logs


-- 6.2 AUDIT LOG
INSERT INTO app_user (email, first_name, last_name, phone, password_hash)
VALUES ('audit@test.com', 'Audit', 'User', '0550000998', 'audit123');

UPDATE app_user
SET last_name = 'UserUpdated'
WHERE email = 'audit@test.com';

DELETE FROM app_user
WHERE email = 'audit@test.com';

-- EXPECT: three rows in audit_log for table_name = 'app_user'
SELECT *
FROM audit_log
ORDER BY audit_id DESC
LIMIT 10;


-- =====================================================================
-- END OF TEST SUITE
-- =====================================================================
