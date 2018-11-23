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
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.repository.EditProfileRepository
import coms.dypatil.noticeboard.data.repository.NoticeRepository
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.viewmodel.contracts.EditProfileContract
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

class EditProfileVM(application: Application) : AndroidViewModel(application) {

    lateinit var contract: EditProfileContract

    lateinit var userProfile: UserProfile
    var userRegistration = UserRegistration()

    var profileBuilderUtil: ProfileBuilderUtil? = null

    var isProfilePicChange = false

    private val preference = App.preference

    var deptIndex: MutableLiveData<Int> = MutableLiveData()
    var desiIndex: MutableLiveData<String> = MutableLiveData()
    var yearIndex: String = ""

    val response = MutableLiveData<Response>()

    var profilePicMimeType: String? = null

    var isDeptSpnChange = false
    var isDesiSpnChange = false
    var isYearSpnChange = false

    private var disposable: Disposable? = null

    fun getProfilePicBitmap(data: Uri): Bitmap {
        val imageFile: DocumentFile? = DocumentFile.fromSingleUri(getApplication(), data)
        userRegistration.profilePicUri = data
        val mime = imageFile!!.type
        profilePicMimeType = "." + mime!!.substring(mime.lastIndexOf("/") + 1)
        return MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, data)
    }

    fun validateProfileDetails(user: UserRegistration){
        user.fbUserId = preference.spUserId
        if (!profileBuilderUtil!!.isValidDetails(user))
            return

        if(preference.spUserType == "faculty" && (isDepartmentChange() || isDesignationChange())) {
            contract.showConfirmationDialog()
            return
        }

        startProfileUpdateProcess()
    }

    fun startProfileUpdateProcess() {

        disposable = Observable.fromIterable(getUpdateTaskList())
                .concatMap { task: Maybe<String> ->
                    task.flatMap { it ->
                        Maybe.just(it)
                    }.toObservable()
                }.toList()
                .subscribeOn(SchedulersFacade.io())
                .observeOn(SchedulersFacade.ui())
                .doOnSubscribe { response.value = Response.loading() }
                .subscribe({
                    updateSuccess()
                    NoticeRepository.deleteNoticeByDepartment(userProfile.department!!)
                    preference.spLoadNoticeFromFb = true
                }, { throwable ->
                    updateFailed(throwable, "Error occurred during updating the profile.")
                })
    }

    private fun getUpdateTaskList(): MutableList<Maybe<String>> {
        val updateTaskList: MutableList<Maybe<String>> = mutableListOf()
        var isProfileDetailsChanged = false

        if (userProfile.password!=userRegistration.password)
            updateTaskList.add(EditProfileRepository.updatePassword(userRegistration.password!!))

        if (isProfilePicChange)
            updateTaskList.add(EditProfileRepository.updateProfilePic(userRegistration.userType!!,
                    userProfile.profilePicUrl!!, userRegistration.profilePicUri!!))

        if (userProfile.email!=userRegistration.email) {
            updateTaskList.add(EditProfileRepository.updateEmail(userRegistration.email!!))
            isProfileDetailsChanged = true
        }

        if (isDepartmentChange() || isDesignationChange() || isYearChange()) {
            updateTaskList.add(EditProfileRepository.updateProfileLocation(userRegistration.department!!,
                    userRegistration.designation!!, userRegistration.year
                    ?: ""))
            isProfileDetailsChanged = true
        }

        if (isDobChange() || isPhoneNoChange() || isGenderChange() || isNameChange())
            isProfileDetailsChanged = true

        if (isProfileDetailsChanged)
            updateTaskList.add(EditProfileRepository.updateProfileDetails(userRegistration))

        return updateTaskList
    }

    private fun updateSuccess() {
        preference.spProfilePicSignature = System.currentTimeMillis().toString()
        App.isProfilePicChanged = true
        preference.spUserName = userRegistration.getUserName()
        preference.spUserDepartment = userRegistration.department!!
        response.value = Response.success("Profile Updated")
    }

    private fun updateFailed(t: Throwable, msg: String = "Registration UnSuccessful") {
        response.value = Response.error(t, msg)
    }

    private fun isNameChange(): Boolean = userProfile.name!=userRegistration.getUserName()

    private fun isDepartmentChange() = userProfile.department!=userRegistration.department

    private fun isDesignationChange() = userProfile.designation!=userRegistration.designation

    private fun isYearChange() = userProfile.year!=userRegistration.year

    private fun isDobChange() = userProfile.dob!=userRegistration.dob

    private fun isPhoneNoChange() = userProfile.phoneNo!=userRegistration.phoneNo

    private fun isGenderChange() = userProfile.gender!=userRegistration.gender


    fun getUserDeptIndex(department: String) = profileBuilderUtil!!.getUserDeptIndex(department)

    fun getUserDesiIndex(designation: String) = profileBuilderUtil!!.getUserDesiIndex(designation)

    fun getUserYearIndex(designation: String, year: String) = profileBuilderUtil!!.getUserYearIndex(designation, year)


    fun getImagePickerIntent(): Intent = ProfileBuilderUtil.getImagePickerIntent()

    fun getDeptObserver(): Observer<Int> = profileBuilderUtil!!.deptObserver()

    fun getDesiObserver(): Observer<String> = profileBuilderUtil!!.desiObserver()

    fun getAllDeptList(): List<String> = profileBuilderUtil!!.allDeptList

    fun getTempDesiList(): MutableList<String> = profileBuilderUtil!!.tempDesiList

    fun getYearList(): MutableList<String> = profileBuilderUtil!!.yearList


    override fun onCleared() {
        profileBuilderUtil = null
        disposable?.dispose()
        super.onCleared()
    }


}


