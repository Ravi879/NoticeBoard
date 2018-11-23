package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import coms.dypatil.noticeboard.data.repository.RegistrationRepository
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.viewmodel.contracts.RegisterContract
import io.reactivex.disposables.Disposable

class RegisterVM(application: Application) : AndroidViewModel(application) {

    lateinit var contract: RegisterContract

    var profileBuilderUtil: ProfileBuilderUtil? = null
    var deptIndex: MutableLiveData<Int> = MutableLiveData()
    var desiIndex: MutableLiveData<String> = MutableLiveData()
    var yearIndex: String = ""

    var profilePicUri: Uri? = null
    var profilePicMimeType: String? = null

    val response = MutableLiveData<Response>()

    private var disposable: Disposable? = null

    fun startSignUpProcess(userRegistration: UserRegistration) {
        if (userRegistration.profilePicUri==null) {
            contract.invalidCredential("profilePic", "Please select the profile picture")
            return
        }

        if (!profileBuilderUtil!!.isValidDetails(userRegistration))
            return

        disposable = RegistrationRepository.signUp(userRegistration)
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({ profilePicPath: UserRegistration? ->
                    RegistrationRepository.uploadProfileDetails(profilePicPath!!)
                            .subscribeOn(SchedulersFacade.io())
                            .observeOn(SchedulersFacade.ui())
                            .subscribe({ result ->
                                if (result.size==2 && result[0]=="uploaded" && result[1]=="uploaded") {

                                    registrationSuccess()
                                } else {
                                    registrationUnSuccess(Throwable("None"))
                                }
                            }, {
                                registrationUnSuccess(it)
                            })
                }, { th: Throwable ->
                    if (th is FirebaseAuthUserCollisionException) registrationUnSuccess(th, "Email id already Exists")
                    else registrationUnSuccess(th)
                })

    }

    fun getProfilePicBitmap(data: Uri): Bitmap {
        val imageFile: DocumentFile = DocumentFile.fromSingleUri(getApplication(), data)!!
        profilePicUri = data

        val mime = imageFile.type
        profilePicMimeType = "." + mime!!.substring(mime.lastIndexOf("/") + 1)

        return MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, data)
    }

    private fun registrationSuccess() {
        response.value = Response.success("Registration successful")
    }

    private fun registrationUnSuccess(t: Throwable, msg: String = "Registration UnSuccessful") {
        t.printStackTrace()
        response.value = Response.error(t, msg)
    }

    fun getImagePickerIntent(): Intent = ProfileBuilderUtil.getImagePickerIntent()

    fun getDeptObserver(): Observer<Int> = profileBuilderUtil!!.deptObserver()

    fun getDesiObserver(): Observer<String> = profileBuilderUtil!!.desiObserver()

    fun getAllDeptList(): List<String> = profileBuilderUtil!!.allDeptList

    fun getTempDesiList(): MutableList<String> = profileBuilderUtil!!.tempDesiList

    fun getYearList(): MutableList<String> = profileBuilderUtil!!.yearList


    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()

    }

}


