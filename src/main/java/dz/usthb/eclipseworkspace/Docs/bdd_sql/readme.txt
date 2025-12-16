ECLIPSEWORK – DATABASE TRIGGERS OVERVIEW
========================================

Goal
----
This document explains how the PostgreSQL triggers are designed to HELP each module
and each person’s work. The idea is:

- Push as much boring / repetitive logic as possible into the database.
- Keep Java code cleaner and more focused on business logic and patterns.
- Guarantee consistent, safe data even if some validations are missed in code.

Modules / owners:
- Dima  : User Management (app_user, registration, login, security, personal todo init)
- Amina : Personal To-Do lists (todo, todo_item)
- Aïda  : Tasks (task, linked to team/team_member)
- Wissam: Teams & membership (team, team_member)
- Lyna  : Workspace / dashboards (global view of teams, tasks, todos)

There are also some “global” triggers (debug logs, audit logs) useful to everyone.


1. USER MANAGEMENT – DIMA
=========================

Tables: app_user, todo, todo_item

Dima’s module covers signup, login, password handling, usernames and creation of the
first personal “workspace” (todo list). Triggers here remove a lot of validation and
boilerplate from AuthService and user-related services.

1.1 Email normalization (lowercasing and trimming)
--------------------------------------------------

Trigger:
- Table: app_user
- When: BEFORE INSERT OR UPDATE OF email

What it does:
- Takes NEW.email
- Trims spaces at the start and end
- Converts it to lowercase

Effect:
- Every email in the database has a canonical form.
- No difference between "Alpha@Usthb.DZ" and "alpha@usthb.dz".

Why this helps Dima:
- In AuthService, you do not need to remember to normalize emails manually.
- The UNIQUE constraint on email works reliably (no case-related duplicates).
- Login logic is simpler: you can just lower() the email in input, or even trust the DB.

1.2 Email format and domain validation
--------------------------------------

Trigger:
- Table: app_user
- When: BEFORE INSERT OR UPDATE OF email

What it does:
- Checks that NEW.email matches a generic email pattern (contains '@', valid domain).
- Checks that the domain is in an allowed list:
  usthb.dz, gmail.com, yahoo.fr, hotmail.com, outlook.com (configurable in code).
- If invalid -> raises an exception and refuses the insert/update.

Why this helps Dima:
- Prevents garbage emails in app_user (e.g. "abc", "test@test").
- Moves a big part of validation into the DB, so even if a frontend or service forgets,
  the DB still protects the data.
- Reduces defensive checks in AuthService. You can assume app_user.email is valid.

1.3 Password hashing with pgcrypto
----------------------------------

Trigger:
- Table: app_user
- When: BEFORE INSERT OR UPDATE OF password_hash

What it does:
- On INSERT (and when the password changes), NEW.password_hash originally contains
  the clear-text password.
- The trigger replaces it with crypt(NEW.password_hash, gen_salt('bf')) using pgcrypto
  (Blowfish/BCrypt).
- Optionally, it can skip hashing if the password did not change (to avoid re-hashing).

Why this helps Dima:
- AuthService only needs to send the clear password once; the DB always stores a hash.
- No risk of accidentally storing a clear-text password if some code path forgets to hash.
- Centralizes password hashing logic, similar to having a "PasswordHashStrategy" at DB level.

1.4 Username generated from email
---------------------------------

Trigger:
- Table: app_user
- When: BEFORE INSERT (or BEFORE INSERT OR UPDATE OF email)

What it does:
- If NEW.username is empty or NULL:
  - Extracts the part before '@' from NEW.email.
  - Uses that as NEW.username (e.g. "alpha@usthb.dz" -> "alpha").

Why this helps Dima:
- Signup can be as simple as email + password. The user still gets a username.
- No need to re-implement the “take substring before @” logic in multiple places in Java.
- Gives a simple, predictable rule for default usernames.

1.5 Auto-creation of TODO and “Welcome Task” per new user
---------------------------------------------------------

Trigger:
- Tables: app_user (trigger), todo, todo_item
- When: AFTER INSERT ON app_user

What it does:
- After inserting a new user:
  1. Inserts a row in todo with user_id = NEW.user_id.
  2. Takes the generated todo_id.
  3. Inserts a default todo_item:
     - title = "Welcome Task"
     - description = "This is your first task! Feel free to edit or delete it."
     - status = "OPEN"
     - due_date = today + 7 days (via priority/due_date logic).

- Another trigger on todo_item (see Amina’s section) will then set todo.status = "ACTIVE".

Why this helps Dima and Amina:
- Every user automatically has a personal todo list and one sample task.
- No need for AuthService to remember "also create default tasks after user creation".
- Guarantees a consistent onboarding experience right from the DB.


2. TO-DO LIST MANAGEMENT – AMINA
================================

Tables: todo, todo_item

Amina’s module manages personal to-do lists. These triggers ensure todos and items are
stored cleanly, with default deadlines and a reliable aggregate status at todo level.

2.1 Normalize todo_item (title cleanup and priority -> due_date)
----------------------------------------------------------------

Trigger:
- Table: todo_item
- When: BEFORE INSERT OR UPDATE OF title, priority, due_date

What it does:
- Title:
  - Trims spaces at the beginning and end.
  - Optionally cuts to a maximum length (e.g. first 200 characters) to avoid overly long titles.
- Due date:
  - If NEW.due_date is NULL, it sets a default based on NEW.priority:
    - HIGH   -> today + 2 days
    - MEDIUM -> today + 7 days
    - LOW    -> today + 14 days
- priority is validated by a CHECK constraint: must be LOW, MEDIUM, or HIGH.

Why this helps Amina:
- Forms can be minimal. If user doesn’t pick a due date, DB picks a reasonable default.
- TodoService can trust that every item has a clean title and a consistent priority/due_date.
- Fits nicely with Template Method / Iterator patterns, since the list is already normalized.

2.2 Global TODO status (EMPTY, ACTIVE, ALL_DONE)
------------------------------------------------

Trigger:
- Trigger table: todo_item
- Updated table: todo
- Columns added to todo:
  - status TEXT NOT NULL DEFAULT 'EMPTY' (must be one of 'EMPTY', 'ACTIVE', 'ALL_DONE')
  - completed_at DATE
- When: AFTER INSERT OR UPDATE OF status OR DELETE ON todo_item

What it does for a given todo_id:
- Counts all items for that todo: total_count
- Counts how many are status = 'DONE': done_count
- Sets todo.status and todo.completed_at:
  - If total_count = 0:
    - status = 'EMPTY'
    - completed_at = NULL
  - Else if done_count = total_count:
    - status = 'ALL_DONE'
    - completed_at = CURRENT_DATE
  - Else:
    - status = 'ACTIVE'
    - completed_at = NULL

Why this helps Amina (and Lyna):
- TodoService does not need to recompute status each time. It just reads todo.status.
- UI can show a simple label: EMPTY / ACTIVE / ALL_DONE without extra COUNT queries.
- Lyna can use status and completed_at to build calendars and dashboards efficiently.


3. TASK MANAGEMENT – AÏDA
=========================

Tables: task, plus foreign keys to team and team_member

Aïda’s module defines the life cycle of tasks (TODO -> IN_PROGRESS -> DONE/CANCELLED),
and how they are assigned. Constraints and triggers here help enforce a valid "state machine"
and maintain metrics.

3.1 Constraints supporting the task state machine
-------------------------------------------------

(Not triggers, but very important for Aïda’s State pattern.)

Key constraints:
- status CHECK: must be one of 'TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED'.
- due_date CHECK: either NULL, or start_date is NULL, or due_date >= start_date.
- completed_at consistency:
  - completed_at cannot be before start_date:
    (completed_at IS NULL OR start_date IS NULL OR completed_at >= start_date)
  - completed_at must be NULL unless status = 'DONE':
    (completed_at IS NULL OR status = 'DONE')

Why this helps the State pattern:
- Database refuses impossible states (ex: completed_at set while status = 'IN_PROGRESS').
- TaskState classes in Java can rely on these invariants being always true after persistence.
- Turns subtle logic bugs into clear constraint violations, easier to detect and fix.

3.2 Per-team task counters (open_tasks_count, done_tasks_count)
---------------------------------------------------------------

Trigger:
- Trigger table: task
- Updated table: team
- Columns on team:
  - open_tasks_count INT DEFAULT 0
  - done_tasks_count INT DEFAULT 0
- When: AFTER INSERT OR UPDATE OF status, team_id OR DELETE ON task

What it does:
- For each team_id affected by the change:
  - Recomputes:
    - open_tasks_count = number of tasks with status = 'TODO' or 'IN_PROGRESS'
    - done_tasks_count = number of tasks with status = 'DONE'
- On UPDATE where team_id changes, it updates both the old team and the new team.

Why this helps Aïda and Lyna:
- Quickly shows "X open tasks / Y completed tasks" per team without heavy queries.
- TaskService does not need to manage these counters manually.
- Lyna’s workspace dashboards can directly use team.open_tasks_count and team.done_tasks_count.

3.3 Per-member task_count and max 5 tasks per member per team
-------------------------------------------------------------

Trigger:
- Trigger table: task
- Updated table: team_member
- Column on team_member:
  - task_count INT DEFAULT 0
- When: AFTER INSERT OR UPDATE OF team_member_id, team_id OR DELETE ON task

What it does:
- For each combination (team_id, team_member_id):
  - Counts how many tasks are assigned to that member in that team.
  - Updates team_member.task_count.
  - If the count > 5, raises an exception:
    "A team member cannot have more than 5 tasks assigned in the same team."

Why this helps:
- Implements the "max 5 tasks per member per team" rule directly in the DB.
- Task assignment code in Java does not need to re-check this every time.
- UI can show task_count next to each member and highlight overloaded members.


4. TEAM MANAGEMENT – WISSAM
===========================

Tables: team, team_member

Wissam’s module is responsible for teams, membership, and basic rules like the maximum
number of members per team. Triggers here keep member counts consistent and enforce limits.

4.1 member_count per team and max 7 members per team
----------------------------------------------------

Trigger:
- Trigger table: team_member
- Updated table: team
- Columns / constraints on team:
  - member_count INT DEFAULT 0
  - CHECK (member_count <= 7)  -> team must have at most 7 members
- When: AFTER INSERT OR DELETE ON team_member

What it does:
- On INSERT or DELETE of a team_member:
  - Finds the relevant team_id (from NEW for insert, from OLD for delete).
  - Counts how many team_member entries exist for that team_id.
  - Updates team.member_count to that count.
  - If member_count > 7, the CHECK constraint fails and the insertion is rolled back.

Why this helps Wissam:
- The team effectively "observes" its member list, thanks to the trigger.
- TeamMemberService does not maintain the count manually.
- The 7-member limit is enforced consistently for every client and every code path.
- member_count is always correct and can be displayed directly in dashboards.


5. WORKSPACE MANAGEMENT – LYNA
==============================

Tables: mainly team, todo, team_member (for aggregation)

Lyna’s module builds the "workspace view": dashboards, calendars, and global metrics.
She relies on the pre-aggregated fields maintained by triggers.

5.1 Workspace metrics provided by triggers
------------------------------------------

Useful fields for Lyna:
- team.member_count:
  - Number of members in each team (workspace size).
- team.open_tasks_count, team.done_tasks_count:
  - Immediate view of workload and progress for each team.
- todo.status, todo.completed_at:
  - Lets her build personal calendars (when todos were completed).
- team_member.task_count:
  - Shows workload per member (for coloring/overload indicators).

Why this helps Lyna:
- WorkspaceDashboardBuilder can use simple SELECTs to get all metrics she needs.
- No need for complex JOIN + GROUP BY in Java for each dashboard.
- Trigger-maintained aggregates keep the workspace views in sync with the actual data.


6. CROSS-CUTTING TRIGGERS (USEFUL TO EVERYONE)
==============================================

These triggers are not tied to one person; they help all modules during development
and debugging.

6.1 Debug logging trigger (RAISE NOTICE)
----------------------------------------

Trigger:
- Tables: app_user, team, team_member, task, todo, todo_item (or a subset)
- When: AFTER INSERT OR UPDATE

What it does:
- Uses RAISE NOTICE to print:
  - Table name
  - Operation type (INSERT, UPDATE)
  - Current DB user
  - JSON version of OLD and/or NEW row
- Example output:
  DBG 2025-11-19: INSERT on app_user by postgres -> NEW = {...}

Why this is useful:
- Instant feedback on what is happening in the DB when you run tests.
- Helps debug trigger logic (e.g. member_count changes, task_count changes).
- Can be disabled or dropped later for production to reduce noise.

6.2 Audit log trigger
---------------------

Trigger:
- audit_log table plus triggers on app_user, team, team_member, task, todo, todo_item
- When: AFTER INSERT OR UPDATE OR DELETE

What it does:
- Inserts a row into audit_log with:
  - table_name
  - op (INSERT, UPDATE, DELETE)
  - changed_at
  - db_user
  - old_row (JSONB)
  - new_row (JSONB)

Why this is useful:
- Gives a full history of changes across modules.
- Very helpful for debugging complicated flows without adding logs in every DAO or service.
- Good base if you later want admin tools that inspect history.


Summary
=======

These triggers are here to:

- Guarantee data consistency (emails, tasks, todos, teams, assignments).
- Enforce business rules (max members per team, max tasks per member, valid task states).
- Pre-aggregate metrics for the workspace (counters, statuses, completion dates).
- Reduce boilerplate and duplication in Java code.

Each team member can focus more on their design patterns and module logic, and rely on
the database to enforce the low-level rules automatically.
