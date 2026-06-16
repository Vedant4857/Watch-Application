<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasTable('school_classes')) {
            Schema::table('school_classes', function (Blueprint $table) {
                if (!Schema::hasColumn('school_classes', 'total_students')) {
                    $table->unsignedInteger('total_students')->default(0)->after('name');
                }
                if (!Schema::hasColumn('school_classes', 'is_active')) {
                    $table->boolean('is_active')->default(true)->after('total_students');
                }
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasTable('school_classes')) {
            Schema::table('school_classes', function (Blueprint $table) {
                $table->dropColumn(['total_students', 'is_active']);
            });
        }
    }
};
