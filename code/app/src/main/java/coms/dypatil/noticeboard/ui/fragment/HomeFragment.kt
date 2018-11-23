package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.ybq.android.spinkit.SpinKitView
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.ui.activity.CreateNotice
import coms.dypatil.noticeboard.ui.activity.LoginActivity
import coms.dypatil.noticeboard.util.AdapterUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.replaceFragment
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.NoticeVM
import coms.dypatil.noticeboard.viewmodel.contracts.HomeContract
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment(), HomeContract {
    companion object {
        const val tag = "HomeFragment"
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private lateinit var viewModel: NoticeVM
    private lateinit var loadingIndicator: SpinKitView
    private var alertDialog: AlertDialog? = null

    private lateinit var spnDeptFilter: Spinner
    private lateinit var spnDesiFilter: Spinner

    private var spnSelectionHack = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setMenuVisibility(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity!! as AppCompatActivity).supportActionBar?.title = "NoticeBoard"

        val layout = inflater.inflate(R.layout.fragment_home, container, false)
        loadingIndicator = layout.loading_indicator

        viewModel = ViewModelProviders.of(this).get(NoticeVM::class.java)
        viewModel.contract = this

        viewModel.response.observe(this, Observer { processResponse(it!!) })

        layout.btn_create_notice.apply {
            if (viewModel.isFabVisible())
                show()
            else
                hide()

            setOnClickListener {
                startActivity(Intent(activity!!, CreateNotice::class.java))
            }
        }

        val noticeFragment = NoticeTabFragment.newInstance()
        replaceFragment(R.id.notice_fragment_container, noticeFragment, NoticeTabFragment.tag)

        return layout
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_fragment_home, menu)
    }

    override fun onStart() {
        super.onStart()

        if (App.preference.spLoadNoticeFromFb) {
            viewModel.loadFBNotice()
            App.preference.spLoadNoticeFromFb = false
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.action_filter -> {
                filterNotice()
                return true
            }

            R.id.action_log_out -> {
                viewModel.onLogOut()
                return true
            }

            R.id.action_github -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ravi879/NoticeBoard"))
                startActivity(intent)
                return true
            }
        }

        return false
    }

    private fun setFilterSpnSelection() {
        val spnObserver = getDeptSpnObserver(spnDesiFilter)

        viewModel.mFilterDeptIndex.observe(this, spnObserver)
        viewModel.mExpireFilterDeptIndex.observe(this, spnObserver)

        var deptIndex = 0
        var desiIndex = 0
        when (true) {
            viewModel.IS_NOTICE_FRAGMENT_VISIBLE -> {
                deptIndex = viewModel.mFilterDeptIndex.value ?: 0
                desiIndex = viewModel.mFilterDesiIndex
            }
            viewModel.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE -> {
                deptIndex = viewModel.mExpireFilterDeptIndex.value ?: 0
                desiIndex = viewModel.mExpireFilterDesiIndex
            }
        }

        spnDeptFilter.setSelection(deptIndex)
        spnSelectionHack = false
        spnDesiFilter.setSelection(desiIndex)

    }

    private fun getFilterDialog(): AlertDialog {
        val builder = AlertDialog.Builder(activity!!)

        val dialogLayout = layoutInflater.inflate(R.layout.dialog_filter, null)

        spnDeptFilter = dialogLayout.spn_department
        spnDesiFilter = dialogLayout.spn_designation

        spnDeptFilter.adapter = AdapterUtil.getAdapter(viewModel.spnDeptList)
        spnDesiFilter.adapter = AdapterUtil.getAdapter(viewModel.spnDesiList[0])

        spnDeptFilter.onItemSelectedListener = spnListen()
        spnDesiFilter.onItemSelectedListener = spnListen()

        val spnObserver = getDeptSpnObserver(spnDesiFilter)

        viewModel.mFilterDeptIndex.observe(this, spnObserver)
        viewModel.mExpireFilterDeptIndex.observe(this, spnObserver)

        viewModel.mFilterDeptIndex.value = 0
        viewModel.mExpireFilterDeptIndex.value = 0

        builder.setView(dialogLayout)
            .setPositiveButton("Ok") { dialog, _ ->
                when (true) {
                    viewModel.IS_NOTICE_FRAGMENT_VISIBLE -> viewModel.FLAG_APPLY_NOTICE_FILTER.value = 1
                    viewModel.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE -> viewModel.FLAG_APPLY_EXPIRE_NOTICE_FILTER.value = 1
                }
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setOnDismissListener {
                viewModel.mFilterDeptIndex.removeObserver(spnObserver)
                viewModel.mExpireFilterDeptIndex.removeObserver(spnObserver)
            }

        return builder.create()
    }

    private fun getDeptSpnObserver(spnDesiFilter: Spinner): Observer<Int?> = Observer { position: Int? ->
        spnDesiFilter.adapter = viewModel.changeSpnDesignation(position!!)
    }


    private fun filterNotice() {
        if (alertDialog != null) {
            setFilterSpnSelection()

            alertDialog!!.show()
            return
        }

        alertDialog = getFilterDialog()
        alertDialog!!.show()
    }

    private fun spnListen(): AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (parent != null && view != null) {
                when (parent.id) {

                    R.id.spn_department -> {
                        when (true) {
                            viewModel.IS_NOTICE_FRAGMENT_VISIBLE -> viewModel.mFilterDeptIndex.value = position
                            viewModel.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE -> viewModel.mExpireFilterDeptIndex.value =
                                    position
                        }
                    }
                    R.id.spn_designation -> {
                        if (spnSelectionHack) {
                            when (true) {
                                viewModel.IS_NOTICE_FRAGMENT_VISIBLE -> {
                                    viewModel.mFilterDesiIndex = position
                                    viewModel.mFilterDesi = parent.getItemAtPosition(position).toString()
                                }
                                viewModel.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE -> {
                                    viewModel.mExpireFilterDesiIndex = position
                                    viewModel.mExpireFilterDesi = parent.getItemAtPosition(position).toString()
                                }
                            }

                        } else {
                            spnSelectionHack = true
                        }
                    }
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }


    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> renderDataState(response.data)
            Status.ERROR -> renderErrorState(response.error!!, response.data!!)
        }
    }

    private fun renderLoadingState() {
        loadingIndicator.visibility = View.VISIBLE
    }

    private fun renderDataState(msg: String?) {
        loadingIndicator.visibility = View.GONE
        msg?.let { activity!!.toast(it) }
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        loadingIndicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(coms.dypatil.noticeboard.util.tag, "HomeFragment Error $throwable")
        activity!!.toast(errorMsg)
    }


    override fun startLoginActivity() {
        startActivity(Intent(activity, LoginActivity::class.java))
        activity!!.finish()
    }


}

