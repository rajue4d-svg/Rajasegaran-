package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools ORDER BY schoolName ASC")
    fun getAllSchools(): Flow<List<SchoolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: SchoolEntity)

    @Query("DELETE FROM schools WHERE schoolId = :schoolId")
    suspend fun deleteSchool(schoolId: String)
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE teacherId = :teacherId")
    suspend fun deleteTeacher(teacherId: String)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: String)

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE qrCodeId = :qrCodeId LIMIT 1")
    suspend fun getStudentByQrCode(qrCodeId: String): StudentEntity?
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE attendanceId = :attendanceId")
    suspend fun deleteAttendance(attendanceId: String)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentToday(studentId: String, date: String): AttendanceEntity?
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 'current_settings' LIMIT 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)
}
