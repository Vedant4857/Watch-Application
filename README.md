# 🏫 School ERP — Principal Watch App

A **Wear OS (Android Watch)** application built for School Principals to monitor key daily metrics
at a glance — directly from their smartwatch.

---

## 📱 Features

| Feature | Description |
|---|---|
| 👨‍🏫 **Staff Attendance** | View total present/absent staff, check-in times |
| 🎒 **Student Attendance** | Class-wise breakdown with colour-coded progress |
| 💰 **Fees Collected Today** | Total amount + category breakdown + transaction list |
| ✨ **New Admissions Today** | Student name, class, admission number, parent info |

---

## 🗂️ Project Structure

```
SchoolERPWatch/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/school/erp/watch/
│       │   ├── MainActivity.kt              ← Navigation host
│       │   ├── data/
│       │   │   └── SchoolDataRepository.kt  ← API client + personal fallback data
│       │   ├── viewmodel/
│       │   │   └── DashboardViewModel.kt    ← State management
│       │   └── presentation/
│       │       ├── theme/
│       │       │   └── Theme.kt             ← Colors + Wear OS theme
│       │       └── screens/
│       │           ├── DashboardScreen.kt   ← Home with 4 metric cards
│       │           ├── StaffAttendanceScreen.kt
│       │           ├── StudentAttendanceScreen.kt
│       │           ├── FeesScreen.kt
│       │           └── AdmissionsScreen.kt
│       └── res/
│           ├── drawable/ic_school.xml       ← App icon (school building SVG)
│           ├── mipmap-anydpi-v26/
│           │   └── ic_launcher.xml
│           └── values/
│               ├── strings.xml
│               └── colors.xml
├── gradle/
│   ├── libs.versions.toml                   ← Version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 How to Build & Run

### Prerequisites
- **Android Studio Hedgehog** (2023.1.1) or later
- **Android SDK 34**
- **Wear OS emulator** OR a physical Android Wear watch

### Step 1 — Open the Project
```bash
# Open Android Studio → File → Open
# Select the folder: /Users/vedantshekhar/Desktop/NewstWath
```

### Step 2 — Sync Gradle
Android Studio will automatically prompt you to sync Gradle.
Click **"Sync Now"** in the notification bar.

### Step 3 — Set Up a Wear OS Emulator
1. Open **AVD Manager** (Tools → Device Manager)
2. Click **"Create Virtual Device"**
3. Select **"Wear OS"** category → choose **Wear OS Large Round** or **Wear OS Square**
4. Select system image: **Wear OS 3 (API 30)** or higher
5. Click **Finish**

### Step 4 — Run the App
1. Select the Wear OS emulator from the device dropdown
2. Click the **▶ Run** button (or press `Shift+F10`)
3. The app installs and launches on the emulator

### Step 5 — Build APK (for physical watch)
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

To install on a physical watch via ADB:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📖 How to Use the App (Principal Guide)

### 🏠 Dashboard (Home Screen)
When you open the app, you see the **Principal Dashboard** showing 4 metric cards:

```
┌─────────────────────────────┐
│  🏫 Principal               │
│  Dr. Rajesh Sharma          │
│  05 Jun 2024                │
│                             │
│  👨‍🏫 Staff Attendance        │
│  12/15  ← 3 absent          │
│                             │
│  🎒 Student Attendance      │
│  296/337  ← 41 absent       │
│                             │
│  💰 Fees Collected          │
│  ₹1,09,300  ← 12 txns      │
│                             │
│  ✨ New Admissions           │
│  5  ← Today                 │
└─────────────────────────────┘
```

**Scroll** with the watch's rotating crown (or swipe up/down) to see all cards.
**Tap any card** to see detailed information.

---

### 👨‍🏫 Staff Attendance Screen
Tap the **Staff Attendance** card to see:
- **Summary pills** — Present count (green) and Absent count (red)
- **Attendance percentage** — e.g., "80% Attendance" in gold
- **Progress bar** — Visual representation of attendance rate
- **Staff list** — Each teacher with:
  - ✓ Green = Present (shows check-in time e.g. "8:12 AM")
  - ✗ Red = Absent

**Scroll down** to see all staff members.
**Swipe right** or tap "← Back to Dashboard" to go back.

---

### 🎒 Student Attendance Screen
Tap **Student Attendance** card to see:
- **Total present/absent** for all students
- **Overall percentage** across all grades
- **Class-wise rows** (Grade 1 through Grade 12) each showing:
  - Present/Total count
  - Mini progress bar coloured by threshold:
    - 🟢 **Green** = 90%+ attendance (excellent)
    - 🟡 **Yellow** = 75–89% (acceptable)
    - 🔴 **Red** = below 75% (needs attention)

---

### 💰 Fees Collected Screen
Tap **Fees Collected** card to see:
- **Total amount** collected today (e.g. ₹1,09,300)
- **Transaction count** (e.g. 12 transactions)
- **Category breakdown** — Tuition, Transport, Library, Annual fees
- **Full transaction list** — Each row shows:
  - Student name and class
  - Fee type (purple badge)
  - Amount in gold
  - Time of payment

---

### ✨ New Admissions Screen
Tap **New Admissions** card to see:
- **Big count** in a circle (e.g. 5 new students today)
- **Individual admission cards** (numbered 1, 2, 3…) each showing:
  - Student name and class assigned
  - Admission number (e.g. ADM-2024-1089)
  - Enrollment time
  - Parent's name

---

## 🕹️ Watch Gestures

| Gesture | Action |
|---|---|
| **Rotate Crown / Swipe Up-Down** | Scroll through lists |
| **Tap a Card** | Navigate to detail screen |
| **Swipe Right** | Go back (dismiss screen) |
| **Tap "← Back to Dashboard"** | Return to home screen |

---

## 🎨 Design System

| Color | Usage |
|---|---|
| 🔵 Cyan (#00C9FF) | Staff attendance, time labels |
| 🟢 Teal Green (#00E5A0) | Student attendance, present status |
| 🟡 Golden Yellow (#FFBB00) | Fees collected, percentages |
| 🟣 Purple (#B06EFF) | New admissions |
| 🔴 Coral Red (#FF5C7A) | Absent status, errors |
| 🌑 Deep Navy (#0A0E27) | Background |

---

## 🔧 Backend (Laravel API)

A full **Laravel backend** is included in the `backend/` folder.

| Doc | Purpose |
|-----|---------|
| [`backend/README.md`](backend/README.md) | Start the API server |
| [`backend/PERSONAL_SETUP.md`](backend/PERSONAL_SETUP.md) | Set up your own MySQL school data |
| [`backend/API_INTEGRATION.md`](backend/API_INTEGRATION.md) | Connect a company school API later, if needed |

### Quick start

```bash
cd backend
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate
php artisan db:seed
php artisan serve
# API: http://127.0.0.1:8000/api/v1/principal/dashboard
```

The backend is configured for **your own MySQL data** by default. See **`backend/PERSONAL_SETUP.md`** for step-by-step setup and MySQL queries. For an external company API later, see **API_INTEGRATION.md**.

### Connect the watch app

The watch app already includes API access and is pointed at:

```kotlin
http://10.0.2.2:8000/api/v1
```

That URL works for the Wear OS emulator when Laravel is running on your Mac. For a physical watch on the same Wi-Fi, change `API_BASE_URL` in `app/src/main/java/com/school/erp/watch/data/SchoolDataRepository.kt` to:

```kotlin
http://YOUR_MAC_IP:8000/api/v1
```

If the API is not running, the app still opens with local personal fallback data.

---

## 📋 Requirements

| Requirement | Minimum |
|---|---|
| Android Wear OS | 3.0 (API 30) |
| Watch Shape | Round or Square |
| Kotlin | 1.9.x |
| Android Studio | Hedgehog+ |

---

## 👨‍💼 Built For
**School Principal** — to monitor the school's daily health metrics at a glance,
without needing to open a phone or laptop.

> *"All key school data, right on your wrist."*
# Watch-Application
# Watch-App-Backend
# Watch-App-Backend
# Real_watch_work
