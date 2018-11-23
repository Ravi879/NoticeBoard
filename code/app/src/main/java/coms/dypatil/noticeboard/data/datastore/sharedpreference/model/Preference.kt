package coms.dypatil.noticeboard.data.datastore.sharedpreference.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class Preference(applicationContext: Context) {

    companion object {
        const val REMOTE_CONFIG_VAR = "RemoteVariables"
        const val USER_DETAILS = "User"

        const val CONFIG_DEPARTMENT = "CONFIG_DEPARTMENT"
        const val CONFIG_DESIGNATION = "CONFIG_DESIGNATION"
        const val CONFIG_YEAR = "CONFIG_YEAR"

        const val USER_TYPE = "userType"

        //user profile details
        const val ID = "firebase_uId"
        const val USER_NAME = "user_name"
        const val EMAIL = "email"
        const val DEPARTMENT = "department"
        const val DESIGNATION = "designation"
        const val PASSWORD = "password"
        const val PROFILE_PIC_NAME = "profilePicName"

        const val YEAR = "year"


        const val LOAD_FB_NOTICE = "fbNotice"

        const val IS_PROFILE_PIC_CHANGE = "profilePicChanged"



        const val NID = "_id_"
    }

    private val remotePrefs: SharedPreferences = applicationContext.getSharedPreferences(REMOTE_CONFIG_VAR, Context.MODE_PRIVATE)
    private val userPrefs: SharedPreferences = applicationContext.getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE)


    //for FirebaseRemote config
    var spRemoteDepartements: String
        get() = remotePrefs.getString(CONFIG_DEPARTMENT, "Architecture:Civil:Chemical:Cse:Mechanical:Electrical,Office,Library,Tpo")
        set(value) = remotePrefs.edit {
            putString(CONFIG_DEPARTMENT, value)
        }
    var spRemoteDesignation: String
        get() = remotePrefs.getString(CONFIG_DESIGNATION, "Hod:Teacher:Lab Assistant:Student,Architecture:Civil:Chemical:Cse:Mechanical:Electrical, Manager:Supervisor, Head:Manager:Coordinator")
        set(value) = remotePrefs.edit {
            putString(CONFIG_DESIGNATION, value)
        }
    var spRemoteYear: String
        get() = remotePrefs.getString(CONFIG_YEAR, "FE:SE:TE:BE:ME,FE:SE:TE:BE,FE:SE:TE:BE,FE:SE:TE:BE,FE:SE:TE:BE,FE:SE:TE:BE")
        set(value) = remotePrefs.edit {
            putString(CONFIG_YEAR, value)
        }


    //for user
    //usertype is either "faculty" or "student"
    var spUserType: String
        get() = userPrefs.getString(USER_TYPE, "")
        set(value) = userPrefs.edit {
            putString(USER_TYPE, value)
        }
    var spUserId: String
        get() = userPrefs.getString(ID, "")
        set(value) = userPrefs.edit {
            putString(ID, value)
        }
    var spUserName: String
        get() = userPrefs.getString(USER_NAME, "")
        set(value) = userPrefs.edit {
            putString(USER_NAME, value)
        }
    var spUserEmail: String
        get() = userPrefs.getString(EMAIL, "")
        set(value) = userPrefs.edit {
            putString(EMAIL, value)
        }
    var spUserDepartment: String
        get() = userPrefs.getString(DEPARTMENT, "")
        set(value) = userPrefs.edit {
            putString(DEPARTMENT, value)
        }
    var spUserDesignation: String
        get() = userPrefs.getString(DESIGNATION, "")
        set(value) = userPrefs.edit {
            putString(DESIGNATION, value)
        }
    var spUserProfilePicName: String
        get() = userPrefs.getString(PROFILE_PIC_NAME, "")
        set(value) = userPrefs.edit {
            putString(PROFILE_PIC_NAME, value)
        }
    var spUserPassword: String
        get() = userPrefs.getString(PASSWORD, "")
        set(value) = userPrefs.edit {
            putString(PASSWORD, value)
        }
    //in case of user type student than this will save the year of the student
    var spUserYear: String
        get() = userPrefs.getString(YEAR, "")
        set(value) = userPrefs.edit {
            putString(YEAR, value)
        }

    var spLoadNoticeFromFb: Boolean
        get() = userPrefs.getBoolean(LOAD_FB_NOTICE, false)
        set(value) = userPrefs.edit {
            putBoolean(LOAD_FB_NOTICE, value)
        }


    var spProfilePicSignature: String
        get() = userPrefs.getString(IS_PROFILE_PIC_CHANGE, "default")
        set(value) = userPrefs.edit {
            putString(IS_PROFILE_PIC_CHANGE, value)
        }


    var spNotification: Int
        get() = userPrefs.getInt(NID, 1)
        set(value) = userPrefs.edit {
            putInt(NID, value)
        }


    fun deleteUserDetails() {
        userPrefs.edit {
            clear()
        }
    }

    fun deleteRemoteConfigVar() {
        remotePrefs.edit {
            clear()
        }
    }


}
