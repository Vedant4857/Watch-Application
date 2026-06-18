<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class StudentSeeder extends Seeder
{
    public function run()
    {
        $students = [];
        for ($i = 1; $i <= 337; $i++) {
            $students[] = [
                'name' => "Student $i",
                'class_name' => "Grade " . (($i % 12) + 1),
                'roll_number' => "R-" . (1000 + $i),
                'created_at' => now(),
                'updated_at' => now(),
            ];
        }
        
        DB::table('students')->insert($students);
    }
}
