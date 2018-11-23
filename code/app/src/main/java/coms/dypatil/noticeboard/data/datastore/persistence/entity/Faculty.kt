package coms.dypatil.noticeboard.data.datastore.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBFaculty
import coms.dypatil.noticeboard.data.datastore.persistence.contract.BaseContract
import coms.dypatil.noticeboard.data.datastore.persistence.contract.FacultyContract
import coms.dypatil.noticeboard.domain.model.UserRegistration

@Entity(tableName = FacultyContract.TABLE_NAME)
data class Faculty(

        @PrimaryKey
        @ColumnInfo(name = BaseContract.FB_USER_ID)
        var fbUserId: String = "",

        @ColumnInfo(name = BaseContract.NAME)
        var name: String?,

        @ColumnInfo(name = BaseContract.EMAIL)
        var email: String?,

        @ColumnInfo(name = BaseContract.GENDER)
        var gender: String?,

        @ColumnInfo(name = BaseContract.DOB)
        var dob: String?,

        @ColumnInfo(name = BaseContract.PHONE_NO)
        var phoneNo: String?,

        @ColumnInfo(name = BaseContract.DEPARTMENT)
        var department: String?,

        @ColumnInfo(name = BaseContract.DESIGNATION)
        var designation: String?,

        @ColumnInfo(name = BaseContract.PROFILE_PIC_URL)
        var profilePicUrl: String?

) {
    @Ignore
    constructor(fbUserId: String, fbFaculty: FBFaculty) : this(
            fbUserId,
            fbFaculty.name,
            fbFaculty.email,
            fbFaculty.gender,
            fbFaculty.dob,
            fbFaculty.phoneNo,
            fbFaculty.department,
            fbFaculty.designation,
            fbFaculty.profilePicUrl)


    @Ignore
    constructor(userProfile: UserRegistration) : this(
            userProfile.fbUserId!!,
            userProfile.getUserName(),
            userProfile.email,
            userProfile.gender,
            userProfile.dob,
            userProfile.phoneNo,
            userProfile.department,
            userProfile.designation,
            userProfile.profilePicUrl
    )

}


