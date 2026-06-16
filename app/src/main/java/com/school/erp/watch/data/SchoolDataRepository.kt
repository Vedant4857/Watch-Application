package com.school.erp.watch.data

import android.util.Log
import androidx.paging.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import androidx.paging.*
import kotlinx.coroutines.flow.map
import com.school.erp.watch.data.db.AppDatabase
import com.school.erp.watch.data.db.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "SchoolDataRepository"

// ─── Data Models ─────────────────────────────────────────────────────────────
// These data classes define the structure of the information used in the app.
// They act like blueprints for objects like attendance, fees, and admissions.

/**
 * Represents the attendance status of an individual (staff or student).
 */
data class AttendanceRecord(
    val name: String,
    val role: String,
    val isPresent: Boolean,
    val checkInTime: String = ""
)

data class FeeTransaction(
    val studentName: String,
    val className: String,
    val amount: Double,
    val time: String,
    val type: String // "Tuition", "Transport", "Library", etc.
)

data class AdmissionRecord(
    val studentName: String,
    val className: String,
    val admissionNumber: String,
    val time: String,
    val parentName: String
)

data class DashboardStats(
    val date: String,
    val staffPresent: Int,
    val staffAbsent: Int,
    val totalStaff: Int,
    val studentsPresent: Int,
    val studentsAbsent: Int,
    val totalStudents: Int,
    val feesCollected: Double,
    val feeTransactionCount: Int,
    val newAdmissions: Int,
    val principalName: String = "Vedant Shekhar"
)

data class StaffAttendanceData(
    val records: List<AttendanceRecord>,
    val presentCount: Int,
    val absentCount: Int,
    val attendancePercentage: Float
)

data class StudentAttendanceData(
    val classWise: List<ClassAttendance>,
    val totalPresent: Int,
    val totalAbsent: Int,
    val attendancePercentage: Float
)

data class ClassAttendance(
    val className: String,
    val present: Int,
    val total: Int
)

data class StudentInfo(
    val name: String,
    val className: String,
    val rollNumber: String
)

// ─── Repository ───────────────────────────────────────────────────────────────

/**
 * SchoolDataRepository is the central hub for fetching data.
 * It handles the logic of requesting data from the backend API,
 * parsing it, and falling back to dummy data if the API fails.
 */
class SchoolDataRepository {

    // A companion object is like 'static' in Java. 
    // Variables here belong to the class itself, not just instances.
    companion object {
        private var API_BASE_URL = ApiConfig.API_BASE_URL
        private const val CONNECT_TIMEOUT_MS = ApiConfig.CONNECT_TIMEOUT_MS
        private const val READ_TIMEOUT_MS = ApiConfig.READ_TIMEOUT_MS

        var database: AppDatabase? = null

        fun initDatabase(context: Context) {
            database = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database!!.studentDao()
                if (dao.getStudentCount() == 0) {
                    val newStudents = mutableListOf<StudentEntity>()
                    for (i in 1..337) {
                        newStudents.add(
                            StudentEntity(
                                name = "Student $i",
                                className = "Grade ${(i % 12) + 1}",
                                rollNumber = "R-${1000 + i}"
                            )
                        )
                    }
                    dao.insertAll(newStudents)
                }
            }
        }

        fun setApiBaseUrl(url: String) {
            Log.d(TAG, "Setting API base URL to: $url")
            API_BASE_URL = url
        }

        fun getApiBaseUrl(): String = API_BASE_URL
    }

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val today = dateFormat.format(Date())

    // Simulate staff attendance data
    private val staffList = listOf(
        AttendanceRecord("Priya Mehta", "Mathematics", true, "8:12 AM"),
        AttendanceRecord("Amit Verma", "Science", true, "8:05 AM"),
        AttendanceRecord("Sunita Rao", "English", false, ""),
        AttendanceRecord("Rahul Gupta", "History", true, "8:20 AM"),
        AttendanceRecord("Kavita Singh", "Geography", true, "8:08 AM"),
        AttendanceRecord("Deepak Joshi", "Physics", true, "8:15 AM"),
        AttendanceRecord("Anita Patel", "Chemistry", false, ""),
        AttendanceRecord("Suresh Kumar", "Biology", true, "8:02 AM"),
        AttendanceRecord("Neha Sharma", "Computer", true, "8:30 AM"),
        AttendanceRecord("Vikram Das", "PE", true, "7:55 AM"),
        AttendanceRecord("Ritu Agarwal", "Art", true, "8:10 AM"),
        AttendanceRecord("Manish Tiwari", "Music", false, ""),
        AttendanceRecord("Pooja Nair", "Hindi", true, "8:18 AM"),
        AttendanceRecord("Arun Mishra", "Sanskrit", true, "8:22 AM"),
        AttendanceRecord("Shweta Bansal", "Economics", true, "8:07 AM")
    )

    private val studentList = listOf(
        StudentInfo("Aryan Kapoor", "Grade 10-A", "R-1001"),
        StudentInfo("Prisha Sharma", "Grade 8-B", "R-8002"),
        StudentInfo("Rohan Mehta", "Grade 6-A", "R-6003"),
        StudentInfo("Isha Patel", "Grade 9-C", "R-9004"),
        StudentInfo("Dev Gupta", "Grade 11-A", "R-11005"),
        StudentInfo("Aanya Singh", "Grade 7-B", "R-7006"),
        StudentInfo("Kabir Verma", "Grade 12-A", "R-12007"),
        StudentInfo("Myra Joshi", "Grade 5-A", "R-5008"),
        StudentInfo("Vihaan Das", "Grade 3-B", "R-3009"),
        StudentInfo("Siya Kumar", "Grade 4-A", "R-4010"),
        StudentInfo("Advait Rao", "Grade 2-A", "R-2011"),
        StudentInfo("Anvi Nair", "Grade 1-B", "R-1012")
    )

    private val feeTransactions = listOf(
        FeeTransaction("Aryan Kapoor", "Grade 10-A", 12500.0, "8:30 AM", "Tuition"),
        FeeTransaction("Prisha Sharma", "Grade 8-B", 8500.0, "9:15 AM", "Tuition"),
        FeeTransaction("Rohan Mehta", "Grade 6-A", 15000.0, "9:45 AM", "Annual"),
        FeeTransaction("Isha Patel", "Grade 9-C", 3200.0, "10:10 AM", "Transport"),
        FeeTransaction("Dev Gupta", "Grade 11-A", 11000.0, "10:30 AM", "Tuition"),
        FeeTransaction("Aanya Singh", "Grade 7-B", 5500.0, "11:00 AM", "Library"),
        FeeTransaction("Kabir Verma", "Grade 12-A", 14500.0, "11:20 AM", "Tuition"),
        FeeTransaction("Myra Joshi", "Grade 5-A", 9800.0, "11:45 AM", "Tuition"),
        FeeTransaction("Vihaan Das", "Grade 3-B", 7200.0, "12:00 PM", "Tuition"),
        FeeTransaction("Siya Kumar", "Grade 4-A", 4800.0, "12:30 PM", "Transport"),
        FeeTransaction("Advait Rao", "Grade 2-A", 6500.0, "1:00 PM", "Tuition"),
        FeeTransaction("Anvi Nair", "Grade 1-B", 5000.0, "1:20 PM", "Tuition")
    )

    private val admissions = listOf(
        AdmissionRecord("Tanisha Bose", "Grade 6-A", "ADM-2024-1089", "8:45 AM", "Mr. Subroto Bose"),
        AdmissionRecord("Kiran Reddy", "Grade 4-B", "ADM-2024-1090", "9:30 AM", "Mrs. Lakshmi Reddy"),
        AdmissionRecord("Arjun Mishra", "Grade 9-A", "ADM-2024-1091", "10:20 AM", "Mr. Arun Mishra"),
        AdmissionRecord("Diya Malhotra", "Grade 2-A", "ADM-2024-1092", "11:10 AM", "Mrs. Rekha Malhotra"),
        AdmissionRecord("Siddharth Roy", "Grade 11-B", "ADM-2024-1093", "12:05 PM", "Mr. Bijoy Roy")
    )

    private val classAttendance = listOf(
        ClassAttendance("Grade 1", 28, 30),
        ClassAttendance("Grade 2", 25, 28),
        ClassAttendance("Grade 3", 32, 35),
        ClassAttendance("Grade 4", 29, 32),
        ClassAttendance("Grade 5", 30, 33),
        ClassAttendance("Grade 6", 27, 30),
        ClassAttendance("Grade 7", 26, 30),
        ClassAttendance("Grade 8", 24, 28),
        ClassAttendance("Grade 9", 22, 25),
        ClassAttendance("Grade 10", 20, 24),
        ClassAttendance("Grade 11", 18, 22),
        ClassAttendance("Grade 12", 15, 20)
    )

    /**
     * Fetches the overall dashboard statistics.
     * Uses Kotlin Flows to emit the result asynchronously.
     */
    fun getDashboardStats(): Flow<DashboardStats> = flow {
        // Try fetching data from the actual API first
        val remote = runCatching {
            Log::class.java // dummy to force import
            Log.d(TAG, "Fetching dashboard stats from: $API_BASE_URL/principal/dashboard")
            fetchDataObject("/principal/dashboard").toDashboardStats()
        }.onFailure { 
            Log.e(TAG, "Error fetching dashboard stats: ${it.message}", it)
        }.getOrNull()

        if (remote != null) {
            Log.d(TAG, "Successfully loaded dashboard stats from API")
            emit(remote)
            return@flow
        }

        Log.d(TAG, "API failed, using mock data for dashboard stats")
        delay(250)
        val staffPresent = staffList.count { it.isPresent }
        val staffAbsent = staffList.count { !it.isPresent }
        val studentsPresent = classAttendance.sumOf { it.present }
        val studentsAbsent = classAttendance.sumOf { it.total - it.present }
        val totalStudents = classAttendance.sumOf { it.total }
        val totalFees = feeTransactions.sumOf { it.amount }

        emit(
            DashboardStats(
                date = today,
                staffPresent = staffPresent,
                staffAbsent = staffAbsent,
                totalStaff = staffList.size,
                studentsPresent = studentsPresent,
                studentsAbsent = studentsAbsent,
                totalStudents = totalStudents,
                feesCollected = totalFees,
                feeTransactionCount = feeTransactions.size,
                newAdmissions = admissions.size
            )
        )
    }

    fun getStaffAttendance(): Flow<StaffAttendanceData> = flow {
        val remote = runCatching {
            fetchDataObject("/attendance/staff").toStaffAttendanceData()
        }.getOrNull()

        if (remote != null) {
            emit(remote)
            return@flow
        }

        delay(250)
        val presentCount = staffList.count { it.isPresent }
        val absentCount = staffList.count { !it.isPresent }
        emit(
            StaffAttendanceData(
                records = staffList,
                presentCount = presentCount,
                absentCount = absentCount,
                attendancePercentage = (presentCount.toFloat() / staffList.size) * 100f
            )
        )
    }

    fun getStudentAttendance(): Flow<StudentAttendanceData> = flow {
        val remote = runCatching {
            fetchDataObject("/attendance/students").toStudentAttendanceData()
        }.getOrNull()

        if (remote != null) {
            emit(remote)
            return@flow
        }

        delay(250)
        val totalPresent = classAttendance.sumOf { it.present }
        val totalAbsent = classAttendance.sumOf { it.total - it.present }
        val totalStudents = classAttendance.sumOf { it.total }
        emit(
            StudentAttendanceData(
                classWise = classAttendance,
                totalPresent = totalPresent,
                totalAbsent = totalAbsent,
                attendancePercentage = (totalPresent.toFloat() / totalStudents) * 100f
            )
        )
    }

    fun getFeeTransactions(): Flow<List<FeeTransaction>> = flow {
        val remote = runCatching {
            fetchDataArray("/fees/transactions").toFeeTransactions()
        }.getOrNull()

        delay(250)
        emit(remote ?: feeTransactions)
    }

    fun getAdmissions(): Flow<List<AdmissionRecord>> = flow {
        val remote = runCatching {
            fetchDataArray("/admissions").toAdmissions()
        }.getOrNull()

        delay(250)
        emit(remote ?: admissions)
    }

    fun getStaffList(): Flow<List<AttendanceRecord>> = flow {
        val remote = runCatching {
            fetchDataArray("/staff").toStaffList()
        }.getOrNull()

        delay(250)
        emit(remote ?: staffList)
    }

    fun getStudentList(): Flow<List<StudentInfo>> = flow {
        val remote = runCatching {
            fetchDataArray("/students").toStudentList()
        }.getOrNull()

        if (remote != null) {
            emit(remote)
            return@flow
        }

        delay(250)
        val dbStudents = database?.studentDao()?.getAllStudents()?.map {
            StudentInfo(name = it.name, className = it.className, rollNumber = it.rollNumber)
        } ?: studentList
        emit(dbStudents)
    }

    fun getPagedStudentList(): Flow<PagingData<StudentInfo>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { database!!.studentDao().getPagedStudents() }
        ).flow.map { pagingData ->
            pagingData.map { entity: com.school.erp.watch.data.db.StudentEntity ->
                StudentInfo(name = entity.name, className = entity.className, rollNumber = entity.rollNumber)
            }
        }
    }
    /**
     * Sends the device's notification token to the backend server.
     * This is typically used for push notifications.
     * Runs on an IO dispatcher (background thread) to not block the main UI.
     */
    suspend fun registerDeviceToken(token: String) = withContext(Dispatchers.IO) {

        val connection =
            (URL("$API_BASE_URL/device-token").openConnection() as HttpURLConnection).apply {

                requestMethod = "POST"
                doOutput = true

                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS

                setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
            }

        try {
            val json = """
            {
                "token":"$token"
            }
        """.trimIndent()

            connection.outputStream.use {
                it.write(json.toByteArray())
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            Log.d(TAG, "Token registration response: $responseCode body=$responseBody")

            if (responseCode !in 200..299) {
                error("Token registration failed with HTTP $responseCode: $responseBody")
            }

        } finally {
            connection.disconnect()
        }
    }

    // --- Helper Methods for Network Calls ---

    /**
     * Fetches a JSON response and extracts a single object from the "data" field.
     */
    private suspend fun fetchDataObject(endpoint: String): JSONObject {
        return fetchRoot(endpoint).getJSONObject("data")
    }

    /**
     * Fetches a JSON response and extracts an array from the "data" field.
     */
    private suspend fun fetchDataArray(endpoint: String): JSONArray {
        return fetchRoot(endpoint).getJSONArray("data")
    }

    private suspend fun fetchRoot(endpoint: String): JSONObject = withContext(Dispatchers.IO) {
        Log.d(TAG, "Making request to: $API_BASE_URL$endpoint")
        val connection = (URL("$API_BASE_URL$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }

        try {
            val status = connection.responseCode
            Log.d(TAG, "Response status: $status")
            
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Response body: $body")

            if (status !in 200..299) {
                error("API returned HTTP $status")
            }

            val root = JSONObject(body)
            if (!root.optBoolean("success", true)) {
                error("API returned success=false")
            }

            root
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchRoot: ${e.message}", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }

    // --- Extension Functions for JSON Parsing ---
    // These functions convert a raw JSONObject/JSONArray into our typed data models

    private fun JSONObject.toDashboardStats(): DashboardStats {
        return DashboardStats(
            date = optString("date", today),
            staffPresent = optInt("staffPresent", 0),
            staffAbsent = optInt("staffAbsent", 0),
            totalStaff = optInt("totalStaff", 0),
            studentsPresent = optInt("studentsPresent", 0),
            studentsAbsent = optInt("studentsAbsent", 0),
            totalStudents = optInt("totalStudents", 0),
            feesCollected = optDouble("feesCollected", 0.0),
            feeTransactionCount = optInt("feeTransactionCount", 0),
            newAdmissions = optInt("newAdmissions", 0),
            principalName = optString("principalName", "Vedant Shekhar")
        )
    }

    private fun JSONObject.toStaffAttendanceData(): StaffAttendanceData {
        val records = getJSONArray("records").mapObjects { item ->
            AttendanceRecord(
                name = item.optString("name"),
                role = item.optString("role"),
                isPresent = item.optBoolean("isPresent"),
                checkInTime = item.optString("checkInTime")
            )
        }

        return StaffAttendanceData(
            records = records,
            presentCount = optInt("presentCount", records.count { it.isPresent }),
            absentCount = optInt("absentCount", records.count { !it.isPresent }),
            attendancePercentage = optDouble("attendancePercentage", 0.0).toFloat()
        )
    }

    private fun JSONObject.toStudentAttendanceData(): StudentAttendanceData {
        val classWise = getJSONArray("classWise").mapObjects { item ->
            ClassAttendance(
                className = item.optString("className"),
                present = item.optInt("present"),
                total = item.optInt("total")
            )
        }

        return StudentAttendanceData(
            classWise = classWise,
            totalPresent = optInt("totalPresent", classWise.sumOf { it.present }),
            totalAbsent = optInt("totalAbsent", classWise.sumOf { it.total - it.present }),
            attendancePercentage = optDouble("attendancePercentage", 0.0).toFloat()
        )
    }

    private fun JSONArray.toFeeTransactions(): List<FeeTransaction> {
        return mapObjects { item ->
            FeeTransaction(
                studentName = item.optString("studentName"),
                className = item.optString("className"),
                amount = item.optDouble("amount"),
                time = item.optString("time"),
                type = item.optString("type")
            )
        }
    }

    private fun JSONArray.toAdmissions(): List<AdmissionRecord> {
        return mapObjects { item ->
            AdmissionRecord(
                studentName = item.optString("studentName"),
                className = item.optString("className"),
                admissionNumber = item.optString("admissionNumber"),
                time = item.optString("time"),
                parentName = item.optString("parentName")
            )
        }
    }

    private fun JSONArray.toStaffList(): List<AttendanceRecord> {
        return mapObjects { item ->
            AttendanceRecord(
                name = item.optString("name"),
                role = item.optString("role"),
                isPresent = item.optBoolean("isPresent"),
                checkInTime = item.optString("checkInTime")
            )
        }
    }

    private fun JSONArray.toStudentList(): List<StudentInfo> {
        return mapObjects { item ->
            StudentInfo(
                name = item.optString("name"),
                className = item.optString("className"),
                rollNumber = item.optString("rollNumber")
            )
        }
    }

    private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return List(length()) { index -> transform(getJSONObject(index)) }
    }
}
