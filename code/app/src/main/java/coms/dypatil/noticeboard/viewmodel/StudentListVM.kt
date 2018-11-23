package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.data.repository.StudentRepository
import coms.dypatil.noticeboard.util.AdapterUtil
import coms.dypatil.noticeboard.util.SpnListUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.plusAssign
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable

class StudentListVM(application: Application) : AndroidViewModel(application) {

    val studentList: MutableLiveData<ArrayList<Student>> = MutableLiveData()

    var spnDeptList: List<String> = SpnListUtil.getStudentFilterDept()
    var spnYearList: List<List<String>> = SpnListUtil.getStudentFilterYear()

    lateinit var mFilterYear: String
    var mFilterYearIndex = 0
    var mFilterDeptIndex: MutableLiveData<Int> = MutableLiveData()

    private var disposable: Disposable? = null

    val response = MutableLiveData<Response>()

    fun getAllStudent() {
        val studentList: Flowable<List<Student>>? = StudentRepository.getAllStudent()
        updateStudentList(studentList)
    }


    fun getStudentData() {
        val filterDept: String = spnDeptList[mFilterDeptIndex.value!!]
        val filterYear: String = mFilterYear
        val newList: Flowable<List<Student>>? = when {
            filterDept!="All" && filterYear=="All" -> StudentRepository.getStudentByDept(filterDept)
            filterDept=="All" && filterYear!="All" -> StudentRepository.getStudentByYear(filterYear)
            filterDept!="All" && filterYear!="All" -> StudentRepository.getStudentByDeptYear(filterDept, filterYear)
            else -> StudentRepository.getAllStudent()
        }
        updateStudentList(newList)
    }

    private fun updateStudentList(newList: Flowable<List<Student>>?) {

        newList?.apply {
            subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .doOnSubscribe { response.value = Response.loading() }
                    .subscribe({ data: List<Student> ->
                        studentList.value = data as ArrayList<Student>
                        loadingSuccess()
                    }, { th ->
                        loadingUnsuccessful(th)
                    })
        }
    }

    fun getSpnYearAdapter(position: Int): ArrayAdapter<String> = AdapterUtil.getAdapter(spnYearList[position])

    fun getFBStudentData() {
        val filterDept = spnDeptList[mFilterDeptIndex.value!!]
        val filterYear = mFilterYear

        val deptList: List<String>
        val yearList: List<List<String>>

        when {
            filterDept!="All" && filterYear=="All" -> {
                deptList = listOf(filterDept)
                yearList = SpnListUtil.getStudentFilterYear()
            }
            filterDept=="All" && filterYear!="All" -> {
                deptList = SpnListUtil.getStudentFilterDept()
                yearList = listOf(listOf(filterYear))
            }
            filterDept!="All" && filterYear!="All" -> {
                deptList = listOf(filterDept)
                yearList = listOf(listOf(filterYear))
            }
            else -> {
                StudentRepository.getAllStudent()
                deptList = SpnListUtil.getStudentFilterDept()
                yearList = SpnListUtil.getStudentFilterYear()
            }
        }

        disposable = StudentRepository.getAllStudentFB(deptList, yearList)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe {
                    studentList.value = arrayListOf()
                    response.value = Response.loading()
                }
                .subscribe({ f: MutableList<Student> ->
                    studentList.plusAssign(f)
                }, { th ->
                    loadingUnsuccessful(th)
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