-- 1) completed_at ne peut pas être avant start_date
ALTER TABLE task
ADD CONSTRAINT chck_completed_after_start
CHECK (
  completed_at IS NULL
  OR start_date IS NULL
  OR completed_at >= start_date
);

-- 2) completed_at uniquement si status = 'DONE'
ALTER TABLE task
ADD CONSTRAINT chck_completed_only_when_done
CHECK (
  completed_at IS NULL
  OR status = 'DONE'
);
SELECT *
FROM task
WHERE completed_at IS NOT NULL
  AND (
        completed_at < start_date
     OR status <> 'DONE'
  );
