package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.CampusRole
import com.example.ui.CampusViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*

/**
 * Screen mode within authentication flow.
 */
enum class AuthScreenMode {
    ROLE_SELECTION,
    STUDENT_LOGIN,
    TEACHER_LOGIN,
    HOD_LOGIN,
    PRINCIPAL_LOGIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: CampusViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var screenMode by remember { mutableStateOf(AuthScreenMode.ROLE_SELECTION) }

    AnimatedContent(
        targetState = screenMode,
        transitionSpec = {
            if (targetState != AuthScreenMode.ROLE_SELECTION) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "auth_screen_transition"
    ) { mode ->
        when (mode) {
            AuthScreenMode.ROLE_SELECTION -> {
                RoleSelectionView(
                    onSelectRole = { selectedRole ->
                        screenMode = when (selectedRole) {
                            CampusRole.HOD, CampusRole.PRINCIPAL -> AuthScreenMode.HOD_LOGIN
                            CampusRole.TEACHER -> AuthScreenMode.TEACHER_LOGIN
                            CampusRole.STUDENT -> AuthScreenMode.STUDENT_LOGIN
                        }
                    }
                )
            }
            AuthScreenMode.STUDENT_LOGIN -> {
                StudentLoginView(
                    viewModel = viewModel,
                    onBack = { screenMode = AuthScreenMode.ROLE_SELECTION },
                    onLoginSuccess = onLoginSuccess
                )
            }
            AuthScreenMode.TEACHER_LOGIN -> {
                TeacherLoginView(
                    viewModel = viewModel,
                    onBack = { screenMode = AuthScreenMode.ROLE_SELECTION },
                    onLoginSuccess = onLoginSuccess
                )
            }
            AuthScreenMode.HOD_LOGIN, AuthScreenMode.PRINCIPAL_LOGIN -> {
                HodLoginView(
                    viewModel = viewModel,
                    onBack = { screenMode = AuthScreenMode.ROLE_SELECTION },
                    onLoginSuccess = onLoginSuccess
                )
            }
        }
    }
}

// ==========================================
// 1. ROLE SELECTION SCREEN
// ==========================================

@Composable
fun RoleSelectionView(
    onSelectRole: (CampusRole) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Official MyCampus Logo
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    color = PrimaryIndigo,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "MyCampus Official Logo",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to MyCampus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Choose how you want to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(28.dp))

                // OPTION 1: HOD (Head of Department)
                RoleSelectionCard(
                    title = "HOD",
                    subtitle = "Head of Department & academic administrator.",
                    badgeText = "HOD / ADMIN",
                    accentColor = Color(0xFF7C3AED),
                    icon = Icons.Default.AdminPanelSettings,
                    testTag = "continue_as_principal_btn",
                    onClick = { onSelectRole(CampusRole.HOD) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // OPTION 2: Faculty
                RoleSelectionCard(
                    title = "Faculty",
                    subtitle = "Manage classes, attendance, assignments and academic activities.",
                    badgeText = "FACULTY",
                    accentColor = SecondaryTeal,
                    icon = Icons.Default.School,
                    testTag = "continue_as_teacher_btn",
                    onClick = { onSelectRole(CampusRole.TEACHER) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // OPTION 3: Student
                RoleSelectionCard(
                    title = "Student",
                    subtitle = "Access academics, attendance, timetable and college updates.",
                    badgeText = "STUDENT",
                    accentColor = PrimaryIndigo,
                    icon = Icons.Default.Badge,
                    testTag = "continue_as_student_btn",
                    onClick = { onSelectRole(CampusRole.STUDENT) }
                )
            }

            // Developer Credit
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Akax ❤ made with",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    subtitle: String,
    badgeText: String,
    accentColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder(true)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Proceed",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==========================================
// 2. STUDENT AUTHENTICATION (Sign In & Create Account)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginView(
    viewModel: CampusViewModel,
    onBack: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Account

    // Sign In State
    var studentCollegeId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Register State
    var regFullName by remember { mutableStateOf("") }
    var regCollegeId by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regDept by remember { mutableStateOf("Computer Science") }
    var regAcadYear by remember { mutableStateOf("2024-2025") }
    var regYearSem by remember { mutableStateOf("2nd Year / Sem 3") }
    var regDivision by remember { mutableStateOf("Division A") }
    var isRegistering by remember { mutableStateOf(false) }
    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var divDropdownExpanded by remember { mutableStateOf(false) }

    val departments = listOf(
        "Computer Science",
        "Information Technology",
        "Artificial Intelligence & Data Science",
        "Electronics & Telecommunication",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering"
    )

    val yearSemList = listOf(
        "1st Year / Sem 1",
        "1st Year / Sem 2",
        "2nd Year / Sem 3",
        "2nd Year / Sem 4",
        "3rd Year / Sem 5",
        "3rd Year / Sem 6",
        "4th Year / Sem 7",
        "4th Year / Sem 8"
    )

    val divisionList = listOf("Division A", "Division B", "Division C", "Division D")

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("student_login_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Role Selection")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                color = PrimaryIndigo.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Student Academic Portal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Access academics, attendance, timetable, and college updates",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector: Sign In vs Create Account
            PrimaryTabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = {
                        selectedAuthTab = 0
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Sign In", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = {
                        selectedAuthTab = 1
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Create Account", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Success / Error Banners
            if (successMessage != null) {
                Surface(
                    color = StatusSafe.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successMessage!!,
                            color = StatusSafe,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = StatusCritical.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = StatusCritical,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Tab Content
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (selectedAuthTab == 0) {
                        // ================= SIGN IN TAB =================
                        OutlinedTextField(
                            value = studentCollegeId,
                            onValueChange = {
                                studentCollegeId = it.trim().uppercase()
                                errorMessage = null
                            },
                            label = { Text("College ID *") },
                            placeholder = { Text("e.g. BD25BE016 or your College ID") },
                            supportingText = {
                                Text("Sign in using your unique assigned College ID")
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Badge, contentDescription = null, tint = PrimaryIndigo)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryIndigo)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.testTag("student_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (studentCollegeId.isBlank()) {
                                    errorMessage = "Please enter your College ID."
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMessage = "Please enter your password."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isLoading = true
                                errorMessage = null
                                viewModel.loginStudent(studentCollegeId, password) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        onLoginSuccess("student")
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("student_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Sign In with College ID",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // ================= CREATE ACCOUNT (REGISTRATION) TAB =================
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it; errorMessage = null },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Akash Thakare") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryIndigo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regCollegeId,
                            onValueChange = { regCollegeId = it.uppercase(); errorMessage = null },
                            label = { Text("College ID * (Unique)") },
                            placeholder = { Text("e.g. BD25BE016") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = PrimaryIndigo) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it; errorMessage = null },
                            label = { Text("Email Address * (For Account Recovery)") },
                            placeholder = { Text("e.g. yourname@gmail.com") },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = PrimaryIndigo) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regMobile,
                            onValueChange = { regMobile = it; errorMessage = null },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("e.g. 9876543210") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = PrimaryIndigo) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Department Dropdown
                        ExposedDropdownMenuBox(
                            expanded = deptDropdownExpanded,
                            onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = regDept,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Department *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = deptDropdownExpanded,
                                onDismissRequest = { deptDropdownExpanded = false }
                            ) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept) },
                                        onClick = {
                                            regDept = dept
                                            deptDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Year / Sem Dropdown
                            ExposedDropdownMenuBox(
                                expanded = yearDropdownExpanded,
                                onExpandedChange = { yearDropdownExpanded = !yearDropdownExpanded },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                OutlinedTextField(
                                    value = regYearSem,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Semester / Year *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = yearDropdownExpanded,
                                    onDismissRequest = { yearDropdownExpanded = false }
                                ) {
                                    yearSemList.forEach { y ->
                                        DropdownMenuItem(
                                            text = { Text(y) },
                                            onClick = {
                                                regYearSem = y
                                                yearDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Division Dropdown
                            ExposedDropdownMenuBox(
                                expanded = divDropdownExpanded,
                                onExpandedChange = { divDropdownExpanded = !divDropdownExpanded },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                OutlinedTextField(
                                    value = regDivision,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Div") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = divDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = divDropdownExpanded,
                                    onDismissRequest = { divDropdownExpanded = false }
                                ) {
                                    divisionList.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d) },
                                            onClick = {
                                                regDivision = d
                                                divDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it; errorMessage = null },
                            label = { Text("Password * (Min 6 chars)") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryIndigo) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it; errorMessage = null },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryIndigo) },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regCollegeId.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Please fill in all mandatory fields (Name, College ID, Email, Password)."
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isRegistering = true
                                errorMessage = null
                                viewModel.registerStudent(
                                    fullName = regFullName,
                                    collegeId = regCollegeId,
                                    email = regEmail,
                                    mobileNumber = regMobile,
                                    password = regPassword,
                                    confirmPassword = regConfirmPassword,
                                    department = regDept,
                                    academicYear = regAcadYear,
                                    yearSemester = regYearSem,
                                    division = regDivision
                                ) { success, msg ->
                                    isRegistering = false
                                    if (success) {
                                        successMessage = "Student account created successfully! Please sign in with your College ID ($regCollegeId)."
                                        studentCollegeId = regCollegeId
                                        selectedAuthTab = 0
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("student_register_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Create Student Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Branding
            Text(
                text = "Akax ❤ made with",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Student Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetIdentifier by remember { mutableStateOf(studentCollegeId) }
        var resetMessage by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Student Password Recovery") },
            text = {
                Column {
                    Text(
                        text = "Enter your registered Email Address or College ID. A recovery reset link will be sent to your student email.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetIdentifier,
                        onValueChange = { resetIdentifier = it },
                        label = { Text("College ID or Registered Email") },
                        placeholder = { Text("e.g. BD25BE016 or student@gmail.com") },
                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = PrimaryIndigo) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetMessage!!,
                            color = PrimaryIndigo,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetIdentifier.isNotBlank()) {
                            isResetting = true
                            viewModel.requestStudentPasswordReset(resetIdentifier) { _, msg ->
                                isResetting = false
                                resetMessage = msg
                            }
                        }
                    }
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Send Recovery Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================
// 3. TEACHER / FACULTY AUTHENTICATION
// ==========================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherLoginView(
    viewModel: CampusViewModel,
    onBack: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    // Sign In State
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Register State
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regFacultyId by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regSubjects by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val availableDepts = listOf(
        "Computer Science",
        "Information Technology",
        "Artificial Intelligence & Data Science",
        "Electronics & Telecommunication",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering"
    )
    val selectedDepts = remember { mutableStateListOf("Computer Science") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faculty Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("teacher_login_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Role Selection")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                color = SecondaryTeal.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Faculty & Teacher Portal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage classes, attendance, assignments, and academic activities",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = {
                        selectedAuthTab = 0
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Sign In", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = {
                        selectedAuthTab = 1
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Create Account", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (successMessage != null) {
                Surface(
                    color = StatusSafe.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successMessage!!,
                            color = StatusSafe,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = StatusCritical.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = StatusCritical,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (selectedAuthTab == 0) {
                        // SIGN IN TAB
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = {
                                identifier = it
                                errorMessage = null
                            },
                            label = { Text("Personal Email Address") },
                            placeholder = { Text("e.g. rahul.sharma@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = SecondaryTeal)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_identifier_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryTeal)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("teacher_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.testTag("teacher_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (identifier.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter your Email and password."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isLoading = true
                                errorMessage = null
                                viewModel.loginTeacher(identifier, password) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        onLoginSuccess("teacher")
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("teacher_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "Sign In as Faculty",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                if (isGoogleLoading) return@OutlinedButton
                                isGoogleLoading = true
                                errorMessage = null
                                viewModel.signInStaffWithGoogle(context, expectedRole = CampusRole.TEACHER, isSignUp = false) { success, role, msg ->
                                    isGoogleLoading = false
                                    if (success) {
                                        onLoginSuccess(role)
                                    } else if (!msg.contains("cancelled", ignoreCase = true)) {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading && !isGoogleLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("teacher_google_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SecondaryTeal)
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Continue with Google", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        // CREATE ACCOUNT TAB (No College ID / No Passcode required)
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it; errorMessage = null },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Dr. Rajesh Sharma") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = SecondaryTeal) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it; errorMessage = null },
                            label = { Text("Personal Email *") },
                            placeholder = { Text("e.g. rajesh.sharma@gmail.com") },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = SecondaryTeal) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regMobile,
                            onValueChange = { regMobile = it; errorMessage = null },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("e.g. 9876543210") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = SecondaryTeal) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it; errorMessage = null },
                            label = { Text("Password * (Min 6 chars)") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryTeal) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it; errorMessage = null },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SecondaryTeal) },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Please fill in all mandatory fields (Name, Email, Password)."
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isRegistering = true
                                errorMessage = null
                                viewModel.registerTeacher(
                                    fullName = regFullName,
                                    personalEmail = regEmail,
                                    mobileNumber = regMobile,
                                    password = regPassword,
                                    confirmPassword = regConfirmPassword
                                ) { success, msg ->
                                    isRegistering = false
                                    if (success) {
                                        successMessage = "Faculty account created successfully! Please sign in with your email."
                                        identifier = regEmail
                                        selectedAuthTab = 0
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("teacher_register_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Create Faculty Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                if (isGoogleLoading) return@OutlinedButton
                                isGoogleLoading = true
                                errorMessage = null
                                viewModel.signUpStaffWithGoogle(context, expectedRole = CampusRole.TEACHER) { success, role, msg ->
                                    isGoogleLoading = false
                                    if (success) {
                                        onLoginSuccess(role)
                                    } else if (!msg.contains("cancelled", ignoreCase = true)) {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isRegistering && !isGoogleLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("teacher_google_reg_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SecondaryTeal)
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Sign up with Google", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Akax ❤ made with",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(identifier) }
        var resetMsg by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Faculty Password Recovery") },
            text = {
                Column {
                    Text(
                        text = "Enter your official college email address. A password reset link will be dispatched immediately.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("College Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetMsg!!,
                            color = SecondaryTeal,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            isResetting = true
                            viewModel.requestStaffPasswordReset(resetEmail) { _, msg ->
                                isResetting = false
                                resetMsg = msg
                            }
                        }
                    }
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Send Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// ==========================================
// 4. HOD (HEAD OF DEPARTMENT) & ADMIN AUTHENTICATION
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HodLoginView(
    viewModel: CampusViewModel,
    onBack: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val hodColor = Color(0xFF7C3AED)
    var selectedAuthTab by remember { mutableIntStateOf(0) }

    val allDepartments by viewModel.allDepartments.collectAsState()

    // Sign In State
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Register State
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    // Department selection
    var selectedDeptId by remember { mutableStateOf("dept_comp") }
    var selectedDeptName by remember { mutableStateOf("Computer Engineering") }
    var deptDropdownExpanded by remember { mutableStateOf(false) }

    // Synchronize default if departments load
    LaunchedEffect(allDepartments) {
        if (allDepartments.isNotEmpty() && allDepartments.none { it.id == selectedDeptId }) {
            selectedDeptId = allDepartments.first().id
            selectedDeptName = allDepartments.first().name
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HOD & Admin Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("principal_login_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Role Selection")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                color = hodColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = hodColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Department Leadership & Administration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Manage your academic department, faculty, students, and curriculum",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = {
                        selectedAuthTab = 0
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Sign In", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = {
                        selectedAuthTab = 1
                        errorMessage = null
                        successMessage = null
                    },
                    text = { Text("Create Account", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (successMessage != null) {
                Surface(
                    color = StatusSafe.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successMessage!!,
                            color = StatusSafe,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = StatusCritical.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusCritical, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = StatusCritical,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (selectedAuthTab == 0) {
                        // SIGN IN TAB
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = {
                                identifier = it
                                errorMessage = null
                            },
                            label = { Text("HOD Email / ID") },
                            placeholder = { Text("e.g. hod.comp@mycampus.edu or gmail") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Email, contentDescription = null, tint = hodColor)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("principal_identifier_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = hodColor)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("principal_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.testTag("principal_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = hodColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (identifier.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter your HOD Email and password."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isLoading = true
                                errorMessage = null
                                viewModel.loginHod(identifier, password) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        onLoginSuccess("hod")
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("principal_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = hodColor)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "Sign In as HOD",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                if (isGoogleLoading) return@OutlinedButton
                                isGoogleLoading = true
                                errorMessage = null
                                viewModel.signInStaffWithGoogle(context, expectedRole = CampusRole.HOD, isSignUp = false) { success, role, msg ->
                                    isGoogleLoading = false
                                    if (success) {
                                        onLoginSuccess(role)
                                    } else if (!msg.contains("cancelled", ignoreCase = true)) {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isLoading && !isGoogleLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("principal_google_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = hodColor)
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Continue with Google", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else {
                        // CREATE HOD ACCOUNT TAB
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it; errorMessage = null },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Prof. Dr. Rajesh Sharma") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = hodColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it; errorMessage = null },
                            label = { Text("Personal Email *") },
                            placeholder = { Text("e.g. rajesh.sharma@gmail.com") },
                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = hodColor) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regMobile,
                            onValueChange = { regMobile = it; errorMessage = null },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("e.g. 9876543210") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = hodColor) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Department Dropdown (Mandatory!)
                        ExposedDropdownMenuBox(
                            expanded = deptDropdownExpanded,
                            onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedDeptName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Department *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                                leadingIcon = { Icon(Icons.Outlined.Domain, contentDescription = null, tint = hodColor) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = deptDropdownExpanded,
                                onDismissRequest = { deptDropdownExpanded = false }
                            ) {
                                if (allDepartments.isNotEmpty()) {
                                    allDepartments.forEach { dept ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(dept.name, fontWeight = FontWeight.Bold)
                                                    Text(dept.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedDeptId = dept.id
                                                selectedDeptName = dept.name
                                                deptDropdownExpanded = false
                                            }
                                        )
                                    }
                                } else {
                                    listOf(
                                        "dept_comp" to "Computer Engineering",
                                        "dept_it" to "Information Technology",
                                        "dept_mech" to "Mechanical Engineering",
                                        "dept_civil" to "Civil Engineering",
                                        "dept_entc" to "Electronics & Telecommunication",
                                        "dept_aids" to "Artificial Intelligence & Data Science"
                                    ).forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedDeptId = id
                                                selectedDeptName = name
                                                deptDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it; errorMessage = null },
                            label = { Text("Password * (Min 6 chars)") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = hodColor) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it; errorMessage = null },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = hodColor) },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (regFullName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    errorMessage = "Please fill in all mandatory fields (Name, Email, Password, Department)."
                                    return@Button
                                }
                                if (regPassword != regConfirmPassword) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                focusManager.clearFocus()
                                isRegistering = true
                                errorMessage = null
                                viewModel.registerHod(
                                    fullName = regFullName,
                                    personalEmail = regEmail,
                                    mobileNumber = regMobile,
                                    password = regPassword,
                                    confirmPassword = regConfirmPassword,
                                    departmentId = selectedDeptId,
                                    departmentName = selectedDeptName
                                ) { success, msg ->
                                    isRegistering = false
                                    if (success) {
                                        successMessage = "HOD account registered for $selectedDeptName! Please sign in with your email."
                                        identifier = regEmail
                                        selectedAuthTab = 0
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("principal_register_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = hodColor)
                        ) {
                            if (isRegistering) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Register HOD Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  or  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                if (isGoogleLoading) return@OutlinedButton
                                isGoogleLoading = true
                                errorMessage = null
                                viewModel.signUpStaffWithGoogle(
                                    context = context,
                                    expectedRole = CampusRole.HOD,
                                    departmentId = selectedDeptId,
                                    departmentName = selectedDeptName
                                ) { success, role, msg ->
                                    isGoogleLoading = false
                                    if (success) {
                                        onLoginSuccess(role)
                                    } else if (!msg.contains("cancelled", ignoreCase = true)) {
                                        errorMessage = msg
                                    }
                                }
                            },
                            enabled = !isRegistering && !isGoogleLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("principal_google_reg_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = hodColor)
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Sign up with Google", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Akax ❤ made with",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(identifier) }
        var resetMsg by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("HOD Password Recovery") },
            text = {
                Column {
                    Text(
                        text = "Enter your official HOD email address. A password reset link will be dispatched immediately.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Official Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetMsg!!,
                            color = hodColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            isResetting = true
                            viewModel.requestStaffPasswordReset(resetEmail) { _, msg ->
                                isResetting = false
                                resetMsg = msg
                            }
                        }
                    }
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Send Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Compatibility alias for PrincipalLoginView.
 */
@Composable
fun PrincipalLoginView(
    viewModel: CampusViewModel,
    onBack: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    HodLoginView(viewModel = viewModel, onBack = onBack, onLoginSuccess = onLoginSuccess)
}
