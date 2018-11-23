package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.util.FileUtil
import java.io.File

class HomeVM(application: Application) : AndroidViewModel(application) {

    var currentFragmentTag: String? = null

    fun getProfilePic(): File {
        val userType = App.preference.spUserType
        val picName = App.preference.spUserProfilePicName
        return FileUtil.getProfilePicFile(userType, picName)
    }

    fun getUserName(): String = App.preference.spUserName

    fun getUserDepartment(): String = App.preference.spUserDepartment

}
