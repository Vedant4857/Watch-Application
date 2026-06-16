package com.school.erp.watch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.school.erp.watch.data.*
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

    private val _staffList = MutableStateFlow<UiState<List<AttendanceRecord>>>(UiState.Loading)
    val staffList: StateFlow<UiState<List<AttendanceRecord>>> = _staffList.asStateFlow()

    val studentList: Flow<PagingData<StudentInfo>> = repository.getPagedStudentList().cachedIn(viewModelScope)

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

    fun refresh() {
        loadAllData()
    }
}
