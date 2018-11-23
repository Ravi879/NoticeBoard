package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.repository.FacultyRepository
import coms.dypatil.noticeboard.util.AdapterUtil
import coms.dypatil.noticeboard.util.SpnListUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.plusAssign
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

class FacultyListVM(application: Application) : AndroidViewModel(application) {

    val facultyList: MutableLiveData<ArrayList<Faculty>> = MutableLiveData()

    var spnDeptList: List<String> = SpnListUtil.getFacultyFilterDeptList()
    var spnDesiList: List<List<String>> = SpnListUtil.getFacultyFilterDesiList()

    lateinit var mFilterDesi: String
    var mFilterDeptIndex: MutableLiveData<Int> = MutableLiveData()
    var mFilterDesiIndex = 0

    private var disposable: Disposable? = null

    val response = MutableLiveData<Response>()


    fun getLocalFacultyData() {
        val filterDept = spnDeptList[mFilterDeptIndex.value!!]
        val filterDesi = mFilterDesi
        val newList: Flowable<List<Faculty>>? = when {
            filterDept!="All" && filterDesi=="All" -> FacultyRepository.getFacultyByDept(filterDept)
            filterDept=="All" && filterDesi!="All" -> FacultyRepository.getFacultyByDesi(filterDesi)
            filterDept!="All" && filterDesi!="All" -> FacultyRepository.getFacultyByDeptDesi(filterDept, filterDesi)
            else -> FacultyRepository.getAllFaculty()
        }
        updateFacultyList(newList)
    }

    private fun updateFacultyList(newList: Flowable<List<Faculty>>?) {

        newList?.apply {
            subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .doOnSubscribe { response.value = Response.loading() }
                    .subscribe({ data: List<Faculty> ->
                        facultyList.value = data as ArrayList<Faculty>
                        loadingSuccess()
                    }, { th ->
                        loadingUnsuccessful(th)
                    })
        }
    }

    fun getSpnDesiAdapter(position: Int): ArrayAdapter<String> = AdapterUtil.getAdapter(spnDesiList[position])

    fun getFBFacultyData() {
        val filterDept: String = spnDeptList[mFilterDeptIndex.value!!]
        val filterDesi: String = mFilterDesi

        val deptList: List<String>
        val desiList: List<List<String>>

        when {
            filterDept!="All" && filterDesi=="All" -> {
                deptList = listOf(filterDept)
                desiList = listOf(spnDesiList[spnDeptList.indexOf(filterDept)])
            }
            filterDept=="All" && filterDesi!="All" -> {
                deptList = SpnListUtil.getFacultyFilterDeptList()
                desiList = listOf(listOf(filterDesi))
            }
            filterDept!="All" && filterDesi!="All" -> {
                deptList = listOf(filterDept)
                desiList = listOf(listOf(filterDesi))
            }
            else -> {
                FacultyRepository.getAllFaculty()
                deptList = SpnListUtil.getFacultyFilterDeptList()
                desiList = SpnListUtil.getFacultyFilterDesiList()
            }
        }

        disposable = FacultyRepository.getFBAllFaculty(deptList, desiList)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe {
                    facultyList.value = arrayListOf()
                    response.value = Response.loading()
                }
                .subscribe({ f: MutableList<Faculty> ->
                    facultyList.plusAssign(f)
                }, { th ->
                    th.printStackTrace()
                }, {
                    loadingSuccess()
                })

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