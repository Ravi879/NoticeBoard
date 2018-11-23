package coms.dypatil.noticeboard.util

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.viewmodel.contracts.NetworkStateCallBack
import java.lang.ref.WeakReference


object NetworkUtil {

    fun getNetworkObserver(networkSnackBar: NetworkStateCallBack) = Observer<Boolean> { isVisible: Boolean ->
        networkSnackBar.onNetworkStateChange(isVisible)
    }

    fun getNetworkSnackBar(layout: View): Snackbar {
        val weekLayout = WeakReference<View>(layout)
        val snackBar: Snackbar = Snackbar.make(weekLayout.get()!!,
                "No Connection :(",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {}
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackBar.setActionTextColor(ContextCompat.getColor(App.getAppContext, R.color.snackbar_action))

        return snackBar
    }

}