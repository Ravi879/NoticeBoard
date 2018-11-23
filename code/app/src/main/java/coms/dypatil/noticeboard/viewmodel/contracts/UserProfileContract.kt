package coms.dypatil.noticeboard.viewmodel.contracts

import coms.dypatil.noticeboard.domain.model.UserProfile

interface UserProfileContract {

    fun showProfileDetails(userProfile: UserProfile)

    fun setYearVisibility(visibility: Int)
}