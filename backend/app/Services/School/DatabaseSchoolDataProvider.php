<?php

namespace App\Services\School;

use App\Contracts\SchoolDataProvider;
use App\Models\FeeTransaction;
use App\Models\School;
use App\Models\Staff;
use App\Models\Student;
use App\Models\StaffLeave;
use App\Models\StudentLeave;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;

class DatabaseSchoolDataProvider implements SchoolDataProvider
{
    public function getDashboardStats(?string $date = null): array
    {
        $schoolId = (int) config('school.school_id');
        $attendanceDate = $this->resolveDate($date);

        // Get total staff count from staff table (never changes per day)
        $totalStaff = DB::table('staff')
            ->where('school_id', $schoolId)
            ->where('is_active', true)
            ->count();

        // Get present staff count for this date (only from attendance records)
        $staffPresent = DB::table('staff_attendance')
            ->join('staff', 'staff.id', '=', 'staff_attendance.staff_id')
            ->where('staff.school_id', $schoolId)
            ->where('staff_attendance.attendance_date', $attendanceDate)
            ->where('staff_attendance.is_present', true)
            ->count();

        // Get total students from class totals (never changes per day)
        $totalStudents = DB::table('school_classes')
            ->where('school_id', $schoolId)
            ->where('is_active', true)
            ->sum('total_students');

        // Get present students for this date (only from attendance records)
        $studentsPresent = DB::table('class_attendance')
            ->join('school_classes', 'school_classes.id', '=', 'class_attendance.class_id')
            ->where('school_classes.school_id', $schoolId)
            ->where('class_attendance.attendance_date', $attendanceDate)
            ->sum('class_attendance.present_count');

        $feeStats = DB::table('fee_transactions')
            ->where('school_id', $schoolId)
            ->where('transaction_date', $attendanceDate)
            ->selectRaw('COALESCE(SUM(amount), 0) as total_amount')
            ->selectRaw('COUNT(*) as transaction_count')
            ->first();

        $newAdmissions = DB::table('admissions')
            ->where('school_id', $schoolId)
            ->where('admission_date', $attendanceDate)
            ->count();

        $staffPresent = (int) $staffPresent;
        $totalStaff = (int) $totalStaff;
        $studentsPresent = (int) ($studentsPresent ?? 0);
        $totalStudents = (int) ($totalStudents ?? 0);

        return [
            'date' => $this->formatDisplayDate($date),
            'principalName' => $this->getPrincipalName($schoolId),
            'staffPresent' => $staffPresent,
            'staffAbsent' => max(0, $totalStaff - $staffPresent),
            'totalStaff' => $totalStaff,
            'studentsPresent' => $studentsPresent,
            'studentsAbsent' => max(0, $totalStudents - $studentsPresent),
            'totalStudents' => $totalStudents,
            'feesCollected' => (float) ($feeStats->total_amount ?? 0),
            'feeTransactionCount' => (int) ($feeStats->transaction_count ?? 0),
            'newAdmissions' => $newAdmissions,
        ];
    }

    public function getStaffAttendance(?string $date = null): array
    {
        $schoolId = (int) config('school.school_id');
        $attendanceDate = $this->resolveDate($date);

        // Get all active staff
        $allStaff = DB::table('staff')
            ->where('school_id', $schoolId)
            ->where('is_active', true)
            ->orderBy('name')
            ->get(['id', 'name', 'role'])
            ->toArray();

        // Get attendance records for this date
        $attendanceRecords = DB::table('staff_attendance')
            ->where('attendance_date', $attendanceDate)
            ->whereIn('staff_id', array_column($allStaff, 'id'))
            ->get()
            ->keyBy('staff_id')
            ->toArray();

        // Merge: use attendance data if available, otherwise mark as absent
        $records = array_map(function ($staff) use ($attendanceRecords) {
            $attendance = $attendanceRecords[$staff->id] ?? null;
            return [
                'name' => $staff->name,
                'role' => $staff->role,
                'isPresent' => (bool) ($attendance->is_present ?? false),
                'checkInTime' => $attendance->check_in_time ?? '',
            ];
        }, $allStaff);

        $presentCount = count(array_filter($records, fn ($r) => $r['isPresent']));
        $total = count($records);

        return [
            'records' => $records,
            'presentCount' => $presentCount,
            'absentCount' => max(0, $total - $presentCount),
            'attendancePercentage' => $total > 0 ? round(($presentCount / $total) * 100, 1) : 0.0,
        ];
    }

    public function getStudentAttendance(?string $date = null): array
    {
        $schoolId = (int) config('school.school_id');
        $attendanceDate = $this->resolveDate($date);

        // Get all active classes with their total student counts
        $allClasses = DB::table('school_classes')
            ->where('school_id', $schoolId)
            ->where('is_active', true)
            ->orderBy('name')
            ->get(['id', 'name', 'total_students'])
            ->toArray();

        // Get attendance records for this date
        $attendanceRecords = DB::table('class_attendance')
            ->where('attendance_date', $attendanceDate)
            ->whereIn('class_id', array_column($allClasses, 'id'))
            ->get()
            ->keyBy('class_id')
            ->toArray();

        // Merge: use attendance data if available, otherwise mark all as absent
        $classWise = array_map(function ($class) use ($attendanceRecords) {
            $attendance = $attendanceRecords[$class->id] ?? null;
            return [
                'className' => $class->name,
                'present' => (int) ($attendance->present_count ?? 0),
                'total' => (int) ($attendance->total_count ?? $class->total_students),
            ];
        }, $allClasses);

        $totalPresent = array_sum(array_column($classWise, 'present'));
        $totalStudents = array_sum(array_column($classWise, 'total'));

        return [
            'classWise' => $classWise,
            'totalPresent' => $totalPresent,
            'totalAbsent' => max(0, $totalStudents - $totalPresent),
            'attendancePercentage' => $totalStudents > 0
                ? round(($totalPresent / $totalStudents) * 100, 1)
                : 0.0,
        ];
    }

    public function getFeeTransactions(?string $date = null): array
    {
        $schoolId = (int) config('school.school_id');
        $attendanceDate = $this->resolveDate($date);

        return DB::table('fee_transactions')
            ->where('school_id', $schoolId)
            ->where('transaction_date', $attendanceDate)
            ->orderBy('transaction_time')
            ->get()
            ->map(fn ($row) => [
                'studentName' => $row->student_name,
                'className' => $row->class_name,
                'amount' => (float) $row->amount,
                'time' => $row->transaction_time,
                'type' => $row->fee_type,
            ])
            ->values()
            ->all();
    }

    public function getAdmissions(?string $date = null): array
    {
        $schoolId = (int) config('school.school_id');
        $attendanceDate = $this->resolveDate($date);

        return DB::table('admissions')
            ->where('school_id', $schoolId)
            ->where('admission_date', $attendanceDate)
            ->orderBy('admission_time')
            ->get()
            ->map(fn ($row) => [
                'studentName' => $row->student_name,
                'className' => $row->class_name,
                'enrollmentNo' => $row->enrollment_no,
                'time' => $row->admission_time,
                'parentName' => $row->parent_name,
            ])
            ->values()
            ->all();
    }

    private function resolveDate(?string $date): string
    {
        $carbon = $date
            ? Carbon::parse($date, config('school.timezone'))
            : Carbon::now(config('school.timezone'));

        return $carbon->toDateString();
    }

    private function getPrincipalName(int $schoolId): string
    {
        return DB::table('schools')
            ->where('id', $schoolId)
            ->value('principal_name') ?: config('school.principal_name');
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
        $schoolId = (int) config('school.school_id');
        $today = Carbon::today(config('school.timezone'))->toDateString();
        return StaffLeave::where('school_id', $schoolId)
            ->where(function($query) use ($today) {
                $query->where('status', 'pending')
                      ->orWhere('leave_date', '>=', $today);
            })
            ->orderByDesc('id')
            ->get()
            ->map(fn($leave) => [
                'id' => $leave->id,
                'staffName' => $leave->staff_name,
                'leaveDate' => Carbon::parse($leave->leave_date)->format('Y-m-d'),
                'leaveType' => $leave->leave_type,
                'reason' => $leave->reason,
                'status' => $leave->status,
            ])
            ->toArray();
    }

    public function getStudentLeaves(): array
    {
        $schoolId = (int) config('school.school_id');
        $today = Carbon::today(config('school.timezone'))->toDateString();
        return StudentLeave::where('school_id', $schoolId)
            ->where(function($query) use ($today) {
                $query->where('status', 'pending')
                      ->orWhere('leave_date', '>=', $today);
            })
            ->orderByDesc('id')
            ->get()
            ->map(fn($leave) => [
                'id' => $leave->id,
                'studentName' => $leave->student_name,
                'className' => $leave->class_name,
                'leaveDate' => Carbon::parse($leave->leave_date)->format('Y-m-d'),
                'leaveType' => $leave->leave_type,
                'reason' => $leave->reason,
                'status' => $leave->status,
            ])
            ->toArray();
    }

    public function updateStaffLeaveStatus(int $id, string $status): void
    {
        $schoolId = (int) config('school.school_id');
        StaffLeave::where('school_id', $schoolId)
            ->where('id', $id)
            ->update(['status' => $status]);
    }

    public function updateStudentLeaveStatus(int $id, string $status): void
    {
        $schoolId = (int) config('school.school_id');
        StudentLeave::where('school_id', $schoolId)
            ->where('id', $id)
            ->update(['status' => $status]);
    }
}
