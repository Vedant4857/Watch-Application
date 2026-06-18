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
                'role',
                'contact_number',
                'email',
                'photo',
            ])
            ->map(function ($row) {
                return [
                    'name'          => $row->name ?: 'Unknown Staff',
                    'department'    => $row->department ?: 'N/A',
                    'role'          => $row->role ?: 'N/A',
                    'contactNumber' => $row->contact_number ?: 'N/A',
                    'email'         => $row->email ?: 'N/A',
                    'photoUrl'      => $row->photo ? asset('storage/' . $row->photo) : '',
                ];
            });

        return response()->json(['success' => true, 'data' => $staffMembers]);
    }
}
