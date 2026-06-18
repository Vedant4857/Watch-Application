<?php

namespace App\Contracts;

interface SchoolDataProvider
{
    /**
     * @return array{
     *     date: string,
     *     principalName: string,
     *     staffPresent: int,
     *     staffAbsent: int,
     *     totalStaff: int,
     *     studentsPresent: int,
     *     studentsAbsent: int,
     *     totalStudents: int,
     *     feesCollected: float,
     *     feeTransactionCount: int,
     *     newAdmissions: int
     * }
     */
    public function getDashboardStats(?string $date = null): array;

    /**
     * @return array{
     *     records: list<array{name: string, role: string, isPresent: bool, checkInTime: string}>,
     *     presentCount: int,
     *     absentCount: int,
     *     attendancePercentage: float
     * }
     */
    public function getStaffAttendance(?string $date = null): array;

    /**
     * @return array{
     *     classWise: list<array{className: string, present: int, total: int}>,
     *     totalPresent: int,
     *     totalAbsent: int,
     *     attendancePercentage: float
     * }
     */
    public function getStudentAttendance(?string $date = null): array;

    /**
     * @return list<array{studentName: string, className: string, amount: float, time: string, type: string}>
     */
    public function getFeeTransactions(?string $date = null): array;

    /**
     * @return list<array{studentName: string, className: string, admissionNumber: string, time: string, parentName: string}>
     */
    public function getAdmissions(?string $date = null): array;

    public function getStaffLeaves(): array;

    public function getStudentLeaves(): array;

    public function updateStaffLeaveStatus(int $id, string $status): void;

    public function updateStudentLeaveStatus(int $id, string $status): void;

    public function getUpcomingEvents(): array;
}
