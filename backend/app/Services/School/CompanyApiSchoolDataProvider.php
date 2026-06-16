<?php

namespace App\Services\School;

use App\Contracts\SchoolDataProvider;
use Carbon\Carbon;
use Illuminate\Http\Client\RequestException;
use Illuminate\Support\Facades\Http;
use RuntimeException;

/**
 * Connects to your company's existing school ERP API.
 *
 * HOW TO CUSTOMIZE:
 * 1. Set SCHOOL_DATA_SOURCE=company_api in .env
 * 2. Set COMPANY_API_BASE_URL and COMPANY_API_KEY in .env
 * 3. Update endpoint paths in config/school.php (or .env)
 * 4. Edit the transform*() methods below if your API returns different JSON shapes
 *
 * See API_INTEGRATION.md for full step-by-step guide.
 */
class CompanyApiSchoolDataProvider implements SchoolDataProvider
{
    public function getDashboardStats(?string $date = null): array
    {
        $response = $this->fetch('dashboard', $date);

        // If your company API returns a pre-built dashboard, map fields here:
        return [
            'date' => $response['date'] ?? $this->formatDisplayDate($date),
            'principalName' => $response['principalName'] ?? $response['principal_name'] ?? config('school.principal_name'),
            'staffPresent' => (int) ($response['staffPresent'] ?? $response['staff_present'] ?? 0),
            'staffAbsent' => (int) ($response['staffAbsent'] ?? $response['staff_absent'] ?? 0),
            'totalStaff' => (int) ($response['totalStaff'] ?? $response['total_staff'] ?? 0),
            'studentsPresent' => (int) ($response['studentsPresent'] ?? $response['students_present'] ?? 0),
            'studentsAbsent' => (int) ($response['studentsAbsent'] ?? $response['students_absent'] ?? 0),
            'totalStudents' => (int) ($response['totalStudents'] ?? $response['total_students'] ?? 0),
            'feesCollected' => (float) ($response['feesCollected'] ?? $response['fees_collected'] ?? 0),
            'feeTransactionCount' => (int) ($response['feeTransactionCount'] ?? $response['fee_transaction_count'] ?? 0),
            'newAdmissions' => (int) ($response['newAdmissions'] ?? $response['new_admissions'] ?? 0),
        ];
    }

    public function getStaffAttendance(?string $date = null): array
    {
        $response = $this->fetch('staff_attendance', $date);
        $records = $this->transformStaffRecords($response['records'] ?? $response['data'] ?? $response);

        $presentCount = (int) ($response['presentCount'] ?? $response['present_count'] ?? count(array_filter($records, fn ($r) => $r['isPresent'])));
        $absentCount = (int) ($response['absentCount'] ?? $response['absent_count'] ?? (count($records) - $presentCount));
        $total = count($records);

        return [
            'records' => $records,
            'presentCount' => $presentCount,
            'absentCount' => $absentCount,
            'attendancePercentage' => $total > 0
                ? round(($presentCount / $total) * 100, 1)
                : (float) ($response['attendancePercentage'] ?? $response['attendance_percentage'] ?? 0),
        ];
    }

    public function getStudentAttendance(?string $date = null): array
    {
        $response = $this->fetch('student_attendance', $date);
        $classWise = $this->transformClassAttendance($response['classWise'] ?? $response['class_wise'] ?? $response['data'] ?? []);

        $totalPresent = (int) ($response['totalPresent'] ?? $response['total_present'] ?? array_sum(array_column($classWise, 'present')));
        $totalAbsent = (int) ($response['totalAbsent'] ?? $response['total_absent'] ?? array_sum(array_map(fn ($c) => $c['total'] - $c['present'], $classWise)));
        $totalStudents = array_sum(array_column($classWise, 'total'));

        return [
            'classWise' => $classWise,
            'totalPresent' => $totalPresent,
            'totalAbsent' => $totalAbsent,
            'attendancePercentage' => $totalStudents > 0
                ? round(($totalPresent / $totalStudents) * 100, 1)
                : (float) ($response['attendancePercentage'] ?? $response['attendance_percentage'] ?? 0),
        ];
    }

    public function getFeeTransactions(?string $date = null): array
    {
        $response = $this->fetch('fee_transactions', $date);
        $items = $response['data'] ?? $response['transactions'] ?? $response;

        if (! is_array($items)) {
            return [];
        }

        return array_map(fn ($item) => $this->transformFeeTransaction($item), $items);
    }

    public function getAdmissions(?string $date = null): array
    {
        $response = $this->fetch('admissions', $date);
        $items = $response['data'] ?? $response['admissions'] ?? $response;

        if (! is_array($items)) {
            return [];
        }

        return array_map(fn ($item) => $this->transformAdmission($item), $items);
    }

    /**
     * Makes the HTTP call to your company API.
     * Customize headers/auth here if your API uses Bearer tokens, OAuth, etc.
     */
    private function fetch(string $endpointKey, ?string $date): array
    {
        $config = config('school.company_api');
        $path = $config['endpoints'][$endpointKey] ?? throw new RuntimeException("Missing endpoint config: {$endpointKey}");

        $query = array_filter([
            'date' => $date ?? Carbon::now(config('school.timezone'))->toDateString(),
            'school_id' => config('school.school_id'),
        ]);

        try {
            $request = Http::timeout($config['timeout'])
                ->acceptJson()
                ->baseUrl(rtrim($config['base_url'], '/'));

            // Common auth patterns — uncomment/edit what your company uses:
            if (! empty($config['api_key'])) {
                $request = $request->withHeaders([
                    'X-API-Key' => $config['api_key'],
                    // 'Authorization' => 'Bearer '.$config['api_key'],
                ]);
            }

            $response = $request->get($path, $query);

            if ($response->failed()) {
                throw new RequestException($response);
            }

            return $response->json() ?? [];
        } catch (RequestException $e) {
            throw new RuntimeException(
                "Company API error [{$endpointKey}]: ".$e->response?->body(),
                $e->response?->status() ?? 502,
                $e
            );
        }
    }

    /** @param  mixed  $records */
    private function transformStaffRecords($records): array
    {
        if (! is_array($records)) {
            return [];
        }

        $map = config('school.company_api.field_map.staff');

        return array_values(array_map(function ($item) use ($map) {
            if (! is_array($item)) {
                return ['name' => '', 'role' => '', 'isPresent' => false, 'checkInTime' => ''];
            }

            return [
                'name' => (string) ($item[$map['name']] ?? $item['name'] ?? $item['staff_name'] ?? ''),
                'role' => (string) ($item[$map['role']] ?? $item['role'] ?? $item['subject'] ?? ''),
                'isPresent' => (bool) ($item[$map['is_present']] ?? $item['isPresent'] ?? $item['is_present'] ?? $item['present'] ?? false),
                'checkInTime' => (string) ($item[$map['check_in_time']] ?? $item['checkInTime'] ?? $item['check_in_time'] ?? $item['check_in'] ?? ''),
            ];
        }, $records));
    }

    /** @param  mixed  $items */
    private function transformClassAttendance($items): array
    {
        if (! is_array($items)) {
            return [];
        }

        $map = config('school.company_api.field_map.class_attendance');

        return array_values(array_map(function ($item) use ($map) {
            if (! is_array($item)) {
                return ['className' => '', 'present' => 0, 'total' => 0];
            }

            return [
                'className' => (string) ($item[$map['class_name']] ?? $item['className'] ?? $item['class_name'] ?? $item['grade'] ?? ''),
                'present' => (int) ($item[$map['present']] ?? $item['present'] ?? 0),
                'total' => (int) ($item[$map['total']] ?? $item['total'] ?? $item['strength'] ?? 0),
            ];
        }, $items));
    }

    /** @param  array<string, mixed>  $item */
    private function transformFeeTransaction(array $item): array
    {
        $map = config('school.company_api.field_map.fee');

        return [
            'studentName' => (string) ($item[$map['student_name']] ?? $item['studentName'] ?? $item['student_name'] ?? ''),
            'className' => (string) ($item[$map['class_name']] ?? $item['className'] ?? $item['class_name'] ?? ''),
            'amount' => (float) ($item[$map['amount']] ?? $item['amount'] ?? 0),
            'time' => (string) ($item[$map['time']] ?? $item['time'] ?? $item['paid_at'] ?? ''),
            'type' => (string) ($item[$map['type']] ?? $item['type'] ?? $item['fee_type'] ?? 'Tuition'),
        ];
    }

    /** @param  array<string, mixed>  $item */
    private function transformAdmission(array $item): array
    {
        $map = config('school.company_api.field_map.admission');

        return [
            'studentName' => (string) ($item[$map['student_name']] ?? $item['studentName'] ?? $item['student_name'] ?? ''),
            'className' => (string) ($item[$map['class_name']] ?? $item['className'] ?? $item['class_name'] ?? ''),
            'admissionNumber' => (string) ($item[$map['admission_number']] ?? $item['admissionNumber'] ?? $item['admission_number'] ?? ''),
            'time' => (string) ($item[$map['time']] ?? $item['time'] ?? $item['enrolled_at'] ?? ''),
            'parentName' => (string) ($item[$map['parent_name']] ?? $item['parentName'] ?? $item['parent_name'] ?? $item['guardian_name'] ?? ''),
        ];
    }

    private function formatDisplayDate(?string $date): string
    {
        $carbon = $date
            ? Carbon::parse($date, config('school.timezone'))
            : Carbon::now(config('school.timezone'));

        return $carbon->format('d M Y');
    }
}
