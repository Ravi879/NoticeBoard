package coms.dypatil.noticeboard.viewmodel

import android.app.Application
import android.os.Environment
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.room.EmptyResultSetException
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.repository.FacultyRepository
import coms.dypatil.noticeboard.data.repository.StudentRepository
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import coms.dypatil.noticeboard.viewmodel.contracts.UserProfileContract
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import java.io.File

class UserProfileVM(application: Application) : AndroidViewModel(application) {

    var contract: UserProfileContract? = null
    var userProfile: UserProfile? = null
    private val preference = App.preference

    fun loadProfile(): Disposable =
            getUserProfile().subscribeOn(SchedulersFacade.io())
                    .observeOn(SchedulersFacade.ui())
                    .subscribe({ profile: UserProfile ->
                        profile.profilePicFile = getProfilePicFile()
                        profile.password = preference.spUserPassword
                        this.userProfile = profile
                        contract?.showProfileDetails(this.userProfile!!)
                    }, { t ->
                        t.printStackTrace()
                    })


    private fun getUserProfile(): Maybe<UserProfile> = when (preference.spUserType) {
        "faculty" -> {
            contract!!.setYearVisibility(View.GONE)
            FacultyRepository.getFBFacultyById(preference.spUserId)
                    .flatMap { faculty ->
                        Maybe.just(UserProfile(faculty))
                    }
        }
        "student" -> {
            contract!!.setYearVisibility(View.VISIBLE)
            StudentRepository.getStudentById(preference.spUserId)
                    .flatMap { student ->
                        Maybe.just(UserProfile(student))
                    }
        }
        else -> Maybe.error(EmptyResultSetException("not Found"))
    }


    private fun getProfilePicFile(): File {
        return File(Environment.getExternalStoragePublicDirectory("NoticeBoard/${preference.spUserType}"), preference.spUserProfilePicName)
    }
}