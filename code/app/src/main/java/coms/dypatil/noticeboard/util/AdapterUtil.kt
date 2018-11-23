package coms.dypatil.noticeboard.util

import android.widget.ArrayAdapter
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App


class AdapterUtil {
    companion object {
        fun getAdapter(list: List<String>): ArrayAdapter<String> {
            val adapter: ArrayAdapter<String> = ArrayAdapter(App.getAppContext, R.layout.item_spn, list)
            adapter.setDropDownViewResource(R.layout.item_spn_dropdown)
            return adapter
        }

    }

}
