<?php

namespace App\Services;

use App\Contracts\SchoolDataProvider;

class SchoolDataService
{
    public function __construct(
        private readonly SchoolDataProvider $provider,
    ) {}

    public function getDashboardStats(?string $date = null): array
    {
        return $this->provider->getDashboardStats($date);
    }

    public function getStaffAttendance(?string $date = null): array
    {
        return $this->provider->getStaffAttendance($date);
    }

    public function getStudentAttendance(?string $date = null): array
    {
        return $this->provider->getStudentAttendance($date);
    }

    public function getFeeTransactions(?string $date = null): array
    {
        return $this->provider->getFeeTransactions($date);
    }

    public function getAdmissions(?string $date = null): array
    {
        return $this->provider->getAdmissions($date);
    }

    public function getStaffLeaves(): array
    {
        return $this->provider->getStaffLeaves();
    }

    public function getStudentLeaves(): array
    {
        return $this->provider->getStudentLeaves();
    }

    public function updateStaffLeaveStatus(int $id, string $status): void
    {
        $this->provider->updateStaffLeaveStatus($id, $status);
    }

    public function updateStudentLeaveStatus(int $id, string $status): void
    {
        $this->provider->updateStudentLeaveStatus($id, $status);
    }

    public function getUpcomingEvents(): array
    {
        return $this->provider->getUpcomingEvents();
    }
}
