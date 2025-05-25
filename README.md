starter queries

-- GOLD TRANSACTIONS
-- add a course
INSERT INTO Currently_Enrolled
VALUES (perm, enrollment_code, 'N');

-- drop a course
UPDATE Currently_Enrolled
SET dropped='Y'
WHERE perm = 'jajaja' AND enrollment_code = 'ajjaa';

-- list courses enrolled in current quarter
SELECT c.title
FROM Currently_Enrolled ce
JOIN Course_Offerings co ON ce.enrollment_code = co.enrollment_code
JOIN Courses c ON co.course_no = c.course_no
WHERE ce.perm = 'ajjaaj' AND ce.dropped = 'N';

-- list grades for previous quarter (requires calculating what the previous quarter is separately)
SELECT c.title, cc.grade
FROM Completed_Course cc
JOIN Course_Offerings co ON cc.enrollment_code = co.enrollment_code
JOIN Courses c ON co.course_no = c.course_no
WHERE cc.perm = 'jajajaa' AND co.quarter = 'jajaja' AND co.year = 'jajaja';

