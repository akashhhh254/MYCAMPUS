package com.example.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AttendanceStatus
import com.example.ui.CampusViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    viewModel: CampusViewModel,
    onLogout: () -> Unit
) {
    var selectedNavTab by remember { mutableIntStateOf(0) }

    val currentUser by viewModel.currentUser.collectAsState()
    val studentEntity by viewModel.currentStudentEntity.collectAsState()
    val prediction by viewModel.studentAttendancePrediction.collectAsState()
    val subjectStats by viewModel.subjectAttendanceStats.collectAsState()
    val allTimetable by viewModel.allTimetable.collectAsState()
    val allMaterials by viewModel.allStudyMaterials.collectAsState()
    val allPapers by viewModel.allPapers.collectAsState()
    val allAssignments by viewModel.allAssignments.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allOpportunities by viewModel.allOpportunities.collectAsState()
    val notifications by viewModel.currentNotifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MyCampus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser?.fullName ?: "Student"} • Roll ${studentEntity?.rollNumber ?: "01"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigoLight
                        )
                    }
                },
                actions = {
                    // Notification Icon with Badge
                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier.testTag("student_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    // Logout
                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("student_logout_btn")
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
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = "Study") },
                    label = { Text("Study") }
                )
                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Connect") },
                    label = { Text("Connect") }
                )
                NavigationBarItem(
                    selected = selectedNavTab == 3,
                    onClick = { selectedNavTab = 3 },
                    icon = { Icon(Icons.Default.FactCheck, contentDescription = "Attendance") },
                    label = { Text("Attendance") }
                )
                NavigationBarItem(
                    selected = selectedNavTab == 4,
                    onClick = { selectedNavTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
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
            when (selectedNavTab) {
                0 -> StudentDashboardView(
                    currentUser = currentUser,
                    studentEntity = studentEntity,
                    prediction = prediction,
                    allTimetable = allTimetable,
                    allMaterials = allMaterials,
                    allAssignments = allAssignments,
                    allNotices = allNotices,
                    allEvents = allEvents,
                    onNavigateTab = { selectedNavTab = it }
                )
                1 -> StudentStudyView(
                    viewModel = viewModel,
                    allMaterials = allMaterials,
                    allPapers = allPapers
                )
                2 -> StudentCampusConnectView(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                3 -> StudentAttendanceTimetableView(
                    prediction = prediction,
                    subjectStats = subjectStats,
                    allTimetable = allTimetable
                )
                4 -> StudentProfileEventsView(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    studentEntity = studentEntity,
                    allEvents = allEvents,
                    allOpportunities = allOpportunities,
                    onOpenNotifications = { showNotificationsSheet = true }
                )
            }
        }
    }

    // Notifications Bottom Sheet
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                        Text("Mark all read", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (notifications.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = "No notifications yet",
                        message = "You are all caught up!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (notif.isRead) MaterialTheme.colorScheme.surface else PrimaryIndigo.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.markNotificationRead(notif.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (notif.type) {
                                                "Attendance" -> Icons.Default.FactCheck
                                                "Notes" -> Icons.Default.MenuBook
                                                "Assignment" -> Icons.Default.Assignment
                                                "Timetable" -> Icons.Default.CalendarMonth
                                                else -> Icons.Default.Campaign
                                            },
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold
                                        )
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    SignOutConfirmDialog(
        show = showSignOutDialog,
        onDismiss = { showSignOutDialog = false },
        onConfirmSignOut = onLogout
    )
}

@Composable
fun StudentDashboardView(
    currentUser: UserEntity?,
    studentEntity: StudentEntity?,
    prediction: com.example.ui.AttendancePrediction,
    allTimetable: List<TimetableEntity>,
    allMaterials: List<StudyMaterialEntity>,
    allAssignments: List<AssignmentEntity>,
    allNotices: List<NoticeEntity>,
    allEvents: List<EventEntity>,
    onNavigateTab: (Int) -> Unit
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
            color = PrimaryNavy,
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
                            text = "Good Morning, ${currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Student"} 👋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${studentEntity?.course ?: "BCA"} • ${studentEntity?.classGroup ?: "2nd Year"} (Sec ${studentEntity?.section ?: "A"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigoLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = studentEntity?.rollNumber ?: "01",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Attendance Widget
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onNavigateTab(3) }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AttendanceStatusChip(status = prediction.status, percentage = prediction.overallPercentage)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.1f", prediction.overallPercentage)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (prediction.status) {
                            AttendanceStatus.SAFE -> StatusSafe
                            AttendanceStatus.LOW -> StatusWarning
                            AttendanceStatus.CRITICAL -> StatusCritical
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${prediction.presentLectures} Present / ${prediction.totalLectures} Lectures",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = prediction.message,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Grid (6 shortcuts)
        Text(
            text = "Campus Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                title = "StudyMate AI",
                icon = Icons.Default.SmartToy,
                color = PrimaryIndigo,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Notes",
                icon = Icons.Default.MenuBook,
                color = SecondaryTeal,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Timetable",
                icon = Icons.Default.CalendarMonth,
                color = AccentAmber,
                onClick = { onNavigateTab(3) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                title = "Connect",
                icon = Icons.Default.Group,
                color = Color(0xFF9333EA),
                onClick = { onNavigateTab(2) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Papers",
                icon = Icons.Default.Description,
                color = Color(0xFF0284C7),
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "Assignments",
                icon = Icons.Default.Assignment,
                color = AccentRose,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Lectures
        Text(
            text = "Today's Schedule (Monday)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val todayLectures = allTimetable.filter { it.dayOfWeek == "Monday" }
        if (todayLectures.isEmpty()) {
            EmptyState(title = "No classes scheduled today")
        } else {
            todayLectures.forEach { entry ->
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
                                color = PrimaryIndigo
                            )
                            Text(text = entry.endTime, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(modifier = Modifier.height(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.subjectName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entry.teacherName} • ${entry.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Notices
        Text(
            text = "Campus Announcements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        allNotices.take(2).forEach { notice ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = notice.priority,
                                color = PrimaryIndigo,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notice.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperCreditBar()
    }
}

@Composable
fun StudentStudyView(
    viewModel: CampusViewModel,
    allMaterials: List<StudyMaterialEntity>,
    allPapers: List<PaperEntity>
) {
    var studyTab by remember { mutableIntStateOf(0) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var viewingItemDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Study & Exam Resources",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sub Tabs
        TabRow(
            selectedTabIndex = studyTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = studyTab == 0,
                onClick = { studyTab = 0 },
                text = { Text("Notes", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = studyTab == 1,
                onClick = { studyTab = 1 },
                text = { Text("Exam Papers", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = studyTab == 2,
                onClick = { studyTab = 2 },
                text = { Text("🤖 StudyMate AI", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (studyTab) {
            0 -> {
                // Study Notes
                if (allMaterials.isEmpty()) {
                    EmptyState(title = "No lecture notes uploaded yet")
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
                                        Text(
                                            text = mat.fileSize,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
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
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "By ${mat.uploaderName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                        Button(
                                            onClick = { viewingItemDialog = mat.title },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Read / Download", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Exam Papers
                if (allPapers.isEmpty()) {
                    EmptyState(title = "No exam papers available")
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
                                            color = SecondaryTeal.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${paper.subjectName} • ${paper.type}",
                                                color = SecondaryTeal,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = "Year ${paper.year}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = paper.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📁 ${paper.fileName} (${paper.fileSize})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryIndigo
                                        )
                                        Button(
                                            onClick = { viewingItemDialog = paper.title },
                                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download PDF", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // StudyMate AI
                StudyMateAiChatView(viewModel = viewModel)
            }
        }
    }

    // Material / Paper preview alert dialog
    if (viewingItemDialog != null) {
        AlertDialog(
            onDismissRequest = { viewingItemDialog = null },
            title = { Text("Resource Ready") },
            text = {
                Text(
                    text = "Opening '$viewingItemDialog' in high-speed college document viewer. Offline cache enabled.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { viewingItemDialog = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun StudyMateAiChatView(viewModel: CampusViewModel) {
    val messages by viewModel.aiMessages.collectAsState()
    val isLoading by viewModel.aiLoading.collectAsState()
    var inputPrompt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Prompt Chips
        Text(
            text = "⚡ Instant Academic Prompts",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryIndigo
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { viewModel.askStudyMate("Explain DBMS Normalization (1NF, 2NF, 3NF, BCNF) with simple examples", "explain") },
                label = { Text("Explain Normalization", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = { viewModel.askStudyMate("Generate 5 practice MCQs with answers for Database Management Systems", "mcq") },
                label = { Text("5 MCQs Drill", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = { viewModel.askStudyMate("What are the top 10 exam questions for end semester DBMS & Java?", "questions") },
                label = { Text("Exam Questions", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        ),
                        color = if (msg.isUser) PrimaryIndigo else MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "StudyMate AI is analyzing your syllabus...",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask StudyMate doubt, chapter, MCQs...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("studymate_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        viewModel.askStudyMate(inputPrompt)
                        inputPrompt = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo)
                    .testTag("studymate_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCampusConnectView(
    viewModel: CampusViewModel,
    currentUser: UserEntity?
) {
    var connectTab by remember { mutableIntStateOf(0) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()
    val allStudyGroups by viewModel.allStudyGroups.collectAsState()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var activePostForComments by remember { mutableStateOf<PostEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CampusConnect",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Universal student directory, peer discussions & study groups",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sub Tabs
        TabRow(
            selectedTabIndex = connectTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = connectTab == 0,
                onClick = { connectTab = 0 },
                text = { Text("🔍 Student Search", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = connectTab == 1,
                onClick = { connectTab = 1 },
                text = { Text("Campus Feed", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = connectTab == 2,
                onClick = { connectTab = 2 },
                text = { Text("Study Groups", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (connectTab) {
            0 -> {
                // Universal Student Search Tab
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search all students by name, roll, skills, dept...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universal_student_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Found ${searchResults.size} students across all departments",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults, key = { it.first.id }) { (user, student) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.rollNumber,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigo,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${student.course} • ${student.classGroup} (Sec ${student.section})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "@${user.username} • ID: ${user.collegeId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryIndigo
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.toggleFollowConnection(user.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Connect", fontSize = 12.sp)
                                    }
                                }

                                if (student.skills.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚡ Skills: ${student.skills}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (student.academicInterests.isNotBlank()) {
                                    Text(
                                        text = "🎯 Interests: ${student.academicInterests}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Campus Feed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Campus Discussions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showCreatePostDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ New Post")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allPosts, key = { it.id }) { post ->
                        val isLiked = post.likedUserIds.contains(currentUser?.id ?: "")

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryIndigoLight.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = post.authorName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryIndigo
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = post.authorName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            RoleBadge(role = post.authorRole)
                                        }
                                        Text(
                                            text = "@${post.authorUsername} • ${post.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = post.content,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.clickable { viewModel.toggleLikePost(post.id) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (isLiked) StatusCritical else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${post.likeCount} Likes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isLiked) StatusCritical else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.clickable { activePostForComments = post },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = "Comment",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${post.commentCount} Comments",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Study Groups
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Study Circles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showCreateGroupDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Create Group")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allStudyGroups, key = { it.id }) { group ->
                        val isMember = group.memberUserIds.contains(currentUser?.id ?: "")

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
                                        text = group.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = SecondaryTeal.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = group.subject,
                                            color = SecondaryTeal,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = group.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "👥 ${group.memberCount} members • Created by ${group.creatorName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                    Button(
                                        onClick = { viewModel.toggleJoinStudyGroup(group.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isMember) StatusSafe else PrimaryIndigo
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (isMember) "Joined ✓" else "Join Group", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets for CampusConnect
    if (showCreatePostDialog) {
        CreatePostDialog(
            onDismiss = { showCreatePostDialog = false },
            onConfirm = { text, cat ->
                viewModel.createPost(text, cat) {
                    showCreatePostDialog = false
                }
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, sub, desc ->
                viewModel.createStudyGroup(name, sub, desc) {
                    showCreateGroupDialog = false
                }
            }
        )
    }

    // Comments Sheet
    if (activePostForComments != null) {
        val post = activePostForComments!!
        val comments by viewModel.repository.getCommentsForPost(post.id).collectAsState(initial = emptyList())
        var newCommentText by remember { mutableStateOf("") }

        ModalBottomSheet(onDismissRequest = { activePostForComments = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discussion Comments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(comments, key = { it.id }) { cmt ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cmt.authorName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RoleBadge(role = cmt.authorRole)
                                }
                                Text(
                                    text = cmt.content,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                viewModel.addComment(post.id, newCommentText) {
                                    newCommentText = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun StudentAttendanceTimetableView(
    prediction: com.example.ui.AttendancePrediction,
    subjectStats: List<com.example.ui.SubjectAttendanceStat>,
    allTimetable: List<TimetableEntity>
) {
    val scrollState = rememberScrollState()
    var selectedDay by remember { mutableStateOf("Monday") }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    val dayLectures = allTimetable.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Attendance & Timetable",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Predictive Attendance Calculator Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AttendanceStatusChip(status = prediction.status, percentage = prediction.overallPercentage)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", prediction.overallPercentage)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (prediction.status) {
                            AttendanceStatus.SAFE -> StatusSafe
                            AttendanceStatus.LOW -> StatusWarning
                            AttendanceStatus.CRITICAL -> StatusCritical
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${prediction.presentLectures} of ${prediction.totalLectures} lectures attended",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${prediction.absentLectures} missed lectures",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (prediction.overallPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = when (prediction.status) {
                        AttendanceStatus.SAFE -> StatusSafe
                        AttendanceStatus.LOW -> StatusWarning
                        AttendanceStatus.CRITICAL -> StatusCritical
                    },
                    trackColor = Slate200
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prediction Advice Box
                Surface(
                    color = when (prediction.status) {
                        AttendanceStatus.SAFE -> StatusSafe.copy(alpha = 0.1f)
                        AttendanceStatus.LOW -> StatusWarning.copy(alpha = 0.1f)
                        AttendanceStatus.CRITICAL -> StatusCritical.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ${prediction.message}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when (prediction.status) {
                            AttendanceStatus.SAFE -> StatusSafe
                            AttendanceStatus.LOW -> StatusWarning
                            AttendanceStatus.CRITICAL -> StatusCritical
                        },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Subject Breakdown Progress Bars
        Text(
            text = "Subject-Wise Attendance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        subjectStats.forEach { stat ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stat.subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", stat.percentage)}% (${stat.present}/${stat.total})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (stat.status) {
                                AttendanceStatus.SAFE -> StatusSafe
                                AttendanceStatus.LOW -> StatusWarning
                                AttendanceStatus.CRITICAL -> StatusCritical
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when (stat.status) {
                            AttendanceStatus.SAFE -> StatusSafe
                            AttendanceStatus.LOW -> StatusWarning
                            AttendanceStatus.CRITICAL -> StatusCritical
                        },
                        trackColor = Slate200
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Weekly Timetable
        Text(
            text = "Dynamic Weekly Timetable",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        ScrollableTabRow(
            selectedTabIndex = days.indexOf(selectedDay).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            days.forEach { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    text = { Text(day, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (dayLectures.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CalendarToday,
                title = "No classes on $selectedDay",
                message = "Enjoy your self-study time or work on project assignments."
            )
        } else {
            dayLectures.forEach { entry ->
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
                                color = PrimaryIndigo
                            )
                            Text(text = entry.endTime, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(modifier = Modifier.height(38.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.subjectName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${entry.teacherName} • ${entry.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
fun StudentProfileEventsView(
    viewModel: CampusViewModel,
    currentUser: UserEntity?,
    studentEntity: StudentEntity?,
    allEvents: List<EventEntity>,
    allOpportunities: List<OpportunityEntity>,
    onOpenNotifications: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Student Profile & Opportunities",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Identity Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = studentEntity?.rollNumber ?: "01",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: "Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "College ID: ${currentUser?.collegeId} • @${currentUser?.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Academic credentials (Locked by Principal)
                Text(
                    text = "🔒 Academic Records (Principal Verified)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Course: ${studentEntity?.course} | Dept: ${studentEntity?.department}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Class: ${studentEntity?.classGroup} | Section: ${studentEntity?.section} | Roll No: ${studentEntity?.rollNumber}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Personal Details (Editable)
                Text(
                    text = "Bio & Interests",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = studentEntity?.bio ?: "No bio added yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚡ Skills: ${studentEntity?.skills}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryTeal,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "🎯 Interests: ${studentEntity?.academicInterests}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryIndigo
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showEditProfileDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Bio, Skills & Interests")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Campus Events
        Text(
            text = "Campus Events & Symposiums",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        allEvents.forEach { event ->
            val isRegistered = event.registeredUserIds.contains(currentUser?.id ?: "")

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📅 ${event.date} (${event.time}) • 📍 ${event.location}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryIndigo
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Organized by ${event.organizer}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                        Button(
                            onClick = { viewModel.registerForEvent(event.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegistered) StatusSafe else SecondaryTeal
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(if (isRegistered) "Registered ✓" else "Register 1-Click", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Career Opportunities & Internships
        Text(
            text = "Internships, Hackathons & Scholarships",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        allOpportunities.forEach { opp ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = opp.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = AccentAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = opp.type,
                                color = AccentAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Organization: ${opp.organization} • Deadline: ${opp.deadline}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opp.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Eligibility: ${opp.eligibility}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperCreditBar()
    }

    // Edit Profile Dialog
    if (showEditProfileDialog && studentEntity != null) {
        var bio by remember { mutableStateOf(studentEntity.bio) }
        var skills by remember { mutableStateOf(studentEntity.skills) }
        var interests by remember { mutableStateOf(studentEntity.academicInterests) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Student Profile") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Academic details (Course, Roll, Dept) are verified by Principal and cannot be altered.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = interests,
                        onValueChange = { interests = it },
                        label = { Text("Academic Interests") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudentPersonalProfile(bio, skills, interests) {
                            showEditProfileDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Dialogs for Student Posts & Groups
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic Discussion") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Campus Post") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What's on your mind? *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Academic Discussion", "Project Help", "Coding", "General").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.take(10), fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) onConfirm(content, category)
                }
            ) {
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("DBMS") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Study Group") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name *") },
                    placeholder = { Text("e.g. Kotlin & Compose Devs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Topic / Subject *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name, subject, description)
                }
            ) {
                Text("Create Circle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
