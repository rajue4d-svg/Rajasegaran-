package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH
                    ) {
                        composable(Routes.SPLASH) {
                            SplashScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.FORGOT_PASSWORD) {
                            ForgotPasswordScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.DASHBOARD) {
                            DashboardScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.STUDENTS) {
                            StudentsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.SCHOOLS) {
                            SchoolsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.TEACHERS) {
                            TeachersScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.SCANNER) {
                            ScannerScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.REPORTS) {
                            ReportsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Routes.ABOUT) {
                            AboutScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
