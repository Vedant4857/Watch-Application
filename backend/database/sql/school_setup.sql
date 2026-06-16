-- =============================================================================
-- Shekhar Public School — Personal MySQL Setup
-- Principal: Vedant Shekhar
--
-- Run this in MySQL Workbench, phpMyAdmin, or terminal:
--   mysql -u root -p < database/sql/school_setup.sql
--
-- Or copy-paste sections into your MySQL client.
-- =============================================================================

-- Step 1: Create database (change name/password as you like)
CREATE DATABASE IF NOT EXISTS school_erp_watch
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE school_erp_watch;

-- Step 2: Create tables
CREATE TABLE IF NOT EXISTS schools (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    principal_name VARCHAR(255) NOT NULL,
    timezone VARCHAR(64) DEFAULT 'Asia/Kolkata',
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS staff (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS staff_attendance (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT UNSIGNED NOT NULL,
    attendance_date DATE NOT NULL,
    is_present TINYINT(1) DEFAULT 0,
    check_in_time VARCHAR(32) DEFAULT '',
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    UNIQUE KEY staff_date_unique (staff_id, attendance_date),
    FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS school_classes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS class_attendance (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT UNSIGNED NOT NULL,
    attendance_date DATE NOT NULL,
    present_count INT UNSIGNED DEFAULT 0,
    total_count INT UNSIGNED DEFAULT 0,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    UNIQUE KEY class_date_unique (class_id, attendance_date),
    FOREIGN KEY (class_id) REFERENCES school_classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS fee_transactions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT UNSIGNED NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(64) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    fee_type VARCHAR(64) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_time VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admissions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT UNSIGNED NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(64) NOT NULL,
    admission_number VARCHAR(64) NOT NULL,
    parent_name VARCHAR(255) NOT NULL,
    admission_date DATE NOT NULL,
    admission_time VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

-- Step 3: Clear old data (safe to re-run)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE admissions;
TRUNCATE TABLE fee_transactions;
TRUNCATE TABLE class_attendance;
TRUNCATE TABLE school_classes;
TRUNCATE TABLE staff_attendance;
TRUNCATE TABLE staff;
TRUNCATE TABLE schools;
SET FOREIGN_KEY_CHECKS = 1;

-- Step 4: Insert your school
INSERT INTO schools (id, name, principal_name, timezone, created_at, updated_at)
VALUES (1, 'Shekhar Public School', 'Vedant Shekhar', 'Asia/Kolkata', NOW(), NOW());

-- Step 5: Staff members
INSERT INTO staff (school_id, name, role, is_active, created_at, updated_at) VALUES
(1, 'Priya Mehta',   'Mathematics', 1, NOW(), NOW()),
(1, 'Amit Verma',    'Science',     1, NOW(), NOW()),
(1, 'Sunita Rao',    'English',     1, NOW(), NOW()),
(1, 'Rahul Gupta',   'History',     1, NOW(), NOW()),
(1, 'Kavita Singh',  'Geography',   1, NOW(), NOW()),
(1, 'Deepak Joshi',  'Physics',     1, NOW(), NOW()),
(1, 'Anita Patel',   'Chemistry',   1, NOW(), NOW()),
(1, 'Suresh Kumar',  'Biology',     1, NOW(), NOW()),
(1, 'Neha Sharma',   'Computer',    1, NOW(), NOW()),
(1, 'Vikram Das',    'PE',          1, NOW(), NOW()),
(1, 'Ritu Agarwal',  'Art',         1, NOW(), NOW()),
(1, 'Manish Tiwari', 'Music',       1, NOW(), NOW()),
(1, 'Pooja Nair',    'Hindi',       1, NOW(), NOW()),
(1, 'Arun Mishra',   'Sanskrit',    1, NOW(), NOW()),
(1, 'Shweta Bansal', 'Economics',   1, NOW(), NOW());

-- Step 6: Today's staff attendance (uses CURDATE() = today when you run this)
INSERT INTO staff_attendance (staff_id, attendance_date, is_present, check_in_time, created_at, updated_at)
SELECT id, CURDATE(),
    CASE name
        WHEN 'Sunita Rao'    THEN 0
        WHEN 'Anita Patel'   THEN 0
        WHEN 'Manish Tiwari' THEN 0
        ELSE 1
    END,
    CASE name
        WHEN 'Priya Mehta'   THEN '8:12 AM'
        WHEN 'Amit Verma'    THEN '8:05 AM'
        WHEN 'Sunita Rao'    THEN ''
        WHEN 'Rahul Gupta'   THEN '8:20 AM'
        WHEN 'Kavita Singh'  THEN '8:08 AM'
        WHEN 'Deepak Joshi'  THEN '8:15 AM'
        WHEN 'Anita Patel'   THEN ''
        WHEN 'Suresh Kumar'  THEN '8:02 AM'
        WHEN 'Neha Sharma'   THEN '8:30 AM'
        WHEN 'Vikram Das'    THEN '7:55 AM'
        WHEN 'Ritu Agarwal'  THEN '8:10 AM'
        WHEN 'Manish Tiwari' THEN ''
        WHEN 'Pooja Nair'    THEN '8:18 AM'
        WHEN 'Arun Mishra'   THEN '8:22 AM'
        WHEN 'Shweta Bansal' THEN '8:07 AM'
    END,
    NOW(), NOW()
FROM staff WHERE school_id = 1;

-- Step 7: Classes
INSERT INTO school_classes (school_id, name, created_at, updated_at) VALUES
(1, 'Grade 1',  NOW(), NOW()),
(1, 'Grade 2',  NOW(), NOW()),
(1, 'Grade 3',  NOW(), NOW()),
(1, 'Grade 4',  NOW(), NOW()),
(1, 'Grade 5',  NOW(), NOW()),
(1, 'Grade 6',  NOW(), NOW()),
(1, 'Grade 7',  NOW(), NOW()),
(1, 'Grade 8',  NOW(), NOW()),
(1, 'Grade 9',  NOW(), NOW()),
(1, 'Grade 10', NOW(), NOW()),
(1, 'Grade 11', NOW(), NOW()),
(1, 'Grade 12', NOW(), NOW());

-- Step 8: Today's class attendance
INSERT INTO class_attendance (class_id, attendance_date, present_count, total_count, created_at, updated_at)
SELECT sc.id, CURDATE(), ca.present_count, ca.total_count, NOW(), NOW()
FROM school_classes sc
JOIN (
    SELECT 'Grade 1'  AS name, 28 AS present_count, 30 AS total_count UNION ALL
    SELECT 'Grade 2',  25, 28 UNION ALL
    SELECT 'Grade 3',  32, 35 UNION ALL
    SELECT 'Grade 4',  29, 32 UNION ALL
    SELECT 'Grade 5',  30, 33 UNION ALL
    SELECT 'Grade 6',  27, 30 UNION ALL
    SELECT 'Grade 7',  26, 30 UNION ALL
    SELECT 'Grade 8',  24, 28 UNION ALL
    SELECT 'Grade 9',  22, 25 UNION ALL
    SELECT 'Grade 10', 20, 24 UNION ALL
    SELECT 'Grade 11', 18, 22 UNION ALL
    SELECT 'Grade 12', 15, 20
) ca ON ca.name = sc.name
WHERE sc.school_id = 1;

-- Step 9: Today's fee transactions
INSERT INTO fee_transactions (school_id, student_name, class_name, amount, fee_type, transaction_date, transaction_time, created_at, updated_at) VALUES
(1, 'Aryan Kapoor',  'Grade 10-A', 12500.00, 'Tuition',   CURDATE(), '8:30 AM',  NOW(), NOW()),
(1, 'Prisha Sharma', 'Grade 8-B',   8500.00,  'Tuition',   CURDATE(), '9:15 AM',  NOW(), NOW()),
(1, 'Rohan Mehta',   'Grade 6-A',  15000.00, 'Annual',    CURDATE(), '9:45 AM',  NOW(), NOW()),
(1, 'Isha Patel',    'Grade 9-C',   3200.00,  'Transport', CURDATE(), '10:10 AM', NOW(), NOW()),
(1, 'Dev Gupta',     'Grade 11-A', 11000.00, 'Tuition',   CURDATE(), '10:30 AM', NOW(), NOW()),
(1, 'Aanya Singh',   'Grade 7-B',   5500.00,  'Library',   CURDATE(), '11:00 AM', NOW(), NOW()),
(1, 'Kabir Verma',   'Grade 12-A', 14500.00, 'Tuition',   CURDATE(), '11:20 AM', NOW(), NOW()),
(1, 'Myra Joshi',    'Grade 5-A',   9800.00,  'Tuition',   CURDATE(), '11:45 AM', NOW(), NOW()),
(1, 'Vihaan Das',    'Grade 3-B',   7200.00,  'Tuition',   CURDATE(), '12:00 PM', NOW(), NOW()),
(1, 'Siya Kumar',    'Grade 4-A',   4800.00,  'Transport', CURDATE(), '12:30 PM', NOW(), NOW()),
(1, 'Advait Rao',    'Grade 2-A',   6500.00,  'Tuition',   CURDATE(), '1:00 PM',  NOW(), NOW()),
(1, 'Anvi Nair',     'Grade 1-B',   5000.00,  'Tuition',   CURDATE(), '1:20 PM',  NOW(), NOW());

-- Step 10: Today's new admissions
INSERT INTO admissions (school_id, student_name, class_name, admission_number, parent_name, admission_date, admission_time, created_at, updated_at) VALUES
(1, 'Tanisha Bose',    'Grade 6-A',  'ADM-2026-1089', 'Mr. Subroto Bose',    CURDATE(), '8:45 AM',  NOW(), NOW()),
(1, 'Kiran Reddy',     'Grade 4-B',  'ADM-2026-1090', 'Mrs. Lakshmi Reddy',  CURDATE(), '9:30 AM',  NOW(), NOW()),
(1, 'Arjun Mishra',    'Grade 9-A',  'ADM-2026-1091', 'Mr. Arun Mishra',     CURDATE(), '10:20 AM', NOW(), NOW()),
(1, 'Diya Malhotra',   'Grade 2-A',  'ADM-2026-1092', 'Mrs. Rekha Malhotra', CURDATE(), '11:10 AM', NOW(), NOW()),
(1, 'Siddharth Roy',   'Grade 11-B', 'ADM-2026-1093', 'Mr. Bijoy Roy',       CURDATE(), '12:05 PM', NOW(), NOW());

-- =============================================================================
-- USEFUL QUERIES — edit and run these anytime
-- =============================================================================

-- Change school name / principal
-- UPDATE schools SET name = 'My School Name', principal_name = 'Vedant Shekhar' WHERE id = 1;

-- Add a new teacher
-- INSERT INTO staff (school_id, name, role, is_active, created_at, updated_at)
-- VALUES (1, 'New Teacher', 'Biology', 1, NOW(), NOW());

-- Mark teacher present today
-- INSERT INTO staff_attendance (staff_id, attendance_date, is_present, check_in_time, created_at, updated_at)
-- VALUES (LAST_INSERT_ID(), CURDATE(), 1, '8:00 AM', NOW(), NOW());

-- Add a fee payment today
-- INSERT INTO fee_transactions (school_id, student_name, class_name, amount, fee_type, transaction_date, transaction_time, created_at, updated_at)
-- VALUES (1, 'Student Name', 'Grade 5-A', 5000.00, 'Tuition', CURDATE(), '10:00 AM', NOW(), NOW());

-- Add a new admission today
-- INSERT INTO admissions (school_id, student_name, class_name, admission_number, parent_name, admission_date, admission_time, created_at, updated_at)
-- VALUES (1, 'New Student', 'Grade 3-A', 'ADM-2026-1100', 'Parent Name', CURDATE(), '9:00 AM', NOW(), NOW());

-- View dashboard summary for today
-- SELECT
--   (SELECT COUNT(*) FROM staff_attendance sa JOIN staff s ON s.id = sa.staff_id WHERE s.school_id = 1 AND sa.attendance_date = CURDATE() AND sa.is_present = 1) AS staff_present,
--   (SELECT SUM(present_count) FROM class_attendance ca JOIN school_classes sc ON sc.id = ca.class_id WHERE sc.school_id = 1 AND ca.attendance_date = CURDATE()) AS students_present,
--   (SELECT COALESCE(SUM(amount), 0) FROM fee_transactions WHERE school_id = 1 AND transaction_date = CURDATE()) AS fees_collected,
--   (SELECT COUNT(*) FROM admissions WHERE school_id = 1 AND admission_date = CURDATE()) AS new_admissions;
