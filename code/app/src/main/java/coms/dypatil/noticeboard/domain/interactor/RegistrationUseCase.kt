package coms.dypatil.noticeboard.domain.interactor


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBFaculty
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBStudent
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.rx.SchedulersFacade
import durdinapps.rxfirebase2.RxFirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Maybe
import io.reactivex.Single


class RegistrationUseCase(private val fAuth: FirebaseAuth, private val fDatabase: FirebaseDatabase, private val fStorage: FirebaseStorage) {

    fun createAccount(userRegistration: UserRegistration): Maybe<UserRegistration?> =
            RxFirebaseAuth.createUserWithEmailAndPassword(fAuth, userRegistration.email!!, userRegistration.password!!)
                    .map {
                        userRegistration.fbUserId = fAuth.uid
                        userRegistration
                    }

    fun uploadProfilePic(userId: Maybe<UserRegistration?>): Maybe<UserRegistration?> =
            userId.flatMap { userRegistration ->
                val imageStoragePath = ProfileBuilderUtil.getProfilePicPath(userRegistration.userType!!,
                        userRegistration.fbUserId!!,
                        userRegistration.mimeType!!)
                val storageReference = fStorage.getReference(imageStoragePath)
                RxFirebaseStorage.putFile(storageReference, userRegistration.profilePicUri!!)
                        .map { userRegistration }
                        .toMaybe()
            }


    fun getProfilePicUrl(userRegistration: UserRegistration): Maybe<UserRegistration?> {
        val imageStoragePath = ProfileBuilderUtil.getProfilePicPath(userRegistration.userType!!, userRegistration.fbUserId!!, userRegistration.mimeType!!)
        val storageReference = fStorage.getReference(imageStoragePath)
        return RxFirebaseStorage.getDownloadUrl(storageReference)
                .map { downloadUri ->
                    userRegistration.profilePicUrl = downloadUri.toString()
                    userRegistration
                }
    }

    fun uploadUserDetails(profilePicUrl: Maybe<UserRegistration?>): Single<MutableList<String>> =
            Maybe.merge(uploadUserIndex(profilePicUrl).subscribeOn(SchedulersFacade.io()).observeOn(SchedulersFacade.ui()),
                    uploadProfileDetails(profilePicUrl).subscribeOn(SchedulersFacade.io()).observeOn(SchedulersFacade.ui()))
                    .toList()


    private fun uploadUserIndex(userId: Maybe<UserRegistration?>): Maybe<String> =
            userId.flatMap { userRegistration ->
                val userLocation = getUserLocation(userRegistration)
                RxFirebaseDatabase.setValue(fDatabase.getReference("userIndex/${userRegistration.fbUserId}"), userLocation)
                        .andThen(Maybe.just("uploaded"))
            }

    private fun uploadProfileDetails(userId: Maybe<UserRegistration?>): Maybe<String> =
            userId.flatMap { userRegistration ->
                val userLocation = getUserLocation(userRegistration)
                when (userRegistration.userType) {
                    "faculty" -> {
                        val fbFaculty = FBFaculty(userRegistration)
                        RxFirebaseDatabase.setValue(fDatabase.getReference(userLocation), fbFaculty)
                                .andThen(Maybe.just("uploaded"))
                    }
                    "student" -> {
                        val fbStudent = FBStudent(userRegistration)
                        RxFirebaseDatabase.setValue(fDatabase.getReference(userLocation), fbStudent)
                                .andThen(Maybe.just("uploaded"))
                    }
                    else -> Maybe.just("Unknown user type")
                }
            }

    private fun getUserLocation(userRegistration: UserRegistration): String = with(userRegistration) {
        ProfileBuilderUtil.getUserLocation(userType!!, fbUserId!!, department!!, designation!!, year!!)
    }


}