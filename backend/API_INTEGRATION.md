# Connecting Your Company's School API

This guide explains **exactly where to plug in your company's existing API and database** so the Android watch app shows real school data.

You do **not** need deep backend knowledge. Follow the steps in order.

---

## How It Works (Simple Picture)

```
┌─────────────────┐      HTTP       ┌──────────────────────┐      HTTP       ┌─────────────────┐
│  Android Watch  │  ────────────►  │  Laravel Backend     │  ────────────►  │  Your Company   │
│  (Wear OS App)  │                 │  (this folder)       │                 │  School ERP API │
└─────────────────┘                 └──────────────────────┘                 └─────────────────┘
```

- The **watch app** only talks to **this Laravel backend**.
- Laravel fetches data from **your company's API** and reshapes it for the watch.
- You customize **one file** and **`.env`** settings — not the whole backend.

---

## Step 1 — Start the Backend (Mock Data First)

Test with sample data before connecting your real API.

```bash
cd backend
cp .env.example .env    # skip if .env already exists
php artisan key:generate
php artisan serve
```

Server runs at: **http://127.0.0.1:8000**

Test in browser or terminal:

```bash
curl http://127.0.0.1:8000/api/v1/health
curl http://127.0.0.1:8000/api/v1/principal/dashboard
curl http://127.0.0.1:8000/api/v1/attendance/staff
curl http://127.0.0.1:8000/api/v1/attendance/students
curl http://127.0.0.1:8000/api/v1/fees/transactions
curl http://127.0.0.1:8000/api/v1/admissions
```

If these return JSON with school data, the backend is working.

---

## Step 2 — Know Your Company's API Details

Ask your IT team (or check API docs) for:

| What you need | Example |
|---|---|
| Base URL | `https://erp.yourschool.com` |
| API Key / Token | `sk_live_abc123...` |
| Auth type | API Key header, Bearer token, or login |
| School ID parameter | `school_id=42` |

Also get the **endpoint paths** for:

1. Dashboard / summary stats
2. Staff attendance (today)
3. Student attendance (class-wise)
4. Fee transactions (today)
5. New admissions (today)

Write these down — you'll enter them in `.env`.

---

## Step 3 — Switch from Mock to Company API

Open `backend/.env` and change:

```env
SCHOOL_DATA_SOURCE=company_api

COMPANY_API_BASE_URL=https://erp.yourschool.com
COMPANY_API_KEY=your-api-key-here

SCHOOL_ID=42
PRINCIPAL_NAME="Dr. Rajesh Sharma"
SCHOOL_TIMEZONE=Asia/Kolkata
```

If your company's URLs are different, add path overrides:

```env
COMPANY_API_DASHBOARD_PATH=/reports/principal-summary
COMPANY_API_STAFF_ATTENDANCE_PATH=/hr/attendance/teachers
COMPANY_API_STAFF_ATTENDANCE_PATH=/attendance/staff/daily
COMPANY_API_STUDENT_ATTENDANCE_PATH=/attendance/students/by-class
COMPANY_API_FEES_PATH=/finance/fees/today
COMPANY_API_ADMISSIONS_PATH=/admissions/new-today
```

Restart the server after changing `.env`:

```bash
php artisan serve
```

---

## Step 4 — Where to Edit Code (The Important Files)

### File 1: `config/school.php`

**Purpose:** Central config — API URLs, school ID, field name mapping.

| Section | What to change |
|---|---|
| `company_api.endpoints` | Path for each feature (or use `.env` overrides) |
| `company_api.field_map` | If your API uses different JSON key names |

Example — your API returns `staff_name` instead of `name`:

```php
'field_map' => [
    'staff' => [
        'name' => 'staff_name',   // was 'name'
        'role' => 'subject',
        'is_present' => 'present',
        'check_in_time' => 'check_in',
    ],
],
```

---

### File 2: `app/Services/School/CompanyApiSchoolDataProvider.php`

**Purpose:** This is the main integration file. It calls your company API and converts responses to the format the watch app expects.

| Method | When to edit |
|---|---|
| `fetch()` | Change authentication (Bearer token, OAuth, custom headers) |
| `getDashboardStats()` | If dashboard JSON structure is very different |
| `transformStaffRecords()` | Staff list field mapping |
| `transformClassAttendance()` | Student class-wise attendance mapping |
| `transformFeeTransaction()` | Fee payment field mapping |
| `transformAdmission()` | Admission record field mapping |

#### Authentication examples (inside `fetch()`)

**Bearer token:**
```php
$request = $request->withToken($config['api_key']);
```

**Custom header:**
```php
$request = $request->withHeaders([
    'Authorization' => 'Bearer '.$config['api_key'],
    'X-School-Id' => config('school.school_id'),
]);
```

**Basic auth:**
```php
$request = $request->withBasicAuth('username', 'password');
```

---

### File 3: `app/Services/School/MockSchoolDataProvider.php`

**Purpose:** Sample data only. Edit this if you want to change demo data while testing. **Not used** when `SCHOOL_DATA_SOURCE=company_api`.

---

## Step 5 — Match JSON Format (What the Watch App Expects)

Your Laravel API must return this shape. The controllers already wrap responses as `{ "success": true, "data": ... }`.

### Dashboard — `GET /api/v1/principal/dashboard`

```json
{
  "success": true,
  "data": {
    "date": "06 Jun 2026",
    "principalName": "Vedant Shekhar",
    "staffPresent": 12,
    "staffAbsent": 3,
    "totalStaff": 15,
    "studentsPresent": 296,
    "studentsAbsent": 41,
    "totalStudents": 337,
    "feesCollected": 109300.0,
    "feeTransactionCount": 12,
    "newAdmissions": 5
  }
}
```

### Staff Attendance — `GET /api/v1/attendance/staff?date=2026-06-06`

```json
{
  "success": true,
  "data": {
    "presentCount": 12,
    "absentCount": 3,
    "attendancePercentage": 80.0,
    "records": [
      {
        "name": "Priya Mehta",
        "role": "Mathematics",
        "isPresent": true,
        "checkInTime": "8:12 AM"
      }
    ]
  }
}
```

### Student Attendance — `GET /api/v1/attendance/students?date=2026-06-06`

```json
{
  "success": true,
  "data": {
    "totalPresent": 296,
    "totalAbsent": 41,
    "attendancePercentage": 87.8,
    "classWise": [
      { "className": "Grade 10", "present": 20, "total": 24 }
    ]
  }
}
```

### Fees — `GET /api/v1/fees/transactions?date=2026-06-06`

```json
{
  "success": true,
  "data": [
    {
      "studentName": "Aryan Kapoor",
      "className": "Grade 10-A",
      "amount": 12500.0,
      "time": "8:30 AM",
      "type": "Tuition"
    }
  ]
}
```

### Admissions — `GET /api/v1/admissions?date=2026-06-06`

```json
{
  "success": true,
  "data": [
    {
      "studentName": "Tanisha Bose",
      "className": "Grade 6-A",
      "admissionNumber": "ADM-2024-1089",
      "time": "8:45 AM",
      "parentName": "Mr. Subroto Bose"
    }
  ]
}
```

---

## Step 6 — Connect the Android Watch App

The Android app already includes network permission and an API-backed `SchoolDataRepository.kt`.
Point `API_BASE_URL` at the Laravel server you want the watch to read from:

| Watch app method | Laravel endpoint |
|---|---|
| `getDashboardStats()` | `GET /api/v1/principal/dashboard` |
| `getStaffAttendance()` | `GET /api/v1/attendance/staff` |
| `getStudentAttendance()` | `GET /api/v1/attendance/students` |
| `getFeeTransactions()` | `GET /api/v1/fees/transactions` |
| `getAdmissions()` | `GET /api/v1/admissions` |

**Emulator base URL:** `http://10.0.2.2:8000/api/v1` (maps to your Mac's localhost)

**Physical watch on same Wi-Fi:** `http://YOUR_COMPUTER_IP:8000/api/v1` (e.g. `http://192.168.1.5:8000/api/v1`)

**Production:** `https://api.yourschool.com/api/v1`

If the API cannot be reached, the app falls back to built-in personal sample data so the watch UI still works.

---

## Step 7 — Troubleshooting

| Problem | Fix |
|---|---|
| `Connection refused` from watch | Use `10.0.2.2` on emulator; check `php artisan serve` is running |
| Empty data from company API | Check `COMPANY_API_BASE_URL` and paths in `.env` |
| Wrong field names in app | Update `field_map` in `config/school.php` or `transform*()` methods |
| 401 Unauthorized | Fix API key / auth in `CompanyApiSchoolDataProvider::fetch()` |
| 500 error | Run `tail -f storage/logs/laravel.log` to see the exact error |

Test company API directly:

```bash
curl -H "X-API-Key: YOUR_KEY" \
  "https://erp.yourschool.com/api/attendance/staff?date=2026-06-06&school_id=1"
```

Compare that JSON with what `CompanyApiSchoolDataProvider` expects.

---

## Quick Reference — Files to Touch

| Goal | File |
|---|---|
| API URL, key, school ID | `backend/.env` |
| Endpoint paths | `backend/.env` or `backend/config/school.php` |
| Auth headers | `backend/app/Services/School/CompanyApiSchoolDataProvider.php` → `fetch()` |
| JSON field renaming | `backend/config/school.php` → `field_map` |
| Complex response shapes | `backend/app/Services/School/CompanyApiSchoolDataProvider.php` → `transform*()` |
| Add new API endpoint | `backend/routes/api.php` + new controller |
| Watch app API calls | `app/.../data/SchoolDataRepository.kt` |

---

## Deployment (When Ready for Production)

1. Deploy Laravel to a server (shared hosting, VPS, or Laravel Forge).
2. Set `APP_ENV=production`, `APP_DEBUG=false` in production `.env`.
3. Use HTTPS (`https://api.yourschool.com`).
4. Point the watch app to the production URL.
5. Add API authentication before going live (contact your developer for JWT/Sanctum if needed).

---

## Need Help From Your IT Team?

Send them this list:

> We need read-only API access for a principal dashboard watch app:
> - Today's staff attendance (name, subject, present/absent, check-in time)
> - Today's student attendance by class (present/total per grade)
> - Today's fee collections (student, class, amount, type, time)
> - Today's new admissions (student, class, admission number, parent)
> - Optional: dashboard summary endpoint
>
> Auth: API key or Bearer token
> Filter by: `school_id` and `date`

They can map their database tables to these fields using the `CompanyApiSchoolDataProvider.php` file.
