package coms.dypatil.noticeboard.domain.model

import android.net.Uri


data class UserRegistration(
        var firstName: String? = null,
        var lastName: String? = null,
        var email: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var phoneNo: String? = null,
        var password: String? = null,

        var profilePicUri: Uri? = null,
        var department: String? = null,
        var designation: String? = null,
        var year: String? = null,

        var userType: String? = null,
        var profilePicUrl: String? = null,

        var mimeType: String? = null,
        var fbUserId: String? = null,
        val profilePicUriStr: String? = null

) {

    fun getUserName(): String = "$firstName $lastName"

}

