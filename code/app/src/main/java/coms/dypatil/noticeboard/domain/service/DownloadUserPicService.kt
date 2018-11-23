package coms.dypatil.noticeboard.domain.service

import android.app.IntentService
import android.content.Intent
import coms.dypatil.noticeboard.util.ProfileBuilderUtil

class DownloadUserPicService : IntentService("DownloadUserPicService") {


    companion object {
        const val USER_TYPE = "userType"
        const val PROFILE_PIC_URL = "profilePicUrl"
    }

    override fun onHandleIntent(intent: Intent?) {

        val userType = intent!!.getStringExtra(USER_TYPE)
        val profilePicUrl = intent.getStringExtra(PROFILE_PIC_URL)

        ProfileBuilderUtil.downloadUserPic(userType, profilePicUrl)
                .subscribe({},
                        { throwable ->
                            throwable.printStackTrace()
                        })

    }

}
