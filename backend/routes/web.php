<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\DemoAdmissionController;

Route::get('/', function () {
    return view('welcome');
});

Route::get('/admissions/create', [DemoAdmissionController::class, 'create']);
Route::post('/admissions/store', [DemoAdmissionController::class, 'store']);

Route::get('/notifications', [DemoAdmissionController::class, 'notifications']);