package coms.dypatil.noticeboard.data.repository

import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.domain.interactor.PreferenceUseCase
import coms.dypatil.noticeboard.domain.model.UserLogin

object PreferenceRepository {

    val preference = Preference(App.getAppContext)

    fun saveProfileDetails(userLogin: UserLogin): Long = PreferenceUseCase.saveProfileDetails(preference, userLogin)

    fun deleteUserDetails(): Unit = preference.deleteUserDetails()

    fun deleteRemoteConfigVar(): Unit = preference.deleteRemoteConfigVar()

}

