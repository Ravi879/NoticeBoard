package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.ybq.android.spinkit.SpinKitView
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.adapter.MyNoticeRecyclerAdapter
import coms.dypatil.noticeboard.ui.activity.NoticeDetailActivity
import coms.dypatil.noticeboard.util.NetworkUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.MyNoticeVM
import coms.dypatil.noticeboard.viewmodel.contracts.NetworkStateCallBack
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.dialog_filter_my_notice.view.*
import kotlinx.android.synthetic.main.fragment_my_notice.view.*

class MyNoticeFragment : Fragment(), NetworkStateCallBack {

    private var viewModel: MyNoticeVM? = null
    private lateinit var noticeAdapter: MyNoticeRecyclerAdapter

    private lateinit var loadingIndicator: SpinKitView
    private var layoutFilter: View? = null

    private var alertDialog: AlertDialog? = null

    companion object {
        const val tag = "MyNoticeFragment"
        fun newInstance(): MyNoticeFragment {
            return MyNoticeFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setMenuVisibility(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity?.findViewById(R.id.app_bar) as Toolbar).title = "My Notice"
    }

    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val layout = inflater.inflate(R.layout.fragment_my_notice, container, false)
        loadingIndicator = layout.loading_indicator
        noticeAdapter = getNoticeAdapter()

        viewModel = ViewModelProviders.of(this).get(MyNoticeVM::class.java)
        viewModel!!.response.observe(this, Observer { processResponse(it!!) })

        layout.rv_my_notice.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = noticeAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel!!.noticeList.observe(this, Observer {
            noticeAdapter.setData(it!!)
        })
        viewModel!!.getAllNotice()
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        return layout
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.action_filter -> {
                filterNotice()
                return true
            }
        }
        return false
    }

    private fun filterNotice() {

        if (alertDialog!=null) {
            alertDialog!!.show()
            return
        }

        val builder = AlertDialog.Builder(activity!!)

        if (layoutFilter==null)
            layoutFilter = layoutInflater.inflate(R.layout.dialog_filter_my_notice, null)

        builder.setView(layoutFilter)
                .setPositiveButton("Filter") { dialog, _ ->
                    viewModel!!.filterIndex = when (layoutFilter!!.rGroup_filter.checkedRadioButtonId) {
                        R.id.rbtn_notice -> "Notice"
                        R.id.rbtn_expired_notice -> "ExpiredNotice"
                        else -> "All"
                    }
                    viewModel!!.getNoticeData()
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

        alertDialog = builder.create()
        alertDialog!!.show()
    }


    override fun onNetworkStateChange(isAvailable: Boolean) {
        when (isAvailable) {
            true -> {
                viewModel!!.getFBAllNotice()
            }
            else -> {
                viewModel!!.getAllNotice()
            }
        }
    }

    private fun getNoticeAdapter() =
            MyNoticeRecyclerAdapter(object : RecyclerViewClickListener {
                override fun onClick(view: View, position: Int) {
                    val notice = viewModel!!.noticeList.value!![position]
                    val intent = Intent().putExtra(NoticeDetailActivity.NOTICE_OBJECT, notice)
                            .putExtra(NoticeDetailActivity.IS_FROM_NOTIFICATION_INTENT, false)
                            .setClass(activity!!, NoticeDetailActivity::class.java)
                    startActivity(intent)
                }
            })

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
        Log.v(tag, "MyNoticeFragment Error $throwable")
        if (throwable is java.util.NoSuchElementException) {
            return
        }
        activity!!.toast(errorMsg)
    }
}