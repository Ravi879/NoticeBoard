package coms.dypatil.noticeboard

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import coms.dypatil.noticeboard.data.datastore.persistence.Injection
import coms.dypatil.noticeboard.data.datastore.persistence.dao.FacultyDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.NoticeDao
import coms.dypatil.noticeboard.data.datastore.persistence.dao.StudentDao
import coms.dypatil.noticeboard.data.datastore.sharedpreference.model.Preference
import coms.dypatil.noticeboard.data.repository.PreferenceRepository
import coms.dypatil.noticeboard.domain.reciever.NetworkReceiver

class App : MultiDexApplication() {

    companion object {

        lateinit var getAppContext: Context

        var isNetworkAvailable: MutableLiveData<Boolean> = MutableLiveData()

        //firebase
        lateinit var fDatabase: FirebaseDatabase
        lateinit var fAuth: FirebaseAuth
        lateinit var fStorage: FirebaseStorage

        //shared preference
        lateinit var preference: Preference

        //Room
        lateinit var facultyDataProvider: FacultyDao
        lateinit var studentDataProvider: StudentDao
        lateinit var noticeDataProvider: NoticeDao

        var isProfilePicChanged = false
    }


    private var mNetworkReceiver: NetworkReceiver? = null


    override fun onCreate() {
        super.onCreate()

        getAppContext = applicationContext

        fDatabase = FirebaseDatabase.getInstance()
        fAuth = FirebaseAuth.getInstance()
        fStorage = FirebaseStorage.getInstance()


        preference = PreferenceRepository.preference

        facultyDataProvider = Injection.facultyDataProvider(App.getAppContext)!!
        studentDataProvider = Injection.studentDataProvider(App.getAppContext)!!
        noticeDataProvider = Injection.noticeDataProvider(App.getAppContext)!!

        mNetworkReceiver = NetworkReceiver()
        val networkFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(mNetworkReceiver, networkFilter)

    }




}