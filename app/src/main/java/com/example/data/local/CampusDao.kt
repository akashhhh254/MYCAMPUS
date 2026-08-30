package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusDao {

    // --- Users & Auth ---
    @Query("SELECT * FROM users WHERE (email = :identifier OR collegeId = :identifier) LIMIT 1")
    suspend fun getUserByCredentials(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE collegeId = :collegeId LIMIT 1")
    suspend fun getUserByCollegeId(collegeId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'student'")
    fun getAllStudentUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'teacher'")
    fun getAllTeacherUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    // --- Students ---
    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    suspend fun getStudentByUserId(userId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    fun getStudentFlowByUserId(userId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classGroup = :classGroup AND section = :section AND isDeactivated = 0 ORDER BY rollNumber ASC")
    fun getStudentsByClassAndSection(classGroup: String, section: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE isDeactivated = 0")
    suspend fun getActiveStudentsDirect(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: String)

    // --- Teachers ---
    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    suspend fun getTeacherByUserId(userId: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    fun getTeacherFlowByUserId(userId: String): Flow<TeacherEntity?>

    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :teacherId")
    suspend fun deleteTeacher(teacherId: String)

    // --- Teacher Assignments ---
    @Query("SELECT * FROM teacher_subject_assignments WHERE teacherId = :teacherId")
    fun getAssignmentsForTeacher(teacherId: String): Flow<List<TeacherAssignmentEntity>>

    @Query("SELECT * FROM teacher_subject_assignments")
    fun getAllTeacherAssignments(): Flow<List<TeacherAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAssignment(assignment: TeacherAssignmentEntity)

    @Query("DELETE FROM teacher_subject_assignments WHERE id = :assignmentId")
    suspend fun deleteTeacherAssignment(assignmentId: String)

    @Query("DELETE FROM teacher_subject_assignments WHERE teacherId = :teacherId")
    suspend fun deleteAssignmentsForTeacher(teacherId: String)

    // --- Subjects & Classes ---
    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubject(subjectId: String)

    @Query("SELECT * FROM classes")
    fun getAllClasses(): Flow<List<ClassGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classGroup: ClassGroupEntity)

    // --- Timetable ---
    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetable(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE classGroup = :classGroup AND section = :section ORDER BY startTime ASC")
    fun getTimetableForClass(classGroup: String, section: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable WHERE teacherId = :teacherId ORDER BY startTime ASC")
    fun getTimetableForTeacher(teacherId: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEntry(entry: TimetableEntity)

    @Update
    suspend fun updateTimetableEntry(entry: TimetableEntity)

    @Query("DELETE FROM timetable WHERE id = :entryId")
    suspend fun deleteTimetableEntry(entryId: String)

    // --- Attendance ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecordEntity>)

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC, timestamp DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE classGroup = :classGroup AND section = :section AND subjectId = :subjectId AND date = :date")
    fun getAttendanceByClassSubjectDate(classGroup: String, section: String, subjectId: String, date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    // --- Study Materials ---
    @Query("SELECT * FROM study_materials ORDER BY createdAt DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE classGroup = :classGroup AND section = :section ORDER BY createdAt DESC")
    fun getStudyMaterialsForClass(classGroup: String, section: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE uploaderId = :teacherId ORDER BY createdAt DESC")
    fun getStudyMaterialsByTeacher(teacherId: String): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity)

    @Query("DELETE FROM study_materials WHERE id = :id")
    suspend fun deleteStudyMaterial(id: String)

    // --- Papers ---
    @Query("SELECT * FROM papers ORDER BY createdAt DESC")
    fun getAllPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getPapersBySubject(subjectId: String): Flow<List<PaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity)

    @Query("DELETE FROM papers WHERE id = :id")
    suspend fun deletePaper(id: String)

    // --- Assignments & Submissions ---
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE classGroup = :classGroup AND section = :section ORDER BY deadline ASC")
    fun getAssignmentsForClass(classGroup: String, section: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignment(id: String)

    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsForAssignment(assignmentId: String): Flow<List<AssignmentSubmissionEntity>>

    @Query("SELECT * FROM assignment_submissions WHERE studentId = :studentId")
    fun getSubmissionsByStudent(studentId: String): Flow<List<AssignmentSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmissionEntity)

    // --- Notices ---
    @Query("SELECT * FROM notices ORDER BY createdAt DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE targetClass = 'All' OR (targetClass = :classGroup AND (targetSection = 'All' OR targetSection = :section)) ORDER BY createdAt DESC")
    fun getNoticesForStudent(classGroup: String, section: String): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    // --- Events & Opportunities ---
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("SELECT * FROM opportunities ORDER BY createdAt DESC")
    fun getAllOpportunities(): Flow<List<OpportunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunity(opp: OpportunityEntity)

    // --- CampusConnect (Posts, Comments, Groups, Connections) ---
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM study_groups ORDER BY createdAt DESC")
    fun getAllStudyGroups(): Flow<List<StudyGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroup(group: StudyGroupEntity)

    @Update
    suspend fun updateStudyGroup(group: StudyGroupEntity)

    @Query("SELECT * FROM connections WHERE followerId = :followerId")
    fun getFollowingConnections(followerId: String): Flow<List<ConnectionEntity>>

    @Query("SELECT * FROM connections WHERE followingId = :followingId")
    fun getFollowerConnections(followingId: String): Flow<List<ConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(conn: ConnectionEntity)

    @Query("DELETE FROM connections WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun deleteConnection(followerId: String, followingId: String)
}
