package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey val schoolId: String,
    val schoolName: String,
    val district: String,
    val division: String,
    val principalName: String,
    val phone: String,
    val status: String // "Active", "Inactive"
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val teacherId: String,
    val name: String,
    val email: String,
    val phone: String,
    val school: String,
    val role: String, // "Principal", "In-Charge Teacher"
    val photo: String,
    val status: String // "Active", "Inactive"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val fullName: String,
    val photo: String,
    val gender: String,
    val dateOfBirth: String,
    val grade: String,
    val school: String,
    val division: String,
    val guardianName: String,
    val guardianPhone: String,
    val address: String,
    val projectName: String,
    val qrCodeId: String,
    val status: String, // "Active", "Inactive"
    val createdDate: String
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val attendanceId: String,
    val studentId: String,
    val studentName: String,
    val school: String,
    val grade: String,
    val teacherName: String,
    val teacherId: String,
    val userRole: String,
    val date: String, // "yyyy-MM-dd"
    val time: String, // "HH:mm:ss"
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val schoolLocation: String?,
    val distance: Double?,
    val deviceName: String,
    val attendanceStatus: String, // "Present", "Late"
    val createdAt: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val school: String,
    val role: String, // "Admin", "Principal", "Teacher"
    val email: String,
    val phone: String,
    val status: String, // "Active", "Inactive"
    val lastLogin: String
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String = "current_settings",
    val organizationName: String = "International Medical Health Organization (IMHO)",
    val organizationLogo: String = "",
    val attendanceTime: String = "08:00 AM",
    val allowGps: Boolean = false,
    val allowOffline: Boolean = true,
    val exportSettings: String = "",
    val notificationSettings: String = "",
    val language: String = "English",
    val darkMode: Boolean = false
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val logId: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val details: String,
    val timestamp: Long,
    val dateTime: String
)
