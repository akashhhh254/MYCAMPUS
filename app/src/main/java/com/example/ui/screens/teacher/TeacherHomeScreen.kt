package com.example.ui.screens.teacher

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.CampusViewModel
import com.example.ui.components.*
import com.example.ui.screens.principal.AddNoticeDialog
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    viewModel: CampusViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTeacher by viewModel.currentTeacherEntity.collectAsState()
    val assignments by viewModel.teacherAssignments.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allStudentUsers by viewModel.allStudentUsers.collectAsState()
    val allMaterials by viewModel.allStudyMaterials.collectAsState()
    val allPapers by viewModel.allPapers.collectAsState()
    val allAssignmentsList by viewModel.allAssignments.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allTimetable by viewModel.allTimetable.collectAsState()

    var showUploadMaterialDialog by remember { mutableStateOf(false) }
    var showUploadPaperDialog by remember { mutableStateOf(false) }
    var showCreateAssignmentDialog by remember { mutableStateOf(false) }
    var showCreateNoticeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MyCampus • Faculty Portal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.fullName ?: "Faculty",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryTeal
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("teacher_logout_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = StatusCritical)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = "Attendance") },
                    label = { Text("Attendance") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Materials") },
                    label = { Text("Materials") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Assignments") },
                    label = { Text("Assignments") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                    label = { Text("Notices") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> TeacherDashboardTab(
                    currentUser = currentUser,
                    teacherAssignments = assignments,
                    allTimetable = allTimetable,
                    allMaterials = allMaterials,
                    allAssignments = allAssignmentsList,
                    onNavigateTab = { selectedTab = it },
                    onUploadMaterial = { showUploadMaterialDialog = true },
                    onCreateAssignment = { showCreateAssignmentDialog = true }
                )
                1 -> TeacherAttendanceTab(
                    viewModel = viewModel,
                    allStudents = allStudents,
                    allStudentUsers = allStudentUsers,
                    teacherAssignments = assignments,
                    currentTeacher = currentTeacher
                )
                2 -> TeacherMaterialsTab(
                    allMaterials = allMaterials,
                    allPapers = allPapers,
                    currentTeacher = currentTeacher,
                    onUploadMaterial = { showUploadMaterialDialog = true },
                    onUploadPaper = { showUploadPaperDialog = true },
                    onDeleteMaterial = { viewModel.deleteStudyMaterial(it) },
                    onDeletePaper = { viewModel.deletePaper(it) }
                )
                3 -> TeacherAssignmentsTab(
                    viewModel = viewModel,
                    allAssignments = allAssignmentsList,
                    currentTeacher = currentTeacher,
                    onCreateAssignment = { showCreateAssignmentDialog = true },
                    onDeleteAssignment = { viewModel.deleteAssignment(it) }
                )
                4 -> TeacherNoticesTab(
                    allNotices = allNotices,
                    currentTeacher = currentTeacher,
                    currentUser = currentUser,
                    onCreateNotice = { showCreateNoticeDialog = true },
                    onDeleteNotice = { viewModel.deleteNotice(it) }
                )
            }
        }
    }

    // Dialogs
    if (showUploadMaterialDialog) {
        UploadMaterialDialog(
            uploaderId = currentTeacher?.id ?: "tch_rahul",
            uploaderName = currentUser?.fullName ?: "Faculty",
            onDismiss = { showUploadMaterialDialog = false },
            onConfirm = { mat ->
                viewModel.uploadStudyMaterial(mat) {
                    showUploadMaterialDialog = false
                }
            }
        )
    }

    if (showUploadPaperDialog) {
        UploadPaperDialog(
            uploaderId = currentTeacher?.id ?: "tch_rahul",
            uploaderName = currentUser?.fullName ?: "Faculty",
            onDismiss = { showUploadPaperDialog = false },
            onConfirm = { paper ->
                viewModel.uploadPaper(paper) {
                    showUploadPaperDialog = false
                }
            }
        )
    }

    if (showCreateAssignmentDialog) {
        CreateAssignmentDialog(
            teacherId = currentTeacher?.id ?: "tch_rahul",
            teacherName = currentUser?.fullName ?: "Faculty",
            onDismiss = { showCreateAssignmentDialog = false },
            onConfirm = { assignment ->
                viewModel.createAssignment(assignment) {
                    showCreateAssignmentDialog = false
                }
            }
        )
    }

    if (showCreateNoticeDialog) {
        AddNoticeDialog(
            authorId = currentUser?.id ?: "user_tch",
            authorName = "${currentUser?.fullName ?: "Faculty"} (Faculty)",
            authorRole = "teacher",
            onDismiss = { showCreateNoticeDialog = false },
            onConfirm = { notice: NoticeEntity ->
                viewModel.createNotice(notice) {
                    showCreateNoticeDialog = false
                }
            }
        )
    }

    SignOutConfirmDialog(
        show = showSignOutDialog,
        onDismiss = { showSignOutDialog = false },
        onConfirmSignOut = onLogout
    )
}

@Composable
fun TeacherDashboardTab(
    currentUser: UserEntity?,
    teacherAssignments: List<TeacherAssignmentEntity>,
    allTimetable: List<TimetableEntity>,
    allMaterials: List<StudyMaterialEntity>,
    allAssignments: List<AssignmentEntity>,
    onNavigateTab: (Int) -> Unit,
    onUploadMaterial: () -> Unit,
    onCreateAssignment: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF134E4A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome, ${currentUser?.fullName ?: "Professor"} 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Department of Computer Applications",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF99F6E4)
                        )
                    }
                    RoleBadge(role = "teacher")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Assigned Subjects",
                value = "${teacherAssignments.size.coerceAtLeast(1)}",
                subtitle = "BCA 2nd Year (Sec A)",
                icon = Icons.Default.MenuBook,
                iconColor = SecondaryTeal,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(2) }
            )
            StatCard(
                title = "Live Materials",
                value = "${allMaterials.size}",
                subtitle = "Notes & Guides",
                icon = Icons.Default.Description,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(2) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        Text(
            text = "Faculty Shortcuts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                title = "Take Attendance",
                icon = Icons.Default.FactCheck,
                color = SecondaryTeal,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "+ Notes",
                icon = Icons.Default.UploadFile,
                color = PrimaryIndigo,
                onClick = onUploadMaterial,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "+ Assignment",
                icon = Icons.Default.AssignmentLate,
                color = AccentAmber,
                onClick = onCreateAssignment,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Notices",
                icon = Icons.Default.Campaign,
                color = Color(0xFF9333EA),
                onClick = { onNavigateTab(4) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Lectures
        Text(
            text = "Today's Schedule & Classes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        val mondayLectures = allTimetable.filter { it.dayOfWeek == "Monday" }
        if (mondayLectures.isEmpty()) {
            EmptyState(title = "No classes scheduled today")
        } else {
            mondayLectures.forEach { entry ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(80.dp)) {
                            Text(
                                text = entry.startTime,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryTeal
                            )
                            Text(text = entry.endTime, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(modifier = Modifier.height(38.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.subjectName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entry.classGroup} (${entry.roomNumber})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onNavigateTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Attend", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperCreditBar()
    }
}

@Composable
fun TeacherAttendanceTab(
    viewModel: CampusViewModel,
    allStudents: List<StudentEntity>,
    allStudentUsers: List<UserEntity>,
    teacherAssignments: List<TeacherAssignmentEntity>,
    currentTeacher: TeacherEntity?
) {
    var selectedClass by remember { mutableStateOf("BCA 2nd Year") }
    var selectedSection by remember { mutableStateOf("A") }
    var selectedSubjectId by remember { mutableStateOf("sub_dbms") }
    var selectedSubjectName by remember { mutableStateOf("DBMS") }
    var attendanceDate by remember { mutableStateOf("2026-08-30") }

    val userMap = remember(allStudentUsers) { allStudentUsers.associateBy { it.id } }

    val classStudents = remember(allStudents, selectedClass, selectedSection) {
        allStudents
            .filter { it.classGroup == selectedClass && it.section == selectedSection && !it.isDeactivated }
            .sortedBy { it.rollNumber.toIntOrNull() ?: 99 }
    }

    // Attendance state map: studentId -> isPresent
    val attendanceMap = remember(classStudents) {
        mutableStateMapOf<String, Boolean>().apply {
            classStudents.forEach { put(it.id, true) } // Default all present
        }
    }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Smart Attendance Register",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Mark student presence with instant database sync & notification",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Class & Subject selector card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$selectedSubjectName • $selectedClass (Sec $selectedSection)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTeal
                        )
                        Text(
                            text = "Date: $attendanceDate • ${classStudents.size} Students Enrolled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Mark All Present / Absent
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            classStudents.forEach { attendanceMap[it.id] = true }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("All Present (✓)", fontSize = 12.sp, color = StatusSafe)
                    }
                    OutlinedButton(
                        onClick = {
                            classStudents.forEach { attendanceMap[it.id] = false }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("All Absent (✗)", fontSize = 12.sp, color = StatusCritical)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live stats count
        val presentCount = attendanceMap.values.count { it }
        val absentCount = classStudents.size - presentCount
        val pct = if (classStudents.isNotEmpty()) (presentCount.toFloat() / classStudents.size.toFloat()) * 100f else 0f

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Present: $presentCount | Absent: $absentCount",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Today's Attendance: ${String.format("%.1f", pct)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (pct >= 75f) StatusSafe else StatusWarning
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (saveSuccessMessage) {
            Surface(
                color = StatusSafe.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Attendance saved to database! Notifications dispatched to students.",
                    color = StatusSafe,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Student Roll List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(classStudents, key = { it.id }) { student ->
                val user = userMap[student.userId]
                val isPresent = attendanceMap[student.id] ?: true

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = ButtonDefaults.outlinedButtonBorder(isPresent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            attendanceMap[student.id] = !isPresent
                            saveSuccessMessage = false
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPresent) StatusSafe else StatusCritical),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.rollNumber,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user?.fullName ?: "Student",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Roll: ${student.rollNumber} • ID: ${user?.collegeId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Toggle indicator button
                        Surface(
                            color = if (isPresent) StatusSafe.copy(alpha = 0.15f) else StatusCritical.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isPresent) StatusSafe else StatusCritical,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPresent) "Present" else "Absent",
                                    color = if (isPresent) StatusSafe else StatusCritical,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save Attendance Button
        Button(
            onClick = {
                val records = classStudents.map { student ->
                    val user = userMap[student.userId]
                    val isPresent = attendanceMap[student.id] ?: true
                    AttendanceRecordEntity(
                        id = "att_${attendanceDate}_${selectedSubjectId}_${student.id}",
                        date = attendanceDate,
                        classGroup = selectedClass,
                        section = selectedSection,
                        subjectId = selectedSubjectId,
                        subjectName = selectedSubjectName,
                        teacherId = currentTeacher?.id ?: "tch_rahul",
                        studentId = student.id,
                        studentRoll = student.rollNumber,
                        studentName = user?.fullName ?: "Student",
                        isPresent = isPresent
                    )
                }
                viewModel.saveAttendance(records, selectedSubjectName, attendanceDate) {
                    saveSuccessMessage = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("teacher_save_attendance_btn")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save & Sync Attendance",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TeacherMaterialsTab(
    allMaterials: List<StudyMaterialEntity>,
    allPapers: List<PaperEntity>,
    currentTeacher: TeacherEntity?,
    onUploadMaterial: () -> Unit,
    onUploadPaper: () -> Unit,
    onDeleteMaterial: (String) -> Unit,
    onDeletePaper: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Study Resources",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Upload notes, lecture slides & exam question papers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tabs
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Study Notes (${allMaterials.size})", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Question Papers (${allPapers.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Upload Button
        Button(
            onClick = { if (subTab == 0) onUploadMaterial() else onUploadPaper() },
            colors = ButtonDefaults.buttonColors(containerColor = if (subTab == 0) PrimaryIndigo else SecondaryTeal),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (subTab == 0) "+ Upload Study Notes / Slides" else "+ Upload Exam Paper / Top Questions")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subTab == 0) {
            if (allMaterials.isEmpty()) {
                EmptyState(title = "No study notes uploaded yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allMaterials, key = { it.id }) { mat ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = PrimaryIndigo.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${mat.subjectName} • ${mat.unit}",
                                            color = PrimaryIndigo,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeleteMaterial(mat.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                                    }
                                }
                                Text(
                                    text = mat.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mat.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📁 ${mat.fileName} (${mat.fileSize})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "By ${mat.uploaderName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (allPapers.isEmpty()) {
                EmptyState(title = "No question papers uploaded yet")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allPapers, key = { it.id }) { paper ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = AccentAmber.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${paper.subjectName} • ${paper.type}",
                                            color = AccentAmber,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(onClick = { onDeletePaper(paper.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                                    }
                                }
                                Text(
                                    text = paper.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "📁 ${paper.fileName} (${paper.fileSize}) • Year: ${paper.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentsTab(
    viewModel: CampusViewModel,
    allAssignments: List<AssignmentEntity>,
    currentTeacher: TeacherEntity?,
    onCreateAssignment: () -> Unit,
    onDeleteAssignment: (String) -> Unit
) {
    var selectedAssignmentForSubmissions by remember { mutableStateOf<AssignmentEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Class Assignments",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track student project submissions & deadlines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCreateAssignment,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (allAssignments.isEmpty()) {
            EmptyState(title = "No assignments created yet")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allAssignments, key = { it.id }) { assignment ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = PrimaryIndigo.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${assignment.subjectName} • ${assignment.classGroup}",
                                        color = PrimaryIndigo,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteAssignment(assignment.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = assignment.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = assignment.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏳ Due: ${assignment.deadline}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AccentAmber
                                )
                                Button(
                                    onClick = { selectedAssignmentForSubmissions = assignment },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Submissions", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Submissions Viewer Bottom Sheet
    if (selectedAssignmentForSubmissions != null) {
        val assignment = selectedAssignmentForSubmissions!!
        val submissions by viewModel.repository.getSubmissionsForAssignment(assignment.id).collectAsState(initial = emptyList())

        ModalBottomSheet(onDismissRequest = { selectedAssignmentForSubmissions = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Submissions: ${assignment.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${submissions.size} students submitted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (submissions.isEmpty()) {
                    EmptyState(title = "No submissions received yet")
                } else {
                    submissions.forEach { sub ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${sub.studentName} (Roll ${sub.studentRoll})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = sub.status,
                                        color = StatusSafe,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sub.submissionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (sub.attachedFileName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📎 ${sub.attachedFileName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TeacherNoticesTab(
    allNotices: List<NoticeEntity>,
    currentTeacher: TeacherEntity?,
    currentUser: UserEntity?,
    onCreateNotice: () -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Faculty Notices",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Announcements to your assigned classes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCreateNotice,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Broadcast")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allNotices.isEmpty()) {
            EmptyState(title = "No notices broadcasted yet")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allNotices, key = { it.id }) { notice ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (notice.authorId == currentUser?.id || notice.authorRole == "teacher") {
                                    IconButton(onClick = { onDeleteNotice(notice.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notice.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "By ${notice.authorName} • Date: ${notice.date}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialogs for Teacher
@Composable
fun UploadMaterialDialog(
    uploaderId: String,
    uploaderName: String,
    onDismiss: () -> Unit,
    onConfirm: (StudyMaterialEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Unit 1") }
    var topic by remember { mutableStateOf("Relational Model & Keys") }
    var subjectName by remember { mutableStateOf("DBMS") }
    var fileName by remember { mutableStateOf("DBMS_Unit1_Lecture_Notes.pdf") }
    var fileType by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Study Material") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Material Title *") },
                    placeholder = { Text("e.g. Unit 1 Complete Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Chapter *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (.pdf / .pptx / .docx)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val mat = StudyMaterialEntity(
                            id = UUID.randomUUID().toString(),
                            subjectId = "sub_dbms",
                            subjectName = subjectName,
                            classGroup = "BCA 2nd Year",
                            section = "A",
                            unit = unit,
                            topic = topic,
                            title = title,
                            description = description,
                            fileType = fileType,
                            fileName = fileName,
                            fileSize = "3.2 MB",
                            uploaderId = uploaderId,
                            uploaderName = uploaderName
                        )
                        onConfirm(mat)
                    }
                }
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UploadPaperDialog(
    uploaderId: String,
    uploaderName: String,
    onDismiss: () -> Unit,
    onConfirm: (PaperEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Previous Year Paper") }
    var year by remember { mutableStateOf("2025") }
    var subjectName by remember { mutableStateOf("DBMS") }
    var fileName by remember { mutableStateOf("DBMS_University_Exam_2025.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Exam Paper") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Paper Title *") },
                    placeholder = { Text("e.g. End Sem Exam 2025 Solved") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Type:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Previous Year Paper", "Model Paper", "Important Questions").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.take(15)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val paper = PaperEntity(
                            id = UUID.randomUUID().toString(),
                            subjectId = "sub_dbms",
                            subjectName = subjectName,
                            unit = "Unit 1-4",
                            type = type,
                            title = title,
                            year = year,
                            fileName = fileName,
                            fileSize = "2.8 MB",
                            uploaderId = uploaderId,
                            uploaderName = uploaderName
                        )
                        onConfirm(paper)
                    }
                }
            ) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateAssignmentDialog(
    teacherId: String,
    teacherName: String,
    onDismiss: () -> Unit,
    onConfirm: (AssignmentEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("2026-09-10 23:59") }
    var subjectName by remember { mutableStateOf("DBMS") }
    var classGroup by remember { mutableStateOf("BCA 2nd Year") }
    var totalMarks by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Class Assignment") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Problem Statement / Instructions *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Total Marks") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val assign = AssignmentEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            subjectId = "sub_dbms",
                            subjectName = subjectName,
                            classGroup = classGroup,
                            section = "A",
                            deadline = deadline,
                            teacherId = teacherId,
                            teacherName = teacherName,
                            totalMarks = totalMarks.toIntOrNull() ?: 50
                        )
                        onConfirm(assign)
                    }
                }
            ) {
                Text("Publish Assignment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
