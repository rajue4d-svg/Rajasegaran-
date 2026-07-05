package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.ScanResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

enum class DateRangeType {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    CUSTOM_DATE,
    YEARLY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)

    // Repository Flows
    val schools: StateFlow<List<SchoolEntity>> = repository.schools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<TeacherEntity>> = repository.teachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<StudentEntity>> = repository.students
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceEntity>> = repository.attendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<SettingsEntity> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.activityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
    val isOnline: StateFlow<Boolean> = repository.isOnline
    val isSyncing: StateFlow<Boolean> = repository.isSyncing

    // Scanning screen states
    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    val lastScanResult: StateFlow<ScanResult?> = _lastScanResult.asStateFlow()

    private val _showScanConfirmation = MutableStateFlow(false)
    val showScanConfirmation: StateFlow<Boolean> = _showScanConfirmation.asStateFlow()

    fun clearScanResult() {
        _lastScanResult.value = null
        _showScanConfirmation.value = false
    }

    // Auth actions
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun resetPassword(email: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                onSuccess(result.getOrNull() ?: "Reset success")
            } else {
                onError(result.exceptionOrNull()?.message ?: "Reset failed")
            }
        }
    }

    // Toggle simulated internet state
    fun toggleOnline(online: Boolean) {
        repository.setOnline(online)
    }

    // Trigger sync
    fun syncNow(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.syncOfflineData()
            onComplete(success)
        }
    }

    // Admin Actions
    fun addSchool(school: SchoolEntity) {
        viewModelScope.launch {
            repository.addSchool(school)
        }
    }

    fun deleteSchool(schoolId: String) {
        viewModelScope.launch {
            repository.deleteSchool(schoolId)
        }
    }

    fun addTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.addTeacher(teacher)
        }
    }

    fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.deleteTeacher(teacherId)
        }
    }

    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.addStudent(student)
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
        }
    }

    fun deleteAttendance(attendanceId: String) {
        viewModelScope.launch {
            repository.deleteAttendance(attendanceId)
        }
    }

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    // QR Scanning action
    fun processQrCode(qrCodeId: String, latitude: Double? = null, longitude: Double? = null) {
        viewModelScope.launch {
            val result = repository.scanStudentQrCode(qrCodeId, latitude, longitude)
            _lastScanResult.value = result
            _showScanConfirmation.value = true
        }
    }

    // Filter attendance list based on range type, school, custom date, etc.
    fun getFilteredAttendance(
        allRecords: List<AttendanceEntity>,
        rangeType: DateRangeType,
        customDateStr: String? = null,
        schoolFilter: String? = null,
        searchQuery: String? = null
    ): List<AttendanceEntity> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(yesterdayCal.time)

        val weekCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        val monthCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }

        return allRecords.filter { record ->
            // 1. Filter by Date Range
            val matchesRange = when (rangeType) {
                DateRangeType.TODAY -> record.date == todayStr
                DateRangeType.YESTERDAY -> record.date == yesterdayStr
                DateRangeType.CUSTOM_DATE -> {
                    if (customDateStr.isNullOrEmpty()) true
                    else record.date == customDateStr
                }
                DateRangeType.THIS_WEEK -> {
                    try {
                        val recDate = sdf.parse(record.date)
                        recDate != null && recDate.after(weekCal.time)
                    } catch (e: Exception) {
                        true
                    }
                }
                DateRangeType.THIS_MONTH -> {
                    try {
                        val recDate = sdf.parse(record.date)
                        recDate != null && recDate.after(monthCal.time)
                    } catch (e: Exception) {
                        true
                    }
                }
                DateRangeType.YEARLY -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                    record.date.startsWith(currentYear)
                }
            }

            // 2. Filter by School
            val matchesSchool = if (schoolFilter.isNullOrEmpty() || schoolFilter == "All Schools") {
                true
            } else {
                record.school == schoolFilter
            }

            // 3. Filter by Search Query (Student ID, Name, Grade, Scanner)
            val matchesSearch = if (searchQuery.isNullOrEmpty()) {
                true
            } else {
                record.studentName.contains(searchQuery, ignoreCase = true) ||
                        record.studentId.contains(searchQuery, ignoreCase = true) ||
                        record.grade.contains(searchQuery, ignoreCase = true) ||
                        record.teacherName.contains(searchQuery, ignoreCase = true)
            }

            matchesRange && matchesSchool && matchesSearch
        }
    }

    // Excel Export function (produces shareable URI of excel-compatible CSV file)
    fun exportToExcel(
        context: Context,
        filteredRecords: List<AttendanceEntity>,
        fileNamePrefix: String,
        onComplete: (String?, Uri?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "${fileNamePrefix}_$timeStamp.csv"
                
                val cacheFile = File(context.cacheDir, fileName)
                val writer = FileWriter(cacheFile)

                // Write UTF-8 Byte Order Mark (BOM) to force Excel to read accents/special characters correctly
                writer.write("\uFEFF")

                // Headers
                writer.write("Student ID,Student Name,School,Grade,Date,Time,Scanned By,Teacher Role,GPS Verified,GPS Location,GPS Distance (m),Status\n")

                for (rec in filteredRecords) {
                    val gpsVerified = if (rec.latitude != null && rec.longitude != null) "Yes" else "No"
                    val gpsLocation = rec.schoolLocation ?: ""
                    val gpsDist = rec.distance?.toString() ?: ""
                    
                    val line = listOf(
                        rec.studentId,
                        "\"${rec.studentName.replace("\"", "\"\"")}\"",
                        "\"${rec.school.replace("\"", "\"\"")}\"",
                        rec.grade,
                        rec.date,
                        rec.time,
                        "\"${rec.teacherName.replace("\"", "\"\"")}\"",
                        rec.userRole,
                        gpsVerified,
                        "\"${gpsLocation.replace("\"", "\"\"")}\"",
                        gpsDist,
                        rec.attendanceStatus
                    ).joinToString(",") + "\n"
                    
                    writer.write(line)
                }
                writer.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    "com.example.fileprovider",
                    cacheFile
                )

                onComplete(fileName, uri)

                // Log the action
                val user = currentUser.value
                if (user != null) {
                    repository.logActivity(
                        userId = user.userId,
                        userName = user.name,
                        userRole = user.role,
                        action = "EXPORT_REPORT",
                        details = "Exported ${filteredRecords.size} records to file: $fileName"
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null, null)
            }
        }
    }
}
