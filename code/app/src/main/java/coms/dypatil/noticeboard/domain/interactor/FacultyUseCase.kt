package coms.dypatil.noticeboard.domain.interactor

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBFaculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single


class FacultyUseCase(private val fDatabase: FirebaseDatabase) {

    fun dropTable(): Single<Int> = Single.just(App.facultyDataProvider.deleteAll())

    fun getAllFacultyFB(deptList: List<String>, desiList: List<List<String>>): Observable<MutableList<Faculty>> {
        var department: String? = null
        return Observable.fromIterable(deptList)
                .filter { currentDept: String ->
                    currentDept!="All"
                }.map { dept: String ->
                    val id = deptList.indexOf(dept)
                    department = dept
                    desiList[id]
                }.flatMap { currentDesiList ->
                    getFacultyDataSnapShot(department!!, currentDesiList)
                }
    }


    private fun getFacultyDataSnapShot(department: String, desiList: List<String>): Observable<MutableList<Faculty>> =
            Observable.fromIterable(desiList)
                    .subscribeOn(SchedulersFacade.io())
                    .filter { currentDesi ->
                        currentDesi!="All"
                    }
                    .flatMap { desi: String ->
                        val path = "faculty/$department/$desi"
                        RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(path)).toObservable()
                                .map { data ->
                                    data
                                }

                    }.flatMap { data ->
                        Observable.fromIterable(data.children)
                                .flatMap { singleFaculty: DataSnapshot ->
                                    val fbFaculty = singleFaculty.getValue(FBFaculty::class.java)
                                    val faculty = Faculty(singleFaculty.key.toString(), fbFaculty!!)
                                    Observable.just(faculty)
                                }.toList().toObservable()
                    }

    fun getFBFacultyProfile(fbUserId: String): Maybe<Faculty> =
            ProfileBuilderUtil.getUserIndex(fbUserId)
                    .flatMap { userLocation: String ->
                        RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(userLocation))
                                .flatMap { data: DataSnapshot ->
                                    val fbFaculty: FBFaculty? = data.getValue(FBFaculty::class.java)
                                    val faculty = Faculty(data.key!!, fbFaculty!!)
                                    Maybe.just(faculty)
                                }
                    }


}


















