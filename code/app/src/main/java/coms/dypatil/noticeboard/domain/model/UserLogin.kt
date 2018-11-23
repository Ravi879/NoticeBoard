package coms.dypatil.noticeboard.domain.model

import com.google.firebase.database.DataSnapshot

class UserLogin(
        var userId: String? = null,
        var userType: String? = null,
        var profilePath: String? = null,
        var dataSnapshot: DataSnapshot? = null,

        var name:String? = null,
        var department: String? = null,
        var designation: String? = null,
        var year: String? = null,
        var profilePicUrl:String? = null
        )

