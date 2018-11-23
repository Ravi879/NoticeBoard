package coms.dypatil.noticeboard.util

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FileDownloadTask
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.util.validation.FormFieldValidation
import coms.dypatil.noticeboard.viewmodel.contracts.ProfileBuilderContract
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File

class ProfileBuilderUtil(private val contract: ProfileBuilderContract, private val isEditProfile: Boolean) {

    private val userType = App.preference.spUserType

    private var teachingDeptList: MutableList<String> = SpnListUtil.getTeachingDeptList()
    var tempDesiList: MutableList<String> = getTemporaryDesiList() as MutableList<String>

    var allDeptList = getDeptList()
    private var allDesiList = getDesiList()

    var yearList = SpnListUtil.getYearList()

    private var deptIndex = 0

    private fun getDeptList(): List<String> {
        val deptList = SpnListUtil.getAllDeptList()
        return if (isEditProfile && userType=="student") {
            teachingDeptList
        } else {
            deptList
        }
    }

    private fun getTemporaryDesiList(): List<String> {
        val desiList = SpnListUtil.getTempDesiList()

        return if (isEditProfile) {
            if (userType=="faculty")
                desiList.dropLast(1)
            else
                listOf("Student")
        } else {
            desiList
        }
    }

    private fun getDesiList(): List<String> {
        val desiList = SpnListUtil.getAllDesiList()
        return if (isEditProfile) {
            if (userType=="faculty") {
                val teachingDesi = desiList[0]
                desiList[0] = teachingDesi.substring(0, teachingDesi.lastIndexOf(":"))
                desiList
            } else {
                listOf("Student")
            }
        } else {
            desiList
        }
    }

    fun deptObserver(): Observer<Int> =
            Observer { position: Int? ->
                deptIndex = position!!
                changeSpnDesignation(position)
            }

    fun desiObserver(): Observer<String> =
            Observer { designation: String? ->
                changeSpnYear(designation!!)
            }


    private fun changeSpnDesignation(position: Int) {
        if (isEditProfile && userType=="student") {
            val adapter = AdapterUtil.getAdapter(listOf("Student"))
            contract.setDesignationAdapter(adapter)
            return
        }

        tempDesiList = when {
            position < teachingDeptList.size ->
                allDesiList[0].getList(':')
            else ->
                allDesiList[position - teachingDeptList.size + 1].getList(':')
        }
        val adapter: ArrayAdapter<String> = AdapterUtil.getAdapter(tempDesiList)
        contract.setDesignationAdapter(adapter)
        hideYearSpn()
    }

    private fun changeSpnYear(designation: String) {
        when (designation) {
            "Student" -> {
                showYearSpn()
                val adapter: ArrayAdapter<String> = AdapterUtil.getAdapter(yearList[deptIndex].getList(':'))
                contract.setYearAdapter(adapter)
            }
            else -> hideYearSpn()
        }
    }

    private fun showYearSpn() {
        contract.setSpnYearVisibility(LinearLayout.VISIBLE)
    }

    private fun hideYearSpn() {
        contract.setSpnYearVisibility(LinearLayout.GONE)
    }

    fun getUserDesiIndex(designation: String): Int {
        val list: List<String> = if (deptIndex < teachingDeptList.size) {
            tempDesiList
        } else {
            allDesiList[allDeptList.size - 1 - deptIndex].split(":")
        }
        contract.setDesignationAdapter(AdapterUtil.getAdapter(list))

        return list.indexOf(designation)
    }

    fun getUserYearIndex(designation: String, year: String): Int = if (designation=="Student") {
        showYearSpn()
        val yearList = yearList[deptIndex].split(":")
        contract.setYearAdapter(AdapterUtil.getAdapter(yearList))
        val index = yearList.indexOf(year)
        index
    } else {
        hideYearSpn()
        0
    }

    fun getUserDeptIndex(department: String): Int = allDeptList.indexOf(department)

    fun isValidDetails(userRegistration: UserRegistration): Boolean =
            when {
                !FormFieldValidation.isNameValid(userRegistration.firstName!!) -> {
                    val msg = if (userRegistration.firstName!!.length < 4)
                        "First name must be at least 4 character."
                    else
                        "Enter Character only."
                    contract.invalidCredential("first_name", msg)
                    false
                }
                !FormFieldValidation.isNameValid(userRegistration.lastName!!) -> {
                    val msg = if (userRegistration.lastName!!.length < 4)
                        "Last name must be at least 4 character."
                    else
                        "Enter Character only."
                    contract.invalidCredential("last_name", msg)
                    false
                }
                !FormFieldValidation.isEmailValid(userRegistration.email!!) -> {
                    contract.invalidCredential("email", "Please enter valid email address.")
                    false
                }
                userRegistration.dob=="" -> {
                    contract.invalidCredential("dob", "Please select the date of birth.")
                    false
                }
                !FormFieldValidation.isPhoneNoValid(userRegistration.phoneNo!!) -> {
                    val msg = if (userRegistration.phoneNo!!.length < 4)
                        "Mobile No must be at least 10 digits."
                    else
                        "Enter digits only."
                    contract.invalidCredential("phone_no", msg)
                    false
                }
                !FormFieldValidation.isPasswordValid(userRegistration.password!!) -> {
                    val msg = if (userRegistration.password!!.length < 6)
                        "Password must be at least 6 character."
                    else
                        "Password must contain 1 Special character[@#$%^&+=], Capital latter and Digit."
                    contract.invalidCredential("password", msg)
                    false
                }
                else -> true


            }


    companion object {

        fun getProfilePicNameFromUrl(profilePicUrl: String): String = App.fStorage.getReferenceFromUrl(profilePicUrl).name

        fun getUserLocation(userType: String, fbUserId: String, department: String, designation: String, year: String = ""): String =
                if (userType=="faculty")
                    "faculty/$department/$designation/$fbUserId"
                else
                    "student/$department/$year/$fbUserId"

        fun getFacultyNoticeReference(fbUserId: String, department: String, designation: String): String =
                "${getUserLocation("faculty", fbUserId, department, designation)}/notice"

        fun getNoticeReference(department: String, designation: String): String = "notice/$department/$designation"

        fun getProfilePicPath(userType: String, userId: String, mimeType: String): String = "$userType/$userId$mimeType"

        fun getUserIndex(userId: String): Maybe<String> {
            return RxFirebaseDatabase.observeSingleValueEvent(App.fDatabase.getReference("userIndex/$userId"))
                    .flatMap { data: DataSnapshot ->
                        Maybe.just(data.value as String)
                    }
        }

        fun downloadUserPic(userType: String, profilePicUrl: String): Single<FileDownloadTask.TaskSnapshot> {
            val storageReference = App.fStorage.getReferenceFromUrl(profilePicUrl)
            val profilePicName: String = storageReference.name

            //store photo into sdcard as  "NoticeBoard/photo_name.extension"
            val appFolder: File = Environment.getExternalStoragePublicDirectory("NoticeBoard/$userType")

            //checking the directory is exists or not
            if (!appFolder.exists())
                appFolder.mkdirs()

            val profilePicFile = File(appFolder, profilePicName)

            return RxFirebaseStorage.getFile(storageReference, profilePicFile)

        }

        fun getImagePickerIntent(): Intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        }

    }

}