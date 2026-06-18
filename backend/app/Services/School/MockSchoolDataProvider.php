<?php

namespace App\Services\School;

use App\Contracts\SchoolDataProvider;
use Carbon\Carbon;

class MockSchoolDataProvider implements SchoolDataProvider
{
    /** @var list<array{name: string, role: string, isPresent: bool, checkInTime: string}> */
    private array $staffList = [
        ['name' => 'Priya Mehta', 'role' => 'Mathematics', 'isPresent' => true, 'checkInTime' => '8:12 AM'],
        ['name' => 'Amit Verma', 'role' => 'Science', 'isPresent' => true, 'checkInTime' => '8:05 AM'],
        ['name' => 'Sunita Rao', 'role' => 'English', 'isPresent' => false, 'checkInTime' => ''],
        ['name' => 'Rahul Gupta', 'role' => 'History', 'isPresent' => true, 'checkInTime' => '8:20 AM'],
        ['name' => 'Kavita Singh', 'role' => 'Geography', 'isPresent' => true, 'checkInTime' => '8:08 AM'],
        ['name' => 'Deepak Joshi', 'role' => 'Physics', 'isPresent' => true, 'checkInTime' => '8:15 AM'],
        ['name' => 'Anita Patel', 'role' => 'Chemistry', 'isPresent' => false, 'checkInTime' => ''],
        ['name' => 'Suresh Kumar', 'role' => 'Biology', 'isPresent' => true, 'checkInTime' => '8:02 AM'],
        ['name' => 'Neha Sharma', 'role' => 'Computer', 'isPresent' => true, 'checkInTime' => '8:30 AM'],
        ['name' => 'Vikram Das', 'role' => 'PE', 'isPresent' => true, 'checkInTime' => '7:55 AM'],
        ['name' => 'Ritu Agarwal', 'role' => 'Art', 'isPresent' => true, 'checkInTime' => '8:10 AM'],
        ['name' => 'Manish Tiwari', 'role' => 'Music', 'isPresent' => false, 'checkInTime' => ''],
        ['name' => 'Pooja Nair', 'role' => 'Hindi', 'isPresent' => true, 'checkInTime' => '8:18 AM'],
        ['name' => 'Arun Mishra', 'role' => 'Sanskrit', 'isPresent' => true, 'checkInTime' => '8:22 AM'],
        ['name' => 'Shweta Bansal', 'role' => 'Economics', 'isPresent' => true, 'checkInTime' => '8:07 AM'],
    ];

    /** @var list<array{studentName: string, className: string, amount: float, time: string, type: string}> */
    private array $feeTransactions = [
        ['studentName' => 'Aryan Kapoor', 'className' => 'Grade 10-A', 'amount' => 12500.0, 'time' => '8:30 AM', 'type' => 'Tuition'],
        ['studentName' => 'Prisha Sharma', 'className' => 'Grade 8-B', 'amount' => 8500.0, 'time' => '9:15 AM', 'type' => 'Tuition'],
        ['studentName' => 'Rohan Mehta', 'className' => 'Grade 6-A', 'amount' => 15000.0, 'time' => '9:45 AM', 'type' => 'Annual'],
        ['studentName' => 'Isha Patel', 'className' => 'Grade 9-C', 'amount' => 3200.0, 'time' => '10:10 AM', 'type' => 'Transport'],
        ['studentName' => 'Dev Gupta', 'className' => 'Grade 11-A', 'amount' => 11000.0, 'time' => '10:30 AM', 'type' => 'Tuition'],
        ['studentName' => 'Aanya Singh', 'className' => 'Grade 7-B', 'amount' => 5500.0, 'time' => '11:00 AM', 'type' => 'Library'],
        ['studentName' => 'Kabir Verma', 'className' => 'Grade 12-A', 'amount' => 14500.0, 'time' => '11:20 AM', 'type' => 'Tuition'],
        ['studentName' => 'Myra Joshi', 'className' => 'Grade 5-A', 'amount' => 9800.0, 'time' => '11:45 AM', 'type' => 'Tuition'],
        ['studentName' => 'Vihaan Das', 'className' => 'Grade 3-B', 'amount' => 7200.0, 'time' => '12:00 PM', 'type' => 'Tuition'],
        ['studentName' => 'Siya Kumar', 'className' => 'Grade 4-A', 'amount' => 4800.0, 'time' => '12:30 PM', 'type' => 'Transport'],
        ['studentName' => 'Advait Rao', 'className' => 'Grade 2-A', 'amount' => 6500.0, 'time' => '1:00 PM', 'type' => 'Tuition'],
        ['studentName' => 'Anvi Nair', 'className' => 'Grade 1-B', 'amount' => 5000.0, 'time' => '1:20 PM', 'type' => 'Tuition'],
    ];

    /** @var list<array{studentName: string, className: string, enrollmentNo: string, time: string, parentName: string}> */
    private array $admissions = [
        ['studentName' => 'Tanisha Bose', 'className' => 'Grade 6-A', 'enrollmentNo' => 'ADM-2024-1089', 'time' => '8:45 AM', 'parentName' => 'Mr. Subroto Bose'],
        ['studentName' => 'Kiran Reddy', 'className' => 'Grade 4-B', 'enrollmentNo' => 'ADM-2024-1090', 'time' => '9:30 AM', 'parentName' => 'Mrs. Lakshmi Reddy'],
        ['studentName' => 'Arjun Mishra', 'className' => 'Grade 9-A', 'enrollmentNo' => 'ADM-2024-1091', 'time' => '10:20 AM', 'parentName' => 'Mr. Arun Mishra'],
        ['studentName' => 'Diya Malhotra', 'className' => 'Grade 2-A', 'enrollmentNo' => 'ADM-2024-1092', 'time' => '11:10 AM', 'parentName' => 'Mrs. Rekha Malhotra'],
        ['studentName' => 'Siddharth Roy', 'className' => 'Grade 11-B', 'enrollmentNo' => 'ADM-2024-1093', 'time' => '12:05 PM', 'parentName' => 'Mr. Bijoy Roy'],
    ];

    /** @var list<array{className: string, present: int, total: int}> */
    private array $classAttendance = [
        ['className' => 'Grade 1', 'present' => 28, 'total' => 30],
        ['className' => 'Grade 2', 'present' => 25, 'total' => 28],
        ['className' => 'Grade 3', 'present' => 32, 'total' => 35],
        ['className' => 'Grade 4', 'present' => 29, 'total' => 32],
        ['className' => 'Grade 5', 'present' => 30, 'total' => 33],
        ['className' => 'Grade 6', 'present' => 27, 'total' => 30],
        ['className' => 'Grade 7', 'present' => 26, 'total' => 30],
        ['className' => 'Grade 8', 'present' => 24, 'total' => 28],
        ['className' => 'Grade 9', 'present' => 22, 'total' => 25],
        ['className' => 'Grade 10', 'present' => 20, 'total' => 24],
        ['className' => 'Grade 11', 'present' => 18, 'total' => 22],
        ['className' => 'Grade 12', 'present' => 15, 'total' => 20],
    ];

    public function getDashboardStats(?string $date = null): array
    {
        $staffPresent = count(array_filter($this->staffList, fn ($s) => $s['isPresent']));
        $staffAbsent = count($this->staffList) - $staffPresent;
        $studentsPresent = array_sum(array_column($this->classAttendance, 'present'));
        $totalStudents = array_sum(array_column($this->classAttendance, 'total'));
        $studentsAbsent = $totalStudents - $studentsPresent;
        $feesCollected = array_sum(array_column($this->feeTransactions, 'amount'));

        return [
            'date' => $this->formatDisplayDate($date),
            'principalName' => config('school.principal_name'),
            'staffPresent' => $staffPresent,
            'staffAbsent' => $staffAbsent,
            'totalStaff' => count($this->staffList),
            'studentsPresent' => $studentsPresent,
            'studentsAbsent' => $studentsAbsent,
            'totalStudents' => $totalStudents,
            'feesCollected' => (float) $feesCollected,
            'feeTransactionCount' => count($this->feeTransactions),
            'newAdmissions' => count($this->admissions),
        ];
    }

    public function getStaffAttendance(?string $date = null): array
    {
        $presentCount = count(array_filter($this->staffList, fn ($s) => $s['isPresent']));
        $absentCount = count($this->staffList) - $presentCount;
        $total = count($this->staffList);

        return [
            'records' => $this->staffList,
            'presentCount' => $presentCount,
            'absentCount' => $absentCount,
            'attendancePercentage' => $total > 0 ? round(($presentCount / $total) * 100, 1) : 0.0,
        ];
    }

    public function getStudentAttendance(?string $date = null): array
    {
        $totalPresent = array_sum(array_column($this->classAttendance, 'present'));
        $totalStudents = array_sum(array_column($this->classAttendance, 'total'));
        $totalAbsent = $totalStudents - $totalPresent;

        return [
            'classWise' => $this->classAttendance,
            'totalPresent' => $totalPresent,
            'totalAbsent' => $totalAbsent,
            'attendancePercentage' => $totalStudents > 0
                ? round(($totalPresent / $totalStudents) * 100, 1)
                : 0.0,
        ];
    }

    public function getFeeTransactions(?string $date = null): array
    {
        return $this->feeTransactions;
    }

    public function getAdmissions(?string $date = null): array
    {
        return $this->admissions;
    }

    private function formatDisplayDate(?string $date): string
    {
        $carbon = $date
            ? Carbon::parse($date, config('school.timezone'))
            : Carbon::now(config('school.timezone'));

        return $carbon->format('d M Y');
    }

    public function getStaffLeaves(): array
    {
        return [
            [
                'id' => 1,
                'staffName' => 'Amit Verma',
                'leaveDate' => '2026-06-18',
                'leaveType' => 'Sick Leave',
                'reason' => 'Fever and cold',
                'status' => 'pending',
            ],
            [
                'id' => 2,
                'staffName' => 'Sunita Rao',
                'leaveDate' => '2026-06-19',
                'leaveType' => 'Personal Leave',
                'reason' => 'Family function',
                'status' => 'approved',
            ]
        ];
    }

    public function getStudentLeaves(): array
    {
        return [
            [
                'id' => 1,
                'studentName' => 'Aryan Kapoor',
                'className' => 'Grade 10-A',
                'leaveDate' => '2026-06-18',
                'leaveType' => 'Sick Leave',
                'reason' => 'Doctor appointment',
                'status' => 'pending',
            ]
        ];
    }

    public function updateStaffLeaveStatus(int $id, string $status): void
    {
        // Mock no-op
    }

    public function updateStudentLeaveStatus(int $id, string $status): void
    {
        // Mock no-op
    }
}
