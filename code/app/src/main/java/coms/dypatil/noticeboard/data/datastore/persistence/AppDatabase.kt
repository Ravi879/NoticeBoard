package coms.dypatil.noticeboard.data.datastore.persistence

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import coms.dypatil.noticeboard.data.datastore.persistence.dao.FacultyDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.NoticeDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.StudentDao
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student

@Database(entities = [Faculty::class, Student::class, Notice::class],
        version = 1,
        exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun facultyDao(): FacultyDao

    abstract fun studentDao(): StudentDao

    abstract fun noticeDao(): NoticeDao

    companion object {

        private const val databaseName = "NoticeBoardApp"
        private var INSTANCE: AppDatabase? = null

        fun getDBInstance(context: Context): AppDatabase? {
            if (INSTANCE==null) {
                synchronized(Database::class.java) {
                    if (INSTANCE==null) {
                        INSTANCE = Room.databaseBuilder(context,
                                AppDatabase::class.java, databaseName)
                                .allowMainThreadQueries()
                                .build()
                    }
                }
            }
            return INSTANCE
        }

    }
}
