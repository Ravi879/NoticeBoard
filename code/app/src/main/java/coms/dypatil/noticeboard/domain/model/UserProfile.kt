package coms.dypatil.noticeboard.domain.model

import android.os.Parcelable
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import kotlinx.android.parcel.Parcelize
import java.io.File


@Parcelize
data class UserProfile(
        var name: String? = null,
        var email: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var phoneNo: String? = null,

        var department: String? = null,
        var designation: String? = null,
        var year: String? = null,
        var profilePicFile: File? = null,
        var profilePicUrl: String? = null,

        var password: String? = null
) : Parcelable {

    constructor(faculty: Faculty) : this(
            faculty.name,
            faculty.email,
            faculty.gender,
            faculty.dob,
            faculty.phoneNo,
            faculty.department,
            faculty.designation,
            null,
            null,
            faculty.profilePicUrl,
            null
    )

    constructor(student: Student) : this(
            student.name,
            student.email,
            student.gender,
            student.dob,
            student.phoneNo,
            student.department,
            student.designation,
            student.year,
            null,
            student.profilePicUrl,
            null
    )
}
