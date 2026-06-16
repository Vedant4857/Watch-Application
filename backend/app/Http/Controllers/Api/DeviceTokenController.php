<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class DeviceTokenController extends Controller
{
    public function store(Request $request)
    {
        $request->validate([
            'token' => ['required', 'string']
        ]);

        $token = trim($request->token);

        DB::table('device_tokens')->updateOrInsert(
            [
                'token' => $token
            ],
            [
                'updated_at' => now(),
                'created_at' => now()
            ]
        );

        return response()->json([
            'success' => true,
            'message' => 'Token registered'
        ]);
    }
}