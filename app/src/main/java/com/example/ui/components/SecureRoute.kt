package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthUserSession
import com.example.data.auth.RoleVerificationResult
import com.example.ui.CampusViewModel
import com.example.ui.theme.*

/**
 * SecureRoute wrapper that validates Firebase Security Rules and Firestore document claims
 * before allowing access to restricted dashboards (Teacher, Principal, Admin).
 *
 * Enforces rule criteria defined in firestore.rules:
 * - isPrincipal(): `getUserRole() == 'principal'`
 * - isTeacher(): `getUserRole() == 'teacher' || getUserRole() == 'principal'`
 * - isStudent(): `getUserRole() == 'student'`
 */
@Composable
fun SecureRoute(
    viewModel: CampusViewModel,
    allowedRoles: List<String>,
    onLogout: () -> Unit,
    onRedirectToRole: (String) -> Unit = {},
    content: @Composable (AuthUserSession) -> Unit
) {
    val currentSession by viewModel.currentUserSession.collectAsState()
    var verificationState by remember { mutableStateOf<RoleVerificationResult>(RoleVerificationResult.Verifying) }

    LaunchedEffect(allowedRoles, currentSession) {
        verificationState = RoleVerificationResult.Verifying
        val result = viewModel.verifySecurityClaims(allowedRoles)
        verificationState = result
    }

    AnimatedContent(
        targetState = verificationState,
        label = "secure_route_state"
    ) { state ->
        when (state) {
            is RoleVerificationResult.Verifying -> {
                SecurityVerifyingView(allowedRoles = allowedRoles)
            }
            is RoleVerificationResult.Authorized -> {
                content(state.session)
            }
            is RoleVerificationResult.Unauthenticated -> {
                SecurityUnauthenticatedView(onLogin = onLogout)
            }
            is RoleVerificationResult.Denied -> {
                SecurityAccessDeniedView(
                    denied = state,
                    onGoToMyDashboard = {
                        onRedirectToRole(state.currentRole)
                    },
                    onSwitchAccount = onLogout
                )
            }
        }
    }
}

@Composable
private fun SecurityVerifyingView(allowedRoles: List<String>) {
    val rolesTitle = allowedRoles.joinToString(" / ") { it.replaceFirstChar { c -> c.uppercase() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 3.dp,
                        color = PrimaryIndigoLight
                    )
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Verifying Credentials",
                        tint = PrimaryIndigoLight,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verifying Security Rules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Validating Firestore document role claims for '$rolesTitle' institutional access...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "firestore.rules • role claim validation",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityUnauthenticatedView(onLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(StatusWarning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = StatusWarning,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Authentication Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your institutional session has expired or is not authenticated. Please sign in to verify your identity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("secure_route_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proceed to Login", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SecurityAccessDeniedView(
    denied: RoleVerificationResult.Denied,
    onGoToMyDashboard: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    val scrollState = rememberScrollState()
    val requiredFormatted = denied.requiredRoles.joinToString(" or ") { it.replaceFirstChar { c -> c.uppercase() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AccentRose.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Access Restricted",
                        tint = AccentRose,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Access Restricted",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentRose.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Firebase Security Rules Clearance Required",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentRose,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Security Details Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current User",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Text(
                                text = denied.currentUserName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        if (denied.currentUserEmail.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Account Email",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                                Text(
                                    text = denied.currentUserEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate700
                                )
                            }
                        }

                        HorizontalDivider(color = Slate200)

                        // Role comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Firestore Claim",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = denied.currentRole.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Required Area Claim",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentRose.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = requiredFormatted.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRose,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Slate200)

                        // Explanation
                        Text(
                            text = denied.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val myDashboardRole = denied.currentRole.lowercase().trim()
                    val destinationLabel = when (myDashboardRole) {
                        "principal", "admin" -> "Principal Portal"
                        "teacher", "faculty" -> "Faculty Dashboard"
                        else -> "Student Portal"
                    }

                    Button(
                        onClick = onGoToMyDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("secure_route_go_dashboard_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open $destinationLabel", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onSwitchAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("secure_route_switch_account_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch to Authorized Account", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
