package coms.dypatil.noticeboard.domain.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBStudent
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single


class StudentUseCase(val fDatabase: FirebaseDatabase) {


    fun dropTable(): Single<Int> = Single.just(App.studentDataProvider.deleteAll())

    fun getAllStudentFB(deptList: List<String>, yearList: List<List<String>>): Observable<MutableList<Student>> {
        var department: String? = null

        return Observable.fromIterable(deptList)
                .filter { currentDept: String ->
                    currentDept!="All"
                }
                .map { dept: String ->
                    val id: Int = deptList.indexOf(dept)
                    department = dept
                    yearList[id]
                }.flatMap { currentDepartmentYearList ->
                    getStudentDataSnapShot(department!!, currentDepartmentYearList)
                }
    }


    private fun getStudentDataSnapShot(department: String, yearList: List<String>) =
            Observable.fromIterable(yearList)
                    .subscribeOn(SchedulersFacade.io())
                    .filter { currentYear ->
                        currentYear!="All"
                    }
                    .flatMap { year: String ->
                        val path = "student/$department/$year"
                        RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(path)).toObservable()
                                .map { data ->
                                    data
                                }

                    }.flatMap { data ->
                        Observable.fromIterable(data.children)
                                .flatMap { singleStudent: DataSnapshot ->
                                    val fbStudent = singleStudent.getValue(FBStudent::class.java)
                                    val student = Student(singleStudent.key.toString(), fbStudent!!)
                                    Observable.just(student)
                                }.toList().toObservable()
                    }

    fun getFBStudentProfile(fbUserId: String): Maybe<Student> =
            ProfileBuilderUtil.getUserIndex(fbUserId)
                    .flatMap { userLocation ->
                        RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(userLocation))
                                .flatMap { data ->
                                    val fbStudent = data.getValue(FBStudent::class.java)
                                    val student = Student(data.key!!, fbStudent!!)
                                    Maybe.just(student)
                                }
                    }


}


















