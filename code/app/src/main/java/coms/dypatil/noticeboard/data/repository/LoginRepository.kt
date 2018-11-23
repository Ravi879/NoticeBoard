package coms.dypatil.noticeboard.data.repository

import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.interactor.LoginUseCase
import coms.dypatil.noticeboard.domain.model.UserLogin
import io.reactivex.Maybe

object LoginRepository {

    private var loginUseCase: LoginUseCase = LoginUseCase(App.fAuth, App.fDatabase, App.preference)

    fun signIn(email: String, password: String): Maybe<String?> =
            loginUseCase.signInWithEmailAndPassword(email, password)

    fun getProfileDetails(userId: String): Maybe<UserLogin?> {
        val profileReference = loginUseCase.getUserProfileReference(userId)
        return loginUseCase.getUserProfileDetails(profileReference)
    }

    fun subscribeFBMessageTopic() = loginUseCase.subscribeFBMessageTopic()

    fun unSubscribeFBMessageTopic() = loginUseCase.unSubscribeFBMessageTopic()

    fun getUserFBLogOut() = loginUseCase.getUserFBLogOut()

}
