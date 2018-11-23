package coms.dypatil.noticeboard.data.datastore.firebase.model

import coms.dypatil.noticeboard.domain.model.UserRegistration

class FBStudent() {
    var name: String? = null
    var email: String? = null
    var gender: String? = null
    var dob: String? = null
    var phoneNo: String? = null
    var department: String? = null
    var designation: String? = null
    var profilePicUrl: String? = null

    var year: String? = null


    constructor(userRegistration: UserRegistration) : this() {
        name = userRegistration.firstName + " " + userRegistration.lastName
        email = userRegistration.email
        gender = userRegistration.gender
        dob = userRegistration.dob
        phoneNo = userRegistration.phoneNo
        department = userRegistration.department
        designation = userRegistration.designation
        profilePicUrl = userRegistration.profilePicUrl

        year = userRegistration.year
    }


}


