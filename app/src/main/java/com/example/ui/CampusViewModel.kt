package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.StudyMateAiService
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AttendancePrediction(
    val overallPercentage: Float,
    val totalLectures: Int,
    val presentLectures: Int,
    val absentLectures: Int,
    val status: AttendanceStatus,
    val neededLecturesFor75: Int,
    val canBunkLectures: Int,
    val message: String
)

enum class AttendanceStatus {
    SAFE, // >= 75%
    LOW,  // 65% - 74%
    CRITICAL // < 65%
}

data class SubjectAttendanceStat(
    val subjectId: String,
    val subjectName: String,
    val total: Int,
    val present: Int,
    val percentage: Float,
    val status: AttendanceStatus
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class CampusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.campusDao()
    val repository = CampusRepository(dao, viewModelScope)
    val authManager = com.example.data.auth.FirebaseAuthManager(application, dao, viewModelScope)
    private val aiService = StudyMateAiService()

    // Auth State & Current Session
    val authState: StateFlow<com.example.data.auth.AuthState> = authManager.authState
    val currentUserSession: StateFlow<com.example.data.auth.AuthUserSession?> = authManager.currentUserSession

    // Current Auth User
    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    // All Users
    val allStudentUsers: StateFlow<List<UserEntity>> = repository.getAllStudentUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeacherUsers: StateFlow<List<UserEntity>> = repository.getAllTeacherUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<ClassGroupEntity>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeacherAssignments: StateFlow<List<TeacherAssignmentEntity>> = repository.getAllTeacherAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimetable: StateFlow<List<TimetableEntity>> = repository.getAllTimetable()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceRecords: StateFlow<List<AttendanceRecordEntity>> = repository.getAllAttendanceRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyMaterials: StateFlow<List<StudyMaterialEntity>> = repository.getAllStudyMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPapers: StateFlow<List<PaperEntity>> = repository.getAllPapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<AssignmentEntity>> = repository.getAllAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.getAllNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<EventEntity>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOpportunities: StateFlow<List<OpportunityEntity>> = repository.getAllOpportunities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<PostEntity>> = repository.getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyGroups: StateFlow<List<StudyGroupEntity>> = repository.getAllStudyGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications for current user
    val currentNotifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = currentNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current Student Entity (if role == student)
    val currentStudentEntity: StateFlow<StudentEntity?> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "student") {
            repository.getCurrentStudentDetails(user.id)
        } else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Teacher Entity (if role == teacher)
    val currentTeacherEntity: StateFlow<TeacherEntity?> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "teacher") {
            repository.getCurrentTeacherDetails(user.id)
        } else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Teacher's assigned subjects & classes
    val teacherAssignments: StateFlow<List<TeacherAssignmentEntity>> = currentTeacherEntity.flatMapLatest { teacher ->
        if (teacher != null) repository.getTeacherAssignments(teacher.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student's dynamic attendance & prediction
    val studentAttendanceRecords: StateFlow<List<AttendanceRecordEntity>> = currentStudentEntity.flatMapLatest { student ->
        if (student != null) repository.getAttendanceForStudent(student.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentAttendancePrediction: StateFlow<AttendancePrediction> = studentAttendanceRecords.map { records ->
        val total = records.size
        val present = records.count { it.isPresent }
        val absent = total - present
        val percentage = if (total > 0) (present.toFloat() / total.toFloat()) * 100f else 100f

        val status = when {
            percentage >= 75f -> AttendanceStatus.SAFE
            percentage >= 65f -> AttendanceStatus.LOW
            else -> AttendanceStatus.CRITICAL
        }

        // Attendance formula: (present + X) / (total + X) >= 0.75
        // present + X >= 0.75 * total + 0.75 * X
        // 0.25 * X >= 0.75 * total - present
        // X >= (0.75 * total - present) / 0.25 = 3 * total - 4 * present
        val needed = if (percentage < 75f) {
            val calc = (3 * total - 4 * present)
            if (calc > 0) calc else 1
        } else 0

        // How many lectures can be skipped while maintaining >= 75%
        // present / (total + Y) >= 0.75 => total + Y <= present / 0.75 => Y <= (present / 0.75) - total
        val canBunk = if (percentage >= 75f && total > 0) {
            val maxTotal = (present / 0.75f).toInt()
            val bunk = maxTotal - total
            if (bunk > 0) bunk else 0
        } else 0

        val msg = when {
            status == AttendanceStatus.SAFE && canBunk > 0 ->
                "Great job! You are in the Safe zone. You can miss up to $canBunk upcoming lectures and stay above 75%."
            status == AttendanceStatus.SAFE ->
                "You are maintaining a healthy 75%+ attendance. Keep it up!"
            status == AttendanceStatus.LOW ->
                "Warning: You need to attend $needed consecutive lectures to reach 75% attendance."
            else ->
                "Critical: Your attendance is below 65%. Attend $needed consecutive lectures immediately to avoid exam debarment."
        }

        AttendancePrediction(
            overallPercentage = percentage,
            totalLectures = total,
            presentLectures = present,
            absentLectures = absent,
            status = status,
            neededLecturesFor75 = needed,
            canBunkLectures = canBunk,
            message = msg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AttendancePrediction(100f, 0, 0, 0, AttendanceStatus.SAFE, 0, 0, "No attendance records yet.")
    )

    // Subject-wise attendance calculation for student
    val subjectAttendanceStats: StateFlow<List<SubjectAttendanceStat>> = combine(
        studentAttendanceRecords,
        allSubjects
    ) { records, subjects ->
        val map = records.groupBy { it.subjectId }
        subjects.mapNotNull { sub ->
            val subRecords = map[sub.id]
            if (subRecords != null && subRecords.isNotEmpty()) {
                val total = subRecords.size
                val present = subRecords.count { it.isPresent }
                val pct = (present.toFloat() / total.toFloat()) * 100f
                val status = when {
                    pct >= 75f -> AttendanceStatus.SAFE
                    pct >= 65f -> AttendanceStatus.LOW
                    else -> AttendanceStatus.CRITICAL
                }
                SubjectAttendanceStat(sub.id, sub.name, total, present, pct, status)
            } else {
                SubjectAttendanceStat(sub.id, sub.name, 0, 0, 100f, AttendanceStatus.SAFE)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Universal student search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Pair<UserEntity, StudentEntity>>> = _searchQuery
        .debounce(250)
        .flatMapLatest { query ->
            repository.searchStudents(query)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // StudyMate AI State
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                text = "Hello! I am StudyMate AI, your dedicated academic companion at MyCampus. How can I help you today?\n\nTry asking:\n• 'Explain DBMS Normalization with examples'\n• 'Generate 5 practice MCQs for Java OOP'\n• 'What are the top exam questions for Data Structures?'",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    fun askStudyMate(prompt: String, mode: String = "general") {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(text = prompt, isUser = true)
        _aiMessages.value = _aiMessages.value + userMsg
        _aiLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val response = aiService.queryStudyMate(prompt, mode)
            val assistantMsg = AiChatMessage(text = response, isUser = false)
            _aiMessages.value = _aiMessages.value + assistantMsg
            _aiLoading.value = false
        }
    }

    // --- Auth Actions ---
    fun loginStudent(collegeId: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authManager.loginStudent(collegeId, pass)
            if (res.isSuccess) {
                val session = res.getOrNull()
                if (session != null) {
                    repository.switchUserDirect(session.uid)
                }
                onResult(true, "Welcome back, ${session?.fullName}!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Student sign-in failed.")
            }
        }
    }

    fun loginTeacher(identifier: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authManager.loginTeacher(identifier, pass)
            if (res.isSuccess) {
                val session = res.getOrNull()
                if (session != null) {
                    repository.switchUserDirect(session.uid)
                }
                onResult(true, "Welcome, ${session?.fullName}!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Teacher sign-in failed.")
            }
        }
    }

    fun loginPrincipal(identifier: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authManager.loginPrincipal(identifier, pass)
            if (res.isSuccess) {
                val session = res.getOrNull()
                if (session != null) {
                    repository.switchUserDirect(session.uid)
                }
                onResult(true, "Welcome, ${session?.fullName}!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Principal sign-in failed.")
            }
        }
    }

    fun signInWithGoogle(
        context: android.content.Context,
        expectedRole: com.example.data.auth.CampusRole,
        serverClientId: String? = null,
        onResult: (Boolean, String, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authManager.signInWithGoogle(context, expectedRole, serverClientId)
            if (res.isSuccess) {
                val session = res.getOrNull()!!
                repository.switchUserDirect(session.uid)
                onResult(true, session.role, "Welcome, ${session.fullName}!")
            } else {
                onResult(false, expectedRole.value, res.exceptionOrNull()?.message ?: "Google Sign-In failed.")
            }
        }
    }

    fun signInStaffWithGoogle(
        context: android.content.Context,
        expectedRole: com.example.data.auth.CampusRole,
        serverClientId: String? = null,
        onResult: (Boolean, String, String) -> Unit
    ) {
        signInWithGoogle(context, expectedRole, serverClientId, onResult)
    }

    fun requestStudentPasswordReset(collegeId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authManager.requestStudentPasswordReset(collegeId)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Password reset instructions dispatched.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to request password reset.")
            }
        }
    }

    fun requestStaffPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = authManager.sendStaffPasswordReset(email)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Password reset instructions dispatched.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to send password reset email.")
            }
        }
    }

    // Multi-Department Faculty Management
    private val _selectedFacultyDepartment = MutableStateFlow<String>("Computer Science")
    val selectedFacultyDepartment: StateFlow<String> = _selectedFacultyDepartment.asStateFlow()

    fun setSelectedFacultyDepartment(department: String) {
        _selectedFacultyDepartment.value = department
    }

    // Dynamic Institutional Departments
    val standardDepartments = listOf(
        "Computer Science",
        "Information Technology",
        "Artificial Intelligence & Data Science",
        "Electronics & Telecommunication",
        "Electrical Engineering",
        "Mechanical Engineering",
        "Civil Engineering"
    )

    private val _activeDepartments = MutableStateFlow<List<String>>(standardDepartments)
    val activeDepartments: StateFlow<List<String>> = _activeDepartments.asStateFlow()

    fun addCustomDepartment(name: String) {
        if (name.isNotBlank() && !_activeDepartments.value.contains(name.trim())) {
            _activeDepartments.value = _activeDepartments.value + name.trim()
        }
    }

    fun toggleDepartmentActive(name: String) {
        val current = _activeDepartments.value
        if (current.contains(name)) {
            if (current.size > 1) {
                _activeDepartments.value = current.filter { it != name }
            }
        } else {
            _activeDepartments.value = current + name
        }
    }

    // --- Registration Actions ---
    fun registerStudent(
        fullName: String,
        collegeId: String,
        email: String,
        mobileNumber: String,
        password: String,
        confirmPassword: String,
        department: String,
        academicYear: String,
        yearSemester: String,
        division: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authManager.registerStudent(
                fullName, collegeId, email, mobileNumber, password, confirmPassword,
                department, academicYear, yearSemester, division
            )
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Account created successfully.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to create student account.")
            }
        }
    }

    fun registerTeacher(
        fullName: String,
        officialEmail: String,
        facultyId: String,
        mobileNumber: String,
        password: String,
        confirmPassword: String,
        departments: List<String>,
        subjects: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authManager.registerTeacher(
                fullName, officialEmail, facultyId, mobileNumber, password, confirmPassword,
                departments, subjects
            )
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Faculty account created successfully.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to create faculty account.")
            }
        }
    }

    fun registerPrincipal(
        fullName: String,
        officialEmail: String,
        principalId: String,
        mobileNumber: String,
        password: String,
        confirmPassword: String,
        securityPasscode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = authManager.registerPrincipal(
                fullName, officialEmail, principalId, mobileNumber, password, confirmPassword, securityPasscode
            )
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Principal account registered successfully.")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to register Principal account.")
            }
        }
    }

    fun logout(context: android.content.Context? = null) {
        repository.logout()
        viewModelScope.launch {
            authManager.signOut(context)
        }
    }

    // --- Principal Operations ---
    fun addStudent(
        fullName: String,
        email: String,
        collegeId: String,
        rollNumber: String,
        department: String,
        course: String,
        year: String,
        classGroup: String,
        section: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addStudent(fullName, email, collegeId, rollNumber, department, course, year, classGroup, section)
            onDone()
        }
    }

    fun updateStudent(user: UserEntity, student: StudentEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudent(user, student)
            onDone()
        }
    }

    fun toggleStudentDeactivation(student: StudentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleStudentDeactivation(student)
        }
    }

    fun deleteStudentAccount(userId: String, studentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudentAccount(userId, studentId)
        }
    }

    fun addTeacher(
        fullName: String,
        email: String,
        employeeId: String,
        department: String,
        designation: String,
        qualification: String,
        assignedSubjectId: String?,
        assignedSubjectName: String?,
        assignedClass: String,
        assignedSection: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTeacher(
                fullName, email, employeeId, department, designation, qualification,
                assignedSubjectId, assignedSubjectName, assignedClass, assignedSection
            )
            onDone()
        }
    }

    fun updateTeacher(user: UserEntity, teacher: TeacherEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTeacher(user, teacher)
            onDone()
        }
    }

    fun toggleTeacherDeactivation(teacher: TeacherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTeacherDeactivation(teacher)
        }
    }

    fun addTimetableEntry(entry: TimetableEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTimetableEntry(entry)
            onDone()
        }
    }

    fun updateTimetableEntry(entry: TimetableEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTimetableEntry(entry)
            onDone()
        }
    }

    fun deleteTimetableEntry(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTimetableEntry(id)
        }
    }

    fun createNotice(notice: NoticeEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createNotice(notice)
            onDone()
        }
    }

    fun deleteNotice(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotice(id)
        }
    }

    fun createEvent(event: EventEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createEvent(event)
            onDone()
        }
    }

    fun registerForEvent(eventId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.registerForEvent(eventId, user.id)
        }
    }

    // --- Teacher Operations ---
    fun saveAttendance(
        records: List<AttendanceRecordEntity>,
        subjectName: String,
        date: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveAttendance(records, subjectName, date)
            onDone()
        }
    }

    fun uploadStudyMaterial(material: StudyMaterialEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.uploadStudyMaterial(material)
            onDone()
        }
    }

    fun deleteStudyMaterial(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyMaterial(id)
        }
    }

    fun uploadPaper(paper: PaperEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.uploadPaper(paper)
            onDone()
        }
    }

    fun deletePaper(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePaper(id)
        }
    }

    fun createAssignment(assignment: AssignmentEntity, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createAssignment(assignment)
            onDone()
        }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAssignment(id)
        }
    }

    fun submitAssignment(
        assignmentId: String,
        studentId: String,
        studentName: String,
        studentRoll: String,
        text: String,
        fileName: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sub = AssignmentSubmissionEntity(
                id = UUID.randomUUID().toString(),
                assignmentId = assignmentId,
                studentId = studentId,
                studentName = studentName,
                studentRoll = studentRoll,
                submissionText = text,
                attachedFileName = fileName
            )
            repository.submitAssignment(sub)
            onDone()
        }
    }

    // --- Student Profile Editing ---
    fun updateStudentPersonalProfile(bio: String, skills: String, interests: String, onDone: () -> Unit) {
        val student = currentStudentEntity.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudentPersonalProfile(student, bio, skills, interests)
            onDone()
        }
    }

    // --- Notifications ---
    fun markNotificationRead(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllNotificationsRead(user.id)
        }
    }

    // --- CampusConnect ---
    fun createPost(content: String, category: String, onDone: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val post = PostEntity(
                id = UUID.randomUUID().toString(),
                authorId = user.id,
                authorName = user.fullName,
                authorUsername = user.username,
                authorRole = user.role,
                content = content,
                category = category
            )
            repository.createPost(post)
            onDone()
        }
    }

    fun toggleLikePost(postId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleLikePost(postId, user.id)
        }
    }

    fun addComment(postId: String, content: String, onDone: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val comment = CommentEntity(
                id = UUID.randomUUID().toString(),
                postId = postId,
                authorId = user.id,
                authorName = user.fullName,
                authorRole = user.role,
                content = content
            )
            repository.addComment(comment)
            onDone()
        }
    }

    fun createStudyGroup(name: String, subject: String, desc: String, onDone: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val group = StudyGroupEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                subject = subject,
                description = desc,
                memberCount = 1,
                memberUserIds = user.id,
                creatorId = user.id,
                creatorName = user.fullName
            )
            repository.createStudyGroup(group)
            onDone()
        }
    }

    fun toggleJoinStudyGroup(groupId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleJoinStudyGroup(groupId, user.id)
        }
    }

    fun toggleFollowConnection(targetUserId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFollowConnection(user.id, targetUserId)
        }
    }
}
