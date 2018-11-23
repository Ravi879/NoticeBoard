package coms.dypatil.noticeboard.data.datastore.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import coms.dypatil.noticeboard.data.datastore.persistence.contract.NoticeContract
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import io.reactivex.Flowable

@Dao
interface NoticeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotice(notice: Notice): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNoticeData(list: List<Notice>): List<Long>


    @Query("DELETE FROM  ${NoticeContract.TABLE_NAME}")
    fun deleteAll(): Int

    @Query("DELETE FROM  ${NoticeContract.TABLE_NAME} WHERE ${NoticeContract.FB_NOTICE_ID} = :fbNoticeId ")
    fun deleteNoticeById(fbNoticeId: String): Int

    @Query("DELETE FROM  ${NoticeContract.TABLE_NAME} WHERE ${NoticeContract.DEPARTMENT} = :department ")
    fun deleteNoticeByDept(department: String): Int


    //=================================  Ongoing Notice  =================================
    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    (${NoticeContract.LAST_DATE} >= :currentMillis)
         ORDER BY ${NoticeContract.ISSUE_DATE} DESC""")
    fun getAllNotice(currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} > :currentMillis AND ${NoticeContract.DEPARTMENT}  = :department
         ORDER BY ${NoticeContract.ISSUE_DATE} DESC""")
    fun getDeptNotice(department: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} > :currentMillis AND  ${NoticeContract.DESIGNATION} = :designation
         ORDER BY ${NoticeContract.ISSUE_DATE} DESC""")
    fun getDesiNotice(designation: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} > :currentMillis AND ${NoticeContract.DEPARTMENT}  = :department AND
                    ${NoticeContract.DESIGNATION} = :designation
         ORDER BY ${NoticeContract.ISSUE_DATE} DESC""")
    fun getDeptDesiNotice(department: String, designation: String, currentMillis: Long): Flowable<List<Notice>>

    //=================================  Expired Notice  ================================
    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE ${NoticeContract.LAST_DATE} < :currentMillis
        ORDER BY ${NoticeContract.LAST_DATE} DESC""")
    fun getAllExpireNotice(currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} < :currentMillis  AND  ${NoticeContract.DEPARTMENT}  = :department
         ORDER BY ${NoticeContract.LAST_DATE} DESC""")
    fun getDeptExpireNotice(department: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} < :currentMillis  AND  ${NoticeContract.DESIGNATION} = :designation
         ORDER BY ${NoticeContract.LAST_DATE} DESC""")
    fun getDesiExpireNotice(designation: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>

    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                    ${NoticeContract.LAST_DATE} < :currentMillis AND ${NoticeContract.DEPARTMENT}  = :department AND
                    ${NoticeContract.DESIGNATION} = :designation
         ORDER BY ${NoticeContract.LAST_DATE} DESC""")
    fun getDeptDesiExpireNotice(department: String, designation: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>


    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE ${NoticeContract.ISSUER_Id} = :issuerId """)
    fun getAllNoticeByIssuerId(issuerId: String): Flowable<List<Notice>>


    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                   ${NoticeContract.ISSUER_Id} = :issuerId AND ${NoticeContract.LAST_DATE} > :currentMillis """)
    fun getNoticeByIssuerId(issuerId: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>


    @Query("""SELECT * FROM  ${NoticeContract.TABLE_NAME} WHERE
                  ${NoticeContract.ISSUER_Id} = :issuerId AND ${NoticeContract.LAST_DATE} < :currentMillis
         ORDER BY ${NoticeContract.LAST_DATE} DESC""")
    fun getExpiredNoticeById(issuerId: String, currentMillis: Long = System.currentTimeMillis()): Flowable<List<Notice>>


    @Query("""UPDATE ${NoticeContract.TABLE_NAME} SET ${NoticeContract.IS_OPEN} = :isNoticeRead WHERE
                   ${NoticeContract.FB_NOTICE_ID} = :noticeId """)
    fun setIsNoticeRead(noticeId: String, isNoticeRead: String): Int

}
