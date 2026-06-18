<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class StaffSeeder extends Seeder
{
    public function run()
    {
        DB::table('staff')->insert([
            ['name' => 'Suresh Kumar', 'department' => 'Mathematics', 'is_present' => true, 'check_in_time' => '7:45 AM', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Anita Desai', 'department' => 'Science', 'is_present' => true, 'check_in_time' => '7:50 AM', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Rahul Verma', 'department' => 'English', 'is_present' => false, 'check_in_time' => '', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Priya Singh', 'department' => 'History', 'is_present' => true, 'check_in_time' => '8:05 AM', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Neha Sharma', 'department' => 'Computer', 'is_present' => true, 'check_in_time' => '8:30 AM', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Vikram Das', 'department' => 'PE', 'is_present' => true, 'check_in_time' => '7:55 AM', 'created_at' => now(), 'updated_at' => now()],
            ['name' => 'Manish Tiwari', 'department' => 'Music', 'is_present' => false, 'check_in_time' => '', 'created_at' => now(), 'updated_at' => now()],
        ]);
    }
}
