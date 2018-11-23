package coms.dypatil.noticeboard.domain.service

import android.app.IntentService
import android.content.Intent
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import coms.dypatil.noticeboard.BuildConfig
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference

class FBConfigService : IntentService("FBConfigService") {

    private lateinit var mRemoteConfig: FirebaseRemoteConfig
    private lateinit var mPreference: Preference

    companion object {
        private const val REMOTE_KEY_CONFIG_DEPARTMENT = "CONFIG_DEPARTMENT"
        private const val REMOTE_KEY_CONFIG_DESIGNATION = "CONFIG_DESIGNATION"
        private const val REMOTE_KEY_CONFIG_YEAR = "CONFIG_YEAR"
    }


    override fun onCreate() {
        super.onCreate()
        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        mPreference = App.preference
    }

    override fun onHandleIntent(p0: Intent?) {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mRemoteConfig.setConfigSettings(configSettings)

        if (mRemoteConfig.info.configSettings.isDeveloperModeEnabled)
            mRemoteConfig.fetch(0)
                    .addOnSuccessListener {
                        mRemoteConfig.activateFetched()

                        mPreference.spRemoteDepartements = mRemoteConfig.getString(REMOTE_KEY_CONFIG_DEPARTMENT)
                        mPreference.spRemoteDesignation = mRemoteConfig.getString(REMOTE_KEY_CONFIG_DESIGNATION)
                        mPreference.spRemoteYear = mRemoteConfig.getString(REMOTE_KEY_CONFIG_YEAR)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }

    }

}
