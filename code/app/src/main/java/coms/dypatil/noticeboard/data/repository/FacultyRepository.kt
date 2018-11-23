package coms.dypatil.noticeboard.data.repository

import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.domain.interactor.FacultyUseCase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

object FacultyRepository {

    private var facultyUseCase = FacultyUseCase(App.fDatabase)
    private val dataProvider = App.facultyDataProvider

    //===============================================================================================================
    fun insert(faculty: Faculty): Long = dataProvider.insert(faculty)

    //===============================================================================================================
    fun dropTable(): Single<Int> = facultyUseCase.dropTable()


    //===============================================================================================================
    //From Local SQLite
    fun getAllFaculty(): Flowable<List<Faculty>> = dataProvider.getAllFaculty()

    fun getFacultyByDept(filterDept: String): Flowable<List<Faculty>> = dataProvider.getFacultyByDept(filterDept)

    fun getFacultyByDesi(filterDesi: String): Flowable<List<Faculty>> = dataProvider.getFacultyByDesi(filterDesi)

    fun getFacultyByDeptDesi(filterDept: String, filterDesi: String): Flowable<List<Faculty>> =
            dataProvider.getFacultyByDeptDesi(filterDept, filterDesi)

    fun getFBFacultyById(fbUserId: String): Maybe<Faculty> = dataProvider.getFacultyByFBId(fbUserId)


    //===============================================================================================================
    //From Firebase
    fun getFBAllFaculty(deptList: List<String>, desiList: List<List<String>>): Observable<MutableList<Faculty>> =
            facultyUseCase.getAllFacultyFB(deptList, desiList)

    fun getFBFacultyProfile(fbUserId: String): Maybe<Faculty> =
            facultyUseCase.getFBFacultyProfile(fbUserId)

}
