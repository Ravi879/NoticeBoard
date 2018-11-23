package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.repository.NoticeRepository
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import io.reactivex.disposables.Disposable

class NoticeDetailVM(application: Application) : AndroidViewModel(application) {

    val response = MutableLiveData<Response>()
    val fbUserId = App.preference.spUserId
    private var disposable: Disposable? = null

    fun deleteNotice(fbNoticeId: String) {
        disposable = NoticeRepository.deleteFBNotice(fbNoticeId)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({ task ->
                    task.addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            NoticeRepository.deleteNoticeById(fbNoticeId)
                            deletingSuccess()
                        } else
                            deletingUnsuccessful(Throwable("Deletion task not completed"))
                    }
                }, { th ->
                    deletingUnsuccessful(th)
                })
    }

    private fun deletingSuccess(msg: String = "Notice deleted successfully") {
        response.value = Response.success(msg)
    }

    private fun deletingUnsuccessful(t: Throwable, msg: String = "Error occurred during deleting the notice") {
        response.value = Response.error(t, msg)
    }

    fun setNoticeIsOpen(fbNoticeId: String): Disposable =
            NoticeRepository.setIsNoticeOpen(fbNoticeId, "open")
                    .subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .subscribe({}, { th: Throwable ->
                        th.printStackTrace()
                    })


    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

}