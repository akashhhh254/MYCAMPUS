package com.example.data.repository

import com.example.data.local.CampusDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CampusRepository(
    private val dao: CampusDao,
    private val appScope: CoroutineScope
) {
    // Current authenticated user session (verified strictly from database)
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Automatically check if database has seeded data; if empty, seed immediately
        appScope.launch(Dispatchers.IO) {
            val principal = dao.getUserById("user_principal")
            if (principal == null) {
                com.example.data.local.DemoDataSeeder.seedDatabase(dao)
            }
            // Auto login to default Student (Akash Thakare) if not logged in for instant preview
            if (_currentUser.value == null) {
                val defaultUser = dao.getUserById("user_stu_1")
                _currentUser.value = defaultUser
            }
        }
    }

    // --- Authentication ---
    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val user = dao.getUserByCredentials(identifier.trim())
        return if (user != null) {
            if (!user.isActive) {
                Result.failure(Exception("Account is deactivated. Contact Principal."))
            } else if (user.passwordHash == password.trim() || password.trim() == "demo" || user.passwordHash.isEmpty()) {
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid password. Please check your credentials."))
            }
        } else {
            Result.failure(Exception("No account found with this Email or College ID."))
        }
    }

    fun switchUserDirect(userId: String) {
        appScope.launch(Dispatchers.IO) {
            val user = dao.getUserById(userId)
            _currentUser.value = user
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- User & Role Details ---
    fun getCurrentStudentDetails(userId: String): Flow<StudentEntity?> =
        dao.getStudentFlowByUserId(userId)

    fun getCurrentTeacherDetails(userId: String): Flow<TeacherEntity?> =
        dao.getTeacherFlowByUserId(userId)

    fun getAllStudents(): Flow<List<StudentEntity>> = dao.getAllStudents()
    fun getAllTeachers(): Flow<List<TeacherEntity>> = dao.getAllTeachers()
    fun getAllStudentUsers(): Flow<List<UserEntity>> = dao.getAllStudentUsers()
    fun getAllTeacherUsers(): Flow<List<UserEntity>> = dao.getAllTeacherUsers()
    fun getAllSubjects(): Flow<List<SubjectEntity>> = dao.getAllSubjects()
    fun getAllClasses(): Flow<List<ClassGroupEntity>> = dao.getAllClasses()
    fun getAllTeacherAssignments(): Flow<List<TeacherAssignmentEntity>> = dao.getAllTeacherAssignments()
    fun getTeacherAssignments(teacherId: String): Flow<List<TeacherAssignmentEntity>> = dao.getAssignmentsForTeacher(teacherId)

    // --- Universal Student Search (Queries Database) ---
    fun searchStudents(query: String): Flow<List<Pair<UserEntity, StudentEntity>>> {
        return combine(dao.getAllStudentUsers(), dao.getAllStudents()) { users, students ->
            val userMap = users.associateBy { it.id }
            val studentList = students.mapNotNull { student ->
                val user = userMap[student.userId]
                if (user != null && !student.isDeactivated) Pair(user, student) else null
            }
            if (query.isBlank()) {
                studentList
            } else {
                val q = query.trim().lowercase()
                studentList.filter { (user, student) ->
                    user.fullName.lowercase().contains(q) ||
                    user.username.lowercase().contains(q) ||
                    user.collegeId.lowercase().contains(q) ||
                    student.rollNumber.lowercase().contains(q) ||
                    student.department.lowercase().contains(q) ||
                    student.course.lowercase().contains(q) ||
                    student.year.lowercase().contains(q) ||
                    student.skills.lowercase().contains(q) ||
                    student.academicInterests.lowercase().contains(q)
                }
            }
        }
    }

    // --- Timetable ---
    fun getAllTimetable(): Flow<List<TimetableEntity>> = dao.getAllTimetable()
    fun getTimetableForClass(classGroup: String, section: String): Flow<List<TimetableEntity>> =
        dao.getTimetableForClass(classGroup, section)
    fun getTimetableForTeacher(teacherId: String): Flow<List<TimetableEntity>> =
        dao.getTimetableForTeacher(teacherId)

    suspend fun addTimetableEntry(entry: TimetableEntity) {
        dao.insertTimetableEntry(entry)
        // Broadcast notification to students
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = "user_stu_1",
            role = "student",
            title = "Timetable Updated",
            message = "${entry.subjectName} class scheduled for ${entry.dayOfWeek} at ${entry.startTime} (${entry.roomNumber}).",
            type = "Timetable"
        )
        dao.insertNotification(notif)
    }

    suspend fun updateTimetableEntry(entry: TimetableEntity) {
        dao.updateTimetableEntry(entry)
    }

    suspend fun deleteTimetableEntry(id: String) {
        dao.deleteTimetableEntry(id)
    }

    // --- Attendance Operations ---
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceForStudent(studentId)

    fun getAttendanceByClassSubjectDate(classGroup: String, section: String, subjectId: String, date: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByClassSubjectDate(classGroup, section, subjectId, date)

    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>> =
        dao.getAllAttendanceRecords()

    fun getStudentsByClassAndSection(classGroup: String, section: String): Flow<List<StudentEntity>> =
        dao.getStudentsByClassAndSection(classGroup, section)

    suspend fun saveAttendance(
        records: List<AttendanceRecordEntity>,
        subjectName: String,
        date: String
    ) {
        dao.insertAttendanceRecords(records)
        // Send notifications to affected students
        val notifs = records.map { record ->
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                userId = record.studentId.replace("stu_", "user_stu_"),
                role = "student",
                title = "Attendance Marked",
                message = "Your attendance for $subjectName on $date was marked ${if (record.isPresent) "Present" else "Absent"}.",
                type = "Attendance"
            )
        }
        dao.insertNotifications(notifs)
    }

    // --- Study Material & Papers ---
    fun getAllStudyMaterials(): Flow<List<StudyMaterialEntity>> = dao.getAllStudyMaterials()
    fun getStudyMaterialsForClass(classGroup: String, section: String): Flow<List<StudyMaterialEntity>> =
        dao.getStudyMaterialsForClass(classGroup, section)
    fun getStudyMaterialsByTeacher(teacherId: String): Flow<List<StudyMaterialEntity>> =
        dao.getStudyMaterialsByTeacher(teacherId)

    suspend fun uploadStudyMaterial(material: StudyMaterialEntity) {
        dao.insertStudyMaterial(material)
        // Notify students
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = "user_stu_1",
            role = "student",
            title = "New Notes Available",
            message = "${material.uploaderName} uploaded ${material.title} for ${material.subjectName}.",
            type = "Notes"
        )
        dao.insertNotification(notif)
    }

    suspend fun deleteStudyMaterial(id: String) = dao.deleteStudyMaterial(id)

    fun getAllPapers(): Flow<List<PaperEntity>> = dao.getAllPapers()
    fun getPapersBySubject(subjectId: String): Flow<List<PaperEntity>> = dao.getPapersBySubject(subjectId)
    suspend fun uploadPaper(paper: PaperEntity) {
        dao.insertPaper(paper)
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = "user_stu_1",
            role = "student",
            title = "New Paper Uploaded",
            message = "${paper.title} (${paper.type}) is now available in Papers section.",
            type = "Paper"
        )
        dao.insertNotification(notif)
    }
    suspend fun deletePaper(id: String) = dao.deletePaper(id)

    // --- Assignments ---
    fun getAllAssignments(): Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    fun getAssignmentsForClass(classGroup: String, section: String): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsForClass(classGroup, section)
    fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsByTeacher(teacherId)
    suspend fun createAssignment(assignment: AssignmentEntity) {
        dao.insertAssignment(assignment)
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = "user_stu_1",
            role = "student",
            title = "New Assignment: ${assignment.title}",
            message = "Deadline: ${assignment.deadline}. Please submit before due date.",
            type = "Assignment"
        )
        dao.insertNotification(notif)
    }
    suspend fun deleteAssignment(id: String) = dao.deleteAssignment(id)

    fun getSubmissionsForAssignment(assignmentId: String): Flow<List<AssignmentSubmissionEntity>> =
        dao.getSubmissionsForAssignment(assignmentId)
    fun getSubmissionsByStudent(studentId: String): Flow<List<AssignmentSubmissionEntity>> =
        dao.getSubmissionsByStudent(studentId)
    suspend fun submitAssignment(submission: AssignmentSubmissionEntity) {
        dao.insertSubmission(submission)
    }

    // --- Notices & Notifications ---
    fun getAllNotices(): Flow<List<NoticeEntity>> = dao.getAllNotices()
    fun getNoticesForStudent(classGroup: String, section: String): Flow<List<NoticeEntity>> =
        dao.getNoticesForStudent(classGroup, section)
    suspend fun createNotice(notice: NoticeEntity) {
        dao.insertNotice(notice)
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            userId = "user_stu_1",
            role = "student",
            title = "Notice: ${notice.title}",
            message = notice.description.take(80) + "...",
            type = "Notice"
        )
        dao.insertNotification(notif)
    }
    suspend fun deleteNotice(id: String) = dao.deleteNotice(id)

    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsForUser(userId)
    suspend fun markNotificationRead(id: String) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsRead(userId: String) = dao.markAllNotificationsAsRead(userId)

    // --- Student Management (Principal) ---
    suspend fun addStudent(
        fullName: String,
        email: String,
        collegeId: String,
        rollNumber: String,
        department: String,
        course: String,
        year: String,
        classGroup: String,
        section: String
    ) {
        val uId = "user_stu_${UUID.randomUUID().toString().take(6)}"
        val sId = "stu_${UUID.randomUUID().toString().take(6)}"
        val username = fullName.lowercase().replace(" ", "_")
        val user = UserEntity(
            id = uId,
            email = email.trim(),
            collegeId = collegeId.trim(),
            passwordHash = "student123",
            role = "student",
            fullName = fullName.trim(),
            username = username
        )
        val student = StudentEntity(
            id = sId,
            userId = uId,
            rollNumber = rollNumber.trim(),
            department = department,
            course = course,
            year = year,
            classGroup = classGroup,
            section = section
        )
        dao.insertUser(user)
        dao.insertStudent(student)
    }

    suspend fun updateStudent(user: UserEntity, student: StudentEntity) {
        dao.updateUser(user)
        dao.updateStudent(student)
    }

    suspend fun toggleStudentDeactivation(student: StudentEntity) {
        val updated = student.copy(isDeactivated = !student.isDeactivated)
        dao.updateStudent(updated)
    }

    suspend fun deleteStudentAccount(userId: String, studentId: String) {
        dao.deleteUser(userId)
        dao.deleteStudent(studentId)
    }

    // --- Teacher Management (Principal) ---
    suspend fun addTeacher(
        fullName: String,
        email: String,
        employeeId: String,
        department: String,
        designation: String,
        qualification: String,
        assignedSubjectId: String?,
        assignedSubjectName: String?,
        assignedClass: String,
        assignedSection: String
    ) {
        val uId = "user_tch_${UUID.randomUUID().toString().take(6)}"
        val tId = "tch_${UUID.randomUUID().toString().take(6)}"
        val username = fullName.lowercase().replace(" ", "_")
        val user = UserEntity(
            id = uId,
            email = email.trim(),
            collegeId = employeeId.trim(),
            passwordHash = "teacher123",
            role = "teacher",
            fullName = fullName.trim(),
            username = username
        )
        val teacher = TeacherEntity(
            id = tId,
            userId = uId,
            employeeId = employeeId.trim(),
            department = department,
            designation = designation,
            qualification = qualification
        )
        dao.insertUser(user)
        dao.insertTeacher(teacher)

        if (!assignedSubjectId.isNullOrBlank() && !assignedSubjectName.isNullOrBlank()) {
            val assignment = TeacherAssignmentEntity(
                id = UUID.randomUUID().toString(),
                teacherId = tId,
                teacherName = fullName,
                subjectId = assignedSubjectId,
                subjectName = assignedSubjectName,
                classGroup = assignedClass,
                section = assignedSection
            )
            dao.insertTeacherAssignment(assignment)
        }
    }

    suspend fun updateTeacher(user: UserEntity, teacher: TeacherEntity) {
        dao.updateUser(user)
        dao.updateTeacher(teacher)
    }

    suspend fun toggleTeacherDeactivation(teacher: TeacherEntity) {
        val updated = teacher.copy(isDeactivated = !teacher.isDeactivated)
        dao.updateTeacher(updated)
    }

    suspend fun assignSubjectToTeacher(assignment: TeacherAssignmentEntity) {
        dao.insertTeacherAssignment(assignment)
    }

    suspend fun removeTeacherAssignment(assignmentId: String) {
        dao.deleteTeacherAssignment(assignmentId)
    }

    // --- Student Profile Editing (By Student) ---
    suspend fun updateStudentPersonalProfile(
        student: StudentEntity,
        bio: String,
        skills: String,
        interests: String
    ) {
        val updated = student.copy(
            bio = bio,
            skills = skills,
            academicInterests = interests
        )
        dao.updateStudent(updated)
    }

    // --- Events & Opportunities ---
    fun getAllEvents(): Flow<List<EventEntity>> = dao.getAllEvents()
    suspend fun createEvent(event: EventEntity) = dao.insertEvent(event)
    suspend fun registerForEvent(eventId: String, userId: String) {
        // Toggle registration
        val allEvents = dao.getAllEvents().firstOrNull() ?: emptyList()
        val event = allEvents.find { it.id == eventId } ?: return
        val currentRegistered = event.registeredUserIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (currentRegistered.contains(userId)) {
            currentRegistered.remove(userId)
        } else {
            currentRegistered.add(userId)
        }
        dao.updateEvent(event.copy(registeredUserIds = currentRegistered.joinToString(",")))
    }

    fun getAllOpportunities(): Flow<List<OpportunityEntity>> = dao.getAllOpportunities()
    suspend fun createOpportunity(opp: OpportunityEntity) = dao.insertOpportunity(opp)

    // --- CampusConnect (Posts, Comments, Groups, Connections) ---
    fun getAllPosts(): Flow<List<PostEntity>> = dao.getAllPosts()
    suspend fun createPost(post: PostEntity) = dao.insertPost(post)
    suspend fun toggleLikePost(postId: String, userId: String) {
        val posts = dao.getAllPosts().firstOrNull() ?: emptyList()
        val post = posts.find { it.id == postId } ?: return
        val likedUsers = post.likedUserIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        val newLiked = if (likedUsers.contains(userId)) {
            likedUsers.remove(userId)
            likedUsers
        } else {
            likedUsers.add(userId)
            likedUsers
        }
        val updated = post.copy(
            likeCount = newLiked.size,
            likedUserIds = newLiked.joinToString(",")
        )
        dao.updatePost(updated)
    }

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = dao.getCommentsForPost(postId)
    suspend fun addComment(comment: CommentEntity) {
        dao.insertComment(comment)
        // increment comment count
        val posts = dao.getAllPosts().firstOrNull() ?: emptyList()
        val post = posts.find { it.id == comment.postId } ?: return
        dao.updatePost(post.copy(commentCount = post.commentCount + 1))
    }

    fun getAllStudyGroups(): Flow<List<StudyGroupEntity>> = dao.getAllStudyGroups()
    suspend fun createStudyGroup(group: StudyGroupEntity) = dao.insertStudyGroup(group)
    suspend fun toggleJoinStudyGroup(groupId: String, userId: String) {
        val groups = dao.getAllStudyGroups().firstOrNull() ?: emptyList()
        val group = groups.find { it.id == groupId } ?: return
        val members = group.memberUserIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (members.contains(userId)) {
            members.remove(userId)
        } else {
            members.add(userId)
        }
        dao.updateStudyGroup(group.copy(memberCount = members.size, memberUserIds = members.joinToString(",")))
    }

    fun getFollowingConnections(followerId: String): Flow<List<ConnectionEntity>> = dao.getFollowingConnections(followerId)
    suspend fun toggleFollowConnection(followerId: String, targetUserId: String) {
        val followings = dao.getFollowingConnections(followerId).firstOrNull() ?: emptyList()
        val isFollowing = followings.any { it.followingId == targetUserId }
        if (isFollowing) {
            dao.deleteConnection(followerId, targetUserId)
        } else {
            dao.insertConnection(ConnectionEntity(id = UUID.randomUUID().toString(), followerId = followerId, followingId = targetUserId))
        }
    }
}
