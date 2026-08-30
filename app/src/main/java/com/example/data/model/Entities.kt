package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true), Index(value = ["collegeId"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val collegeId: String,
    val passwordHash: String,
    val role: String, // "principal", "teacher", "student"
    val fullName: String,
    val username: String,
    val avatarUrl: String = "",
    val isActive: Boolean = true,
    val status: String = "active", // "active", "pending", "suspended", "disabled"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    indices = [Index(value = ["userId"], unique = true), Index(value = ["rollNumber"])]
)
data class StudentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val rollNumber: String,
    val department: String, // e.g. "Computer Applications", "Information Technology"
    val course: String,     // e.g. "BCA", "B.Tech CS"
    val year: String,       // e.g. "1st Year", "2nd Year", "3rd Year", "4th Year"
    val classGroup: String, // e.g. "BCA 2nd Year"
    val section: String,    // e.g. "A", "B"
    val bio: String = "Enthusiastic student eager to learn and collaborate.",
    val skills: String = "Kotlin, Python, DBMS, UI/UX",
    val academicInterests: String = "Artificial Intelligence, Mobile Dev, Cloud Computing",
    val isDeactivated: Boolean = false
)

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["userId"], unique = true), Index(value = ["employeeId"], unique = true)]
)
data class TeacherEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val employeeId: String,
    val department: String,
    val designation: String = "Assistant Professor",
    val qualification: String = "Ph.D. in Computer Science",
    val isDeactivated: Boolean = false
)

@Entity(
    tableName = "teacher_subject_assignments",
    indices = [Index(value = ["teacherId", "subjectId", "classGroup", "section"])]
)
data class TeacherAssignmentEntity(
    @PrimaryKey val id: String,
    val teacherId: String,
    val teacherName: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String, // e.g. "BCA 2nd Year"
    val section: String     // e.g. "A"
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val department: String,
    val semester: Int
)

@Entity(tableName = "classes")
data class ClassGroupEntity(
    @PrimaryKey val id: String,
    val name: String,       // e.g. "BCA 2nd Year"
    val department: String,
    val year: String,
    val section: String
)

@Entity(
    tableName = "timetable",
    indices = [Index(value = ["dayOfWeek", "classGroup", "section", "startTime"])]
)
data class TimetableEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String,  // "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    val startTime: String,  // "09:00 AM"
    val endTime: String,    // "10:00 AM"
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val teacherName: String,
    val classGroup: String, // "BCA 2nd Year"
    val section: String,    // "A"
    val roomNumber: String  // "Lab 3" or "Room 204"
)

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["studentId", "date", "subjectId"]),
        Index(value = ["date", "classGroup", "section", "subjectId"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey val id: String,
    val date: String,       // "2026-08-30"
    val classGroup: String,
    val section: String,
    val subjectId: String,
    val subjectName: String,
    val teacherId: String,
    val studentId: String,
    val studentRoll: String,
    val studentName: String,
    val isPresent: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_materials",
    indices = [Index(value = ["subjectId", "classGroup", "unit"])]
)
data class StudyMaterialEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String,
    val section: String,
    val unit: String,       // "Unit 1", "Unit 2"
    val topic: String,
    val title: String,
    val description: String,
    val fileType: String,   // "PDF", "PPT", "DOC", "IMAGE"
    val fileName: String,
    val fileSize: String,   // "3.4 MB"
    val uploaderId: String,
    val uploaderName: String,
    val downloadUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "papers",
    indices = [Index(value = ["subjectId", "type"])]
)
data class PaperEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val subjectName: String,
    val unit: String,
    val type: String,        // "Previous Year Paper", "Question Paper", "Practical Paper", "Model Paper", "Important Questions"
    val title: String,
    val year: String,        // "2025"
    val fileName: String,
    val fileSize: String,
    val uploaderId: String,
    val uploaderName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assignments",
    indices = [Index(value = ["subjectId", "classGroup", "section"])]
)
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subjectId: String,
    val subjectName: String,
    val classGroup: String,
    val section: String,
    val deadline: String,    // "2026-09-05 23:59"
    val attachedFileName: String = "",
    val teacherId: String,
    val teacherName: String,
    val totalMarks: Int = 100,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assignment_submissions",
    indices = [Index(value = ["assignmentId", "studentId"], unique = true)]
)
data class AssignmentSubmissionEntity(
    @PrimaryKey val id: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val studentRoll: String,
    val submissionText: String,
    val attachedFileName: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val status: String = "Submitted", // "Submitted", "Late", "Graded"
    val grade: String = ""
)

@Entity(
    tableName = "notices",
    indices = [Index(value = ["createdAt"])]
)
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priority: String,    // "High", "Medium", "Low", "Urgent"
    val targetRole: String,  // "All", "Students", "Teachers"
    val targetClass: String, // "All" or "BCA 2nd Year"
    val targetSection: String, // "All" or "A"
    val authorId: String,
    val authorName: String,
    val authorRole: String,  // "principal", "teacher"
    val attachment: String = "",
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId", "isRead"]), Index(value = ["createdAt"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String,
    val title: String,
    val message: String,
    val type: String, // "Notice", "Notes", "Paper", "Assignment", "Timetable", "Attendance", "Announcement"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val bannerUrl: String = "",
    val registrationDetails: String = "Free entry for all college students.",
    val registeredUserIds: String = "", // Comma-separated user IDs
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val description: String,
    val type: String, // "Internship", "Hackathon", "Competition", "Workshop", "Scholarship", "Coding Event"
    val eligibility: String,
    val deadline: String,
    val externalLink: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "posts",
    indices = [Index(value = ["createdAt"])]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorRole: String,
    val authorAvatarUrl: String = "",
    val content: String,
    val category: String = "Academic Discussion", // "General", "Academic Discussion", "Project Help", "Coding"
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedUserIds: String = "", // Comma-separated user IDs
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comments",
    indices = [Index(value = ["postId"])]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subject: String,
    val description: String,
    val memberCount: Int = 1,
    val memberUserIds: String = "",
    val creatorId: String,
    val creatorName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "connections",
    indices = [Index(value = ["followerId", "followingId"], unique = true)]
)
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)
