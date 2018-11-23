package coms.dypatil.noticeboard.data.repository

import android.net.Uri
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.interactor.EditProfileUseCase
import coms.dypatil.noticeboard.domain.model.UserRegistration
import io.reactivex.Maybe

object EditProfileRepository {
    private val useCase = EditProfileUseCase(App.fAuth, App.fDatabase, App.fStorage, App.preference)

    fun updateEmail(newEmail: String): Maybe<String> = useCase.updateEmail(newEmail)

    fun updatePassword(newPassword: String): Maybe<String> = useCase.updatePassword(newPassword)

    fun updateProfilePic(userType: String, profilePicUrl: String, fileUri: Uri): Maybe<String> = useCase.updateProfilePic(userType, profilePicUrl, fileUri)

    fun updateProfileDetails(userRegistration: UserRegistration): Maybe<String> = useCase.updateProfileDetails(userRegistration)

    fun updateProfileLocation(newDept: String, newDesi: String, newYear: String = ""): Maybe<String> =
            useCase.updateProfileLocation(newDept, newDesi, newYear)
}


