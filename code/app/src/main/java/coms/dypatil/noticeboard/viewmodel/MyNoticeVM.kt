package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.data.repository.NoticeRepository
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class MyNoticeVM(application: Application) : AndroidViewModel(application) {


    var noticeList: MutableLiveData<ArrayList<Notice>> = MutableLiveData()
    var filterIndex: String = "All"

    private val userId = App.preference.spUserId

    private var disposable: Disposable? = null

    val response = MutableLiveData<Response>()

    fun getAllNotice() {
        val newList: Flowable<List<Notice>>? = NoticeRepository.getAllNoticeByIssuerId(userId)
        updateNoticeList(newList)
    }

    fun getNoticeData() {
        val newList: Flowable<List<Notice>> = when (filterIndex) {
            "Notice" -> NoticeRepository.getNoticeByIssuerId(userId)
            "ExpiredNotice" -> NoticeRepository.getExpiredNoticeByIssuerId(userId)
            else -> NoticeRepository.getAllNoticeByIssuerId(userId)
        }
        updateNoticeList(newList)
    }

    fun getFBAllNotice() {
        val noticeList: Flowable<List<Notice>> = NoticeRepository.getFBFacultyNotice().toFlowable()
                .flatMap { list ->
                    NoticeRepository.insertNoticeData(list).flatMap {
                        Single.just(list)
                    }.toFlowable()
                }
        updateNoticeList(noticeList, isUpdateList = false)
    }

    private fun updateNoticeList(newList: Flowable<List<Notice>>?, isUpdateList: Boolean = true) {
        newList?.apply {
            disposable = subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .doOnSubscribe { if (!isUpdateList) response.value = Response.loading() }
                    .subscribe({ data: List<Notice> ->
                        if (isUpdateList)
                            noticeList.value = data as ArrayList<Notice>
                        loadingSuccess()
                    }, { th ->
                        th.printStackTrace()
                        loadingUnsuccessful(th)
                    })
        }
    }

    private fun loadingSuccess(msg: String? = null) {
        response.value = Response.success(msg)
    }

    private fun loadingUnsuccessful(t: Throwable, msg: String = "Error occurred during loading faculty list") {
        response.value = Response.error(t, msg)
    }


    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}
