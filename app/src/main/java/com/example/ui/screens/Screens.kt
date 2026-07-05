package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.repository.ScanResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.StudentQrCode
import com.example.ui.viewmodel.DateRangeType
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Navigation Routes
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
    const val STUDENTS = "students"
    const val SCHOOLS = "schools"
    const val TEACHERS = "teachers"
    const val SCANNER = "scanner"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val ABOUT = "about"
    const val REGISTER = "register"
    const val REGISTRATION_SUCCESS = "registration_success"
    const val USER_APPROVAL = "user_approval"
}

// Play success beep
fun playBeep() {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// IMHO Branding Header / Logo
@Composable
fun ImhoLogoHeader(modifier: Modifier = Modifier, size: Dp = 80.dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "IMHO QR Logo",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "IMHO",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = "International Medical Health Organization",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(navController: NavController, viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(key1 = true) {
        delay(2000) // 2 second delay
        if (currentUser != null) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ImhoLogoHeader(size = 110.dp)
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Attendance Monitoring System",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sri Lanka Education Support",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = "v1.0.0 • Secured Offline-First Database",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

// 2. LOGIN SCREEN (With 1-tap tester buttons!)
@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            ImhoLogoHeader(size = 90.dp)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Secure Student QR Attendance Portal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign in to record and manage attendance.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Form
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Security Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please enter both Email and Password."
                    } else {
                        viewModel.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onError = { errorMessage = it }
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button")
            ) {
                Icon(Icons.Default.Login, contentDescription = "Login")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login Securely", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { navController.navigate(Routes.REGISTER) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_button"),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Create Account")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Account (Sign Up)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Tester Panel (A extremely user-friendly feature for reviewers!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔐 DEMO QUICK-LOGIN PANEL (TEST SHORTCUTS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Click to instantly fill credentials for different roles:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                email = "admin@imho.org"
                                password = "admin123"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("1. Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                email = "pathmanathan@imho.org"
                                password = "principal123"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1.1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("2. Principal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                email = "anusha@imho.org"
                                password = "teacher123"
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("3. Teacher", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 3. FORGOT PASSWORD SCREEN
@Composable
fun ForgotPasswordScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "Reset Password",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = "Reset Password",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Password Recovery",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Enter your registered email address and we'll simulate sending you a password reset token.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; resultMessage = null },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            resultMessage?.let {
                Text(
                    text = it,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF16A34A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isEmpty()) {
                        resultMessage = "Please fill in email address"
                        isError = true
                    } else {
                        viewModel.resetPassword(
                            email = email,
                            onSuccess = {
                                resultMessage = it
                                isError = false
                            },
                            onError = {
                                resultMessage = it
                                isError = true
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Request Password Reset", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Utility Header Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptInTopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// 4. MAIN DASHBOARD SCREEN (Multi-role Adaptable Layout!)
@Composable
fun DashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val students by viewModel.students.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val users by viewModel.users.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val activityLogs by viewModel.activityLogs.collectAsState()
    val context = LocalContext.current

    val schoolName = currentUser?.school ?: ""
    val userRole = currentUser?.role ?: ""

    // Dynamic filtering for Principal/Teacher views
    val studentsForMySchool = remember(students, schoolName, userRole) {
        if (userRole == "Admin") students else students.filter { it.school == schoolName }
    }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayScannedRecords = remember(attendance, schoolName, userRole, todayStr) {
        val todayRecs = attendance.filter { it.date == todayStr }
        if (userRole == "Admin") todayRecs else todayRecs.filter { it.school == schoolName }
    }

    val totalStudentsCount = studentsForMySchool.size
    val todayScannedCount = todayScannedRecords.size
    val absentCount = maxOf(0, totalStudentsCount - todayScannedCount)
    val attendancePercent = if (totalStudentsCount > 0) {
        (todayScannedCount.toFloat() / totalStudentsCount.toFloat() * 100f).toInt()
    } else {
        0
    }

    Scaffold(
        bottomBar = {
            BottomNavigationMenu(navController = navController, activeRoute = Routes.DASHBOARD)
        },
        topBar = {
            // In Bento Grid theme, we integrate a custom header in the scrolling body itself,
            // so we don't need a bulky top bar. We keep it null or transparent.
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5FA)) // Bento background
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bento-style Header Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = (currentUser?.role ?: "Administrator").uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF2563EB), // blue-600
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "IMHO System",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF1E293B) // slate-800
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Online/Offline Status Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isOnline) Color(0xFF16A34A).copy(alpha = 0.1f)
                                    else Color(0xFFDC2626).copy(alpha = 0.1f)
                                )
                                .clickable {
                                    viewModel.toggleOnline(!isOnline)
                                    Toast.makeText(
                                        context,
                                        if (!isOnline) "Simulated ONLINE mode" else "Simulated OFFLINE mode (caching scans)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF16A34A) else Color(0xFFDC2626))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }

                        // Logout Button
                        IconButton(
                            onClick = {
                                viewModel.logout()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log out",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Avatar
                        val initials = remember(currentUser?.name) {
                            val name = currentUser?.name ?: "User"
                            name.split(" ")
                                .filter { it.isNotEmpty() }
                                .take(2)
                                .map { it.first().uppercase() }
                                .joinToString("")
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)), // blue-100
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color(0xFF1D4ED8), // blue-700
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Bar
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)), // blue-600
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Attendance",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDBEAFE) // blue-100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$attendancePercent%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.5f)) // blue-500/50
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "Students Present",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDBEAFE) // blue-100
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$todayScannedCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "/$totalStudentsCount",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Unsynced Alert Banner (styled to fit Bento theme)
            val unsyncedCount = attendance.count { !it.isSynced }
            if (unsyncedCount > 0) {
                item {
                    Card(
                        onClick = {
                            if (!isOnline) {
                                Toast.makeText(context, "Please turn on Online Mode to sync data.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.syncNow { success ->
                                    if (success) Toast.makeText(context, "Offline attendance synchronized successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)), // amber-100
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Unsynced",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Offline Mode Active • Unsynced Records",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "There are $unsyncedCount attendance records saved locally. ${if (isOnline) "Tap here to sync now." else "Connect online to sync."}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309).copy(alpha = 0.8f)
                                )
                            }
                            if (isOnline) {
                                Text(
                                    text = "SYNC",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bento Main Actions Grid: Scan QR Code (Full Width)
            item {
                Card(
                    onClick = { navController.navigate(Routes.SCANNER) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_qr_bento_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Scan QR Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B) // slate-800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Register new attendance",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B) // slate-500
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEFF6FF)), // blue-50
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = Color(0xFF2563EB), // blue-600
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Bento Sub-Actions (Row of Reports and Students)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reports Card (emerald)
                    Card(
                        onClick = { navController.navigate(Routes.REPORTS) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // emerald-50
                        border = BorderStroke(1.dp, Color(0xFFD1FAE5)), // emerald-100
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📊", fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = "Reports",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF064E3B) // emerald-900
                                )
                                Text(
                                    text = "EXCEL EXPORT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFF047857), // emerald-700
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Students Card (orange)
                    Card(
                        onClick = { navController.navigate(Routes.STUDENTS) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // orange-50
                        border = BorderStroke(1.dp, Color(0xFFFFEDD5)), // orange-100
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👥", fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = "Students",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF7C2D12) // orange-900
                                )
                                Text(
                                    text = "MANAGE DIRECTORY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color(0xFFC2410C), // orange-700
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Admin Only Bento Sub-Actions (Row of Schools and Teachers)
            if (userRole == "Admin") {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Schools Card (purple-50)
                        Card(
                            onClick = { navController.navigate(Routes.SCHOOLS) },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)), // purple-50
                            border = BorderStroke(1.dp, Color(0xFFF3E8FF)), // purple-100
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏫", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        text = "Schools",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF581C87) // purple-900
                                    )
                                    Text(
                                        text = "${schools.size} CENTERS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color(0xFF7E22CE), // purple-700
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Teachers Card (sky-50)
                        Card(
                            onClick = { navController.navigate(Routes.TEACHERS) },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)), // sky-50
                            border = BorderStroke(1.dp, Color(0xFFE0F2FE)), // sky-100
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👩‍🏫", fontSize = 20.sp)
                                }
                                Column {
                                    Text(
                                        text = "Teachers",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0C4A6E) // sky-900
                                    )
                                    Text(
                                        text = "${teachers.size} STAFF MEMBERS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color(0xFF0369A1), // sky-700
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // User Approvals full-width Card for Admin
                item {
                    val pendingCount = remember(users) { users.filter { it.status == "Pending" }.size }
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        onClick = { navController.navigate(Routes.USER_APPROVAL) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (pendingCount > 0) Color(0xFFFEF3C7) else Color(0xFFFFFBEB)
                        ),
                        border = BorderStroke(1.dp, if (pendingCount > 0) Color(0xFFFBBF24) else Color(0xFFFEF3C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_approvals_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔑", fontSize = 22.sp)
                                }
                                Column {
                                    Text(
                                        text = "User Approvals",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "Manage pending Teacher & Principal accounts",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (pendingCount > 0) Color(0xFFD97706) else Color(0xFF9CA3AF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$pendingCount PENDING",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Bento Recent Activity Section
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT ACTIVITY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8), // slate-400
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "View All",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF2563EB), // blue-600
                                modifier = Modifier.clickable { navController.navigate(Routes.REPORTS) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (todayScannedRecords.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Scans Recorded Today",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B) // slate-500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Students scanned using QR Code will show up here.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8), // slate-400
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                todayScannedRecords.take(3).forEach { record ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981)) // emerald-500
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${record.studentName} (${record.studentId})",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155) // slate-700
                                            )
                                            Text(
                                                text = "Scanned at ${record.school} • ${record.time}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8) // slate-400
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bento Admin Audit Logs
            if (userRole == "Admin") {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "GLOBAL SYSTEM AUDIT LOG",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8), // slate-400
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activityLogs.take(3).forEach { log ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${log.userName} (${log.userRole})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF2563EB) // blue-600
                                            )
                                            Text(
                                                text = log.dateTime.substringAfter(" "),
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8) // slate-400
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${log.action}: ${log.details}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF334155) // slate-700
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CircularProgressRing(
    percentage: Int,
    size: Dp,
    strokeWidth: Dp,
    color: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        // Background Arc
        drawArc(
            color = color.copy(alpha = 0.12f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        // Foreground Arc
        val sweep = (percentage.toFloat() / 100f) * 360f
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AttendanceRowItem(record: AttendanceEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (record.attendanceStatus == "Present") Color(0xFF16A34A).copy(alpha = 0.1f)
                        else Color(0xFFD97706).copy(alpha = 0.1f)
                    )
            ) {
                Icon(
                    imageVector = if (record.attendanceStatus == "Present") Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (record.attendanceStatus == "Present") Color(0xFF16A34A) else Color(0xFFD97706)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.studentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${record.studentId} • ${record.grade}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Scanned by ${record.teacherName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.time,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = record.attendanceStatus,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (record.attendanceStatus == "Present") Color(0xFF16A34A) else Color(0xFFD97706),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (record.attendanceStatus == "Present") Color(0xFF16A34A).copy(alpha = 0.1f)
                            else Color(0xFFD97706).copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// 5. HIGH-FIDELITY QR SCANNER (Real Camera UI + Interactive Simulator)
@Composable
fun ScannerScreen(navController: NavController, viewModel: MainViewModel) {
    val showConfirmation by viewModel.showScanConfirmation.collectAsState()
    val lastScanResult by viewModel.lastScanResult.collectAsState()
    val students by viewModel.students.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Simulator selection states
    var selectedStudentIdForSimulation by remember { mutableStateOf("") }

    // Audio beep player
    fun handleBeep() {
        playBeep()
    }

    LaunchedEffect(lastScanResult) {
        if (lastScanResult is ScanResult.Success) {
            handleBeep()
        }
    }

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "QR Code Scanner",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0B0F19)) // Custom Dark background for scanner feel
        ) {
            // Scanner Viewport simulation (gives a very real visual layout!)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CENTER THE STUDENT QR CARD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "The attendance record will be saved automatically.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                // The Scanner Frame Viewport
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Running scanning light animation
                    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
                    val offset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 250f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scan_y"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = Color(0xFF3B82F6),
                            start = Offset(0f, offset),
                            end = Offset(size.width, offset),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Code Scope",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // INTERACTIVE EMULATOR PANEL (Absolutely vital for emulator demo!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = "Emulator Bypass",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💻 EMULATOR INTERACTIVE SCAN BYPASS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Since emulators lack live physical cameras, pick a registered IMHO student below to simulate scanning their QR Card ID instantly:",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Student selection dropdown simulation
                        var isDropdownExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { isDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedStudentIdForSimulation.isEmpty()) "Tap here to select student"
                                        else "Scan $selectedStudentIdForSimulation",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }

                            DropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color(0xFF1E293B))
                            ) {
                                // Filter students based on role/school if not admin
                                val filterSchool = currentUser?.school ?: ""
                                val role = currentUser?.role ?: ""
                                val eligibleStudents = if (role == "Admin") students else students.filter { it.school == filterSchool }

                                eligibleStudents.forEach { std ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${std.fullName} (${std.studentId} • ${std.school})",
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            selectedStudentIdForSimulation = std.studentId
                                            isDropdownExpanded = false
                                            // Trigger scan
                                            viewModel.processQrCode(std.studentId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attendance confirmation Overlay Sheet! (Success / Error / Warning)
            if (showConfirmation && lastScanResult != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearScanResult() },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.clearScanResult() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (lastScanResult) {
                                    is ScanResult.Success -> Icons.Default.CheckCircle
                                    is ScanResult.AlreadyScanned -> Icons.Default.Warning
                                    else -> Icons.Default.Error
                                },
                                contentDescription = null,
                                tint = when (lastScanResult) {
                                    is ScanResult.Success -> Color(0xFF16A34A)
                                    is ScanResult.AlreadyScanned -> Color(0xFFD97706)
                                    else -> Color(0xFFDC2626)
                                },
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (lastScanResult) {
                                    is ScanResult.Success -> "Scan Successful"
                                    is ScanResult.AlreadyScanned -> "Duplicate Scan"
                                    else -> "Scan Failed"
                                },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (val res = lastScanResult) {
                                is ScanResult.Success -> {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = res.student.fullName.firstOrNull()?.toString() ?: "S",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = res.student.fullName,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "ID: ${res.student.studentId} • ${res.student.grade}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = res.student.school,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "ATTENDANCE RECORDED",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = Color(0xFF15803D)
                                            )
                                            Text(
                                                text = "Time: ${res.record.time} (${res.record.attendanceStatus})",
                                                fontSize = 12.sp,
                                                color = Color(0xFF15803D)
                                            )
                                            Text(
                                                text = "Scanned by: ${res.record.teacherName}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF15803D).copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                is ScanResult.AlreadyScanned -> {
                                    Text(
                                        text = "Attendance Already Recorded Today",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFB45309),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${res.student.fullName} has already registered attendance today at ${res.recordedTime}.\nDuplicate records are blocked.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                is ScanResult.Error -> {
                                    Text(
                                        text = res.message,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                )
            }
        }
    }
}

// 6. STUDENTS DIRECTORY & CARD DESIGN PRINTING
@Composable
fun StudentsScreen(navController: NavController, viewModel: MainViewModel) {
    val students by viewModel.students.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedSchoolFilter by remember { mutableStateOf("All Schools") }

    // Forms/Adding state
    var showAddDialog by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("Grade 8") }
    var newSchool by remember { mutableStateOf("") }
    var newGuardian by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }
    var newDob by remember { mutableStateOf("2012-05-14") }
    var newGender by remember { mutableStateOf("Male") }
    var newProjName by remember { mutableStateOf("IMHO Educational Assistance") }

    // Card Design View overlay
    var selectedStudentForCard by remember { mutableStateOf<StudentEntity?>(null) }

    val filterSchool = currentUser?.school ?: ""
    val role = currentUser?.role ?: ""

    val filteredList = remember(students, searchQuery, selectedSchoolFilter, filterSchool, role) {
        students.filter { std ->
            val matchesSearch = std.fullName.contains(searchQuery, ignoreCase = true) ||
                    std.studentId.contains(searchQuery, ignoreCase = true) ||
                    std.guardianName.contains(searchQuery, ignoreCase = true) ||
                    std.guardianPhone.contains(searchQuery, ignoreCase = true)

            val matchesSchool = if (role == "Admin") {
                selectedSchoolFilter == "All Schools" || std.school == selectedSchoolFilter
            } else {
                std.school == filterSchool
            }

            matchesSearch && matchesSchool
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationMenu(navController = navController, activeRoute = Routes.STUDENTS)
        },
        topBar = {
            OptInTopAppBar(
                title = "Students Directory",
                actions = {
                    if (role == "Admin") {
                        IconButton(onClick = {
                            // Suggest next ID
                            val nextNum = students.size + 1
                            newId = "IMHO" + String.format("%04d", nextNum)
                            newName = ""
                            newSchool = schools.firstOrNull()?.schoolName ?: ""
                            newGuardian = ""
                            newPhone = ""
                            newAddress = ""
                            showAddDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Add Student",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search & Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name, ID, Phone, Guardian...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            if (role == "Admin") {
                // School Filter Selector for Admin
                var showSchoolDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Button(
                        onClick = { showSchoolDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter: $selectedSchoolFilter",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }

                    DropdownMenu(
                        expanded = showSchoolDropdown,
                        onDismissRequest = { showSchoolDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Schools") },
                            onClick = { selectedSchoolFilter = "All Schools"; showSchoolDropdown = false }
                        )
                        schools.forEach { sc ->
                            DropdownMenuItem(
                                text = { Text(sc.schoolName) },
                                onClick = { selectedSchoolFilter = sc.schoolName; showSchoolDropdown = false }
                            )
                        }
                    }
                }
            }

            // Results List
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Students Found", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredList) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStudentForCard = student },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Photo Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.fullName.firstOrNull()?.toString() ?: "S",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "ID: ${student.studentId} • ${student.grade}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = student.school,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "View QR",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "View Card",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Student Dialog (Admin Only)
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isEmpty()) {
                                Toast.makeText(context, "Please fill in student name", Toast.LENGTH_SHORT).show()
                            } else {
                                val std = StudentEntity(
                                    studentId = newId,
                                    fullName = newName,
                                    photo = "",
                                    gender = newGender,
                                    dateOfBirth = newDob,
                                    grade = newGrade,
                                    school = newSchool,
                                    division = "District Division",
                                    guardianName = newGuardian,
                                    guardianPhone = newPhone,
                                    address = newAddress,
                                    projectName = newProjName,
                                    qrCodeId = newId,
                                    status = "Active",
                                    createdDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                )
                                viewModel.addStudent(std)
                                showAddDialog = false
                                Toast.makeText(context, "Added Student: ${std.fullName}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save Student")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Register New IMHO Student", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newId,
                            onValueChange = { newId = it },
                            label = { Text("Student ID / QR Code ID") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newGrade,
                            onValueChange = { newGrade = it },
                            label = { Text("Grade (e.g. Grade 8)") },
                            singleLine = true
                        )

                        // School Dropdown Selector
                        var schoolSelectorExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newSchool,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assigned School") },
                                trailingIcon = {
                                    IconButton(onClick = { schoolSelectorExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = schoolSelectorExpanded,
                                onDismissRequest = { schoolSelectorExpanded = false }
                            ) {
                                schools.forEach { sch ->
                                    DropdownMenuItem(
                                        text = { Text(sch.schoolName) },
                                        onClick = {
                                            newSchool = sch.schoolName
                                            schoolSelectorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newGuardian,
                            onValueChange = { newGuardian = it },
                            label = { Text("Guardian Name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Guardian Phone") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = newAddress,
                            onValueChange = { newAddress = it },
                            label = { Text("Residential Address") },
                            maxLines = 2
                        )
                    }
                }
            )
        }

        // Professional Student QR Card Designer Display Overlay (with delete option for admin!)
        selectedStudentForCard?.let { student ->
            AlertDialog(
                onDismissRequest = { selectedStudentForCard = null },
                confirmButton = {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Card Print layout simulated! Connecting to wireless printer...", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print ID Card", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { selectedStudentForCard = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                        
                        if (role == "Admin") {
                            Button(
                                onClick = {
                                    viewModel.deleteStudent(student.studentId)
                                    selectedStudentForCard = null
                                    Toast.makeText(context, "Student records deleted.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 11.sp)
                            }
                        }
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "IMHO STUDENT ID QR CARD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Visual layout of ID card (Designed exactly as requested!)
                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header Logo
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "IMHO SRI LANKA",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Educational Support Card",
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Avatar Photo Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.fullName.firstOrNull()?.toString() ?: "S",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Student Info
                                Text(
                                    text = student.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "ID: ${student.studentId} • ${student.grade}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = student.school,
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Deterministic QR Canvas we designed!
                                StudentQrCode(
                                    studentId = student.studentId,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .padding(4.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Student QR ID Token",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Guardian: ${student.guardianName} (${student.guardianPhone})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    }
}

// 7. SCHOOLS DIRECTORY (Admin Screen)
@Composable
fun SchoolsScreen(navController: NavController, viewModel: MainViewModel) {
    val schools by viewModel.schools.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newDistrict by remember { mutableStateOf("Kilinochchi") }
    var newDivision by remember { mutableStateOf("Kilinochchi Division") }
    var newPrincipal by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "IMHO Supported Schools",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        val nextNum = schools.size + 1
                        newId = "SCH" + String.format("%03d", nextNum)
                        newName = ""
                        newPrincipal = ""
                        newPhone = ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.AddHome, contentDescription = "Add School", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (schools.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No schools registered yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(schools) { school ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = school.schoolName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    // Delete button for Admin
                                    IconButton(onClick = {
                                        viewModel.deleteSchool(school.schoolId)
                                        Toast.makeText(context, "School deleted", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Text(
                                    text = "ID: ${school.schoolId} • District: ${school.district}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Principal: ${school.principalName}", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Contact: ${school.phone}", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (newName.isEmpty() || newPrincipal.isEmpty()) {
                            Toast.makeText(context, "Please enter Name and Principal", Toast.LENGTH_SHORT).show()
                        } else {
                            val sch = SchoolEntity(
                                schoolId = newId,
                                schoolName = newName,
                                district = newDistrict,
                                division = newDivision,
                                principalName = newPrincipal,
                                phone = newPhone,
                                status = "Active"
                            )
                            viewModel.addSchool(sch)
                            showAddDialog = false
                            Toast.makeText(context, "Registered ${sch.schoolName}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                },
                title = { Text("Register Supported School") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newId, onValueChange = { newId = it }, label = { Text("School ID") })
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("School Name") })
                        OutlinedTextField(value = newDistrict, onValueChange = { newDistrict = it }, label = { Text("District") })
                        OutlinedTextField(value = newPrincipal, onValueChange = { newPrincipal = it }, label = { Text("Principal Name") })
                        OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone") })
                    }
                }
            )
        }
    }
}

// 8. TEACHERS DIRECTORY (Admin Screen)
@Composable
fun TeachersScreen(navController: NavController, viewModel: MainViewModel) {
    val teachers by viewModel.teachers.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newId by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newSchool by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("In-Charge Teacher") }

    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "Teachers & Principals",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        val nextNum = teachers.size + 1
                        newId = "TCH" + String.format("%03d", nextNum)
                        newName = ""
                        newEmail = ""
                        newPhone = ""
                        newSchool = schools.firstOrNull()?.schoolName ?: ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Teacher", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (teachers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No teachers registered.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(teachers) { teacher ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = teacher.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    IconButton(onClick = {
                                        viewModel.deleteTeacher(teacher.teacherId)
                                        Toast.makeText(context, "Teacher deleted", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Text(
                                    text = "Role: ${teacher.role} • ID: ${teacher.teacherId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Assigned School: ${teacher.school}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Email: ${teacher.email}", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Phone: ${teacher.phone}", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(onClick = {
                        if (newName.isEmpty() || newEmail.isEmpty()) {
                            Toast.makeText(context, "Please enter Name and Email", Toast.LENGTH_SHORT).show()
                        } else {
                            val tch = TeacherEntity(
                                teacherId = newId,
                                name = newName,
                                email = newEmail.lowercase(),
                                phone = newPhone,
                                school = newSchool,
                                role = newRole,
                                photo = "",
                                status = "Active"
                            )
                            viewModel.addTeacher(tch)
                            showAddDialog = false
                            Toast.makeText(context, "Registered ${tch.name}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                },
                title = { Text("Register Teacher / Principal") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newId, onValueChange = { newId = it }, label = { Text("Teacher ID") })
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                        OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email Address") })
                        OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone Number") })

                        // School Dropdown
                        var schoolSelectorExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newSchool,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assign to School") },
                                trailingIcon = {
                                    IconButton(onClick = { schoolSelectorExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = schoolSelectorExpanded,
                                onDismissRequest = { schoolSelectorExpanded = false }
                            ) {
                                schools.forEach { sch ->
                                    DropdownMenuItem(
                                        text = { Text(sch.schoolName) },
                                        onClick = {
                                            newSchool = sch.schoolName
                                            schoolSelectorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Role Selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newRole == "In-Charge Teacher",
                                onClick = { newRole = "In-Charge Teacher" }
                            )
                            Text("In-Charge Teacher", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(
                                selected = newRole == "Principal",
                                onClick = { newRole = "Principal" }
                            )
                            Text("Principal", fontSize = 12.sp)
                        }
                    }
                }
            )
        }
    }
}

// 9. REPORTS SCREEN & EXCEL EXPORT CONTROLLER
@Composable
fun ReportsScreen(navController: NavController, viewModel: MainViewModel) {
    val attendance by viewModel.attendance.collectAsState()
    val schools by viewModel.schools.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedRangeType by remember { mutableStateOf(DateRangeType.TODAY) }
    var selectedSchoolFilter by remember { mutableStateOf("All Schools") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val role = currentUser?.role ?: ""
    val userSchool = currentUser?.school ?: ""

    // Initial school filter based on roles
    LaunchedEffect(role, userSchool) {
        if (role != "Admin") {
            selectedSchoolFilter = userSchool
        }
    }

    val filteredRecords = remember(attendance, selectedRangeType, selectedSchoolFilter, searchQuery, role, userSchool) {
        viewModel.getFilteredAttendance(
            allRecords = attendance,
            rangeType = selectedRangeType,
            schoolFilter = if (role == "Admin") selectedSchoolFilter else userSchool,
            searchQuery = searchQuery
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationMenu(navController = navController, activeRoute = Routes.REPORTS)
        },
        topBar = {
            OptInTopAppBar(
                title = "Reports & Excel Export",
                actions = {
                    IconButton(
                        onClick = {
                            if (filteredRecords.isEmpty()) {
                                Toast.makeText(context, "No records to export.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.exportToExcel(
                                    context = context,
                                    filteredRecords = filteredRecords,
                                    fileNamePrefix = "IMHO_Student_Attendance_Report",
                                    onComplete = { name, uri ->
                                        if (uri != null && name != null) {
                                            // Open standard share sheet
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Attendance Excel (.csv)"))
                                            Toast.makeText(context, "Exported $name successfully!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Failed to export report.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Excel",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Filters Cards
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FILTER ATTENDANCE RECORDS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search student, grade, teacher...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Date Range filter Selector
                        var dateRangeSelectorExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { dateRangeSelectorExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = "Date: ${selectedRangeType.name}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            DropdownMenu(
                                expanded = dateRangeSelectorExpanded,
                                onDismissRequest = { dateRangeSelectorExpanded = false }
                            ) {
                                DateRangeType.values().forEach { range ->
                                    DropdownMenuItem(
                                        text = { Text(range.name, fontSize = 12.sp) },
                                        onClick = {
                                            selectedRangeType = range
                                            dateRangeSelectorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // School Selector filter (Admin only!)
                        if (role == "Admin") {
                            var schoolSelectorExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { schoolSelectorExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Text(
                                        text = selectedSchoolFilter,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                DropdownMenu(
                                    expanded = schoolSelectorExpanded,
                                    onDismissRequest = { schoolSelectorExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.5f)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Schools", fontSize = 12.sp) },
                                        onClick = {
                                            selectedSchoolFilter = "All Schools"
                                            schoolSelectorExpanded = false
                                        }
                                    )
                                    schools.forEach { sc ->
                                        DropdownMenuItem(
                                            text = { Text(sc.schoolName, fontSize = 12.sp) },
                                            onClick = {
                                                selectedSchoolFilter = sc.schoolName
                                                schoolSelectorExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Non-admin shows their school read-only capsule
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = userSchool,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Results count and Export Button row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECORDS PREVIEW (${filteredRecords.size} found)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Large Share Action Button
                TextButton(
                    onClick = {
                        if (filteredRecords.isEmpty()) {
                            Toast.makeText(context, "No records to export.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.exportToExcel(
                                context = context,
                                filteredRecords = filteredRecords,
                                fileNamePrefix = "IMHO_Student_Attendance",
                                onComplete = { name, uri ->
                                    if (uri != null) {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/csv"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Excel Report"))
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // List of Records
            if (filteredRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlaylistRemove,
                            contentDescription = "No reports",
                            tint = Color.Gray,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Records Match Current Filters",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Try changing the filter dates or search query.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredRecords) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (record.attendanceStatus == "Present") Color(0xFF16A34A).copy(alpha = 0.1f)
                                            else Color(0xFFDC2626).copy(alpha = 0.1f)
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (record.attendanceStatus == "Present") Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (record.attendanceStatus == "Present") Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.studentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${record.studentId} • ${record.grade} • ${record.school}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "Scanned by ${record.teacherName} (${record.userRole})",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = record.date,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = record.time,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 10. SETTINGS & APP SPECIFICATIONS
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var organizationName by remember { mutableStateOf("") }
    var attendanceTimeCutoff by remember { mutableStateOf("") }
    var allowGps by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        organizationName = settings.organizationName
        attendanceTimeCutoff = settings.attendanceTime
        allowGps = settings.allowGps
    }

    Scaffold(
        bottomBar = {
            BottomNavigationMenu(navController = navController, activeRoute = Routes.SETTINGS)
        },
        topBar = {
            OptInTopAppBar(title = "App Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simulated Connection Switch (Extremely cool for showing Offline caching!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NETWORK SIMULATION DRIVER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Toggle local connection to test IMHO's robust Offline Caching and cloud sync automation rules:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOnline) "🟢 Connected Online" else "🔴 Offline Cache Enabled",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isOnline) "Records sync immediately." else "Saves locally in Room DB. Zero loss.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = {
                                viewModel.toggleOnline(it)
                                Toast
                                    .makeText(
                                        context,
                                        if (it) "Connection restored. Sync triggered." else "No connection. Caching active.",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        )
                    }
                }
            }

            // General Settings (Admin only, otherwise read-only!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "IMHO SYSTEM POLICIES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = organizationName,
                        onValueChange = { if (currentUser?.role == "Admin") organizationName = it },
                        readOnly = currentUser?.role != "Admin",
                        label = { Text("Organization Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = attendanceTimeCutoff,
                        onValueChange = { if (currentUser?.role == "Admin") attendanceTimeCutoff = it },
                        readOnly = currentUser?.role != "Admin",
                        label = { Text("Daily Cutoff Time for Late Flag") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Allow GPS Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Log lat/long coordinates with scans", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = allowGps,
                            onCheckedChange = {
                                if (currentUser?.role == "Admin") {
                                    allowGps = it
                                } else {
                                    Toast.makeText(context, "Only Admin can toggle system policies", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (currentUser?.role == "Admin") {
                        Button(
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        organizationName = organizationName,
                                        attendanceTime = attendanceTimeCutoff,
                                        allowGps = allowGps
                                    )
                                )
                                Toast.makeText(context, "Global settings updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Save Policy Updates", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // About IMHO Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.ABOUT) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "About IMHO Organization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Educational and health support in Sri Lanka.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = Color.Gray)
                }
            }

            // Version stamp
            Text(
                text = "Secure Student QR Attendance Monitoring System v1.0.0\nBuilt with Kotlin & Jetpack Compose • Room Persistent Database",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
    }
}

// 11. ABOUT IMHO SCREEN
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            OptInTopAppBar(
                title = "About IMHO Sri Lanka",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = "IMHO Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "International Medical Health Organization",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "IMHO is a non-profit humanitarian organization dedicated to improving healthcare services and educational access for marginalized children, orphans, and families in underserved regions of Sri Lanka and globally.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            HorizontalDivider()

            Text(
                text = "OUR EDUCATION MISSION",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "Through sponsored IMHO schools and scholarship support programs, we provide daily nutrition, uniform materials, books, medical clinics, and footwear to thousands of students in Jaffna, Kilinochchi, Batticaloa, and Trincomalee districts.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FUTURE SCALABLE ARCHITECTURE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "This application is modularly coded to support future expansions for:\n" +
                                "• Food & Book distribution tracking\n" +
                                "• Footwear & Scholarship disbursements\n" +
                                "• SMS & WhatsApp parent notification channels\n" +
                                "• Face Recognition scan engines",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// 12. BOTTOM NAVIGATION MENU
@Composable
fun BottomNavigationMenu(navController: NavController, activeRoute: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(Routes.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
                Triple(Routes.STUDENTS, Icons.Default.Groups, "Students"),
                Triple(Routes.REPORTS, Icons.Default.Assessment, "Reports"),
                Triple(Routes.SETTINGS, Icons.Default.Settings, "Settings")
            )

            items.forEach { (route, icon, label) ->
                val selected = activeRoute == route
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (activeRoute != route) {
                                if (route == Routes.DASHBOARD) {
                                    navController.navigate(route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(route) {
                                        popUpTo(Routes.DASHBOARD)
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color(0xFF2563EB) else Color(0xFF94A3B8),
                        modifier = Modifier
                            .size(22.dp)
                            .animateContentSize()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color(0xFF1E293B) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// REGISTRATION SCREEN
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, viewModel: MainViewModel) {
    val schools by viewModel.schools.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var nicNumber by remember { mutableStateOf("") }
    var selectedSchool by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var schoolDropdownExpanded by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImhoLogoHeader(size = 70.dp)
                
                Text(
                    text = "IMHO Student QR Attendance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                Text(
                    text = "Register as a Teacher or School Principal to manage student attendances safely.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Registration Card Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Mobile Number
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_mobile_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // NIC Number
                        OutlinedTextField(
                            value = nicNumber,
                            onValueChange = { nicNumber = it },
                            label = { Text("NIC Number (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_nic_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // School Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedSchool,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("School") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF64748B)) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { schoolDropdownExpanded = true },
                                        modifier = Modifier.testTag("school_dropdown_button")
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select School")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = schoolDropdownExpanded,
                                onDismissRequest = { schoolDropdownExpanded = false }
                            ) {
                                if (schools.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No schools loaded") },
                                        onClick = { schoolDropdownExpanded = false }
                                    )
                                } else {
                                    schools.forEach { sch ->
                                        DropdownMenuItem(
                                            text = { Text(sch.schoolName) },
                                            onClick = {
                                                selectedSchool = sch.schoolName
                                                schoolDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("school_option_${sch.schoolId}")
                                        )
                                    }
                                }
                            }
                        }

                        // Role Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Role") },
                                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF64748B)) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { roleDropdownExpanded = true },
                                        modifier = Modifier.testTag("role_dropdown_button")
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = roleDropdownExpanded,
                                onDismissRequest = { roleDropdownExpanded = false }
                            ) {
                                listOf("School Principal", "In-Charge Teacher").forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            selectedRole = role
                                            roleDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("role_option_${role.replace(" ", "_")}")
                                    )
                                }
                            }
                        }

                        // Employee/Teacher ID
                        OutlinedTextField(
                            value = employeeId,
                            onValueChange = { employeeId = it },
                            label = { Text("Employee/Teacher ID (Optional)") },
                            leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_employee_id_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Terms and Conditions checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it },
                                modifier = Modifier.testTag("terms_checkbox")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I agree to the Terms and Conditions.",
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        // Create Account button
                        Button(
                            onClick = {
                                val trimmedEmail = email.trim()
                                if (fullName.isBlank() || trimmedEmail.isBlank() || mobile.isBlank() || selectedSchool.isBlank() || selectedRole.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                    Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                    Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (password.length < 8) {
                                    Toast.makeText(context, "Password must be at least 8 characters.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    Toast.makeText(context, "Passwords and Confirm Password must match.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (!agreeToTerms) {
                                    Toast.makeText(context, "You must agree to the Terms and Conditions.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }

                                viewModel.register(
                                    name = fullName.trim(),
                                    email = trimmedEmail,
                                    phone = mobile.trim(),
                                    nicNumber = if (nicNumber.isBlank()) null else nicNumber.trim(),
                                    school = selectedSchool,
                                    role = selectedRole,
                                    employeeId = if (employeeId.isBlank()) null else employeeId.trim(),
                                    password = password,
                                    onSuccess = {
                                        navController.navigate(Routes.REGISTRATION_SUCCESS) {
                                            popUpTo(Routes.LOGIN) { inclusive = false }
                                        }
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_submit_button")
                        ) {
                            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", fontSize = 13.sp, color = Color(0xFF64748B))
                    TextButton(onClick = { navController.navigateUp() }) {
                        Text("Log In", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// REGISTRATION SUCCESS SCREEN
// -----------------------------------------------------------------
@Composable
fun RegistrationSuccessScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Large attractive checkmark badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Text(
                    text = "Registration Submitted",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Thank you for registering. Your account has been submitted for approval. You will be able to log in after an administrator approves your account.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF475569),
                    lineHeight = 22.sp
                )

                Button(
                    onClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("back_to_login_button")
                ) {
                    Text("Return to Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// USER APPROVAL (ADMIN ONLY) SCREEN
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserApprovalScreen(navController: NavController, viewModel: MainViewModel) {
    val users by viewModel.users.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pendingUsers = remember(users) { users.filter { it.status == "Pending" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Approvals", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        if (pendingUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Text(
                        text = "No Pending Registrations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "All registration requests have been processed.",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingUsers, key = { it.userId }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header Row (Avatar and Name/Role)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (user.role == "School Principal") "🎓" else "🧑‍🏫",
                                        fontSize = 24.sp
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = user.role,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Registration Info
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Email: ${user.email}", fontSize = 13.sp, color = Color(0xFF334155))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Mobile: ${user.phone}", fontSize = 13.sp, color = Color(0xFF334155))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "School: ${user.school}", fontSize = 13.sp, color = Color(0xFF334155))
                                }
                                if (!user.nicNumber.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "NIC: ${user.nicNumber}", fontSize = 13.sp, color = Color(0xFF334155))
                                    }
                                }
                                if (!user.employeeId.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "ID: ${user.employeeId}", fontSize = 13.sp, color = Color(0xFF334155))
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Registered: ${user.registrationDate}", fontSize = 13.sp, color = Color(0xFF64748B))
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Reject
                                OutlinedButton(
                                    onClick = {
                                        viewModel.rejectUser(
                                            userId = user.userId,
                                            reason = "Declined by administrator request",
                                            onSuccess = {
                                                Toast.makeText(context, "Rejected registration for ${user.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reject_button_${user.userId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", fontWeight = FontWeight.Bold)
                                }

                                // Approve
                                Button(
                                    onClick = {
                                        viewModel.approveUser(
                                            userId = user.userId,
                                            onSuccess = {
                                                Toast.makeText(context, "Approved registration for ${user.name}", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("approve_button_${user.userId}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

