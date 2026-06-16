<?php

namespace App\Services;

use Kreait\Firebase\Factory;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class FirebaseNotificationService
{
    private $messaging;

    public function __construct()
    {
        $factory = (new Factory)
            ->withServiceAccount(
                storage_path('firebase/firebase-service-account.json')
            );

        $this->messaging = $factory->createMessaging();
    }

    public function sendToAllDevices(
        string $title,
        string $body
    ): array {
        $tokens = DB::table('device_tokens')
            ->pluck('token')
            ->map(fn ($token) => trim($token))
            ->filter()
            ->values()
            ->toArray();

        $sent = 0;
        $failed = 0;
        $removed = 0;

        foreach ($tokens as $token) {
            try {
                $this->messaging->send([
                    'token' => $token,
                    'notification' => [
                        'title' => $title,
                        'body' => $body,
                    ],
                    'data' => [
                        'title' => $title,
                        'body' => $body,
                    ],
                    'android' => [
                        'priority' => 'HIGH',
                        'notification' => [
                            'channel_id' => 'school_erp_channel',
                            'sound' => 'default',
                        ],
                    ],
                ]);

                $sent++;

                Log::info('Firebase message dispatched', [
                    'token' => substr($token, 0, 20).'...',
                    'title' => $title,
                ]);
            } catch (\Throwable $e) {
                $failed++;
                $error = $e->getMessage();

                if ($this->isInvalidTokenError($error)) {
                    DB::table('device_tokens')->where('token', $token)->delete();
                    $removed++;
                }

                Log::error('Failed to send Firebase notification', [
                    'token' => substr($token, 0, 20).'...',
                    'error' => $error,
                ]);
            }
        }

        return [
            'tokens' => count($tokens),
            'sent' => $sent,
            'failed' => $failed,
            'removed_invalid' => $removed,
        ];
    }

    private function isInvalidTokenError(string $error): bool
    {
        return str_contains($error, 'not a valid FCM registration token')
            || str_contains($error, 'not found')
            || str_contains($error, 'UNREGISTERED')
            || str_contains($error, 'Invalid registration');
    }
}
