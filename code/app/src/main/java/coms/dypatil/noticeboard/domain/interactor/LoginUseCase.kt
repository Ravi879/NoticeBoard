package coms.dypatil.noticeboard.domain.interactor

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.domain.model.UserLogin
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.SpnListUtil
import coms.dypatil.noticeboard.util.tag
import durdinapps.rxfirebase2.RxFirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Maybe

class LoginUseCase(private val fAuth: FirebaseAuth, private val fDatabase: FirebaseDatabase,
        private val preference: Preference) {

    fun signInWithEmailAndPassword(email: String, password: String): Maybe<String?> =
            RxFirebaseAuth
                    .signInWithEmailAndPassword(fAuth, email, password)
                    .map {
                        preference.spUserEmail = email
                        preference.spUserPassword = password
                        fAuth.uid
                    }


    fun getUserProfileReference(userId: String): Maybe<UserLogin> =
            ProfileBuilderUtil.getUserIndex(userId)
                    .flatMap { profilePath ->
                        val user = UserLogin()
                        user.userId = userId
                        user.profilePath = profilePath
                        user.userType = if (profilePath.contains("faculty")) "faculty" else "student"
                        Maybe.just(user)
                    }

    fun getUserProfileDetails(profileReferenceObservable: Maybe<UserLogin>): Maybe<UserLogin?> =
            profileReferenceObservable.flatMap { userLogin: UserLogin ->
                RxFirebaseDatabase
                        .observeSingleValueEvent(fDatabase.getReference(userLogin.profilePath!!)) { dataSnapshot ->
                            Log.v(tag, " snapshot " + dataSnapshot.toString())
                            userLogin.dataSnapshot = dataSnapshot
                            userLogin
                        }
            }

    fun getUserFBLogOut() = fAuth.signOut()


    fun subscribeFBMessageTopic() {
        val fbMessaging = FirebaseMessaging.getInstance()
        if (preference.spUserType=="faculty" && preference.spUserDepartment=="Office") {
            fbMessaging.subscribeToTopic("${preference.spUserDepartment}-${preference.spUserDesignation}")
        } else {
            fbMessaging.subscribeToTopic(preference.spUserDepartment)
        }

        if (preference.spUserType=="student") {
            fbMessaging.subscribeToTopic("Office-${preference.spUserDepartment}")
            SpnListUtil.getNonTeachingDeptList().forEach { dept: String ->
                fbMessaging.subscribeToTopic(dept)
            }
        }
    }

    fun unSubscribeFBMessageTopic() {
        val fbMessaging = FirebaseMessaging.getInstance()

        if (preference.spUserType=="faculty" && preference.spUserDepartment=="Office") {
            fbMessaging.unsubscribeFromTopic("${preference.spUserDepartment}-${preference.spUserDesignation}")
        } else {
            fbMessaging.unsubscribeFromTopic(preference.spUserDepartment)
        }

        if (preference.spUserType=="student") {
            fbMessaging.unsubscribeFromTopic("Office-${preference.spUserDepartment}")
            SpnListUtil.getNonTeachingDeptList().forEach { dept: String ->
                fbMessaging.unsubscribeFromTopic(dept)
            }
        }

    }


}


