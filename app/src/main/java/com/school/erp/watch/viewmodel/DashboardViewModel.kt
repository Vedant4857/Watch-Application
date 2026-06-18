package com.school.erp.watch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.school.erp.watch.data.*
import com.school.erp.watch.domain.model.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class DashboardViewModel : ViewModel() {

    private val repository = SchoolDataRepository()

    private val _dashboardStats = MutableStateFlow<UiState<DashboardStats>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<DashboardStats>> = _dashboardStats.asStateFlow()

    private val _staffAttendance = MutableStateFlow<UiState<StaffAttendanceData>>(UiState.Loading)
    val staffAttendance: StateFlow<UiState<StaffAttendanceData>> = _staffAttendance.asStateFlow()

    private val _studentAttendance = MutableStateFlow<UiState<StudentAttendanceData>>(UiState.Loading)
    val studentAttendance: StateFlow<UiState<StudentAttendanceData>> = _studentAttendance.asStateFlow()

    private val _feeTransactions = MutableStateFlow<UiState<List<FeeTransaction>>>(UiState.Loading)
    val feeTransactions: StateFlow<UiState<List<FeeTransaction>>> = _feeTransactions.asStateFlow()

    private val _admissions = MutableStateFlow<UiState<List<AdmissionRecord>>>(UiState.Loading)
    val admissions: StateFlow<UiState<List<AdmissionRecord>>> = _admissions.asStateFlow()

    private val _staffList = MutableStateFlow<UiState<List<StaffInfo>>>(UiState.Loading)
    val staffList: StateFlow<UiState<List<StaffInfo>>> = _staffList.asStateFlow()

    private val _studentList = MutableStateFlow<UiState<List<StudentInfo>>>(UiState.Loading)
    val studentList: StateFlow<UiState<List<StudentInfo>>> = _studentList.asStateFlow()

    private val _staffLeaves = MutableStateFlow<UiState<List<StaffLeave>>>(UiState.Loading)
    val staffLeaves: StateFlow<UiState<List<StaffLeave>>> = _staffLeaves.asStateFlow()

    private val _studentLeaves = MutableStateFlow<UiState<List<StudentLeave>>>(UiState.Loading)
    val studentLeaves: StateFlow<UiState<List<StudentLeave>>> = _studentLeaves.asStateFlow()

    private val _notifications = MutableStateFlow<UiState<List<Notification>>>(UiState.Loading)
    val notifications: StateFlow<UiState<List<Notification>>> = _notifications.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<UiState<List<Event>>>(UiState.Loading)
    val upcomingEvents: StateFlow<UiState<List<Event>>> = _upcomingEvents.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        loadDashboard()
        loadStaffAttendance()
        loadStudentAttendance()
        loadFees()
        loadAdmissions()
        loadStaffList()
        loadStudentList()
        loadStaffLeaves()
        loadStudentLeaves()
        loadUpcomingEvents()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _dashboardStats.value = UiState.Loading
            repository.getDashboardStats()
                .catch { e -> _dashboardStats.value = UiState.Error(e.message ?: "Error") }
                .collect { _dashboardStats.value = UiState.Success(it) }
        }
    }

    private fun loadStaffAttendance() {
        viewModelScope.launch {
            _staffAttendance.value = UiState.Loading
            repository.getStaffAttendance()
                .catch { e -> _staffAttendance.value = UiState.Error(e.message ?: "Error") }
                .collect { _staffAttendance.value = UiState.Success(it) }
        }
    }

    private fun loadStudentAttendance() {
        viewModelScope.launch {
            _studentAttendance.value = UiState.Loading
            repository.getStudentAttendance()
                .catch { e -> _studentAttendance.value = UiState.Error(e.message ?: "Error") }
                .collect { _studentAttendance.value = UiState.Success(it) }
        }
    }

    private fun loadFees() {
        viewModelScope.launch {
            _feeTransactions.value = UiState.Loading
            repository.getFeeTransactions()
                .catch { e -> _feeTransactions.value = UiState.Error(e.message ?: "Error") }
                .collect { _feeTransactions.value = UiState.Success(it) }
        }
    }

    private fun loadAdmissions() {
        viewModelScope.launch {
            _admissions.value = UiState.Loading
            repository.getAdmissions()
                .catch { e -> _admissions.value = UiState.Error(e.message ?: "Error") }
                .collect { _admissions.value = UiState.Success(it) }
        }
    }

    private fun loadStaffList() {
        viewModelScope.launch {
            _staffList.value = UiState.Loading
            repository.getStaffList()
                .catch { e -> _staffList.value = UiState.Error(e.message ?: "Error") }
                .collect { _staffList.value = UiState.Success(it) }
        }
    }

    private fun loadStudentList() {
        viewModelScope.launch {
            _studentList.value = UiState.Loading
            repository.getStudentList()
                .catch { e -> _studentList.value = UiState.Error(e.message ?: "Error") }
                .collect { _studentList.value = UiState.Success(it) }
        }
    }

    private fun loadStaffLeaves() {
        viewModelScope.launch {
            _staffLeaves.value = UiState.Loading
            repository.getStaffLeaves()
                .catch { e -> _staffLeaves.value = UiState.Error(e.message ?: "Error") }
                .collect { _staffLeaves.value = UiState.Success(it) }
        }
    }

    private fun loadStudentLeaves() {
        viewModelScope.launch {
            _studentLeaves.value = UiState.Loading
            repository.getStudentLeaves()
                .catch { e -> _studentLeaves.value = UiState.Error(e.message ?: "Error") }
                .collect { _studentLeaves.value = UiState.Success(it) }
        }
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            _upcomingEvents.value = UiState.Loading
            repository.getUpcomingEvents()
                .catch { e -> _upcomingEvents.value = UiState.Error(e.message ?: "Error") }
                .collect { _upcomingEvents.value = UiState.Success(it) }
        }
        viewModelScope.launch {
            repository.getNotifications()
                .catch { e -> _notifications.value = UiState.Error(e.message ?: "Error") }
                .collect { _notifications.value = UiState.Success(it) }
        }
    }

    fun handleStaffLeaveStatus(id: Int, status: String) {
        viewModelScope.launch {
            try {
                repository.updateStaffLeaveStatus(id, status)
                loadStaffLeaves() // Refresh list
            } catch (e: Exception) {
                // Log error or show toast in a real app
            }
        }
    }

    fun handleStudentLeaveStatus(id: Int, status: String) {
        viewModelScope.launch {
            try {
                repository.updateStudentLeaveStatus(id, status)
                refresh()
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun refresh() {
        loadAllData()
    }
}
