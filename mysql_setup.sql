-- ==============================================================================
-- MySQL Database Setup for School ERP
-- ==============================================================================
-- INSTRUCTIONS FOR BACKEND DEVELOPERS:
-- 1. Run this script in your MySQL server.
-- 2. Build a backend API (Node.js/Python/Java/PHP) that connects to this DB.
-- 3. Expose the data as JSON endpoints.
-- 4. Update the Android Watch app's `ApiConfig.API_BASE_URL` to point to your API.
-- ==============================================================================

-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS school_erp_watch;
USE school_erp_watch;

-- ==============================================================================
-- 2. Create the Tables
-- ==============================================================================

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Staff Table
CREATE TABLE IF NOT EXISTS staff (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    is_present BOOLEAN DEFAULT TRUE,
    check_in_time VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Staff Leaves Table
CREATE TABLE IF NOT EXISTS staff_leaves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    staff_name VARCHAR(255) NOT NULL,
    leave_date DATE NOT NULL,
    leave_type VARCHAR(100) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student Leaves Table
CREATE TABLE IF NOT EXISTS student_leaves (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    leave_date DATE NOT NULL,
    leave_type VARCHAR(100) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 3. Insert Dummy Data (For Testing)
-- ==============================================================================

-- Insert sample students
INSERT INTO students (name, class_name, roll_number) VALUES
('Aryan Kapoor', 'Grade 10-A', 'R-1001'),
('Prisha Sharma', 'Grade 8-B', 'R-8002'),
('Rohan Mehta', 'Grade 6-A', 'R-6003'),
('Isha Patel', 'Grade 9-C', 'R-9004'),
('Dev Gupta', 'Grade 11-A', 'R-11005'),
('Aanya Singh', 'Grade 7-B', 'R-7006'),
('Kabir Verma', 'Grade 12-A', 'R-12007'),
('Myra Joshi', 'Grade 5-A', 'R-5008');

-- Insert sample staff
INSERT INTO staff (name, department, is_present, check_in_time) VALUES
('Suresh Kumar', 'Mathematics', TRUE, '7:45 AM'),
('Anita Desai', 'Science', TRUE, '7:50 AM'),
('Rahul Verma', 'English', FALSE, ''),
('Priya Singh', 'History', TRUE, '8:05 AM'),
('Neha Sharma', 'Computer', TRUE, '8:30 AM'),
('Vikram Das', 'PE', TRUE, '7:55 AM'),
('Manish Tiwari', 'Music', FALSE, '');

-- Insert sample staff leaves
INSERT INTO staff_leaves (staff_name, leave_date, leave_type, reason, status) VALUES
('Amit Verma', '2026-06-18', 'Sick Leave', 'Fever and cold', 'pending'),
('Sunita Rao', '2026-06-19', 'Personal Leave', 'Family function', 'pending'),
('Rajesh Kumar', '2026-06-10', 'Sick Leave', 'Doctor Appointment', 'approved');

-- Insert sample student leaves
INSERT INTO student_leaves (student_name, class_name, leave_date, leave_type, reason, status) VALUES
('Aryan Kapoor', 'Grade 10-A', '2026-06-18', 'Sick Leave', 'Doctor appointment', 'pending'),
('Isha Patel', 'Grade 9-C', '2026-06-20', 'Personal Leave', 'Out of station', 'pending'),
('Kavya Singh', 'Grade 8-B', '2026-06-05', 'Sick Leave', 'Flu', 'disapproved');

-- ==============================================================================
-- HOW THE API SHOULD RESPOND TO THE ANDROID WATCH:
-- ==============================================================================
-- 
-- 1. Endpoint: GET /students
-- Query: SELECT name, class_name AS className, roll_number AS rollNumber FROM students
-- JSON Format Expected by Watch:
-- [
--   { "name": "Aryan Kapoor", "className": "Grade 10-A", "rollNumber": "R-1001" },
--   { "name": "Prisha Sharma", "className": "Grade 8-B", "rollNumber": "R-8002" }
-- ]
-- 
-- 2. Endpoint: GET /staff
-- Query: SELECT name, department, is_present AS isPresent, check_in_time AS checkInTime FROM staff
-- JSON Format Expected by Watch:
-- [
--   { "name": "Suresh Kumar", "department": "Mathematics", "isPresent": true, "checkInTime": "7:45 AM" },
--   { "name": "Rahul Verma", "department": "English", "isPresent": false, "checkInTime": "" }
-- ]
-- ==============================================================================
