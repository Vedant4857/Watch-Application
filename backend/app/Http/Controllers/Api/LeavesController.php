<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\SchoolDataService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class LeavesController extends Controller
{
    public function __construct(
        private readonly SchoolDataService $schoolData,
    ) {}

    public function getStaffLeaves(Request $request): JsonResponse
    {
        $data = $this->schoolData->getStaffLeaves();

        return response()->json([
            'success' => true,
            'data' => $data,
        ]);
    }

    public function getStudentLeaves(Request $request): JsonResponse
    {
        $data = $this->schoolData->getStudentLeaves();

        return response()->json([
            'success' => true,
            'data' => $data,
        ]);
    }

    public function updateStaffLeaveStatus(Request $request, int $id): JsonResponse
    {
        $request->validate(['status' => 'required|string']);
        $this->schoolData->updateStaffLeaveStatus($id, $request->input('status'));

        return response()->json(['success' => true]);
    }

    public function updateStudentLeaveStatus(Request $request, int $id): JsonResponse
    {
        $request->validate(['status' => 'required|string']);
        $this->schoolData->updateStudentLeaveStatus($id, $request->input('status'));

        return response()->json(['success' => true]);
    }
}
