package coms.dypatil.noticeboard.viewmodel.contracts

import android.view.View
import android.widget.ArrayAdapter

interface CheckCredential {
    fun invalidCredential(field: String, errorMsg: String)
}

interface SpnContract {

    fun setSpnYearVisibility(visibility: Int)

    fun setDesignationAdapter(adapter: ArrayAdapter<String>)

    fun setYearAdapter(adapter: ArrayAdapter<String>)
}

interface NetworkStateCallBack {
    fun onNetworkStateChange(isAvailable: Boolean)
}

interface RecyclerViewClickListener {
    fun onClick(view: View, position: Int)
}