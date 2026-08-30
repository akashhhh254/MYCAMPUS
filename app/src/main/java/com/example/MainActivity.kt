package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.CampusViewModel
import com.example.ui.components.SecureRoute
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.principal.PrincipalHomeScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.student.StudentHomeScreen
import com.example.ui.screens.teacher.TeacherHomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CampusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CampusApp(viewModel: CampusViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToDashboard = { destination ->
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    val dest = when (role.lowercase().trim()) {
                        "hod", "principal", "admin" -> "principal_home"
                        "teacher", "faculty" -> "teacher_home"
                        else -> "student_home"
                    }
                    navController.navigate(dest) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("principal_home") {
            SecureRoute(
                viewModel = viewModel,
                allowedRoles = listOf("hod", "principal", "admin"),
                onLogout = {
                    viewModel.signOut {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onRedirectToRole = { role ->
                    val dest = when (role.lowercase().trim()) {
                        "hod", "principal", "admin" -> "principal_home"
                        "teacher", "faculty" -> "teacher_home"
                        else -> "student_home"
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) { _ ->
                PrincipalHomeScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.signOut {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable("teacher_home") {
            SecureRoute(
                viewModel = viewModel,
                allowedRoles = listOf("teacher", "faculty"),
                onLogout = {
                    viewModel.signOut {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onRedirectToRole = { role ->
                    val dest = when (role.lowercase().trim()) {
                        "hod", "principal", "admin" -> "principal_home"
                        "teacher", "faculty" -> "teacher_home"
                        else -> "student_home"
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) { _ ->
                TeacherHomeScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.signOut {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable("student_home") {
            SecureRoute(
                viewModel = viewModel,
                allowedRoles = listOf("student"),
                onLogout = {
                    viewModel.signOut {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onRedirectToRole = { role ->
                    val dest = when (role.lowercase().trim()) {
                        "hod", "principal", "admin" -> "principal_home"
                        "teacher", "faculty" -> "teacher_home"
                        else -> "student_home"
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) { _ ->
                StudentHomeScreen(
                    viewModel = viewModel,
                    onLogout = {
                        viewModel.signOut {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
