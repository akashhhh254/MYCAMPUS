package com.example.ui.screens.principal

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
import com.example.ui.AttendanceStatus
import com.example.ui.CampusViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalHomeScreen(
    viewModel: CampusViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allStudentUsers by viewModel.allStudentUsers.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allTeacherUsers by viewModel.allTeacherUsers.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allTimetable by viewModel.allTimetable.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allAttendance by viewModel.allAttendanceRecords.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddTimetableDialog by remember { mutableStateOf(false) }
    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MyCampus • Principal Portal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.fullName ?: "Dr. Alok Verma (Principal)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigoLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("principal_logout_btn")
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
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Teachers") },
                    label = { Text("Teachers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable") },
                    label = { Text("Timetable") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") }
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
                0 -> PrincipalDashboardTab(
                    viewModel = viewModel,
                    allStudents = allStudents,
                    allTeachers = allTeachers,
                    allAttendance = allAttendance,
                    allNotices = allNotices,
                    onNavigateTab = { selectedTab = it },
                    onAddNotice = { showAddNoticeDialog = true },
                    onAddEvent = { showAddEventDialog = true }
                )
                1 -> PrincipalStudentsTab(
                    allStudents = allStudents,
                    allStudentUsers = allStudentUsers,
                    onAddStudent = { showAddStudentDialog = true },
                    onToggleDeactivate = { viewModel.toggleStudentDeactivation(it) },
                    onDelete = { uId, sId -> viewModel.deleteStudentAccount(uId, sId) }
                )
                2 -> PrincipalTeachersTab(
                    allTeachers = allTeachers,
                    allTeacherUsers = allTeacherUsers,
                    allSubjects = allSubjects,
                    onAddTeacher = { showAddTeacherDialog = true },
                    onToggleDeactivate = { viewModel.toggleTeacherDeactivation(it) }
                )
                3 -> PrincipalTimetableTab(
                    allTimetable = allTimetable,
                    allSubjects = allSubjects,
                    allTeachers = allTeachers,
                    allTeacherUsers = allTeacherUsers,
                    onAddEntry = { showAddTimetableDialog = true },
                    onDeleteEntry = { viewModel.deleteTimetableEntry(it) }
                )
                4 -> PrincipalAnalyticsTab(
                    allStudents = allStudents,
                    allAttendance = allAttendance,
                    allSubjects = allSubjects,
                    allNotices = allNotices,
                    allEvents = allEvents,
                    onBroadcastNotice = { showAddNoticeDialog = true },
                    onAddEvent = { showAddEventDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, email, colId, roll, dept, course, yr, cls, sec ->
                viewModel.addStudent(name, email, colId, roll, dept, course, yr, cls, sec) {
                    showAddStudentDialog = false
                }
            }
        )
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            subjects = allSubjects,
            onDismiss = { showAddTeacherDialog = false },
            onConfirm = { name, email, empId, dept, desig, qual, subId, subName, cls, sec ->
                viewModel.addTeacher(name, email, empId, dept, desig, qual, subId, subName, cls, sec) {
                    showAddTeacherDialog = false
                }
            }
        )
    }

    if (showAddTimetableDialog) {
        AddTimetableDialog(
            subjects = allSubjects,
            teachers = allTeachers,
            teacherUsers = allTeacherUsers,
            onDismiss = { showAddTimetableDialog = false },
            onConfirm = { entry ->
                viewModel.addTimetableEntry(entry) {
                    showAddTimetableDialog = false
                }
            }
        )
    }

    if (showAddNoticeDialog) {
        AddNoticeDialog(
            authorId = currentUser?.id ?: "user_principal",
            authorName = "${currentUser?.fullName ?: "Principal"} (Principal)",
            authorRole = "principal",
            onDismiss = { showAddNoticeDialog = false },
            onConfirm = { notice ->
                viewModel.createNotice(notice) {
                    showAddNoticeDialog = false
                }
            }
        )
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { event ->
                viewModel.createEvent(event) {
                    showAddEventDialog = false
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
fun PrincipalDashboardTab(
    viewModel: CampusViewModel,
    allStudents: List<StudentEntity>,
    allTeachers: List<TeacherEntity>,
    allAttendance: List<AttendanceRecordEntity>,
    allNotices: List<NoticeEntity>,
    onNavigateTab: (Int) -> Unit,
    onAddNotice: () -> Unit,
    onAddEvent: () -> Unit
) {
    val scrollState = rememberScrollState()

    val totalStudents = allStudents.size
    val activeStudents = allStudents.count { !it.isDeactivated }
    val totalTeachers = allTeachers.size
    val totalLectures = allAttendance.size
    val totalPresents = allAttendance.count { it.isPresent }
    val avgAttendance = if (totalLectures > 0) (totalPresents.toFloat() / totalLectures.toFloat()) * 100f else 88.5f

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Admin Control Center",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Full Institutional Authority & Analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                    RoleBadge(role = "principal")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stat KPI Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Total Students",
                value = "$totalStudents",
                subtitle = "$activeStudents active",
                icon = Icons.Default.School,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(1) }
            )
            StatCard(
                title = "Faculty Members",
                value = "$totalTeachers",
                subtitle = "Active Teachers",
                icon = Icons.Default.Person,
                iconColor = SecondaryTeal,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(2) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Campus Attendance",
                value = "${String.format("%.1f", avgAttendance)}%",
                subtitle = "Across all classes",
                icon = Icons.Default.CheckCircle,
                iconColor = if (avgAttendance >= 75f) StatusSafe else StatusWarning,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(4) }
            )
            StatCard(
                title = "College Classes",
                value = "3 Batches",
                subtitle = "BCA & B.Tech",
                icon = Icons.Default.Class,
                iconColor = AccentAmber,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(3) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        Text(
            text = "Institutional Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionButton(
                title = "+ Student",
                icon = Icons.Default.PersonAdd,
                color = PrimaryIndigo,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "+ Teacher",
                icon = Icons.Default.GroupAdd,
                color = SecondaryTeal,
                onClick = { onNavigateTab(2) },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "+ Notice",
                icon = Icons.Default.Campaign,
                color = AccentAmber,
                onClick = onAddNotice,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                title = "+ Event",
                icon = Icons.Default.Event,
                color = Color(0xFF9333EA),
                onClick = onAddEvent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Notices section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Campus Notices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onAddNotice) {
                Text("+ Broadcast", color = PrimaryIndigo, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (allNotices.isEmpty()) {
            EmptyState(title = "No notices published yet")
        } else {
            allNotices.take(3).forEach { notice ->
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
                                color = if (notice.priority == "Urgent") StatusCritical.copy(alpha = 0.12f) else PrimaryIndigo.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = notice.priority,
                                    color = if (notice.priority == "Urgent") StatusCritical else PrimaryIndigo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target: ${notice.targetRole} (${notice.targetClass}) • ${notice.date}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperCreditBar()
    }
}

@Composable
fun PrincipalStudentsTab(
    allStudents: List<StudentEntity>,
    allStudentUsers: List<UserEntity>,
    onAddStudent: () -> Unit,
    onToggleDeactivate: (StudentEntity) -> Unit,
    onDelete: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterClass by remember { mutableStateOf("All") }

    val userMap = remember(allStudentUsers) { allStudentUsers.associateBy { it.id } }

    val filteredList = remember(allStudents, searchQuery, selectedFilterClass, userMap) {
        allStudents.filter { s ->
            val user = userMap[s.userId]
            val matchesClass = selectedFilterClass == "All" || s.classGroup == selectedFilterClass
            val q = searchQuery.lowercase().trim()
            val matchesQuery = q.isEmpty() ||
                    (user?.fullName?.lowercase()?.contains(q) == true) ||
                    (user?.collegeId?.lowercase()?.contains(q) == true) ||
                    s.rollNumber.lowercase().contains(q) ||
                    s.department.lowercase().contains(q)
            matchesClass && matchesQuery
        }
    }

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
                    text = "Student Directory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allStudents.size} registered students",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onAddStudent,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("principal_add_student_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Student")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, roll no, college ID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Class Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All", "BCA 2nd Year", "B.Tech CS 3rd Year").forEach { cls ->
                FilterChip(
                    selected = selectedFilterClass == cls,
                    onClick = { selectedFilterClass = cls },
                    label = { Text(cls) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            EmptyState(title = "No students match your query")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { student ->
                    val user = userMap[student.userId]
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
                                        .background(if (student.isDeactivated) Slate400 else PrimaryIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = student.rollNumber,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user?.fullName ?: "Student",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (student.isDeactivated) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = StatusCritical.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Deactivated",
                                                    color = StatusCritical,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${student.course} • ${student.classGroup} (Sec ${student.section})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "ID: ${user?.collegeId} • ${user?.email}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onToggleDeactivate(student) }
                                ) {
                                    Text(
                                        text = if (student.isDeactivated) "Activate" else "Deactivate",
                                        color = if (student.isDeactivated) StatusSafe else StatusWarning
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (user != null) {
                                            onDelete(user.id, student.id)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrincipalTeachersTab(
    allTeachers: List<TeacherEntity>,
    allTeacherUsers: List<UserEntity>,
    allSubjects: List<SubjectEntity>,
    onAddTeacher: () -> Unit,
    onToggleDeactivate: (TeacherEntity) -> Unit
) {
    val userMap = remember(allTeacherUsers) { allTeacherUsers.associateBy { it.id } }

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
                    text = "Faculty Directory",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allTeachers.size} faculty members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onAddTeacher,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("principal_add_teacher_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Teacher")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (allTeachers.isEmpty()) {
            EmptyState(title = "No faculty registered yet")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allTeachers, key = { it.id }) { teacher ->
                    val user = userMap[teacher.userId]
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SecondaryTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SecondaryTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user?.fullName ?: "Faculty",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${teacher.designation} • ${teacher.department}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Emp ID: ${teacher.employeeId} • ${teacher.qualification}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = user?.email ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                                TextButton(onClick = { onToggleDeactivate(teacher) }) {
                                    Text(
                                        text = if (teacher.isDeactivated) "Activate" else "Deactivate",
                                        color = if (teacher.isDeactivated) StatusSafe else StatusWarning
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

@Composable
fun PrincipalTimetableTab(
    allTimetable: List<TimetableEntity>,
    allSubjects: List<SubjectEntity>,
    allTeachers: List<TeacherEntity>,
    allTeacherUsers: List<UserEntity>,
    onAddEntry: () -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    var selectedDay by remember { mutableStateOf("Monday") }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    val dayEntries = remember(allTimetable, selectedDay) {
        allTimetable.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

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
                    text = "Timetable Manager",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dynamic class schedule controller",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onAddEntry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Class")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Day Selector Tabs
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

        if (dayEntries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CalendarToday,
                title = "No classes scheduled for $selectedDay",
                message = "Tap '+ Add Class' above to schedule a lecture."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dayEntries, key = { it.id }) { entry ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(85.dp)) {
                                Text(
                                    text = entry.startTime,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = entry.endTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            VerticalDivider(modifier = Modifier.height(40.dp))
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
                                Text(
                                    text = "${entry.classGroup} (Sec ${entry.section})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { onDeleteEntry(entry.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCritical)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrincipalAnalyticsTab(
    allStudents: List<StudentEntity>,
    allAttendance: List<AttendanceRecordEntity>,
    allSubjects: List<SubjectEntity>,
    allNotices: List<NoticeEntity>,
    allEvents: List<EventEntity>,
    onBroadcastNotice: () -> Unit,
    onAddEvent: () -> Unit
) {
    val scrollState = rememberScrollState()

    val totalLectures = allAttendance.size
    val totalPresents = allAttendance.count { it.isPresent }
    val avgAttendance = if (totalLectures > 0) (totalPresents.toFloat() / totalLectures.toFloat()) * 100f else 88.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Institutional Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Real-time institutional attendance & performance statistics",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Overall Attendance Meter
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Overall College Attendance Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", avgAttendance)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (avgAttendance >= 75f) StatusSafe else StatusWarning
                    )
                    AttendanceStatusChip(
                        status = if (avgAttendance >= 75f) AttendanceStatus.SAFE else AttendanceStatus.LOW,
                        percentage = avgAttendance
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (avgAttendance / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (avgAttendance >= 75f) StatusSafe else StatusWarning,
                    trackColor = Slate200
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on $totalLectures student-lecture records logged by faculty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Breakdown
        Text(
            text = "Subject-Wise Attendance Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val subjectGroups = allAttendance.groupBy { it.subjectName }
        if (subjectGroups.isEmpty()) {
            EmptyState(title = "No attendance data logged yet")
        } else {
            subjectGroups.forEach { (subName, records) ->
                val subTotal = records.size
                val subPresent = records.count { it.isPresent }
                val pct = if (subTotal > 0) (subPresent.toFloat() / subTotal.toFloat()) * 100f else 0f
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
                                text = subName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", pct)}% ($subPresent / $subTotal)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 75f) StatusSafe else StatusWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (pct >= 75f) StatusSafe else StatusWarning,
                            trackColor = Slate200
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Administrative Broadcast & Event Hub
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PrimaryIndigo.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📢 Institutional Announcements & Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Broadcast college-wide notices or publish new symposium events instantly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBroadcastNotice,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Broadcast Notice")
                    }
                    Button(
                        onClick = onAddEvent,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Event")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DeveloperCreditBar()
    }
}

// Dialogs for Principal actions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var collegeId by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Applications") }
    var course by remember { mutableStateOf("BCA") }
    var year by remember { mutableStateOf("2nd Year") }
    var classGroup by remember { mutableStateOf("BCA 2nd Year") }
    var section by remember { mutableStateOf("A") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Student") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (email.isBlank()) email = "${it.lowercase().replace(" ", ".")}@mycampus.edu"
                    },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("College Email *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = collegeId,
                        onValueChange = { collegeId = it },
                        label = { Text("College ID") },
                        placeholder = { Text("STU011") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll No *") },
                        placeholder = { Text("11") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = classGroup,
                    onValueChange = { classGroup = it },
                    label = { Text("Class Group") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        val colId = if (collegeId.isNotBlank()) collegeId else "STU0" + (10..99).random()
                        val roll = if (rollNumber.isNotBlank()) rollNumber else "12"
                        onConfirm(name, email, colId, roll, department, course, year, classGroup, section)
                    }
                }
            ) {
                Text("Add Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeacherDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String?, String?, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("TCH005") }
    var department by remember { mutableStateOf("Computer Applications") }
    var designation by remember { mutableStateOf("Assistant Professor") }
    var qualification by remember { mutableStateOf("M.Tech in CS") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var classGroup by remember { mutableStateOf("BCA 2nd Year") }
    var section by remember { mutableStateOf("A") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Faculty Member") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (email.isBlank()) email = "${it.lowercase().replace(" ", ".")}@mycampus.edu"
                    },
                    label = { Text("Faculty Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Emp ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Dept") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Assign Subject & Class",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = classGroup,
                    onValueChange = { classGroup = it },
                    label = { Text("Assigned Class") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val sub = selectedSubject ?: subjects.firstOrNull()
                        onConfirm(name, email, employeeId, department, designation, qualification, sub?.id, sub?.name, classGroup, section)
                    }
                }
            ) {
                Text("Add Faculty")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTimetableDialog(
    subjects: List<SubjectEntity>,
    teachers: List<TeacherEntity>,
    teacherUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TimetableEntity) -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var dayOfWeek by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("10:00 AM") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()) }
    var selectedTeacher by remember { mutableStateOf(teachers.firstOrNull()) }
    var classGroup by remember { mutableStateOf("BCA 2nd Year") }
    var section by remember { mutableStateOf("A") }
    var roomNumber by remember { mutableStateOf("Room 204") }

    val userMap = remember(teacherUsers) { teacherUsers.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Timetable Class") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text("Select Day:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    days.take(4).forEach { d ->
                        FilterChip(
                            selected = dayOfWeek == d,
                            onClick = { dayOfWeek = d },
                            label = { Text(d.take(3)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room / Lab Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = classGroup,
                    onValueChange = { classGroup = it },
                    label = { Text("Class Batch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = selectedSubject ?: subjects.firstOrNull() ?: SubjectEntity("sub_dbms", "BCA301", "DBMS", "CA", 3)
                    val tch = selectedTeacher ?: teachers.firstOrNull() ?: TeacherEntity("tch_rahul", "user_tch_rahul", "TCH001", "CA")
                    val tchName = userMap[tch.userId]?.fullName ?: "Prof. Rahul Sharma"

                    val entry = TimetableEntity(
                        id = UUID.randomUUID().toString(),
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        subjectId = sub.id,
                        subjectName = sub.name,
                        teacherId = tch.id,
                        teacherName = tchName,
                        classGroup = classGroup,
                        section = section,
                        roomNumber = roomNumber
                    )
                    onConfirm(entry)
                }
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddNoticeDialog(
    authorId: String,
    authorName: String,
    authorRole: String,
    onDismiss: () -> Unit,
    onConfirm: (NoticeEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var targetRole by remember { mutableStateOf("All") }
    var targetClass by remember { mutableStateOf("All") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast Campus Notice") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notice Content *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Priority:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Urgent", "High", "Medium", "Low").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val notice = NoticeEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            priority = priority,
                            targetRole = targetRole,
                            targetClass = targetClass,
                            targetSection = "All",
                            authorId = authorId,
                            authorName = authorName,
                            authorRole = authorRole,
                            date = "2026-08-30"
                        )
                        onConfirm(notice)
                    }
                }
            ) {
                Text("Broadcast")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (EventEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-09-20") }
    var time by remember { mutableStateOf("10:00 AM - 04:00 PM") }
    var location by remember { mutableStateOf("Main Auditorium") }
    var organizer by remember { mutableStateOf("College Cultural & Tech Board") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Campus Event") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Venue") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val event = EventEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            date = date,
                            time = time,
                            location = location,
                            organizer = organizer
                        )
                        onConfirm(event)
                    }
                }
            ) {
                Text("Publish Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
