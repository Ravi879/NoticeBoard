package coms.dypatil.noticeboard.data.datastore.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import coms.dypatil.noticeboard.data.datastore.firebase.model.FBStudent
import coms.dypatil.noticeboard.data.datastore.persistence.contract.BaseContract
import coms.dypatil.noticeboard.data.datastore.persistence.contract.StudentContract
import coms.dypatil.noticeboard.domain.model.UserRegistration

@Entity(tableName = StudentContract.TABLE_NAME)
data class Student(

        @PrimaryKey
        @ColumnInfo(name = BaseContract.FB_USER_ID)
        var fbUserId: String,

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
        var profilePicUrl: String?,

        @ColumnInfo(name = StudentContract.YEAR)
        var year: String?


) {
    @Ignore
    constructor(fbUserId: String, fbStudent: FBStudent) : this(
            fbUserId,
            fbStudent.name,
            fbStudent.email,
            fbStudent.gender,
            fbStudent.dob,
            fbStudent.phoneNo,
            fbStudent.department,
            fbStudent.designation,
            fbStudent.profilePicUrl,
            fbStudent.year)


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
            userProfile.profilePicUrl,
            userProfile.year
    )

}