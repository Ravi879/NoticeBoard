package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBFaculty
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBStudent
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.data.repository.FacultyRepository
import coms.dypatil.noticeboard.data.repository.LoginRepository
import coms.dypatil.noticeboard.data.repository.PreferenceRepository
import coms.dypatil.noticeboard.data.repository.StudentRepository
import coms.dypatil.noticeboard.domain.model.UserLogin
import coms.dypatil.noticeboard.domain.service.DownloadUserPicService
import coms.dypatil.noticeboard.util.getString
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.util.validation.FormFieldValidation
import coms.dypatil.noticeboard.viewmodel.contracts.LoginContract
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class LoginVM(application: Application) : AndroidViewModel(application) {

    var loginContract: LoginContract? = null

    private val disposables = CompositeDisposable()

    val response = MutableLiveData<Response>()

    fun isValidCredentials(email: String, password: String): Boolean {
        if (!FormFieldValidation.isEmailValid(email)) {
            loginContract!!.invalidCredential("email", "Please enter valid email address")
            return false
        }
        if (!FormFieldValidation.isPasswordValid(password)) {
            loginContract!!.invalidCredential("password", "Please enter the correct password")
            return false
        }
        return true
    }

    fun startLoginProcess(email: String, password: String) {
        if (!isValidCredentials(email, password))
            return

        disposables.add(LoginRepository.signIn(email, password)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe(
                        { userId: String? ->
                            LoginRepository.getProfileDetails(userId!!)
                                    .subscribeOn(SchedulersFacade.io())
                                    .observeOn(SchedulersFacade.ui())
                                    .subscribe({ userLogin: UserLogin? ->

                                        downloadProfilePic(getApplication(), userLogin!!)

                                        saveUserDetails(userLogin)

                                    }, { throwable ->
                                        loginUnSuccess(throwable)
                                    })
                        },
                        { throwable ->
                            loginUnSuccess(throwable, "Please Enter the Correct Email and Password")
                        }
                )

        )
    }

    private fun saveUserDetails(userLogin: UserLogin) =
            Single.merge(saveUserInDB(userLogin).subscribeOn(SchedulersFacade.io()).observeOn(SchedulersFacade.ui()),
                    saveUserInSPreference(userLogin).subscribeOn(SchedulersFacade.io()).observeOn(SchedulersFacade.ui()))
                    .toList()
                    .subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .subscribe({
                        if (it[0] > 0L && (it[1]==1L)) {
                            loginSuccess()
                        } else
                            loginUnSuccess(Throwable("Error during saving the profile in local storage"))

                    }, { throwable ->
                        loginUnSuccess(throwable)
                    })


    private fun saveUserInDB(userLogin: UserLogin): Single<Long> =
            if (userLogin.userType=="faculty") {
                val fbFaculty: FBFaculty? = userLogin.dataSnapshot!!.getValue(FBFaculty::class.java)
                val faculty = Faculty(userLogin.userId!!, fbFaculty!!)
                faculty.fbUserId = userLogin.userId!!
                Single.just(FacultyRepository.insert(faculty))
            } else {
                val fbStudent = userLogin.dataSnapshot!!.getValue(FBStudent::class.java)
                val student = Student(userLogin.userId!!, fbStudent!!)
                student.fbUserId = userLogin.userId!!
                Single.just(StudentRepository.insert(student))
            }

    private fun saveUserInSPreference(userLogin: UserLogin): Single<Long> =
            with(userLogin.dataSnapshot!!) {
                userLogin.apply {
                    name = getString("name")
                    department = getString("department")
                    designation = getString("designation")
                    year = getString("year")
                    profilePicUrl = getString("profilePicUrl")
                }.let { Single.just(PreferenceRepository.saveProfileDetails(it)) }
            }


    private fun loginSuccess() {
        App.preference.spLoadNoticeFromFb = true
        response.value = Response.success("Login successful")
    }

    private fun loginUnSuccess(t: Throwable, msg: String = "Login UnSuccessful") {
        response.value = Response.error(t, msg)
    }


    private fun downloadProfilePic(context: Context, userLogin: UserLogin) {
        val profilePicUrl = userLogin.dataSnapshot!!.getString("profilePicUrl")
        val intent = Intent(getApplication(), DownloadUserPicService::class.java).apply {
            putExtra(DownloadUserPicService.USER_TYPE, userLogin.userType)
            putExtra(DownloadUserPicService.PROFILE_PIC_URL, profilePicUrl)
        }

        context.startService(intent)
    }

    fun subscribeMessageTopic() {
        LoginRepository.subscribeFBMessageTopic()
    }

    override fun onCleared() {
        if (!disposables.isDisposed) disposables.dispose()
        super.onCleared()
    }

}