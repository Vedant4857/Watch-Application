# Set Up Your Own School (No Company API Needed)

Use **your own MySQL database** with your school data. No external company API required.

**Your school (default):**
- School: **Shekhar Public School**
- Principal: **Vedant Shekhar**
- Timezone: **Asia/Kolkata**

---

## Option A — Laravel Migrations (Recommended)

### 1. Install MySQL

Make sure MySQL is running on your Mac. Create the database:

```sql
CREATE DATABASE school_erp_watch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure `.env`

```bash
cd backend
cp .env.example .env   # skip if .env already exists
php artisan key:generate
```

Edit `backend/.env`:

```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=school_erp_watch
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

SCHOOL_DATA_SOURCE=database
SCHOOL_ID=1
SCHOOL_NAME="Shekhar Public School"
PRINCIPAL_NAME="Vedant Shekhar"
SCHOOL_TIMEZONE=Asia/Kolkata
```

### 3. Create tables and load sample data

```bash
php artisan migrate
php artisan db:seed
```

### 4. Start the API

```bash
php artisan serve
```

### 5. Test it

```bash
curl http://127.0.0.1:8000/api/v1/principal/dashboard
curl http://127.0.0.1:8000/api/v1/attendance/staff
curl http://127.0.0.1:8000/api/v1/attendance/students
curl http://127.0.0.1:8000/api/v1/fees/transactions
curl http://127.0.0.1:8000/api/v1/admissions
```

You should see JSON with your school data.

---

## Option B — Run SQL Directly in MySQL

If you prefer phpMyAdmin or MySQL Workbench, run the full script:

**File:** `backend/database/sql/school_setup.sql`

```bash
mysql -u root -p < backend/database/sql/school_setup.sql
```

Then set `.env` to use MySQL + `SCHOOL_DATA_SOURCE=database` (same as Option A step 2) and run:

```bash
php artisan migrate
php artisan serve
```

> **Note:** The SQL file creates its own `school_erp_watch` database and school tables. Laravel also needs its system tables (`users`, `cache`, `jobs`, `sessions`), so run `php artisan migrate` once after the SQL script.

---

## Customize Your Data

### Change school name / principal

In `.env`:
```env
SCHOOL_NAME="My School Name"
PRINCIPAL_NAME="Vedant Shekhar"
```

Or in MySQL:
```sql
UPDATE schools SET name = 'My School Name', principal_name = 'Vedant Shekhar' WHERE id = 1;
```

### Add a teacher

```sql
INSERT INTO staff (school_id, name, role, is_active, created_at, updated_at)
VALUES (1, 'Rajesh Kumar', 'Mathematics', 1, NOW(), NOW());

INSERT INTO staff_attendance (staff_id, attendance_date, is_present, check_in_time, created_at, updated_at)
VALUES (LAST_INSERT_ID(), CURDATE(), 1, '8:15 AM', NOW(), NOW());
```

### Add a fee payment today

```sql
INSERT INTO fee_transactions (school_id, student_name, class_name, amount, fee_type, transaction_date, transaction_time, created_at, updated_at)
VALUES (1, 'Riya Sharma', 'Grade 7-A', 7500.00, 'Tuition', CURDATE(), '11:30 AM', NOW(), NOW());
```

### Add a new admission today

```sql
INSERT INTO admissions (school_id, student_name, class_name, admission_number, parent_name, admission_date, admission_time, created_at, updated_at)
VALUES (1, 'Aarav Singh', 'Grade 5-B', 'ADM-2026-1100', 'Mr. Vikram Singh', CURDATE(), '10:00 AM', NOW(), NOW());
```

### Update class attendance for today

```sql
UPDATE class_attendance ca
JOIN school_classes sc ON sc.id = ca.class_id
SET ca.present_count = 25, ca.total_count = 30
WHERE sc.name = 'Grade 10' AND ca.attendance_date = CURDATE();
```

### Mark staff absent today

```sql
UPDATE staff_attendance sa
JOIN staff s ON s.id = sa.staff_id
SET sa.is_present = 0, sa.check_in_time = ''
WHERE s.name = 'Priya Mehta' AND sa.attendance_date = CURDATE();
```

---

## Database Tables

| Table | What it stores |
|-------|----------------|
| `schools` | School name, principal, timezone |
| `staff` | Teachers and their subjects |
| `staff_attendance` | Daily present/absent per teacher |
| `school_classes` | Grade names (Grade 1–12) |
| `class_attendance` | Daily present/total per class |
| `fee_transactions` | Fee payments for a date |
| `admissions` | New student admissions for a date |

All daily data is filtered by **date**. The API uses **today** by default, or pass `?date=2026-06-08`.

---

## Data Source Modes

| `.env` value | Where data comes from |
|--------------|----------------------|
| `SCHOOL_DATA_SOURCE=database` | Your MySQL tables |
| `SCHOOL_DATA_SOURCE=mock` | Built-in PHP sample (no DB) |
| `SCHOOL_DATA_SOURCE=company_api` | External company API |

---

## Connect the Watch App

The Android app is already connected to the Laravel API from `SchoolDataRepository.kt`.

- Emulator: keep `API_BASE_URL = "http://10.0.2.2:8000/api/v1"`
- Physical watch on same Wi-Fi: change it to `http://YOUR_MAC_IP:8000/api/v1`
- If Laravel is not running, the watch app falls back to built-in personal sample data.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Empty dashboard | Data is date-based — run seeder/SQL on **today**, or use `?date=YYYY-MM-DD` matching your data |
| `SQLSTATE connection refused` | Start MySQL; check `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD` in `.env` |
| `Base table not found` | Run `php artisan migrate` |
| Still shows old mock data | Set `SCHOOL_DATA_SOURCE=database` and restart `php artisan serve` |
