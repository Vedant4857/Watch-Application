<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class NotificationController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $schoolId = (int) config('school.school_id', 1);

        $notifications = DB::table('notifications')
            ->where('school_id', $schoolId)
            ->orderBy('is_read', 'asc') // Unread first
            ->orderByDesc('created_at')
            ->get()
            ->map(function ($notification) {
                return [
                    'id' => $notification->id,
                    'title' => $notification->title,
                    'message' => $notification->message,
                    'type' => $notification->type,
                    'isRead' => (bool) $notification->is_read,
                    'createdAt' => $notification->created_at,
                ];
            });

        return response()->json([
            'success' => true,
            'data' => $notifications,
        ]);
    }
}
