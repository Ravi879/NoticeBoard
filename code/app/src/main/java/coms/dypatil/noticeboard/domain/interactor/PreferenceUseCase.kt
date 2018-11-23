package coms.dypatil.noticeboard.domain.interactor

import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.domain.model.UserLogin
import coms.dypatil.noticeboard.util.ProfileBuilderUtil


class PreferenceUseCase {

    companion object {

        fun saveProfileDetails(preference: Preference, userLogin: UserLogin): Long {
            with(preference) {
                spUserId = userLogin.userId!!
                spUserName = userLogin.name!!
                spUserDepartment = userLogin.department!!
                spUserDesignation = userLogin.designation!!
                spUserType = userLogin.userType!!
                spUserYear = userLogin.year!!
                spUserProfilePicName = ProfileBuilderUtil.getProfilePicNameFromUrl(userLogin.profilePicUrl!!)
            }
            return 1L
        }

    }
}


















