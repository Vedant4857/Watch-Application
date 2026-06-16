<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;

class NotificationService
{
    public function notify(string $title, string $message): void
    {
        DB::table('notifications')->insert([
            'title' => $title,
            'message' => $message,
            'created_at' => now(),
            'updated_at' => now(),
        ]);
    }
}