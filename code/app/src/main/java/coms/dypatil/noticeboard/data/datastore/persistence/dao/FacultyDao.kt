package coms.dypatil.noticeboard.data.datastore.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import coms.dypatil.noticeboard.data.datastore.persistence.contract.BaseContract
import coms.dypatil.noticeboard.data.datastore.persistence.contract.FacultyContract
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface FacultyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(faculty: Faculty): Long

    @Query("DELETE FROM  ${FacultyContract.TABLE_NAME}")
    fun deleteAll(): Int


    @Query("SELECT * FROM  ${FacultyContract.TABLE_NAME}")
    fun getAllFaculty(): Flowable<List<Faculty>>

    @Query("SELECT * FROM  ${FacultyContract.TABLE_NAME} WHERE ${BaseContract.FB_USER_ID} = :fbUserId")
    fun getFacultyByFBId(fbUserId: String): Maybe<Faculty>


    @Query("""SELECT * FROM  ${FacultyContract.TABLE_NAME}
        WHERE ${BaseContract.DEPARTMENT}  = :department
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getFacultyByDept(department: String): Flowable<List<Faculty>>

    @Query("""SELECT * FROM  ${FacultyContract.TABLE_NAME}
        WHERE ${BaseContract.DESIGNATION} = :designation
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getFacultyByDesi(designation: String): Flowable<List<Faculty>>

    @Query("""SELECT * FROM  ${FacultyContract.TABLE_NAME}
        WHERE ${BaseContract.DEPARTMENT}  = :department AND
         ${BaseContract.DESIGNATION} = :designation
         ORDER BY ${BaseContract.NAME} DESC""")
    fun getFacultyByDeptDesi(department: String, designation: String): Flowable<List<Faculty>>


}