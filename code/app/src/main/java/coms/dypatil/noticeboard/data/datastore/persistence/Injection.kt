package coms.dypatil.noticeboard.data.datastore.persistence

import android.content.Context
import coms.dypatil.noticeboard.data.datastore.persistence.dao.FacultyDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.NoticeDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.StudentDao


object Injection {

    fun facultyDataProvider(context: Context): FacultyDao? =
            AppDatabase.getDBInstance(context)?.facultyDao()

    fun studentDataProvider(context: Context): StudentDao? =
            AppDatabase.getDBInstance(context)?.studentDao()

    fun noticeDataProvider(context: Context): NoticeDao? =
            AppDatabase.getDBInstance(context)?.noticeDao()

}