package coms.dypatil.noticeboard.util

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.lang.ref.WeakReference

//Google Play Service
class GPSAvailability {

    companion object {

        fun isGooglePlayServicesAvailable(activity: Activity): Boolean {

            val weakReference: WeakReference<Activity> = WeakReference(activity)
            val weakActivity = weakReference.get()

            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val status: Int = googleApiAvailability.isGooglePlayServicesAvailable(weakActivity)

            return if (status==ConnectionResult.SUCCESS) {
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE
                true
            } else {
                if (googleApiAvailability.isUserResolvableError(status)) {
                    val dialog = googleApiAvailability.getErrorDialog(weakActivity, status, 2404)
                    dialog.show()
                }
                false
            }
        }
    }


}