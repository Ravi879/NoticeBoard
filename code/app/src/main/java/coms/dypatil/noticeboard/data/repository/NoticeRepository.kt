package coms.dypatil.noticeboard.data.repository

import com.google.android.gms.tasks.Task
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBNotice
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.domain.interactor.NoticeUseCase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

object NoticeRepository {

    private val noticeUseCase = NoticeUseCase(App.fDatabase, App.preference, App.noticeDataProvider)
    private val dataProvider = App.noticeDataProvider


    //=======================================================================
    //following operations are only access by "faculty" user, not by student user
    fun sendNotice(fbNotice: FBNotice): Maybe<String> {
        val noticeId = noticeUseCase.sendNotice(fbNotice)
        return noticeUseCase.addNoticeIdToFacultyProfile(noticeId)
    }

    fun updateNotice(fbNotice: FBNotice, noticeId: String): Maybe<String> = noticeUseCase.sendNotice(fbNotice, noticeId)

    //Local SQLite
    fun getAllNoticeByIssuerId(userId: String): Flowable<List<Notice>> =
            dataProvider.getAllNoticeByIssuerId(userId)

    fun getExpiredNoticeByIssuerId(userId: String): Flowable<List<Notice>> =
            dataProvider.getExpiredNoticeById(userId)

    fun getNoticeByIssuerId(userId: String): Flowable<List<Notice>> =
            dataProvider.getNoticeByIssuerId(userId)


    //=======================================================================
    //getting notice

    //----------------------------------------------------------------------
    //From Firebase
    //----------------------------------------------------------------------
    fun getFBFacultyNotice(): Single<List<Notice>> {
        val noticeIdList = noticeUseCase.getFacultyNoticeIds()
        return noticeUseCase.getFacultyNoticeList(noticeIdList)
    }

    fun getFBALLData(): Single<List<Notice>> = noticeUseCase.getFBAllNotice()

    //----------------------------------------------------------------------
    //From Local SQLite db
    //----------------------------------------------------------------------
    //operations for Ongoing notice
    fun getAllNotice(): Flowable<List<Notice>> = dataProvider.getAllNotice()

    fun getDeptNotice(filterDept: String): Flowable<List<Notice>> = dataProvider.getDeptNotice(filterDept)

    fun getDesiNotice(filterDesi: String): Flowable<List<Notice>> = dataProvider.getDesiNotice(filterDesi)

    fun getDeptDesiNotice(filterDept: String, filterDesi: String, currentMillis: Long): Flowable<List<Notice>> =
            dataProvider.getDeptDesiNotice(filterDept, filterDesi, currentMillis)


    //----------------------------------------------------------------------
    //From Local SQLite db
    //----------------------------------------------------------------------
    //operations for Expired notice
    fun getAllExpireNotice(): Flowable<List<Notice>> = dataProvider.getAllExpireNotice()

    fun getDeptExpireNotice(filterDept: String): Flowable<List<Notice>> = dataProvider.getDeptExpireNotice(filterDept)

    fun getDesiExpireNotice(filterDesi: String): Flowable<List<Notice>> = dataProvider.getDesiExpireNotice(filterDesi)

    fun getDeptDesiExpireNotice(filterDept: String, filterDesi: String): Flowable<List<Notice>> =
            dataProvider.getDeptDesiExpireNotice(filterDept, filterDesi)


    //=======================================================================
    //Insert notice
    fun insertNoticeData(data: List<Notice>): Single<List<Long>?> = noticeUseCase.insertNoticeData(data)

    fun insertNoticeData(data: Notice): Single<Long?> = noticeUseCase.insertNotice(data)


    //=======================================================================
    //Update notice is read("open") or not read("close") by user
    fun setIsNoticeOpen(fbNoticeId: String, isOpen: String): Single<Int> = Single.just(dataProvider.setIsNoticeRead(fbNoticeId, isOpen))

    //=====================================================================================
    //Delete notice

    fun dropTable(): Single<Int> = noticeUseCase.dropTable()

    fun deleteFBNotice(fbNoticeId: String): Maybe<Task<Void>> = noticeUseCase.deleteNotice(fbNoticeId)

    fun deleteNoticeById(fbNoticeId: String): Int = dataProvider.deleteNoticeById(fbNoticeId)

    fun deleteNoticeByDepartment(department: String) = dataProvider.deleteNoticeByDept(department)

}

