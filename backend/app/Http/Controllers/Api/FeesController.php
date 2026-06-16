<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\SchoolDataService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class FeesController extends Controller
{
    public function __construct(
        private readonly SchoolDataService $schoolData,
    ) {}

    public function index(Request $request): JsonResponse
    {
        $data = $this->schoolData->getFeeTransactions($request->query('date'));

        return response()->json([
            'success' => true,
            'data' => $data,
        ]);
    }
}
