# School ERP Watch — Laravel API Backend

REST API for the **Principal Watch App** (Wear OS). Serves dashboard, attendance, fees, and admissions data to the Android watch.

**Works in three modes:**
- **`mock`** — Sample data for testing without a database
- **`database`** — **Your own MySQL school data** (recommended for personal use)
- **`company_api`** — Proxies an external company school ERP API

**Set up your own school:** **[PERSONAL_SETUP.md](./PERSONAL_SETUP.md)**  
For connecting a company API later: **[API_INTEGRATION.md](./API_INTEGRATION.md)**

---

## Requirements

- PHP 8.2+
- Composer
- SQLite (included) or MySQL/PostgreSQL for production

---

## Quick Start

```bash
cd backend
composer install          # if vendor/ is missing
cp .env.example .env      # if .env doesn't exist
php artisan key:generate
php artisan migrate
php artisan db:seed
php artisan serve
```

API base URL: **http://127.0.0.1:8000/api/v1**

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Health check |
| GET | `/api/v1/principal/dashboard` | Dashboard summary |
| GET | `/api/v1/attendance/staff` | Staff attendance list |
| GET | `/api/v1/attendance/students` | Class-wise student attendance |
| GET | `/api/v1/fees/transactions` | Today's fee payments |
| GET | `/api/v1/admissions` | Today's new admissions |

**Query params (all optional):**
- `date` — `YYYY-MM-DD` (defaults to today in school timezone)

**Example:**
```bash
curl "http://127.0.0.1:8000/api/v1/principal/dashboard?date=2026-06-06"
```

---

## Project Structure

```
backend/
├── PERSONAL_SETUP.md           ← MySQL setup for your own school data
├── API_INTEGRATION.md          ← Optional company API setup
├── config/school.php           ← School settings & API path mapping
├── routes/api.php              ← API route definitions
├── app/
│   ├── Contracts/
│   │   └── SchoolDataProvider.php
│   ├── Http/Controllers/Api/   ← One controller per feature
│   └── Services/
│       ├── SchoolDataService.php
│       └── School/
│           ├── MockSchoolDataProvider.php       ← Demo data
│           ├── DatabaseSchoolDataProvider.php   ← Your MySQL school data
│           └── CompanyApiSchoolDataProvider.php ← Optional company API
```

---

## Configuration (.env)

```env
SCHOOL_DATA_SOURCE=database      # mock | database | company_api
SCHOOL_ID=1
SCHOOL_NAME="Shekhar Public School"
PRINCIPAL_NAME="Vedant Shekhar"
SCHOOL_TIMEZONE=Asia/Kolkata

COMPANY_API_BASE_URL=https://your-company-api.example.com
COMPANY_API_KEY=your-key-here
```

---

## Switching Data Source

| Mode | `.env` value | Data from |
|------|--------------|-----------|
| Testing | `SCHOOL_DATA_SOURCE=mock` | Built-in sample data |
| Personal / own school | `SCHOOL_DATA_SOURCE=database` | Your MySQL database |
| Company ERP | `SCHOOL_DATA_SOURCE=company_api` | External company API |

Binding is in `app/Providers/AppServiceProvider.php`.
