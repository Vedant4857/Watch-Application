<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class StaffLeave extends Model
{
    use HasFactory;

    protected $fillable = [
        'school_id',
        'staff_name',
        'leave_date',
        'leave_type',
        'reason',
        'status',
    ];

    public function school()
    {
        return $this->belongsTo(School::class);
    }
}
