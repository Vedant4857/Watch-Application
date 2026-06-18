<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;

class StaffDirectoryController extends Controller
{
    /**
     * STAFF DIRECTORY API
     * Fetches details from the staff_directory table
     */
    public function index()
    {
        $staffMembers = DB::table('staff_directory')
            ->orderBy('name')
            ->get([
                'name',
                'department',
                'contact_number',
                'email',
                'role',
                'photo',
            ])
            ->map(function ($row) {
                return [
                    'name'          => $row->name ?: 'Unknown Staff',
                    'department'    => $row->department ?: 'N/A',
                    'contactNumber' => $row->contact_number ?: 'N/A',
                    'email'         => $row->email ?: 'N/A',
                    'role'          => $row->role ?: 'N/A',
                    'photoUrl'      => $row->photo ? request()->getSchemeAndHttpHost() . '/storage/' . ltrim($row->photo, '/') : '',
                ];
            });

        return response()->json(['success' => true, 'data' => $staffMembers]);
    }
}
