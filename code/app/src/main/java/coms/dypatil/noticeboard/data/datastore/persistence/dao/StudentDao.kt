package coms.dypatil.noticeboard.data.datastore.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import coms.dypatil.noticeboard.data.datastore.persistence.contract.BaseContract
import coms.dypatil.noticeboard.data.datastore.persistence.contract.StudentContract
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(student: Student): Long

    @Query("DELETE FROM  ${StudentContract.TABLE_NAME}")
    fun deleteAll(): Int


    @Query("SELECT * FROM  ${StudentContract.TABLE_NAME}")
    fun getAllStuednt(): Flowable<List<Student>>

    @Query("SELECT * FROM  ${StudentContract.TABLE_NAME} WHERE ${BaseContract.FB_USER_ID} = :fbUserId")
    fun getStudentByFBId(fbUserId: String): Maybe<Student>


    @Query("""SELECT * FROM  ${StudentContract.TABLE_NAME}
        WHERE ${BaseContract.DEPARTMENT}  = :department
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getStudentByDept(department: String): Flowable<List<Student>>

    @Query("""SELECT * FROM  ${StudentContract.TABLE_NAME}
        WHERE ${StudentContract.YEAR} = :year
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getStudentByYear(year: String): Flowable<List<Student>>

    @Query("""SELECT * FROM  ${StudentContract.TABLE_NAME}
        WHERE ${BaseContract.DEPARTMENT}  = :department AND
         ${StudentContract.YEAR} = :year
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getStudentByDeptYear(department: String, year: String): Flowable<List<Student>>


}