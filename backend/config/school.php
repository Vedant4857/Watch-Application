<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Data Source
    |--------------------------------------------------------------------------
    |
    | "mock"        — Built-in sample data (no database needed)
    | "database"    — Your own MySQL data (recommended for personal use)
    | "company_api" — External company ERP API
    |
    */
    'data_source' => env('SCHOOL_DATA_SOURCE', 'mock'),

    /*
    |--------------------------------------------------------------------------
    | School Identity
    |--------------------------------------------------------------------------
    */
    'school_id' => env('SCHOOL_ID', '1'),
    'name' => env('SCHOOL_NAME', 'Shekhar Public School'),
    'principal_name' => env('PRINCIPAL_NAME', 'Vedant Shekhar'),
    'timezone' => env('SCHOOL_TIMEZONE', 'Asia/Kolkata'),

    /*
    |--------------------------------------------------------------------------
    | Company API Connection
    |--------------------------------------------------------------------------
    |
    | Fill these in when connecting to your company's database/API.
    | See API_INTEGRATION.md for step-by-step instructions.
    |
    */
    'company_api' => [
        'base_url' => env('COMPANY_API_BASE_URL', 'https://your-company-api.example.com'),
        'api_key' => env('COMPANY_API_KEY', ''),
        'timeout' => (int) env('COMPANY_API_TIMEOUT', 30),

        /*
        | Map your company's API paths here.
        | Change only the path strings — keep the keys the same.
        */
        'endpoints' => [
            'dashboard' => env('COMPANY_API_DASHBOARD_PATH', '/api/dashboard'),
            'staff_attendance' => env('COMPANY_API_STAFF_ATTENDANCE_PATH', '/api/attendance/staff'),
            'student_attendance' => env('COMPANY_API_STUDENT_ATTENDANCE_PATH', '/api/attendance/students'),
            'fee_transactions' => env('COMPANY_API_FEES_PATH', '/api/fees/transactions'),
            'admissions' => env('COMPANY_API_ADMISSIONS_PATH', '/api/admissions'),
        ],

        /*
        | If your company API uses different JSON field names, map them here.
        | Left side = our watch app field, right side = your API field name.
        */
        'field_map' => [
            'staff' => [
                'name' => 'name',
                'role' => 'role',
                'is_present' => 'is_present',
                'check_in_time' => 'check_in_time',
            ],
            'fee' => [
                'student_name' => 'student_name',
                'class_name' => 'class_name',
                'amount' => 'amount',
                'time' => 'time',
                'type' => 'type',
            ],
            'admission' => [
                'student_name' => 'student_name',
                'class_name' => 'class_name',
                'admission_number' => 'admission_number',
                'time' => 'time',
                'parent_name' => 'parent_name',
            ],
            'class_attendance' => [
                'class_name' => 'class_name',
                'present' => 'present',
                'total' => 'total',
            ],
        ],
    ],

];
