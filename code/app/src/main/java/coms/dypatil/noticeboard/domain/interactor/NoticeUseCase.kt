package coms.dypatil.noticeboard.domain.interactor

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBNotice
import coms.dypatil.noticeboard.data.datastore.persistence.dao.NoticeDao
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.SpnListUtil
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single


class NoticeUseCase(val fDatabase: FirebaseDatabase, val preference: Preference,
        private val dataProvider: NoticeDao) {

    fun sendNotice(fbNotice: FBNotice, fbNoticeId: String? = null): Maybe<String> {

        val noticeReference = "notice/${preference.spUserDepartment}/${preference.spUserDesignation}"
        val noticeId: String? = fbNoticeId ?: fDatabase.getReference(noticeReference).push().key

        return RxFirebaseDatabase.setValue(fDatabase.getReference(noticeReference).child(noticeId!!), fbNotice)
                .andThen(Maybe.just(noticeId))
    }

    fun addNoticeIdToFacultyProfile(noticeId: Maybe<String>): Maybe<String> = noticeId.flatMap { nId ->
        val facultyNoticeField = ProfileBuilderUtil.getFacultyNoticeReference(preference.spUserId,
                preference.spUserDepartment, preference.spUserDesignation) + "/" + nId
        RxFirebaseDatabase.setValue(fDatabase.getReference(facultyNoticeField), "").andThen(
                Maybe.just("Completed")
        )
    }


    fun getFacultyNoticeIds(): Single<ArrayList<String>> {
        val facultyNoticeField = ProfileBuilderUtil.getFacultyNoticeReference(preference.spUserId,
                preference.spUserDepartment, preference.spUserDesignation)
        return RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(facultyNoticeField)).map { data: DataSnapshot? ->
            val noticeIdList = arrayListOf<String>()
            if (data==null)
                return@map noticeIdList
            for (noticeId in data.children) {
                noticeIdList.add(noticeId.key!!)
            }
            noticeIdList
        }.toSingle()
    }


    fun getFacultyNoticeList(noticeIdList: Single<ArrayList<String>>): Single<List<Notice>> {
        val noticeReference = ProfileBuilderUtil.getNoticeReference(preference.spUserDepartment, preference.spUserDesignation)
        return noticeIdList.flatMap { idList ->
            Observable.fromIterable(idList)
                    .flatMap { id ->
                        val reference = fDatabase.getReference(noticeReference).child(id)
                        RxFirebaseDatabase.observeSingleValueEvent(reference).toObservable()
                                .flatMap { data ->
                                    val fbNotice: FBNotice? = data.getValue(FBNotice::class.java)
                                    val notice = FBNotice.getNotice(fbNotice!!)
                                    notice.fbNoticeId = data.key!!
                                    notice.issuerName = preference.spUserName
                                    notice.department = preference.spUserDepartment
                                    notice.designation = preference.spUserDesignation
                                    Observable.just(notice)
                                }
                    }.toList()

        }

    }

    fun getFBAllNotice(): Single<List<Notice>> {
        val deptList: List<String> = SpnListUtil.getNoticeDeptList()
        val desiList: List<List<String>> = SpnListUtil.getNoticeDesiList()
        var department: String? = null
        val currentAllDesiNoticeList: MutableList<Notice> = mutableListOf()
        var tempList: MutableList<Notice> = mutableListOf()
        return Observable.fromIterable(deptList)
                .filter { currentDept: String ->
                    currentDept!="All"
                }
                .map { dept: String ->
                    val deptIndex = deptList.indexOf(dept)
                    department = dept
                    desiList[deptIndex]
                }.concatMap { currentDepartmentDesiList: List<String> ->
                    getNoticeDataSnapShot(department!!, currentDepartmentDesiList)
                }.flatMapIterable { singleDeptWithTheirDesi: MutableList<Map<String, DataSnapshot>> ->
                    singleDeptWithTheirDesi
                }.concatMap { singleDesiNoticeMap: Map<String, DataSnapshot> ->
                    getSingleDesiNoticeList(department!!, singleDesiNoticeMap)
                }.flatMap { singleDesiNoticeList: MutableList<Notice> ->
                    tempList = arrayListOf()
                    Observable.fromIterable(singleDesiNoticeList)
                }.concatMap { notice: Notice ->
                    getNoticeIssuerDetails(tempList, notice)
                }.toList()
                .flatMap { singleDesiNoticeList: MutableList<Notice> ->
                    currentAllDesiNoticeList.addAll(singleDesiNoticeList)
                    Single.just(currentAllDesiNoticeList)
                }
    }

    fun deleteNotice(fbNoticeId: String): Maybe<Task<Void>> {
        val department = preference.spUserDepartment
        val designation = preference.spUserDesignation
        val noticeReference = "notice/$department/$designation/$fbNoticeId"
        return Maybe.just(noticeReference)
                .map { reference ->
                    fDatabase.getReference(reference).removeValue()
                }.flatMap {
                    val reference = ProfileBuilderUtil.getFacultyNoticeReference(preference.spUserId, department, designation)
                    Maybe.just(fDatabase.getReference(reference).child(fbNoticeId).removeValue())
                }


    }

    fun insertNoticeData(list: List<Notice>): Single<List<Long>?> =
            Single.just(dataProvider.insertNoticeData(list))


    fun insertNotice(list: Notice): Single<Long?> =
            Single.just(dataProvider.insertNotice(list))


    fun dropTable(): Single<Int> = Single.just(dataProvider.deleteAll())

    private fun getNoticeIssuerDetails(tempList: MutableList<Notice>, notice: Notice): Observable<Notice> {

        if (isUserAlreadyFetch(tempList, notice))
            return Observable.just(notice)

        return ProfileBuilderUtil.getUserIndex(notice.issuerId!!)
                .flatMap { userLocation ->
                    getIssuerProfileField(userLocation, "name")
                            .map { userName ->
                                notice.issuerName = userName
                                notice
                            }
                }.toObservable()
    }


    private fun getIssuerProfileField(userLocation: String, profileField: String): Maybe<String?> =
            RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference("$userLocation/$profileField"))
                    .map { userName: DataSnapshot ->
                        userName.value as String?
                    }


    private fun isUserAlreadyFetch(tempList: MutableList<Notice>, notice: Notice): Boolean {
        for (n in tempList) {
            if (n.issuerName==null) {
                return false
            } else {
                if (n.issuerId==notice.issuerId) {
                    notice.issuerName = n.issuerName
                    return true
                }
            }
        }
        return false
    }


    private fun getNoticeDataSnapShot(department: String, desiList: List<String>): Observable<MutableList<Map<String, DataSnapshot>>> =
            Observable.fromIterable(desiList)
                    .subscribeOn(SchedulersFacade.io())
                    .filter { currentDesi ->
                        currentDesi!="All"
                    }
                    .flatMap { desi: String ->
                        val path = ProfileBuilderUtil.getNoticeReference(department, desi)
                        RxFirebaseDatabase.observeSingleValueEvent(fDatabase.getReference(path).limitToLast(2))
                                .map { data ->
                                    mapOf(Pair(desi, data))
                                }.toObservable()
                    }.toList().toObservable()

    private fun getSingleDesiNoticeList(department: String, singleDesiNoticeMap: Map<String, DataSnapshot>): Observable<MutableList<Notice>> {
        val currentDesi = singleDesiNoticeMap.keys.first()
        val snapshot = singleDesiNoticeMap[currentDesi]
        return Observable.fromIterable(snapshot!!.children)
                .flatMap { singleNotice: DataSnapshot ->
                    val fbNotice = singleNotice.getValue(FBNotice::class.java)
                    val notice = FBNotice.getNotice(fbNotice!!)
                    notice.fbNoticeId = singleNotice.key!!
                    notice.department = department
                    notice.designation = currentDesi
                    Observable.just(notice)
                }.toList().toObservable()
    }

    fun getFBFilterData(filterDept: String, filterDesi: String): Maybe<DataSnapshot> =
            RxFirebaseDatabase
                    .observeSingleValueEvent(fDatabase.reference.child("notice").child(filterDept).child(filterDesi)) { data: DataSnapshot ->
                        val noticeArray = arrayListOf<Notice>()
                        for (dataSnapShot1 in data.children) {
                            val postDetails = dataSnapShot1.getValue(Notice::class.java)!!
                            postDetails.fbNoticeId = dataSnapShot1.key!!
                            postDetails.isOpen = "close"
                            noticeArray.add(postDetails)
                        }

                        data
                    }


}