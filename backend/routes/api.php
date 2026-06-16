<?php

use App\Http\Controllers\Api\AdmissionsController;
use App\Http\Controllers\Api\DashboardController;
use App\Http\Controllers\Api\FeesController;
use App\Http\Controllers\Api\StaffAttendanceController;
use App\Http\Controllers\Api\StudentAttendanceController;
use App\Http\Controllers\Api\DeviceTokenController;
use App\Services\FirebaseNotificationService;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| School ERP Watch App — API Routes
|--------------------------------------------------------------------------
|
| These endpoints match the Android watch app data models exactly.
| Base URL: http://your-server:8000/api
|
*/

Route::prefix('v1')->group(function () {
    Route::get('/health', fn () => response()->json([
        'status' => 'ok',
        'app' => 'School ERP Watch API',
        'data_source' => config('school.data_source'),
    ]));

    Route::get('/principal/dashboard', [DashboardController::class, 'index']);
    Route::get('/attendance/staff', [StaffAttendanceController::class, 'index']);
    Route::get('/attendance/students', [StudentAttendanceController::class, 'index']);
    Route::get('/fees/transactions', [FeesController::class, 'index']);
    Route::get('/admissions', [AdmissionsController::class, 'index']);
    Route::post('/device-token',[DeviceTokenController::class, 'store']);
    Route::get('/test-notification', function (FirebaseNotificationService $firebase) {
        $result = $firebase->sendToAllDevices(
            'School ERP',
            'Test notification from Laravel'
        );

        return response()->json([
            'success' => true,
            'data' => $result,
        ]);
    });
});
