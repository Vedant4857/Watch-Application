package com.school.erp.watch.data

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "SchoolDataRepository"

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
    val type: String 
)

data class AdmissionRecord(
    val studentName: String,
    val className: String,
    val enrollmentNo: String,
    val time: String,
    val parentName: String
)

data class StaffLeave(
    val id: Int,
    val staffName: String,
    val leaveDate: String,
    val leaveType: String,
    val reason: String,
    val status: String
)

data class StudentLeave(
    val id: Int,
    val studentName: String,
    val className: String,
    val leaveDate: String,
    val leaveType: String,
    val reason: String,
    val status: String
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
    val contactNumber: String,
    val photoUrl: String
)

data class StaffInfo(
    val name: String,
    val role: String,
    val department: String,
    val email: String,
    val contactNumber: String,
    val photoUrl: String
)

class SchoolDataRepository {

    companion object {
        private var API_BASE_URL = ApiConfig.API_BASE_URL
        private const val CONNECT_TIMEOUT_MS = ApiConfig.CONNECT_TIMEOUT_MS
        private const val READ_TIMEOUT_MS = ApiConfig.READ_TIMEOUT_MS

        fun setApiBaseUrl(url: String) {
            API_BASE_URL = url
        }

        fun getApiBaseUrl(): String = API_BASE_URL
    }

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val today = dateFormat.format(Date())

    fun getDashboardStats(): Flow<DashboardStats> = flow {
        emit(fetchDataObject("/principal/dashboard").toDashboardStats())
    }.flowOn(Dispatchers.IO)

    fun getStaffAttendance(): Flow<StaffAttendanceData> = flow {
        emit(fetchDataObject("/attendance/staff").toStaffAttendanceData())
    }.flowOn(Dispatchers.IO)

    fun getStudentAttendance(): Flow<StudentAttendanceData> = flow {
        emit(fetchDataObject("/attendance/students").toStudentAttendanceData())
    }.flowOn(Dispatchers.IO)

    fun getFeeTransactions(): Flow<List<FeeTransaction>> = flow {
        emit(fetchDataArray("/fees/transactions").toFeeTransactions())
    }.flowOn(Dispatchers.IO)

    fun getAdmissions(): Flow<List<AdmissionRecord>> = flow {
        emit(fetchDataArray("/admissions").toAdmissions())
    }.flowOn(Dispatchers.IO)

    fun getStaffList(): Flow<List<StaffInfo>> = flow {
        emit(fetchDataArray("/staff").toStaffList())
    }.flowOn(Dispatchers.IO)

    fun getStudentList(): Flow<List<StudentInfo>> = flow {
        emit(fetchDataArray("/students").toStudentList())
    }.flowOn(Dispatchers.IO)

    fun getStaffLeaves(): Flow<List<StaffLeave>> = flow {
        emit(fetchDataArray("/leaves/staff").toStaffLeaves())
    }.flowOn(Dispatchers.IO)

    fun getStudentLeaves(): Flow<List<StudentLeave>> = flow {
        emit(fetchDataArray("/leaves/students").toStudentLeaves())
    }.flowOn(Dispatchers.IO)

    suspend fun updateStaffLeaveStatus(id: Int, newStatus: String) = withContext(Dispatchers.IO) {
        val connection = (URL("$API_BASE_URL/leaves/staff/$id/status").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val json = """{"status":"$newStatus"}"""
            connection.outputStream.use { it.write(json.toByteArray()) }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                error("Staff leave update failed with HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun updateStudentLeaveStatus(id: Int, newStatus: String) = withContext(Dispatchers.IO) {
        val connection = (URL("$API_BASE_URL/leaves/students/$id/status").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val json = """{"status":"$newStatus"}"""
            connection.outputStream.use { it.write(json.toByteArray()) }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                error("Student leave update failed with HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun registerDeviceToken(token: String) = withContext(Dispatchers.IO) {
        val connection = (URL("$API_BASE_URL/device-token").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val json = """{"token":"$token"}"""
            connection.outputStream.use { it.write(json.toByteArray()) }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                error("Token registration failed with HTTP $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchDataObject(endpoint: String): JSONObject {
        return fetchRoot(endpoint).getJSONObject("data")
    }

    private suspend fun fetchDataArray(endpoint: String): JSONArray {
        return fetchRoot(endpoint).getJSONArray("data")
    }

    private suspend fun fetchRoot(endpoint: String): JSONObject = withContext(Dispatchers.IO) {
        val connection = (URL("$API_BASE_URL$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (status !in 200..299) error("API returned HTTP $status")
            val root = JSONObject(body)
            if (!root.optBoolean("success", true)) error("API returned success=false")
            root
        } catch (e: Exception) {
            throw e
        } finally {
            connection.disconnect()
        }
    }

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
                enrollmentNo = item.optString("enrollmentNo"),
                time = item.optString("time"),
                parentName = item.optString("parentName")
            )
        }
    }

    private fun JSONArray.toStaffList(): List<StaffInfo> {
        return mapObjects { item ->
            StaffInfo(
                name = item.optString("name"),
                role = item.optString("role"),
                department = item.optString("department"),
                email = item.optString("email"),
                contactNumber = item.optString("contactNumber"),
                photoUrl = item.optString("photoUrl")
            )
        }
    }

    private fun JSONArray.toStudentList(): List<StudentInfo> {
        return mapObjects { item ->
            StudentInfo(
                name = item.optString("name"),
                className = item.optString("className"),
                contactNumber = item.optString("contactNumber"),
                photoUrl = item.optString("photo")
            )
        }
    }

    private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> {
        return List(length()) { index -> transform(getJSONObject(index)) }
    }

    private fun JSONArray.toStaffLeaves(): List<StaffLeave> {
        return mapObjects { item ->
            StaffLeave(
                id = item.optInt("id"),
                staffName = item.optString("staffName"),
                leaveDate = item.optString("leaveDate"),
                leaveType = item.optString("leaveType"),
                reason = item.optString("reason"),
                status = item.optString("status")
            )
        }
    }

    private fun JSONArray.toStudentLeaves(): List<StudentLeave> {
        return mapObjects { item ->
            StudentLeave(
                id = item.optInt("id"),
                studentName = item.optString("studentName"),
                className = item.optString("className"),
                leaveDate = item.optString("leaveDate"),
                leaveType = item.optString("leaveType"),
                reason = item.optString("reason"),
                status = item.optString("status")
            )
        }
    }
}
