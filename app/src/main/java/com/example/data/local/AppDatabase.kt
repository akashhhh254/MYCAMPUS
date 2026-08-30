package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        TeacherAssignmentEntity::class,
        SubjectEntity::class,
        ClassGroupEntity::class,
        TimetableEntity::class,
        AttendanceRecordEntity::class,
        StudyMaterialEntity::class,
        PaperEntity::class,
        AssignmentEntity::class,
        AssignmentSubmissionEntity::class,
        NoticeEntity::class,
        NotificationEntity::class,
        EventEntity::class,
        OpportunityEntity::class,
        PostEntity::class,
        CommentEntity::class,
        StudyGroupEntity::class,
        ConnectionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun campusDao(): CampusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycampus_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedAsync()
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                seedAsync()
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                seedAsync()
            }

            private fun seedAsync() {
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            val dao = database.campusDao()
                            val existing = dao.getUserById("user_principal")
                            if (existing == null) {
                                DemoDataSeeder.seedDatabase(dao)
                            }
                        } catch (e: Exception) {
                            // Seed if table is fresh
                            try {
                                DemoDataSeeder.seedDatabase(database.campusDao())
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }
}
