package com.example.data.local

import com.example.data.model.*

object DemoDataSeeder {

    suspend fun seedDatabase(dao: CampusDao) {
        // 0. Seed Official College Departments
        val departments = listOf(
            DepartmentEntity("dept_comp", "COMP", "Computer Engineering", "Department of Computer Engineering & Software Systems"),
            DepartmentEntity("dept_it", "IT", "Information Technology", "Department of Information Technology & Network Systems"),
            DepartmentEntity("dept_aids", "AI-DS", "Artificial Intelligence & Data Science", "Department of AI, Machine Learning & Analytics"),
            DepartmentEntity("dept_mech", "MECH", "Mechanical Engineering", "Department of Mechanical & Automation Engineering"),
            DepartmentEntity("dept_civil", "CIVIL", "Civil Engineering", "Department of Civil & Structural Engineering"),
            DepartmentEntity("dept_elec", "ELEC", "Electrical Engineering", "Department of Electrical & Power Systems"),
            DepartmentEntity("dept_extc", "EXTC", "Electronics Engineering", "Department of Electronics & Telecommunication"),
            DepartmentEntity("dept_math", "MATH", "Applied Mathematics & Sciences", "Department of Applied Mathematics & Basic Sciences")
        )
        dao.insertDepartments(departments)

        // 1. HODs (Head of Department)
        val hodUsers = listOf(
            UserEntity(
                id = "user_hod_comp",
                email = "hod.comp@mycampus.edu",
                collegeId = "BD25HOD001",
                passwordHash = "admin123",
                role = "hod",
                fullName = "Dr. Alok Verma",
                username = "hod_comp",
                avatarUrl = "",
                phoneNumber = "+91 98765 43210",
                departmentId = "dept_comp",
                departmentName = "Computer Engineering",
                status = "active"
            ),
            UserEntity(
                id = "user_hod_mech",
                email = "hod.mech@mycampus.edu",
                collegeId = "BD25HOD002",
                passwordHash = "admin123",
                role = "hod",
                fullName = "Dr. Rajesh Kulkarni",
                username = "hod_mech",
                avatarUrl = "",
                phoneNumber = "+91 98765 43211",
                departmentId = "dept_mech",
                departmentName = "Mechanical Engineering",
                status = "active"
            ),
            // Legacy / alternate login support for existing credentials
            UserEntity(
                id = "user_principal",
                email = "principal@mycampus.edu",
                collegeId = "BD25PR001",
                passwordHash = "admin123",
                role = "hod",
                fullName = "Dr. Alok Verma",
                username = "principal_alok",
                avatarUrl = "",
                phoneNumber = "+91 98765 43210",
                departmentId = "dept_comp",
                departmentName = "Computer Engineering",
                status = "active"
            )
        )
        hodUsers.forEach { dao.insertUser(it) }

        val hodRecords = listOf(
            HodEntity(
                id = "hod_comp_record",
                userId = "user_hod_comp",
                employeeId = "BD25HOD001",
                departmentId = "dept_comp",
                departmentName = "Computer Engineering",
                designation = "Head of Department (HOD) - Computer Engineering",
                qualification = "Ph.D. in Computer Science (IIT Bombay)"
            ),
            HodEntity(
                id = "hod_mech_record",
                userId = "user_hod_mech",
                employeeId = "BD25HOD002",
                departmentId = "dept_mech",
                departmentName = "Mechanical Engineering",
                designation = "Head of Department (HOD) - Mechanical Engineering",
                qualification = "Ph.D. in Thermal Engineering"
            )
        )
        hodRecords.forEach { dao.insertHod(it) }

        // 2. Teachers
        val teacherUsers = listOf(
            UserEntity("user_tch_rahul", "rahul.sharma@mycampus.edu", "BD25TC001", "teacher123", "teacher", "Prof. Rahul Sharma", "rahul_dbms", departmentId = "dept_comp", departmentName = "Computer Engineering", status = "active"),
            UserEntity("user_tch_priya", "priya.nair@mycampus.edu", "BD25TC002", "teacher123", "teacher", "Prof. Priya Nair", "priya_java", departmentId = "dept_comp", departmentName = "Computer Engineering", status = "active"),
            UserEntity("user_tch_vikram", "vikram.malhotra@mycampus.edu", "BD25TC003", "teacher123", "teacher", "Prof. Vikram Malhotra", "vikram_ds", departmentId = "dept_it", departmentName = "Information Technology", status = "active"),
            UserEntity("user_tch_sunita", "sunita.rao@mycampus.edu", "BD25TC004", "teacher123", "teacher", "Prof. Sunita Rao", "sunita_math", departmentId = "dept_math", departmentName = "Applied Mathematics & Sciences", status = "active")
        )
        teacherUsers.forEach { dao.insertUser(it) }

        val teachers = listOf(
            TeacherEntity("tch_rahul", "user_tch_rahul", "BD25TC001", "Computer Engineering", "Associate Professor", "Ph.D. in Database Systems"),
            TeacherEntity("tch_priya", "user_tch_priya", "BD25TC002", "Computer Engineering", "Assistant Professor", "M.Tech in Software Engineering"),
            TeacherEntity("tch_vikram", "user_tch_vikram", "BD25TC003", "Information Technology", "Assistant Professor", "M.Tech in Algorithms"),
            TeacherEntity("tch_sunita", "user_tch_sunita", "BD25TC004", "Applied Mathematics & Sciences", "Senior Professor", "Ph.D. in Applied Mathematics")
        )
        teachers.forEach { dao.insertTeacher(it) }

        // 3. Subjects & Classes
        val subjects = listOf(
            SubjectEntity("sub_dbms", "BCA301", "DBMS", "Computer Applications", 3),
            SubjectEntity("sub_java", "BCA302", "Java Programming", "Computer Applications", 3),
            SubjectEntity("sub_ds", "BCA303", "Data Structures", "Computer Applications", 3),
            SubjectEntity("sub_math", "BCA304", "Discrete Mathematics", "Mathematics", 3)
        )
        subjects.forEach { dao.insertSubject(it) }

        val classes = listOf(
            ClassGroupEntity("cls_bca_2a", "BCA 2nd Year", "Computer Applications", "2nd Year", "A"),
            ClassGroupEntity("cls_bca_2b", "BCA 2nd Year", "Computer Applications", "2nd Year", "B"),
            ClassGroupEntity("cls_btech_3a", "B.Tech CS 3rd Year", "Computer Science", "3rd Year", "A")
        )
        classes.forEach { dao.insertClass(it) }

        // 4. Teacher Subject Assignments
        val teacherAssignments = listOf(
            TeacherAssignmentEntity("ta_1", "tch_rahul", "Prof. Rahul Sharma", "sub_dbms", "DBMS", "BCA 2nd Year", "A"),
            TeacherAssignmentEntity("ta_2", "tch_priya", "Prof. Priya Nair", "sub_java", "Java Programming", "BCA 2nd Year", "A"),
            TeacherAssignmentEntity("ta_3", "tch_vikram", "Prof. Vikram Malhotra", "sub_ds", "Data Structures", "BCA 2nd Year", "A"),
            TeacherAssignmentEntity("ta_4", "tch_sunita", "Prof. Sunita Rao", "sub_math", "Discrete Mathematics", "BCA 2nd Year", "A")
        )
        teacherAssignments.forEach { dao.insertTeacherAssignment(it) }

        // 5. Students (At least 10 realistic students)
        val studentsData = listOf(
            Triple("Akash Thakare", "akash.thakare@mycampus.edu", "01"),
            Triple("Rohan Mehta", "rohan.mehta@mycampus.edu", "02"),
            Triple("Sneha Kapoor", "sneha.kapoor@mycampus.edu", "03"),
            Triple("Ananya Roy", "ananya.roy@mycampus.edu", "04"),
            Triple("Aditya Patel", "aditya.patel@mycampus.edu", "05"),
            Triple("Neha Singh", "neha.singh@mycampus.edu", "06"),
            Triple("Karthik Iyer", "karthik.iyer@mycampus.edu", "07"),
            Triple("Pooja Sharma", "pooja.sharma@mycampus.edu", "08"),
            Triple("Devansh Gupta", "devansh.gupta@mycampus.edu", "09"),
            Triple("Riya Sen", "riya.sen@mycampus.edu", "10")
        )

        studentsData.forEachIndexed { index, (name, email, roll) ->
            val uId = "user_stu_${index + 1}"
            val sId = "stu_${index + 1}"
            val collegeId = String.format("BD25BE%03d", index + 1)
            val username = name.lowercase().replace(" ", "_")
            dao.insertUser(
                UserEntity(
                    id = uId,
                    email = if (index == 0) "thakareakash254@gmail.com" else email,
                    collegeId = collegeId,
                    passwordHash = "student123",
                    role = "student",
                    fullName = name,
                    username = username,
                    status = "active"
                )
            )
            dao.insertStudent(
                StudentEntity(
                    id = sId,
                    userId = uId,
                    rollNumber = roll,
                    department = "Computer Applications",
                    course = "BCA",
                    year = "2nd Year",
                    classGroup = "BCA 2nd Year",
                    section = "A",
                    bio = "Tech enthusiast passionate about building mobile & full stack solutions.",
                    skills = "Kotlin, Android Compose, SQL, Java, Git",
                    academicInterests = "Database Design, App Architecture, Machine Learning"
                )
            )
        }

        // 6. Timetable
        val timetableEntries = listOf(
            TimetableEntity("tt_1", "Monday", "09:00 AM", "10:00 AM", "sub_dbms", "DBMS", "tch_rahul", "Prof. Rahul Sharma", "BCA 2nd Year", "A", "Room 204"),
            TimetableEntity("tt_2", "Monday", "10:00 AM", "11:00 AM", "sub_java", "Java Programming", "tch_priya", "Prof. Priya Nair", "BCA 2nd Year", "A", "Lab 2"),
            TimetableEntity("tt_3", "Monday", "11:30 AM", "12:30 PM", "sub_ds", "Data Structures", "tch_vikram", "Prof. Vikram Malhotra", "BCA 2nd Year", "A", "Room 205"),
            TimetableEntity("tt_4", "Tuesday", "09:00 AM", "10:00 AM", "sub_math", "Discrete Mathematics", "tch_sunita", "Prof. Sunita Rao", "BCA 2nd Year", "A", "Room 204"),
            TimetableEntity("tt_5", "Tuesday", "10:00 AM", "11:00 AM", "sub_dbms", "DBMS", "tch_rahul", "Prof. Rahul Sharma", "BCA 2nd Year", "A", "Lab 1"),
            TimetableEntity("tt_6", "Wednesday", "09:00 AM", "10:00 AM", "sub_java", "Java Programming", "tch_priya", "Prof. Priya Nair", "BCA 2nd Year", "A", "Room 204"),
            TimetableEntity("tt_7", "Wednesday", "10:00 AM", "11:00 AM", "sub_ds", "Data Structures", "tch_vikram", "Prof. Vikram Malhotra", "BCA 2nd Year", "A", "Lab 3"),
            TimetableEntity("tt_8", "Thursday", "09:00 AM", "10:00 AM", "sub_dbms", "DBMS", "tch_rahul", "Prof. Rahul Sharma", "BCA 2nd Year", "A", "Room 204"),
            TimetableEntity("tt_9", "Thursday", "10:00 AM", "11:00 AM", "sub_math", "Discrete Mathematics", "tch_sunita", "Prof. Sunita Rao", "BCA 2nd Year", "A", "Room 204"),
            TimetableEntity("tt_10", "Friday", "09:00 AM", "10:00 AM", "sub_java", "Java Programming", "tch_priya", "Prof. Priya Nair", "BCA 2nd Year", "A", "Lab 2"),
            TimetableEntity("tt_11", "Friday", "10:00 AM", "11:00 AM", "sub_ds", "Data Structures", "tch_vikram", "Prof. Vikram Malhotra", "BCA 2nd Year", "A", "Room 205")
        )
        timetableEntries.forEach { dao.insertTimetableEntry(it) }

        // 7. Seed Attendance for testing stats and predictions
        val dates = listOf("2026-08-25", "2026-08-26", "2026-08-27", "2026-08-28", "2026-08-29")
        val attendanceList = mutableListOf<AttendanceRecordEntity>()
        dates.forEachIndexed { dIndex, date ->
            subjects.forEach { sub ->
                studentsData.forEachIndexed { sIndex, (name, _, roll) ->
                    val sId = "stu_${sIndex + 1}"
                    // Generate realistic attendance (Akash has high attendance, some have medium or lower)
                    val isPresent = when {
                        sIndex == 0 -> dIndex != 1 // Akash present 4/5
                        sIndex == 1 -> true        // Rohan 5/5
                        sIndex == 2 -> dIndex < 3  // Sneha 3/5
                        sIndex == 3 -> dIndex % 2 == 0 // Ananya 3/5
                        else -> dIndex != 0
                    }
                    attendanceList.add(
                        AttendanceRecordEntity(
                            id = "att_${date}_${sub.id}_$sId",
                            date = date,
                            classGroup = "BCA 2nd Year",
                            section = "A",
                            subjectId = sub.id,
                            subjectName = sub.name,
                            teacherId = "tch_rahul",
                            studentId = sId,
                            studentRoll = roll,
                            studentName = name,
                            isPresent = isPresent
                        )
                    )
                }
            }
        }
        dao.insertAttendanceRecords(attendanceList)

        // 8. Study Materials
        val materials = listOf(
            StudyMaterialEntity(
                id = "mat_1",
                subjectId = "sub_dbms",
                subjectName = "DBMS",
                classGroup = "BCA 2nd Year",
                section = "A",
                unit = "Unit 1",
                topic = "Relational Model & ER Diagrams",
                title = "Unit 1 — Comprehensive DBMS Notes",
                description = "Complete guide covering Entity-Relationship concepts, 1NF to BCNF, Relational Algebra, and constraints.",
                fileType = "PDF",
                fileName = "Introduction_to_DBMS_Unit1.pdf",
                fileSize = "4.2 MB",
                uploaderId = "tch_rahul",
                uploaderName = "Prof. Rahul Sharma"
            ),
            StudyMaterialEntity(
                id = "mat_2",
                subjectId = "sub_dbms",
                subjectName = "DBMS",
                classGroup = "BCA 2nd Year",
                section = "A",
                unit = "Unit 2",
                topic = "SQL Queries & Indexing",
                title = "Unit 2 — Advanced SQL & Transaction Control",
                description = "Deep dive into ACID properties, joins, subqueries, concurrency control, and B+ Trees.",
                fileType = "PDF",
                fileName = "DBMS_Unit2_SQL_Transactions.pdf",
                fileSize = "3.8 MB",
                uploaderId = "tch_rahul",
                uploaderName = "Prof. Rahul Sharma"
            ),
            StudyMaterialEntity(
                id = "mat_3",
                subjectId = "sub_java",
                subjectName = "Java Programming",
                classGroup = "BCA 2nd Year",
                section = "A",
                unit = "Unit 1",
                topic = "OOP Concepts & Multithreading",
                title = "Java Masterclass — OOP & Concurrency",
                description = "Class hierarchies, Polymorphism, Abstract classes, Interface vs Abstract, Thread Lifecycle, and Synchronized blocks.",
                fileType = "PPT",
                fileName = "Java_OOP_Multithreading.pptx",
                fileSize = "6.1 MB",
                uploaderId = "tch_priya",
                uploaderName = "Prof. Priya Nair"
            ),
            StudyMaterialEntity(
                id = "mat_4",
                subjectId = "sub_ds",
                subjectName = "Data Structures",
                classGroup = "BCA 2nd Year",
                section = "A",
                unit = "Unit 3",
                topic = "Graphs & Trees Algorithms",
                title = "Graph Traversal BFS/DFS & Shortest Path",
                description = "Dijkstra, Bellman-Ford, Kruskal's MST algorithm, and AVL Tree balancing rotations.",
                fileType = "DOC",
                fileName = "DSA_Unit3_Graphs_Trees.docx",
                fileSize = "2.5 MB",
                uploaderId = "tch_vikram",
                uploaderName = "Prof. Vikram Malhotra"
            )
        )
        materials.forEach { dao.insertStudyMaterial(it) }

        // 9. Papers
        val papers = listOf(
            PaperEntity(
                id = "paper_1",
                subjectId = "sub_dbms",
                subjectName = "DBMS",
                unit = "Unit 1-4",
                type = "Previous Year Paper",
                title = "DBMS University End-Semester Paper 2025",
                year = "2025",
                fileName = "DBMS_EndSem_2025_Solved.pdf",
                fileSize = "2.1 MB",
                uploaderId = "tch_rahul",
                uploaderName = "Prof. Rahul Sharma"
            ),
            PaperEntity(
                id = "paper_2",
                subjectId = "sub_java",
                subjectName = "Java Programming",
                unit = "Unit 1-3",
                type = "Model Paper",
                title = "Java Mid-Semester Model Question Paper",
                year = "2026",
                fileName = "Java_Model_Paper_2026.pdf",
                fileSize = "1.4 MB",
                uploaderId = "tch_priya",
                uploaderName = "Prof. Priya Nair"
            ),
            PaperEntity(
                id = "paper_3",
                subjectId = "sub_ds",
                subjectName = "Data Structures",
                unit = "Unit 1-5",
                type = "Important Questions",
                title = "Top 50 DSA Exam Questions with Solutions",
                year = "2026",
                fileName = "DSA_Top50_Exam_Prep.pdf",
                fileSize = "3.2 MB",
                uploaderId = "tch_vikram",
                uploaderName = "Prof. Vikram Malhotra"
            )
        )
        papers.forEach { dao.insertPaper(it) }

        // 10. Assignments & Submissions
        val assignments = listOf(
            AssignmentEntity(
                id = "assign_1",
                title = "DBMS Schema Design & Normalization",
                description = "Design an ER diagram for a Hospital Management System and convert it to 3NF/BCNF relational schemas. Include SQL DDL queries.",
                subjectId = "sub_dbms",
                subjectName = "DBMS",
                classGroup = "BCA 2nd Year",
                section = "A",
                deadline = "2026-09-08 23:59",
                attachedFileName = "Assignment1_Problem_Statement.pdf",
                teacherId = "tch_rahul",
                teacherName = "Prof. Rahul Sharma",
                totalMarks = 50
            ),
            AssignmentEntity(
                id = "assign_2",
                title = "Java Thread Pool & Socket Server",
                description = "Build a multi-client chat server in Java using ServerSocket and ExecutorService thread pooling with clean exception handling.",
                subjectId = "sub_java",
                subjectName = "Java Programming",
                classGroup = "BCA 2nd Year",
                section = "A",
                deadline = "2026-09-12 18:00",
                attachedFileName = "Java_Socket_Lab_Guide.pdf",
                teacherId = "tch_priya",
                teacherName = "Prof. Priya Nair",
                totalMarks = 50
            )
        )
        assignments.forEach { dao.insertAssignment(it) }

        val sampleSubmission = AssignmentSubmissionEntity(
            id = "subm_1",
            assignmentId = "assign_1",
            studentId = "stu_1",
            studentName = "Akash Thakare",
            studentRoll = "01",
            submissionText = "Implemented complete hospital ER diagram, relational tables, and converted anomalies up to BCNF.",
            attachedFileName = "Akash_Thakare_DBMS_Assign1.pdf",
            status = "Submitted"
        )
        dao.insertSubmission(sampleSubmission)

        // 11. Notices
        val notices = listOf(
            NoticeEntity(
                id = "not_1",
                title = "TechNova 2026 — Annual Department Tech Symposium",
                description = "Registrations are now open for the department's flagship tech fest including 24-hr Hackathon, Web3 sprint, and AI arena.",
                priority = "High",
                targetRole = "All",
                targetClass = "All",
                targetSection = "All",
                authorId = "user_hod_comp",
                authorName = "Dr. Alok Verma (HOD)",
                authorRole = "hod",
                departmentId = "dept_comp",
                date = "2026-08-28"
            ),
            NoticeEntity(
                id = "not_2",
                title = "Mid-Term Examination Schedule Released",
                description = "All students of Computer and IT Engineering are hereby notified that mid-term examinations commence from Sept 15th, 2026. Hall tickets will be issued next week.",
                priority = "Urgent",
                targetRole = "Students",
                targetClass = "All",
                targetSection = "All",
                authorId = "user_hod_comp",
                authorName = "Dr. Alok Verma (HOD)",
                authorRole = "hod",
                departmentId = "dept_comp",
                date = "2026-08-27"
            ),
            NoticeEntity(
                id = "not_3",
                title = "DBMS Lab Viva & Project Submission",
                description = "BCA 2nd Year Section A students must submit their lab journals and functional project demo by this Friday.",
                priority = "Medium",
                targetRole = "Students",
                targetClass = "BCA 2nd Year",
                targetSection = "A",
                authorId = "tch_rahul",
                authorName = "Prof. Rahul Sharma",
                authorRole = "teacher",
                departmentId = "dept_comp",
                date = "2026-08-29"
            )
        )
        notices.forEach { dao.insertNotice(it) }

        // 12. Events
        val events = listOf(
            EventEntity(
                id = "evt_1",
                title = "TechNova 2026: Campus Hackathon",
                description = "A 24-hour non-stop hackathon with prize pool of \$5,000. Tracks include AI & ML, Web3, and Smart Campus.",
                date = "2026-09-18",
                time = "09:00 AM - 05:00 PM",
                location = "Auditorium & Innovation Labs",
                organizer = "ACM Student Chapter & CS Dept",
                registrationDetails = "Team of 2-4 members. Free registration.",
                registeredUserIds = "user_stu_1,user_stu_2"
            ),
            EventEntity(
                id = "evt_2",
                title = "Goonj — Annual Cultural Festival",
                description = "The biggest college cultural festival featuring battle of bands, dance drama, art exhibitions, and celebrity night.",
                date = "2026-10-05",
                time = "10:00 AM - 10:00 PM",
                location = "College Main Grounds",
                organizer = "Student Council",
                registrationDetails = "Open to all students with valid College ID.",
                registeredUserIds = "user_stu_1"
            ),
            EventEntity(
                id = "evt_3",
                title = "Workshop: Modern Android with Jetpack Compose",
                description = "Hands-on session with industry experts building reactive, declarative Android mobile applications.",
                date = "2026-09-02",
                time = "02:00 PM - 05:00 PM",
                location = "Computer Lab 4",
                organizer = "Google Developer Student Club",
                registrationDetails = "Limited to 60 seats.",
                registeredUserIds = "user_stu_1,user_stu_3"
            )
        )
        events.forEach { dao.insertEvent(it) }

        // 13. Opportunities
        val opps = listOf(
            OpportunityEntity(
                id = "opp_1",
                title = "Google Summer Internship 2027",
                organization = "Google",
                description = "Software Engineering Internships for 2nd and 3rd year students. Work on cutting-edge cloud and mobile platforms.",
                type = "Internship",
                eligibility = "BCA / B.Tech Computer Science with 7.5+ CGPA",
                deadline = "2026-09-30",
                externalLink = "https://careers.google.com"
            ),
            OpportunityEntity(
                id = "opp_2",
                title = "Smart India Hackathon 2026",
                organization = "Ministry of Education",
                description = "Nationwide initiative providing students a platform to solve pressing problems of government ministries.",
                type = "Hackathon",
                eligibility = "All college enrolled students",
                deadline = "2026-09-15",
                externalLink = "https://sih.gov.in"
            ),
            OpportunityEntity(
                id = "opp_3",
                title = "National Merit STEM Scholarship",
                organization = "Higher Education Foundation",
                description = "Scholarship grants of \$2,000 per academic year for top performing STEM students.",
                type = "Scholarship",
                eligibility = "8.0+ CGPA & family income < \$15,000",
                deadline = "2026-10-15",
                externalLink = "https://scholarships.gov"
            )
        )
        opps.forEach { dao.insertOpportunity(it) }

        // 14. CampusConnect Posts & Comments
        val posts = listOf(
            PostEntity(
                id = "post_1",
                authorId = "user_stu_1",
                authorName = "Akash Thakare",
                authorUsername = "akash_thakare",
                authorRole = "student",
                content = "Hey everyone! We are forming a study circle for DBMS Normalization & SQL Query Optimization. Anyone interested in weekly peer problem-solving sessions?",
                category = "Academic Discussion",
                likeCount = 8,
                commentCount = 2,
                likedUserIds = "user_stu_2,user_stu_3,user_stu_4"
            ),
            PostEntity(
                id = "post_2",
                authorId = "user_stu_3",
                authorName = "Sneha Kapoor",
                authorUsername = "sneha_kapoor",
                authorRole = "student",
                content = "Looking for a teammate for the upcoming TechNova Hackathon. Need someone familiar with backend API development (Node/Python/Kotlin). DM me!",
                category = "Project Help",
                likeCount = 12,
                commentCount = 3,
                likedUserIds = "user_stu_1,user_stu_5"
            ),
            PostEntity(
                id = "post_3",
                authorId = "user_tch_rahul",
                authorName = "Prof. Rahul Sharma",
                authorUsername = "rahul_dbms",
                authorRole = "teacher",
                content = "Tip for upcoming midterm: Focus on B-Trees vs B+ Trees indexing differences and 2-Phase Locking protocol.",
                category = "Academic Discussion",
                likeCount = 24,
                commentCount = 1,
                likedUserIds = "user_stu_1,user_stu_2,user_stu_3,user_stu_4"
            )
        )
        posts.forEach { dao.insertPost(it) }

        val comments = listOf(
            CommentEntity("cmt_1", "post_1", "user_stu_2", "Rohan Mehta", "student", "Count me in! When is the first meeting?"),
            CommentEntity("cmt_2", "post_1", "user_stu_4", "Ananya Roy", "student", "Great initiative! Let's do Thursday after class."),
            CommentEntity("cmt_3", "post_2", "user_stu_1", "Akash Thakare", "student", "Sent you a message! I can help with Android and Kotlin backend.")
        )
        comments.forEach { dao.insertComment(it) }

        // 15. Study Groups
        val studyGroups = listOf(
            StudyGroupEntity(
                id = "sg_1",
                name = "DBMS & SQL Wizards",
                subject = "DBMS",
                description = "Collaborative discussions on relational modeling, query tuning, and database transactions.",
                memberCount = 14,
                memberUserIds = "user_stu_1,user_stu_2,user_stu_3,user_stu_4",
                creatorId = "user_stu_1",
                creatorName = "Akash Thakare"
            ),
            StudyGroupEntity(
                id = "sg_2",
                name = "Java & Android Developers",
                subject = "Java / Mobile Dev",
                description = "Building real Android applications with Jetpack Compose, Kotlin coroutines, and clean architecture.",
                memberCount = 21,
                memberUserIds = "user_stu_1,user_stu_3,user_stu_5",
                creatorId = "user_stu_3",
                creatorName = "Sneha Kapoor"
            ),
            StudyGroupEntity(
                id = "sg_3",
                name = "DSA & Competitive Coders",
                subject = "Data Structures",
                description = "Daily LeetCode problem discussions, tree traversal algorithms, and contest preparation.",
                memberCount = 18,
                memberUserIds = "user_stu_1,user_stu_7",
                creatorId = "user_stu_7",
                creatorName = "Karthik Iyer"
            )
        )
        studyGroups.forEach { dao.insertStudyGroup(it) }

        // 16. Notifications
        val notifs = listOf(
            NotificationEntity("notif_1", "user_stu_1", "student", "New DBMS Notes Available", "Prof. Rahul Sharma uploaded Unit 1 DBMS Comprehensive Notes.", "Notes"),
            NotificationEntity("notif_2", "user_stu_1", "student", "New Assignment Posted", "Assignment 1: DBMS Schema Design deadline is Sept 8th.", "Assignment"),
            NotificationEntity("notif_3", "user_stu_1", "student", "Timetable Updated", "Class schedule for Monday has been updated by Principal.", "Timetable"),
            NotificationEntity("notif_4", "user_stu_1", "student", "Attendance Recorded", "Your attendance for DBMS lecture on 2026-08-29 was marked Present.", "Attendance"),
            NotificationEntity("notif_5", "user_tch_rahul", "teacher", "Timetable Notification", "Room 204 assigned for Monday 09:00 AM class.", "Timetable")
        )
        dao.insertNotifications(notifs)
    }
}
