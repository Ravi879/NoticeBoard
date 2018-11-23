package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.data.repository.*
import coms.dypatil.noticeboard.util.AdapterUtil.Companion.getAdapter
import coms.dypatil.noticeboard.util.SpnListUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.viewmodel.contracts.HomeContract
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class NoticeVM(application: Application) : AndroidViewModel(application) {

    var contract: HomeContract? = null

    var spnDeptList: List<String> = SpnListUtil.getNoticeDeptList()
    var spnDesiList: List<List<String>> = SpnListUtil.getNoticeDesiList()

    var mFilterDeptIndex: MutableLiveData<Int> = MutableLiveData()
    lateinit var mFilterDesi: String
    var mFilterDesiIndex = 0


    var mExpireFilterDeptIndex: MutableLiveData<Int> = MutableLiveData()
    var mExpireFilterDesiIndex = 0
    lateinit var mExpireFilterDesi: String

    var IS_NOTICE_FRAGMENT_VISIBLE: Boolean = false
    var IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE: Boolean = false


    var FLAG_APPLY_NOTICE_FILTER: MutableLiveData<Int> = MutableLiveData()
    var FLAG_APPLY_EXPIRE_NOTICE_FILTER: MutableLiveData<Int> = MutableLiveData()

    var noticeList: MutableLiveData<ArrayList<Notice>> = MutableLiveData()
    var noticeExpireList: MutableLiveData<ArrayList<Notice>> = MutableLiveData()

    private var disposable: Disposable? = null

    val response = MutableLiveData<Response>()

    fun changeSpnDesignation(position: Int): ArrayAdapter<String> = getAdapter(spnDesiList[position])

    fun loadLocalAllNotice() {
        val newList: Flowable<List<Notice>>? = NoticeRepository.getAllNotice()
        updateNoticeList(newList, "notice")
    }

    fun loadFilterLocalNoticeData() {
        val filterDept = spnDeptList[mFilterDeptIndex.value!!]
        val filterDesi = mFilterDesi
        val newList: Flowable<List<Notice>>? = when {
            filterDept!="All" && filterDesi=="All" -> NoticeRepository.getDeptNotice(filterDept)
            filterDept=="All" && filterDesi!="All" -> NoticeRepository.getDesiNotice(filterDesi)
            filterDept!="All" && filterDesi!="All" -> NoticeRepository.getDeptDesiNotice(filterDept, filterDesi, System.currentTimeMillis())
            else -> NoticeRepository.getAllNotice()
        }
        updateNoticeList(newList, "notice")
    }


    fun loadLocalAllExpireNotice() {
        val newList = NoticeRepository.getAllExpireNotice()
        updateNoticeList(newList, "expire")
    }

    fun loadFilterLocalExpireNoticeData() {
        val filterDept = spnDeptList[mExpireFilterDeptIndex.value!!]
        val filterDesi = mExpireFilterDesi
        val newList: Flowable<List<Notice>>? = when {
            filterDept!="All" && filterDesi=="All" -> NoticeRepository.getDeptExpireNotice(filterDept)
            filterDept=="All" && filterDesi!="All" -> NoticeRepository.getDesiExpireNotice(filterDesi)
            filterDept!="All" && filterDesi!="All" -> NoticeRepository.getDeptDesiExpireNotice(filterDept, filterDesi)
            else -> NoticeRepository.getAllExpireNotice()
        }

        updateNoticeList(newList, "expire")
    }

    private fun updateNoticeList(newList: Flowable<List<Notice>>?, type: String) {
        newList?.apply {
            disposable = subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .doOnSubscribe { response.value = Response.loading() }
                    .subscribe({ data ->
                        if (type=="notice")
                            noticeList.value = data as ArrayList<Notice>
                        else if (type=="expire")
                            noticeExpireList.value = data as ArrayList<Notice>
                        loadingSuccess()
                    }, { th: Throwable ->
                        th.printStackTrace()
                        loadingUnsuccessful(th)
                    })
        }
    }

    fun loadFBNotice(): Disposable = NoticeRepository.getFBALLData()
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .doOnSubscribe { response.value = Response.loading() }
            .subscribe({ list: List<Notice> ->
                NoticeRepository.insertNoticeData(list)
                        .subscribeOn(SchedulersFacade.io())
                        .observeOn(SchedulersFacade.ui())
                        .subscribe({
                            loadingSuccess()
                        }, { th: Throwable ->
                            th.printStackTrace()
                            loadingUnsuccessful(th)
                        })
            }, { th: Throwable ->
                th.printStackTrace()
                loadingUnsuccessful(th)
            })


    //============================================            Log Out            ============================================
    fun onLogOut() {
        LoginRepository.unSubscribeFBMessageTopic()
        dropTables()
        deleteFromPreferences()
        LoginRepository.getUserFBLogOut()
        contract!!.startLoginActivity()
    }

    private fun deleteFromPreferences() {
        PreferenceRepository.deleteUserDetails()
    }

    private fun dropTables(): Disposable = Single.merge(NoticeRepository.dropTable().subscribeOn(SchedulersFacade.io()),
            FacultyRepository.dropTable().subscribeOn(SchedulersFacade.io()),
            StudentRepository.dropTable().subscribeOn(SchedulersFacade.io()))
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .doOnSubscribe { response.value = Response.loading() }
            .subscribe({},
                    { th ->
                        th.printStackTrace()
                        loadingUnsuccessful(th, "Error occurred during deleting the sqlite table")
                    }, {
                loadingSuccess("Log out Successfully")
            })


    private fun loadingSuccess(msg: String? = null) {
        response.value = Response.success(msg)
    }

    private fun loadingUnsuccessful(t: Throwable, msg: String = "Error occurred during loading notice") {
        t.printStackTrace()
        response.value = Response.error(t, msg)
    }

    fun isFabVisible(): Boolean = App.preference.spUserType=="faculty"



    override fun onCleared() {
        contract = null
        disposable?.dispose()
        super.onCleared()
    }


}