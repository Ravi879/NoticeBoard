package coms.dypatil.noticeboard.domain.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.util.tag


class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {
        if (ConnectivityManager.CONNECTIVITY_ACTION==intent!!.action) {
            val isAvailable = !(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
            Log.v(tag, " NetworkReceiver isNetworkAvailable ......................... $isAvailable")
            App.isNetworkAvailable.value = isAvailable
        }
    }


}