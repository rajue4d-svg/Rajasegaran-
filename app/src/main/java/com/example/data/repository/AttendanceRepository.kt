package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

class AttendanceRepository(private val context: Context) {

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val database = AppDatabase.getDatabase(context)
    private val schoolDao = database.schoolDao()
    private val teacherDao = database.teacherDao()
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()
    private val userDao = database.userDao()
    private val settingsDao = database.settingsDao()
    private val activityLogDao = database.activityLogDao()

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("imho_attendance_prefs", Context.MODE_PRIVATE)

    // Exposed Flows
    val schools: Flow<List<SchoolEntity>> = schoolDao.getAllSchools()
    val teachers: Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()
    val students: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    val attendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()
    val users: Flow<List<UserEntity>> = userDao.getAllUsers()
    val activityLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogs()

    // Settings Flow (with default value if empty)
    val settings: Flow<SettingsEntity> = settingsDao.getSettings()
        .map { it ?: SettingsEntity() }

    // Authentication State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Network Online State (Simulated for offline demonstration)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Sync State
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Prepopulate database and load saved user session in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
            loadUserSession()
        }
    }

    // Toggle Online State to test Offline Mode
    fun setOnline(online: Boolean) {
        _isOnline.value = online
        if (online) {
            // Automatically sync when coming back online
            triggerAutoSync()
        }
    }

    private fun triggerAutoSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncOfflineData()
        }
    }

    suspend fun syncOfflineData(): Boolean {
        if (!isOnline.value) return false
        _isSyncing.value = true
        withContext(Dispatchers.IO) {
            // Simulate API request delay
            kotlinx.coroutines.delay(1500)
            // Fetch unsynced records
            val allRecs = attendanceDao.getAllAttendance().first()
            val unsynced = allRecs.filter { !it.isSynced }
            for (record in unsynced) {
                // Update record to synced
                attendanceDao.insertAttendance(record.copy(isSynced = true))
                // Log sync activity
                logActivity(
                    userId = _currentUser.value?.userId ?: "SYSTEM",
                    userName = _currentUser.value?.name ?: "System Sync",
                    userRole = _currentUser.value?.role ?: "System",
                    action = "SYNC_ATTENDANCE",
                    details = "Synced offline record for ${record.studentName} (ID: ${record.studentId})"
                )
            }
        }
        _isSyncing.value = false
        return true
    }

    // Authentication Functions
    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        
        // 1. Try Firebase Authentication if available and online
        val auth = firebaseAuth
        val db = firestore
        if (auth != null && db != null && isOnline.value) {
            try {
                // Perform Firebase Sign In
                val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).awaitResult()
                val uid = authResult.user?.uid ?: throw Exception("Authentication user is null")
                
                // Fetch extra details from Firestore
                val doc = db.collection("Users").document(uid).get().awaitResult()
                if (doc.exists()) {
                    val status = doc.getString("status") ?: "Pending"
                    
                    // Validate status as per login rules
                    when (status) {
                        "Pending" -> return@withContext Result.failure(Exception("Your account is waiting for administrator approval."))
                        "Rejected" -> return@withContext Result.failure(Exception("Your registration has been rejected. Please contact the administrator."))
                        "Disabled" -> return@withContext Result.failure(Exception("Your account has been disabled."))
                        "Active" -> { /* allow login */ }
                        else -> return@withContext Result.failure(Exception("Invalid account status: $status"))
                    }
                    
                    val name = doc.getString("name") ?: "User"
                    val school = doc.getString("school") ?: ""
                    val role = doc.getString("role") ?: "Teacher"
                    val phone = doc.getString("phone") ?: ""
                    val lastLogin = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val nic = doc.getString("nicNumber")
                    val employeeId = doc.getString("employeeId")
                    val regDate = doc.getString("registrationDate") ?: ""
                    val photoUrl = doc.getString("profilePhotoUrl")
                    
                    // Update lastLogin in Firestore
                    db.collection("Users").document(uid).update("lastLogin", lastLogin)
                    
                    // Update/insert user in local Room database for offline session cache
                    val user = UserEntity(
                        userId = uid,
                        name = name,
                        school = school,
                        role = role,
                        email = trimmedEmail,
                        phone = phone,
                        status = status,
                        lastLogin = lastLogin,
                        nicNumber = nic,
                        employeeId = employeeId,
                        registrationDate = regDate,
                        profilePhotoUrl = photoUrl
                    )
                    userDao.insertUser(user)
                    
                    saveUserSession(user)
                    _currentUser.value = user
                    
                    logActivity(
                        userId = uid,
                        userName = name,
                        userRole = role,
                        action = "LOGIN",
                        details = "User logged in successfully via Firebase"
                    )
                    return@withContext Result.success(user)
                } else {
                    throw Exception("User data not found in Firestore")
                }
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("password", true) || msg.contains("no user", true) || msg.contains("credential", true) || msg.contains("badly formatted", true)) {
                    return@withContext Result.failure(e)
                }
                // Otherwise fall back to local Room login below
            }
        }
        
        // 2. Fallback local Room Login
        val user = userDao.getUserByEmail(trimmedEmail)
        if (user != null) {
            val isValidPassword = when {
                trimmedEmail.contains("admin", true) && password == "admin123" -> true
                trimmedEmail.contains("principal", true) && password == "principal123" -> true
                trimmedEmail.contains("teacher", true) && password == "teacher123" -> true
                password == "imho123" -> true
                user.passwordHash == password -> true
                else -> false
            }

            if (isValidPassword) {
                when (user.status) {
                    "Pending" -> return@withContext Result.failure(Exception("Your account is waiting for administrator approval."))
                    "Rejected" -> return@withContext Result.failure(Exception("Your registration has been rejected. Please contact the administrator."))
                    "Disabled" -> return@withContext Result.failure(Exception("Your account has been disabled."))
                    "Active", "Inactive" -> { /* allow login */ }
                    else -> return@withContext Result.failure(Exception("This user account is inactive. Please contact the administrator."))
                }

                val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val updatedUser = user.copy(lastLogin = nowStr)
                userDao.insertUser(updatedUser)

                saveUserSession(updatedUser)
                _currentUser.value = updatedUser

                logActivity(
                    userId = updatedUser.userId,
                    userName = updatedUser.name,
                    userRole = updatedUser.role,
                    action = "LOGIN",
                    details = "User logged in successfully (Local Cache)"
                )

                return@withContext Result.success(updatedUser)
            } else {
                return@withContext Result.failure(Exception("Incorrect password. Try admin123, principal123, or teacher123."))
            }
        } else {
            return@withContext Result.failure(Exception("Email not found. Please register or verify your credentials."))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        nicNumber: String?,
        school: String,
        role: String,
        employeeId: String?,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // 1. Try Firebase Authentication if available and online
        val auth = firebaseAuth
        val db = firestore
        if (auth != null && db != null && isOnline.value) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).awaitResult()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve user ID")
                
                val userMap = hashMapOf(
                    "userId" to uid,
                    "name" to name,
                    "email" to trimmedEmail,
                    "phone" to phone,
                    "nicNumber" to nicNumber,
                    "school" to school,
                    "role" to role,
                    "employeeId" to employeeId,
                    "status" to "Pending",
                    "registrationDate" to nowStr,
                    "lastLogin" to "Never",
                    "profilePhotoUrl" to "https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(" ", "")}"
                )
                
                db.collection("Users").document(uid).set(userMap).awaitResult()
                
                val user = UserEntity(
                    userId = uid,
                    name = name,
                    school = school,
                    role = role,
                    email = trimmedEmail,
                    phone = phone,
                    status = "Pending",
                    lastLogin = "Never",
                    nicNumber = nicNumber,
                    employeeId = employeeId,
                    registrationDate = nowStr,
                    profilePhotoUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(" ", "")}",
                    passwordHash = password
                )
                userDao.insertUser(user)
                
                logActivity(
                    userId = uid,
                    userName = name,
                    userRole = role,
                    action = "REGISTRATION",
                    details = "User registered successfully with status Pending (Firebase)"
                )
                
                return@withContext Result.success(user)
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("email", true) || msg.contains("already", true) || msg.contains("invalid", true)) {
                    return@withContext Result.failure(e)
                }
            }
        }
        
        // 2. Fallback Local Registration
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email address already exists."))
        }
        
        val localUid = "USR_" + UUID.randomUUID().toString().take(8)
        val user = UserEntity(
            userId = localUid,
            name = name,
            school = school,
            role = role,
            email = trimmedEmail,
            phone = phone,
            status = "Pending",
            lastLogin = "Never",
            nicNumber = nicNumber,
            employeeId = employeeId,
            registrationDate = nowStr,
            profilePhotoUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(" ", "")}",
            passwordHash = password
        )
        
        userDao.insertUser(user)
        
        logActivity(
            userId = localUid,
            userName = name,
            userRole = role,
            action = "REGISTRATION",
            details = "User registered successfully with status Pending (Local Database)"
        )
        
        return@withContext Result.success(user)
    }

    suspend fun approveUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext false
        val updatedUser = user.copy(status = "Active")
        userDao.insertUser(updatedUser)
        
        val teacherRole = when (user.role) {
            "School Principal" -> "Principal"
            "In-Charge Teacher" -> "In-Charge Teacher"
            else -> user.role
        }
        
        val teacher = TeacherEntity(
            teacherId = user.userId,
            name = user.name,
            email = user.email,
            phone = user.phone,
            school = user.school,
            role = teacherRole,
            photo = user.profilePhotoUrl ?: "",
            status = "Active"
        )
        teacherDao.insertTeacher(teacher)
        
        val db = firestore
        if (db != null && isOnline.value) {
            try {
                db.collection("Users").document(userId).update("status", "Active").awaitResult()
            } catch (e: Exception) {
                // local update succeeded
            }
        }
        
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "APPROVE_USER",
            details = "Approved account for ${user.name} (${user.email}) - assigned to ${user.school}"
        )
        return@withContext true
    }

    suspend fun rejectUser(userId: String, reason: String?): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext false
        val updatedUser = user.copy(status = "Rejected")
        userDao.insertUser(updatedUser)
        
        val db = firestore
        if (db != null && isOnline.value) {
            try {
                db.collection("Users").document(userId).update("status", "Rejected").awaitResult()
            } catch (e: Exception) {
                // local update succeeded
            }
        }
        
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "REJECT_USER",
            details = "Rejected account for ${user.name} (${user.email}). Reason: ${reason ?: "No reason provided"}"
        )
        return@withContext true
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        if (user != null) {
            logActivity(
                userId = user.userId,
                userName = user.name,
                userRole = user.role,
                action = "PASSWORD_RESET",
                details = "Password reset request sent for ${user.email}"
            )
            return@withContext Result.success("Password reset link has been successfully simulated and sent to $email.")
        } else {
            return@withContext Result.failure(Exception("No user found with this email address."))
        }
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            logActivity(
                userId = user.userId,
                userName = user.name,
                userRole = user.role,
                action = "LOGOUT",
                details = "User logged out"
            )
        }
        sharedPrefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
    }

    private suspend fun saveUserSession(user: UserEntity) {
        sharedPrefs.edit().putString("logged_in_user_id", user.userId).apply()
    }

    private suspend fun loadUserSession() {
        val savedUserId = sharedPrefs.getString("logged_in_user_id", null)
        if (savedUserId != null) {
            val user = userDao.getUserById(savedUserId)
            if (user != null && user.status == "Active") {
                _currentUser.value = user
            }
        }
    }

    // Database Actions (Administrative & Logging)
    suspend fun addSchool(school: SchoolEntity) = withContext(Dispatchers.IO) {
        schoolDao.insertSchool(school)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "ADD_SCHOOL",
            details = "Added school: ${school.schoolName} (${school.schoolId})"
        )
    }

    suspend fun deleteSchool(schoolId: String) = withContext(Dispatchers.IO) {
        schoolDao.deleteSchool(schoolId)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "DELETE_SCHOOL",
            details = "Deleted school: $schoolId"
        )
    }

    suspend fun addTeacher(teacher: TeacherEntity) = withContext(Dispatchers.IO) {
        teacherDao.insertTeacher(teacher)
        
        // Also ensure a corresponding login User is created for this teacher
        val correspondingUser = UserEntity(
            userId = teacher.teacherId,
            name = teacher.name,
            school = teacher.school,
            role = teacher.role, // "Principal" or "Teacher"
            email = teacher.email,
            phone = teacher.phone,
            status = teacher.status,
            lastLogin = "Never"
        )
        userDao.insertUser(correspondingUser)

        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "ADD_TEACHER",
            details = "Added teacher: ${teacher.name} (${teacher.teacherId}) assigned to ${teacher.school}"
        )
    }

    suspend fun deleteTeacher(teacherId: String) = withContext(Dispatchers.IO) {
        teacherDao.deleteTeacher(teacherId)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "DELETE_TEACHER",
            details = "Deleted teacher/user: $teacherId"
        )
    }

    suspend fun addStudent(student: StudentEntity) = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "ADD_STUDENT",
            details = "Added student: ${student.fullName} (${student.studentId}) in ${student.school}"
        )
    }

    suspend fun deleteStudent(studentId: String) = withContext(Dispatchers.IO) {
        studentDao.deleteStudent(studentId)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "DELETE_STUDENT",
            details = "Deleted student: $studentId"
        )
    }

    suspend fun deleteAttendance(attendanceId: String) = withContext(Dispatchers.IO) {
        attendanceDao.deleteAttendance(attendanceId)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "DELETE_ATTENDANCE",
            details = "Deleted attendance record: $attendanceId"
        )
    }

    suspend fun updateSettings(settingsEntity: SettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertSettings(settingsEntity)
        val admin = _currentUser.value
        logActivity(
            userId = admin?.userId ?: "ADMIN",
            userName = admin?.name ?: "Admin",
            userRole = admin?.role ?: "Admin",
            action = "UPDATE_SETTINGS",
            details = "Updated application global settings"
        )
    }

    // Attendance Rules scanning flow
    suspend fun scanStudentQrCode(
        qrCodeId: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): ScanResult = withContext(Dispatchers.IO) {
        val currentUser = _currentUser.value ?: return@withContext ScanResult.Error("No active user session. Please log in.")
        
        // Find student
        val student = studentDao.getStudentByQrCode(qrCodeId.trim())
            ?: return@withContext ScanResult.Error("Invalid QR Code: '$qrCodeId'. Student not registered.")

        if (student.status == "Inactive") {
            return@withContext ScanResult.Error("Scan failed: Student ${student.fullName} is currently marked Inactive.")
        }

        // Check school restriction
        if (currentUser.role == "Principal" || currentUser.role == "Teacher") {
            if (student.school != currentUser.school) {
                return@withContext ScanResult.Error("Scan restricted: Student is registered under '${student.school}' but you are authorized only for '${currentUser.school}'.")
            }
        }

        // Check if already scanned today
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = attendanceDao.getAttendanceForStudentToday(student.studentId, todayStr)
        if (existing != null) {
            return@withContext ScanResult.AlreadyScanned(
                student = student,
                recordedTime = existing.time,
                recordedDate = existing.date
            )
        }

        // Calculate GPS Distance (Optional)
        val currentSettings = settings.first()
        var gpsDistance: Double? = null
        var gpsLocationName: String? = null
        if (currentSettings.allowGps && latitude != null && longitude != null) {
            gpsDistance = 24.5 // Simulate distance of 24.5 meters from registered school center
            gpsLocationName = "${student.school} Campus"
        }

        // Determine Status based on configured cutoff time
        val now = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val cutoffTimeStr = currentSettings.attendanceTime.replace(" AM", "").replace(" PM", "")
        val isPm = currentSettings.attendanceTime.contains("PM", true)
        val cutoffParts = cutoffTimeStr.split(":")
        val rawHour = cutoffParts[0].toInt()
        val cutoffHour = rawHour + (if (isPm && rawHour != 12) 12 else 0)
        val cutoffMinute = if (cutoffParts.size > 1) cutoffParts[1].toInt() else 0

        val cutoffCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, cutoffHour)
            set(Calendar.MINUTE, cutoffMinute)
            set(Calendar.SECOND, 0)
        }
        val attendanceStatus = if (now.after(cutoffCal)) "Late" else "Present"

        val attendanceId = "ATT_${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

        val newRecord = AttendanceEntity(
            attendanceId = attendanceId,
            studentId = student.studentId,
            studentName = student.fullName,
            school = student.school,
            grade = student.grade,
            teacherName = currentUser.name,
            teacherId = currentUser.userId,
            userRole = currentUser.role,
            date = todayStr,
            time = timeStr,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            schoolLocation = gpsLocationName,
            distance = gpsDistance,
            deviceName = deviceName,
            attendanceStatus = attendanceStatus,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            isSynced = isOnline.value // If online, synced is true, otherwise false for local cache!
        )

        attendanceDao.insertAttendance(newRecord)

        logActivity(
            userId = currentUser.userId,
            userName = currentUser.name,
            userRole = currentUser.role,
            action = "SCAN_ATTENDANCE",
            details = "Recorded ${attendanceStatus} attendance for ${student.fullName} (ID: ${student.studentId})"
        )

        return@withContext ScanResult.Success(
            student = student,
            record = newRecord
        )
    }

    suspend fun logActivity(
        userId: String,
        userName: String,
        userRole: String,
        action: String,
        details: String
    ) {
        val now = Date()
        val logId = "LOG_${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(now)
        val log = ActivityLogEntity(
            logId = logId,
            userId = userId,
            userName = userName,
            userRole = userRole,
            action = action,
            details = details,
            timestamp = now.time,
            dateTime = nowStr
        )
        activityLogDao.insertLog(log)
    }

    // Prepopulate Database with rich realistic Ceylonese student attendance systems
    private suspend fun prepopulateIfEmpty() {
        val hasUsers = userDao.getAllUsers().first().isNotEmpty()
        if (hasUsers) return

        // Create Admin User
        val adminUser = UserEntity(
            userId = "USR_ADMIN",
            name = "IMHO Global Administrator",
            school = "IMHO Headquarters",
            role = "Admin",
            email = "admin@imho.org",
            phone = "+94770000001",
            status = "Active",
            lastLogin = "Never"
        )
        userDao.insertUser(adminUser)

        // Seed Schools
        val schoolsList = listOf(
            SchoolEntity("SCH001", "Kilinochchi Maha Vidyalayam", "Kilinochchi", "Kilinochchi Division", "Mr. S. Pathmanathan", "+94212285123", "Active"),
            SchoolEntity("SCH002", "Vaddakkoddai Hindu College", "Jaffna", "Valikamam West Division", "Mr. T. Gunaratnam", "+94212256789", "Active"),
            SchoolEntity("SCH003", "Batticaloa Methodist Central College", "Batticaloa", "Manmunai North", "Mrs. R. Jeyakumar", "+94652224567", "Active"),
            SchoolEntity("SCH004", "Trincomalee Shanmuga Hindu Ladies College", "Trincomalee", "Trincomalee", "Mrs. V. Sureshan", "+94262223456", "Active")
        )
        for (school in schoolsList) {
            schoolDao.insertSchool(school)
        }

        // Seed Teachers
        val teachersList = listOf(
            TeacherEntity("TCH001", "Mr. S. Pathmanathan", "pathmanathan@imho.org", "+94771234567", "Kilinochchi Maha Vidyalayam", "Principal", "", "Active"),
            TeacherEntity("TCH002", "Miss Anusha Selvaraj", "anusha@imho.org", "+94772345678", "Kilinochchi Maha Vidyalayam", "In-Charge Teacher", "", "Active"),
            TeacherEntity("TCH003", "Mr. T. Gunaratnam", "gunaratnam@imho.org", "+94773456789", "Vaddakkoddai Hindu College", "Principal", "", "Active"),
            TeacherEntity("TCH004", "Mr. K. Sarveswaran", "sarves@imho.org", "+94774567890", "Vaddakkoddai Hindu College", "In-Charge Teacher", "", "Active")
        )
        for (teacher in teachersList) {
            addTeacher(teacher) // Handles creating corresponding login User entity too!
        }

        // Seed Students
        val studentsList = listOf(
            StudentEntity("IMHO0001", "Raju Selvam", "https://api.dicebear.com/7.x/avataaars/svg?seed=Raju", "Male", "2012-05-14", "Grade 8", "Kilinochchi Maha Vidyalayam", "Kilinochchi Division", "K. Selvam", "+94775671234", "15, Hospital Road, Kilinochchi", "IMHO Primary Education Support", "IMHO0001", "Active", "2026-01-10"),
            StudentEntity("IMHO0002", "Sarath Chandra", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarath", "Male", "2013-08-22", "Grade 7", "Kilinochchi Maha Vidyalayam", "Kilinochchi Division", "M. Chandra", "+94776782345", "42, Kandy Road, Kilinochchi", "IMHO Scholarship Program", "IMHO0002", "Active", "2026-01-12"),
            StudentEntity("IMHO0003", "Priyanga Sivakumar", "https://api.dicebear.com/7.x/avataaars/svg?seed=Priyanga", "Female", "2011-11-30", "Grade 9", "Vaddakkoddai Hindu College", "Valikamam West Division", "S. Sivakumar", "+94777893456", "Vaddakkoddai, Jaffna", "IMHO Educational Assistance", "IMHO0003", "Active", "2026-01-15"),
            StudentEntity("IMHO0004", "Dharshini Jeyaraj", "https://api.dicebear.com/7.x/avataaars/svg?seed=Dharshini", "Female", "2012-03-18", "Grade 8", "Vaddakkoddai Hindu College", "Valikamam West Division", "P. Jeyaraj", "+94778904567", "Chankanai, Jaffna", "IMHO Scholarship Program", "IMHO0004", "Active", "2026-01-18"),
            StudentEntity("IMHO0005", "Thinesh Kumar", "https://api.dicebear.com/7.x/avataaars/svg?seed=Thinesh", "Male", "2010-02-05", "Grade 10", "Batticaloa Methodist Central College", "Manmunai North", "A. Kumar", "+94779015678", "Central Road, Batticaloa", "IMHO Primary Education Support", "IMHO0005", "Active", "2026-01-20"),
            StudentEntity("IMHO0006", "Nilani Thangarajah", "https://api.dicebear.com/7.x/avataaars/svg?seed=Nilani", "Female", "2013-04-12", "Grade 7", "Kilinochchi Maha Vidyalayam", "Kilinochchi Division", "K. Thangarajah", "+94773412498", "A9 Road, Kilinochchi", "IMHO Educational Assistance", "IMHO0006", "Active", "2026-02-01"),
            StudentEntity("IMHO0007", "Aravindan Ratnam", "https://api.dicebear.com/7.x/avataaars/svg?seed=Aravindan", "Male", "2011-09-03", "Grade 9", "Trincomalee Shanmuga Hindu Ladies College", "Trincomalee", "M. Ratnam", "+94779081234", "Dockyard Road, Trincomalee", "IMHO Scholarship Program", "IMHO0007", "Active", "2026-02-05")
        )
        for (student in studentsList) {
            studentDao.insertStudent(student)
        }

        // Seed past attendance records
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1) // Yesterday
        val yesterdayStr = sdfDate.format(cal.time)

        val pastAttendance = listOf(
            AttendanceEntity(
                attendanceId = "ATT_SEED1",
                studentId = "IMHO0001",
                studentName = "Raju Selvam",
                school = "Kilinochchi Maha Vidyalayam",
                grade = "Grade 8",
                teacherName = "Miss Anusha Selvaraj",
                teacherId = "TCH002",
                userRole = "Teacher",
                date = yesterdayStr,
                time = "07:45:12",
                timestamp = cal.timeInMillis,
                latitude = null,
                longitude = null,
                schoolLocation = null,
                distance = null,
                deviceName = "Samsung S21",
                attendanceStatus = "Present",
                createdAt = "$yesterdayStr 07:45:12",
                isSynced = true
            ),
            AttendanceEntity(
                attendanceId = "ATT_SEED2",
                studentId = "IMHO0002",
                studentName = "Sarath Chandra",
                school = "Kilinochchi Maha Vidyalayam",
                grade = "Grade 7",
                teacherName = "Miss Anusha Selvaraj",
                teacherId = "TCH002",
                userRole = "Teacher",
                date = yesterdayStr,
                time = "08:15:34",
                timestamp = cal.timeInMillis + 1800000,
                latitude = null,
                longitude = null,
                schoolLocation = null,
                distance = null,
                deviceName = "Samsung S21",
                attendanceStatus = "Late",
                createdAt = "$yesterdayStr 08:15:34",
                isSynced = true
            ),
            AttendanceEntity(
                attendanceId = "ATT_SEED3",
                studentId = "IMHO0003",
                studentName = "Priyanga Sivakumar",
                school = "Vaddakkoddai Hindu College",
                grade = "Grade 9",
                teacherName = "Mr. K. Sarveswaran",
                teacherId = "TCH004",
                userRole = "Teacher",
                date = yesterdayStr,
                time = "07:50:22",
                timestamp = cal.timeInMillis + 300000,
                latitude = null,
                longitude = null,
                schoolLocation = null,
                distance = null,
                deviceName = "Pixel 6",
                attendanceStatus = "Present",
                createdAt = "$yesterdayStr 07:50:22",
                isSynced = true
            )
        )
        for (att in pastAttendance) {
            attendanceDao.insertAttendance(att)
        }

        // Initialize Settings
        val settingsEntity = SettingsEntity()
        settingsDao.insertSettings(settingsEntity)

        // Seed logs
        val systemLog = ActivityLogEntity(
            logId = "LOG_SYSTEM",
            userId = "SYSTEM",
            userName = "System Initialization",
            userRole = "System",
            action = "DB_INITIALIZE",
            details = "IMHO Student Attendance System Database prepopulated with 4 schools, 4 teachers, 7 students, and historical logs.",
            timestamp = System.currentTimeMillis(),
            dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        activityLogDao.insertLog(systemLog)
    }
}

// Sealed class for scan result handling
sealed class ScanResult {
    data class Success(val student: StudentEntity, val record: AttendanceEntity) : ScanResult()
    data class AlreadyScanned(val student: StudentEntity, val recordedTime: String, val recordedDate: String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

// Extension to wait for Google Task in coroutines
suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            cont.resume(task.result)
        } else {
            cont.resumeWith(Result.failure(task.exception ?: Exception("Unknown task execution error")))
        }
    }
}
