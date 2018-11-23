package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBNotice
import coms.dypatil.noticeboard.data.repository.NoticeRepository
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.util.tag
import coms.dypatil.noticeboard.viewmodel.contracts.CreateNoticeContract
import io.reactivex.disposables.Disposable

class CreateNoticeVM(application: Application) : AndroidViewModel(application) {

    val fbNotice = FBNotice()
    lateinit var createNoticeContract: CreateNoticeContract

    var isUpdateNotice = false

    private var disposable: Disposable? = null

    val response = MutableLiveData<Response>()

    fun startSendingProcess(fbNotice: FBNotice) {
        if (!isValidDetails(fbNotice))
            return
        fbNotice.apply {
            issuerId = App.preference.spUserId
        }

        disposable = NoticeRepository.sendNotice(fbNotice)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({
                    sendingNoticeSuccess("Notice Sent")
                }, {
                    sendingNoticeUnsuccessful(it)
                })
    }


    fun updateNotice(fbNotice: FBNotice, fbNoticeId: String) {
        if (!isValidDetails(fbNotice))
            return

        disposable = NoticeRepository.updateNotice(fbNotice, fbNoticeId)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({
                    sendingNoticeSuccess(" Notice Updated ")
                }, {
                    sendingNoticeUnsuccessful(it, "Error occurred during updating the notice")
                })
    }


    private fun isValidDetails(fbNotice: FBNotice): Boolean =
            when {
                fbNotice.title!!.isEmpty() -> {
                    createNoticeContract.invalidCredential("title", "You must have to give the fbNotice title")
                    false
                }
                fbNotice.description!!.isEmpty() -> {
                    createNoticeContract.invalidCredential("description", "Please enter the fbNotice description")
                    false
                }
                fbNotice.lastDate==null -> {
                    createNoticeContract.invalidCredential("lastDate", "Select the last issueDate of your fbNotice")
                    false
                }
                else -> true
            }

    private fun sendingNoticeSuccess(msg: String) {
        response.value = Response.success(msg)
    }

    private fun sendingNoticeUnsuccessful(t: Throwable, msg: String = "Error occurred during sending the notice") {
        t.printStackTrace()
        Log.v(tag, " CreateNoticeVM Error...... " + t.toString())
        response.value = Response.error(t, msg)
    }

    override fun onCleared() {
        disposable?.apply { dispose() }
        super.onCleared()
    }


}