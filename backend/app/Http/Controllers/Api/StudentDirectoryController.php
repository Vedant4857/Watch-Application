<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;

class StudentDirectoryController extends Controller
{
    /**
     * =========================================================================
     * STUDENT DIRECTORY - REAL ERP DATA FLOW
     * =========================================================================
     */
    public function index()
    {
        $students = DB::table('student_details')
            ->orderBy('name')
            ->get([
                'name',
                'contact_number',
                'class_name',
                'photo',
            ])
            ->map(function ($student) {
                return [
                    'name'          => $student->name ?: 'Unknown Student',
                    'className'     => $student->class_name ?: 'N/A',
                    'contactNumber' => $student->contact_number ?: 'N/A',
                    'photo'         => $student->photo ? request()->getSchemeAndHttpHost() . '/storage/' . ltrim($student->photo, '/') : '',
                ];
            });

        return response()->json([
            'success' => true,
            'data'    => $students,
        ]);
    }
}
