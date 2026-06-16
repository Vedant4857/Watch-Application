<?php

namespace App\Http\Controllers;

use App\Services\FirebaseNotificationService;
use App\Services\NotificationService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class DemoAdmissionController extends Controller
{
    public function create()
    {
        return view('admissions.create');
    }

    public function store(
        Request $request,
        NotificationService $notificationService,
        FirebaseNotificationService $firebaseNotificationService
    ) {
        DB::table('admissions')->insert([
            'school_id' => 1,
            'student_name' => $request->student_name,
            'class_name' => $request->class_name,
            'admission_number' => $request->admission_number,
            'parent_name' => $request->parent_name,
            'admission_date' => now()->toDateString(),
            'admission_time' => now()->format('H:i:s'),
            'created_at' => now(),
            'updated_at' => now(),
        ]);

        $message = $request->student_name . ' admitted to ' . $request->class_name;

        $notificationService->notify('New Admission', $message);

        try {
            $firebaseNotificationService->sendToAllDevices('New Admission', $message);
        } catch (\Throwable $e) {
            Log::error('Firebase push notification failed: ' . $e->getMessage(), [
                'exception' => $e,
            ]);
        }

        return redirect('/notifications');
    }

    public function notifications()
    {
        $notifications = DB::table('notifications')
            ->latest()
            ->get();

        return view(
            'notifications.index',
            compact('notifications')
        );
    }
}