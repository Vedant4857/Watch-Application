<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (! Schema::hasTable('schools')) {
            Schema::create('schools', function (Blueprint $table) {
                $table->id();
                $table->string('name');
                $table->string('principal_name');
                $table->string('timezone')->default('Asia/Kolkata');
                $table->timestamps();
            });
        }

        if (! Schema::hasTable('staff')) {
            Schema::create('staff', function (Blueprint $table) {
                $table->id();
                $table->foreignId('school_id')->constrained()->cascadeOnDelete();
                $table->string('name');
                $table->string('role');
                $table->boolean('is_active')->default(true);
                $table->timestamps();
            });
        }

        if (! Schema::hasTable('staff_attendance')) {
            Schema::create('staff_attendance', function (Blueprint $table) {
                $table->id();
                $table->foreignId('staff_id')->constrained()->cascadeOnDelete();
                $table->date('attendance_date');
                $table->boolean('is_present')->default(false);
                $table->string('check_in_time', 32)->default('');
                $table->timestamps();

                $table->unique(['staff_id', 'attendance_date']);
            });
        }

        if (! Schema::hasTable('school_classes')) {
            Schema::create('school_classes', function (Blueprint $table) {
                $table->id();
                $table->foreignId('school_id')->constrained()->cascadeOnDelete();
                $table->string('name', 64);
                $table->unsignedInteger('total_students')->default(0);
                $table->boolean('is_active')->default(true);
                $table->timestamps();
            });
        }

        if (! Schema::hasTable('class_attendance')) {
            Schema::create('class_attendance', function (Blueprint $table) {
                $table->id();
                $table->foreignId('class_id')->constrained('school_classes')->cascadeOnDelete();
                $table->date('attendance_date');
                $table->unsignedInteger('present_count')->default(0);
                $table->unsignedInteger('total_count')->default(0);
                $table->timestamps();

                $table->unique(['class_id', 'attendance_date']);
            });
        }

        if (! Schema::hasTable('fee_transactions')) {
            Schema::create('fee_transactions', function (Blueprint $table) {
                $table->id();
                $table->foreignId('school_id')->constrained()->cascadeOnDelete();
                $table->string('student_name');
                $table->string('class_name', 64);
                $table->decimal('amount', 12, 2);
                $table->string('fee_type', 64);
                $table->date('transaction_date');
                $table->string('transaction_time', 32);
                $table->timestamps();
            });
        }

        if (! Schema::hasTable('admissions')) {
            Schema::create('admissions', function (Blueprint $table) {
                $table->id();
                $table->foreignId('school_id')->constrained()->cascadeOnDelete();
                $table->string('student_name');
                $table->string('class_name', 64);
                $table->string('enrollment_no', 64);
                $table->string('parent_name');
                $table->date('admission_date');
                $table->string('admission_time', 32);
                $table->timestamps();
            });
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('admissions');
        Schema::dropIfExists('fee_transactions');
        Schema::dropIfExists('class_attendance');
        Schema::dropIfExists('school_classes');
        Schema::dropIfExists('staff_attendance');
        Schema::dropIfExists('staff');
        Schema::dropIfExists('schools');
    }
};
