package coms.dypatil.noticeboard.viewmodel.contracts

import coms.dypatil.noticeboard.domain.model.UserProfile

interface FacultyProfileContract {

    fun showProfileDetails(userProfile: UserProfile)

    fun profileNotFoundInRoom()
}