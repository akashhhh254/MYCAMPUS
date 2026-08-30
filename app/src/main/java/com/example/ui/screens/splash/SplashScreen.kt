package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CampusViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: CampusViewModel,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val currentUser by viewModel.currentUser.collectAsState()
    val authSession by viewModel.currentUserSession.collectAsState()

    var secondsLeft by remember { mutableIntStateOf(5) }

    LaunchedEffect(Unit) {
        startAnimation = true
        for (i in 5 downTo 1) {
            secondsLeft = i
            delay(1000)
        }

        // Automatic authentication check based on persisted verified session
        val role = authSession?.role
        if (!role.isNullOrBlank()) {
            val destination = when (role.lowercase().trim()) {
                "hod", "principal", "admin" -> "principal_home"
                "teacher", "faculty" -> "teacher_home"
                else -> "student_home"
            }
            onNavigateToDashboard(destination)
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryNavy,
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            // Enterprise Institution Icon
            Surface(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = PrimaryIndigo.copy(alpha = 0.25f),
                border = ButtonDefaults.outlinedButtonBorder(true)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "MyCampus Logo",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Brand Title
            Text(
                text = "MyCampus",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Brand Tagline
            Text(
                text = "Akax ❤ made with",
                color = Slate300,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subtle loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AccentCyan,
                strokeWidth = 2.5.dp
            )
        }

        // Bottom Institutional Subtitle
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enterprise Campus & Academic ERP",
                color = Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.4.sp
            )
        }
    }
}
