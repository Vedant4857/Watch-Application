<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ExpenseController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $schoolId = (int) config('school.school_id', 1);

        // Calculate total approved spendings for the current month
        $startOfMonth = Carbon::now(config('school.timezone'))->startOfMonth()->toDateString();
        $endOfMonth = Carbon::now(config('school.timezone'))->endOfMonth()->toDateString();

        $totalSpendings = DB::table('expenses')
            ->where('school_id', $schoolId)
            ->where('status', 'APPROVED')
            ->whereBetween('date_incurred', [$startOfMonth, $endOfMonth])
            ->sum('amount');

        // Fetch category breakdown for this month
        $breakdown = DB::table('expenses')
            ->select('category', DB::raw('SUM(amount) as total'))
            ->where('school_id', $schoolId)
            ->where('status', 'APPROVED')
            ->whereBetween('date_incurred', [$startOfMonth, $endOfMonth])
            ->groupBy('category')
            ->get()
            ->map(fn($row) => ['category' => $row->category, 'total' => (float) $row->total]);

        // Fetch pending expenses
        $pendingExpenses = DB::table('expenses')
            ->where('school_id', $schoolId)
            ->where('status', 'PENDING')
            ->orderBy('created_at', 'desc')
            ->get()
            ->map(fn($expense) => [
                'id' => $expense->id,
                'category' => $expense->category,
                'description' => $expense->description,
                'amount' => (float) $expense->amount,
                'requestedBy' => $expense->requested_by,
                'dateIncurred' => $expense->date_incurred,
            ]);

        // Fetch recent approved expenses
        $recentApproved = DB::table('expenses')
            ->where('school_id', $schoolId)
            ->where('status', 'APPROVED')
            ->orderBy('date_incurred', 'desc')
            ->orderBy('created_at', 'desc')
            ->take(5) // Get the top 5 most recent
            ->get()
            ->map(fn($expense) => [
                'id' => $expense->id,
                'category' => $expense->category,
                'description' => $expense->description,
                'amount' => (float) $expense->amount,
                'requestedBy' => $expense->requested_by,
                'dateIncurred' => $expense->date_incurred,
            ]);

        return response()->json([
            'success' => true,
            'data' => [
                'totalSpendings' => (float) $totalSpendings,
                'breakdown' => $breakdown,
                'pendingExpenses' => $pendingExpenses,
                'recentApproved' => $recentApproved,
            ],
        ]);
    }

    public function updateStatus(Request $request, $id): JsonResponse
    {
        $schoolId = (int) config('school.school_id', 1);
        $status = $request->input('status'); // 'APPROVED' or 'REJECTED'

        if (!in_array($status, ['APPROVED', 'REJECTED'])) {
            return response()->json(['success' => false, 'message' => 'Invalid status'], 400);
        }

        $updated = DB::table('expenses')
            ->where('id', $id)
            ->where('school_id', $schoolId)
            ->update(['status' => $status, 'updated_at' => Carbon::now()]);

        if ($updated) {
            return response()->json(['success' => true]);
        }

        return response()->json(['success' => false, 'message' => 'Expense not found'], 404);
    }
}
