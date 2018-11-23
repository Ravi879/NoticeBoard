package coms.dypatil.noticeboard.domain.interactor

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBFaculty
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBStudent
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.data.repository.FacultyRepository
import coms.dypatil.noticeboard.data.repository.StudentRepository
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.util.FileUtil
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import durdinapps.rxfirebase2.RxFirebaseUser
import io.reactivex.Maybe
import java.io.File


class EditProfileUseCase(private val fAuth: FirebaseAuth,
        private val fDatabase: FirebaseDatabase,
        private val fStorage: FirebaseStorage,
        private val preference: Preference) {

    fun updateEmail(updateEmail: String): Maybe<String> {
        val credential: AuthCredential? = EmailAuthProvider.getCredential(preference.spUserEmail, preference.spUserPassword)

        return Maybe.just(fAuth.currentUser)
                .flatMap { user: FirebaseUser ->
                    RxFirebaseUser.reAuthenticate(user, credential!!).andThen(Maybe.just(user))
                }.flatMap { fbUser: FirebaseUser ->
                    RxFirebaseUser.updateEmail(fbUser, updateEmail)
                            .andThen(Maybe.just(updateEmail)
                                    .map { email ->
                                        preference.spUserEmail = email
                                        "Updated"
                                    })
                }
    }

    fun updatePassword(updatePassword: String): Maybe<String> {

        return Maybe.just(fAuth.currentUser)
                .flatMap { user: FirebaseUser ->
                    val credential: AuthCredential = EmailAuthProvider.getCredential(preference.spUserEmail, preference.spUserPassword)
                    RxFirebaseUser.reAuthenticate(user, credential).andThen(Maybe.just(user))
                }.flatMap { fbUser: FirebaseUser ->
                    RxFirebaseUser.updatePassword(fbUser, updatePassword)
                            .andThen(Maybe.just(updatePassword)
                                    .map { password ->
                                        preference.spUserPassword = password
                                        "Completed"
                                    })
                }
    }


    fun updateProfilePic(userType: String, profilePicUrl: String, fileUri: Uri): Maybe<String> {
        return RxFirebaseStorage.putFile(fStorage.getReferenceFromUrl(profilePicUrl), fileUri)
                .toMaybe()
                .flatMap {
                    val profilePicName = preference.spUserProfilePicName
                    val oldFile = FileUtil.getProfilePicFile(userType, profilePicName)

                    if (oldFile.exists() && oldFile.delete()) {
                        val destFile = FileUtil.getProfilePicFile(userType, profilePicName)
                        if (destFile.createNewFile()) {
                            val sourceFile = File(FileUtil.getFilePathFromMediaUri(fileUri))
                            FileUtil.copyFile(sourceFile, destFile)
                        }
                    }
                    Maybe.just("")
                }
    }

    fun updateProfileDetails(userRegistration: UserRegistration): Maybe<String> {
        val userIndex: String = userRegistration.let { user ->
            ProfileBuilderUtil.getUserLocation(user.userType!!, user.fbUserId!!,
                    user.department!!, user.designation!!, user.year ?: "")
        }
        return if (userRegistration.userType=="faculty") {
            val fbFaculty = FBFaculty(userRegistration)
            RxFirebaseDatabase.setValue(fDatabase.getReference(userIndex), fbFaculty).andThen(
                    Maybe.just(Faculty(userRegistration))
                            .flatMap { faculty ->
                                updateProfileLocally(faculty = faculty, student = null)
                            })
        } else {
            val fbStudent = FBStudent(userRegistration)
            RxFirebaseDatabase.setValue(fDatabase.getReference(userIndex), fbStudent).andThen(
                    Maybe.just(Student(userRegistration))
                            .flatMap { student ->
                                updateProfileLocally(faculty = null, student = student)
                            })
        }

    }

    private fun updateProfileLocally(faculty: Faculty?, student: Student?): Maybe<String> {

        val rowInserted: Long = when {
            faculty!=null -> FacultyRepository.insert(faculty)
            student!=null -> StudentRepository.insert(student)
            else -> -1
        }

        return if (rowInserted > 0) {
            Maybe.just("Updated")
        } else {
            Maybe.just("Update unsuccessful")
        }
    }

    fun updateProfileLocation(newDept: String, newDesi: String, newYear: String = ""): Maybe<String> {
        val userId = preference.spUserId
        val oldLocation: String = preference.let { p ->
            ProfileBuilderUtil.getUserLocation(p.spUserType, userId,
                    p.spUserDepartment, p.spUserDesignation, p.spUserYear)
        }
        val newLocation: String? = ProfileBuilderUtil.getUserLocation(preference.spUserType, userId,
                newDept, newDesi, newYear)

        val oldReference: DatabaseReference = fDatabase.getReference(oldLocation)

        return Maybe.just(oldReference)
                .flatMap { location ->
                    location.removeValue()
                    RxFirebaseDatabase.setValue(fDatabase.getReference("userIndex/$userId"), newLocation)
                            .andThen(Maybe.just("Completed"))
                }.flatMap {
                    updatePreferences(newDept, newDesi, newYear)
                    Maybe.just("Completed")
                }
    }

    private fun updatePreferences(newDept: String, newDesi: String, newYear: String = "") {
        preference.spUserDepartment = newDept
        preference.spUserDesignation = newDesi
        preference.spUserYear = newYear
    }

}