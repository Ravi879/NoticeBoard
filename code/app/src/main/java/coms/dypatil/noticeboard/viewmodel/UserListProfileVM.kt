package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.EmptyResultSetException
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.data.repository.FacultyRepository
import coms.dypatil.noticeboard.data.repository.StudentRepository
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.util.tag
import coms.dypatil.noticeboard.viewmodel.contracts.FacultyProfileContract
import io.reactivex.disposables.Disposable

class UserListProfileVM(application: Application) : AndroidViewModel(application) {

    var faculty: Faculty? = null

    var contract: FacultyProfileContract? = null

    val response = MutableLiveData<Response>()

    private var disposable: Disposable? = null

    fun getLocalFacultyProfile(fbUserId: String): Disposable =
            FacultyRepository.getFBFacultyById(fbUserId)
                    .subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .doOnSubscribe { response.value = Response.loading() }
                    .subscribe({ faculty: Faculty? ->
                        if (faculty!=null) {
                            showProfile(UserProfile(faculty))
                            loadingProfileSuccessful()
                        } else {
                            loadingProfileUnsuccessful(Throwable("Unable to find profile details in local sqlite db"), "Loading profile unsuccessful")
                        }
                    }, { th: Throwable? ->
                        if (th is EmptyResultSetException)
                            contract!!.profileNotFoundInRoom()
                        else
                            loadingProfileUnsuccessful(th!!)
                    }, {
                        contract!!.profileNotFoundInRoom()
                    })


    fun getFBFacultyProfile(fbUserId: String) {
        disposable = FacultyRepository.getFBFacultyProfile(fbUserId)
                .map { faculty ->
                    //store faculty in to sqlite db
                    FacultyRepository.insert(faculty)
                    faculty
                }.subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({ faculty: Faculty ->
                    showProfile(UserProfile(faculty))
                    loadingProfileSuccessful()
                }, { th ->
                    th.printStackTrace()
                    loadingProfileUnsuccessful(th)
                })

    }

    fun getLocalStudentProfile(fbUserId: String): Disposable = StudentRepository.getStudentById(fbUserId)
            .subscribeOn(SchedulersFacade.io())
            .observeOn(SchedulersFacade.ui())
            .doOnSubscribe { response.value = Response.loading() }
            .subscribe({ student: Student? ->
                if (student!=null) {
                    showProfile(UserProfile(student))
                    loadingProfileSuccessful()
                } else {
                    loadingProfileUnsuccessful(Throwable("Unable to find profile details in local sqlite db"), "Loading profile unsuccessful")
                }
            }, { th ->
                if (th is EmptyResultSetException)
                    contract!!.profileNotFoundInRoom()
                else
                    loadingProfileUnsuccessful(th!!)
            }, {
                contract!!.profileNotFoundInRoom()
            })


    fun getFBStudentProfile(fbUserId: String) {
        disposable = StudentRepository.getFBStudentProfile(fbUserId)
                .map { student ->
                    //store faculty in to sqlite db
                    StudentRepository.insert(student)
                    student
                }
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({ student: Student ->
                    showProfile(UserProfile(student))
                    loadingProfileSuccessful()
                }, { th: Throwable? ->
                    th!!.printStackTrace()
                    loadingProfileUnsuccessful(th)
                })
    }

    private fun loadingProfileSuccessful(msg: String = "Loading profile successfully") {
        response.value = Response.success(msg)
    }

    private fun loadingProfileUnsuccessful(t: Throwable, msg: String = "Error occurred during loading profile") {
        Log.v(tag, " CreateNoticeVM Error...... " + t.toString())
        response.value = Response.error(t, msg)
    }


    private fun showProfile(userProfile: UserProfile) = contract!!.showProfileDetails(userProfile)


    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }
}