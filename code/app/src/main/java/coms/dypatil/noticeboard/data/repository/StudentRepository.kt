package coms.dypatil.noticeboard.data.repository

import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.domain.interactor.StudentUseCase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

object StudentRepository {

    private val studentUseCase = StudentUseCase(App.fDatabase)
    private val dataProvider = App.studentDataProvider

    //===============================================================================================================
    fun insert(student: Student): Long = dataProvider.insert(student)

    //===============================================================================================================
    fun dropTable(): Single<Int> = studentUseCase.dropTable()


    //===============================================================================================================
    //From Local SQLite
    fun getAllStudent(): Flowable<List<Student>> = dataProvider.getAllStuednt()

    fun getStudentByDept(filterDept: String): Flowable<List<Student>> = dataProvider.getStudentByDept(filterDept)

    fun getStudentByYear(filterYear: String): Flowable<List<Student>> = dataProvider.getStudentByYear(filterYear)

    fun getStudentByDeptYear(filterDept: String, filterYear: String): Flowable<List<Student>> =
            dataProvider.getStudentByDeptYear(filterDept, filterYear)

    fun getStudentById(fbUserId: String): Maybe<Student> = dataProvider.getStudentByFBId(fbUserId)


    //===============================================================================================================
    //From Firebase
    fun getAllStudentFB(deptList: List<String>, yearList: List<List<String>>): Observable<MutableList<Student>> =
            studentUseCase.getAllStudentFB(deptList, yearList)

    fun getFBStudentProfile(fbUserId: String): Maybe<Student> =
            studentUseCase.getFBStudentProfile(fbUserId)


}



