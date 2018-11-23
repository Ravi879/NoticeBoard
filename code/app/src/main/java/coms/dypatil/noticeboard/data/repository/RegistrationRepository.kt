package coms.dypatil.noticeboard.data.repository

import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.interactor.RegistrationUseCase
import coms.dypatil.noticeboard.domain.model.UserRegistration
import io.reactivex.Maybe
import io.reactivex.Single

object RegistrationRepository {

    private var loginUseCase = RegistrationUseCase(App.fAuth, App.fDatabase, App.fStorage)

    fun signUp(userRegistration: UserRegistration): Maybe<UserRegistration?> {
        val userId: Maybe<UserRegistration?> = loginUseCase.createAccount(userRegistration)
        return loginUseCase.uploadProfilePic(userId)
    }

    fun uploadProfileDetails(profilePicPath: UserRegistration): Single<MutableList<String>> {
        val profilePicUrl: Maybe<UserRegistration?> = loginUseCase.getProfilePicUrl(profilePicPath)
        return loginUseCase.uploadUserDetails(profilePicUrl)
    }

}