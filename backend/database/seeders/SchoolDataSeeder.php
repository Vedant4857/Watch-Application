<?php

namespace Database\Seeders;

use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class SchoolDataSeeder extends Seeder
{
    public function run(): void
    {
        $timezone = config('school.timezone', 'Asia/Kolkata');
        $today = Carbon::now($timezone)->toDateString();
        $now = Carbon::now($timezone);

        foreach ([
            'admissions',
            'fee_transactions',
            'class_attendance',
            'school_classes',
            'staff_attendance',
            'staff',
        ] as $table) {
            DB::table($table)->delete();
        }

        DB::table('schools')->updateOrInsert(['id' => 1], [
            'id' => 1,
            'name' => config('school.name', 'Shekhar Public School'),
            'principal_name' => config('school.principal_name', 'Vedant Shekhar'),
            'timezone' => $timezone,
            'created_at' => $now,
            'updated_at' => $now,
        ]);

        $staff = [
            ['name' => 'Priya Mehta', 'role' => 'Mathematics', 'is_present' => true, 'check_in_time' => '8:12 AM'],
            ['name' => 'Amit Verma', 'role' => 'Science', 'is_present' => true, 'check_in_time' => '8:05 AM'],
            ['name' => 'Sunita Rao', 'role' => 'English', 'is_present' => false, 'check_in_time' => ''],
            ['name' => 'Rahul Gupta', 'role' => 'History', 'is_present' => true, 'check_in_time' => '8:20 AM'],
            ['name' => 'Kavita Singh', 'role' => 'Geography', 'is_present' => true, 'check_in_time' => '8:08 AM'],
            ['name' => 'Deepak Joshi', 'role' => 'Physics', 'is_present' => true, 'check_in_time' => '8:15 AM'],
            ['name' => 'Anita Patel', 'role' => 'Chemistry', 'is_present' => false, 'check_in_time' => ''],
            ['name' => 'Suresh Kumar', 'role' => 'Biology', 'is_present' => true, 'check_in_time' => '8:02 AM'],
            ['name' => 'Neha Sharma', 'role' => 'Computer', 'is_present' => true, 'check_in_time' => '8:30 AM'],
            ['name' => 'Vikram Das', 'role' => 'PE', 'is_present' => true, 'check_in_time' => '7:55 AM'],
            ['name' => 'Ritu Agarwal', 'role' => 'Art', 'is_present' => true, 'check_in_time' => '8:10 AM'],
            ['name' => 'Manish Tiwari', 'role' => 'Music', 'is_present' => false, 'check_in_time' => ''],
            ['name' => 'Pooja Nair', 'role' => 'Hindi', 'is_present' => true, 'check_in_time' => '8:18 AM'],
            ['name' => 'Arun Mishra', 'role' => 'Sanskrit', 'is_present' => true, 'check_in_time' => '8:22 AM'],
            ['name' => 'Shweta Bansal', 'role' => 'Economics', 'is_present' => true, 'check_in_time' => '8:07 AM'],
        ];

        foreach ($staff as $member) {
            $staffId = DB::table('staff')->insertGetId([
                'school_id' => 1,
                'name' => $member['name'],
                'role' => $member['role'],
                'is_active' => true,
                'created_at' => $now,
                'updated_at' => $now,
            ]);

            DB::table('staff_attendance')->insert([
                'staff_id' => $staffId,
                'attendance_date' => $today,
                'is_present' => $member['is_present'],
                'check_in_time' => $member['check_in_time'],
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }

        $classes = [
            ['name' => 'Grade 1', 'present' => 28, 'total' => 30],
            ['name' => 'Grade 2', 'present' => 25, 'total' => 28],
            ['name' => 'Grade 3', 'present' => 32, 'total' => 35],
            ['name' => 'Grade 4', 'present' => 29, 'total' => 32],
            ['name' => 'Grade 5', 'present' => 30, 'total' => 33],
            ['name' => 'Grade 6', 'present' => 27, 'total' => 30],
            ['name' => 'Grade 7', 'present' => 26, 'total' => 30],
            ['name' => 'Grade 8', 'present' => 24, 'total' => 28],
            ['name' => 'Grade 9', 'present' => 22, 'total' => 25],
            ['name' => 'Grade 10', 'present' => 20, 'total' => 24],
            ['name' => 'Grade 11', 'present' => 18, 'total' => 22],
            ['name' => 'Grade 12', 'present' => 15, 'total' => 20],
        ];

        foreach ($classes as $class) {
            $classId = DB::table('school_classes')->insertGetId([
                'school_id' => 1,
                'name' => $class['name'],
                'total_students' => $class['total'],
                'is_active' => true,
                'created_at' => $now,
                'updated_at' => $now,
            ]);

            DB::table('class_attendance')->insert([
                'class_id' => $classId,
                'attendance_date' => $today,
                'present_count' => $class['present'],
                'total_count' => $class['total'],
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }

        $fees = [
            ['student_name' => 'Aryan Kapoor', 'class_name' => 'Grade 10-A', 'amount' => 12500, 'fee_type' => 'Tuition', 'transaction_time' => '8:30 AM'],
            ['student_name' => 'Prisha Sharma', 'class_name' => 'Grade 8-B', 'amount' => 8500, 'fee_type' => 'Tuition', 'transaction_time' => '9:15 AM'],
            ['student_name' => 'Rohan Mehta', 'class_name' => 'Grade 6-A', 'amount' => 15000, 'fee_type' => 'Annual', 'transaction_time' => '9:45 AM'],
            ['student_name' => 'Isha Patel', 'class_name' => 'Grade 9-C', 'amount' => 3200, 'fee_type' => 'Transport', 'transaction_time' => '10:10 AM'],
            ['student_name' => 'Dev Gupta', 'class_name' => 'Grade 11-A', 'amount' => 11000, 'fee_type' => 'Tuition', 'transaction_time' => '10:30 AM'],
            ['student_name' => 'Aanya Singh', 'class_name' => 'Grade 7-B', 'amount' => 5500, 'fee_type' => 'Library', 'transaction_time' => '11:00 AM'],
            ['student_name' => 'Kabir Verma', 'class_name' => 'Grade 12-A', 'amount' => 14500, 'fee_type' => 'Tuition', 'transaction_time' => '11:20 AM'],
            ['student_name' => 'Myra Joshi', 'class_name' => 'Grade 5-A', 'amount' => 9800, 'fee_type' => 'Tuition', 'transaction_time' => '11:45 AM'],
            ['student_name' => 'Vihaan Das', 'class_name' => 'Grade 3-B', 'amount' => 7200, 'fee_type' => 'Tuition', 'transaction_time' => '12:00 PM'],
            ['student_name' => 'Siya Kumar', 'class_name' => 'Grade 4-A', 'amount' => 4800, 'fee_type' => 'Transport', 'transaction_time' => '12:30 PM'],
            ['student_name' => 'Advait Rao', 'class_name' => 'Grade 2-A', 'amount' => 6500, 'fee_type' => 'Tuition', 'transaction_time' => '1:00 PM'],
            ['student_name' => 'Anvi Nair', 'class_name' => 'Grade 1-B', 'amount' => 5000, 'fee_type' => 'Tuition', 'transaction_time' => '1:20 PM'],
        ];

        foreach ($fees as $fee) {
            DB::table('fee_transactions')->insert([
                'school_id' => 1,
                'student_name' => $fee['student_name'],
                'class_name' => $fee['class_name'],
                'amount' => $fee['amount'],
                'fee_type' => $fee['fee_type'],
                'transaction_date' => $today,
                'transaction_time' => $fee['transaction_time'],
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }

        $admissions = [
            ['student_name' => 'Tanisha Bose', 'class_name' => 'Grade 6-A', 'admission_number' => 'ADM-2026-1089', 'parent_name' => 'Mr. Subroto Bose', 'admission_time' => '8:45 AM'],
            ['student_name' => 'Kiran Reddy', 'class_name' => 'Grade 4-B', 'admission_number' => 'ADM-2026-1090', 'parent_name' => 'Mrs. Lakshmi Reddy', 'admission_time' => '9:30 AM'],
            ['student_name' => 'Arjun Mishra', 'class_name' => 'Grade 9-A', 'admission_number' => 'ADM-2026-1091', 'parent_name' => 'Mr. Arun Mishra', 'admission_time' => '10:20 AM'],
            ['student_name' => 'Diya Malhotra', 'class_name' => 'Grade 2-A', 'admission_number' => 'ADM-2026-1092', 'parent_name' => 'Mrs. Rekha Malhotra', 'admission_time' => '11:10 AM'],
            ['student_name' => 'Siddharth Roy', 'class_name' => 'Grade 11-B', 'admission_number' => 'ADM-2026-1093', 'parent_name' => 'Mr. Bijoy Roy', 'admission_time' => '12:05 PM'],
        ];

        foreach ($admissions as $admission) {
            DB::table('admissions')->insert([
                'school_id' => 1,
                'student_name' => $admission['student_name'],
                'class_name' => $admission['class_name'],
                'admission_number' => $admission['admission_number'],
                'parent_name' => $admission['parent_name'],
                'admission_date' => $today,
                'admission_time' => $admission['admission_time'],
                'created_at' => $now,
                'updated_at' => $now,
            ]);
        }
    }
}
