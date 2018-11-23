package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.adapter.NoticeRecyclerAdapter
import coms.dypatil.noticeboard.ui.activity.NoticeDetailActivity
import coms.dypatil.noticeboard.viewmodel.NoticeVM
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.fragment_notice.view.*


class NoticeFragment : Fragment() {

    companion object {
        fun newInstance(): NoticeFragment {
            return NoticeFragment()
        }
    }

    private var viewModel: NoticeVM? = null
    private lateinit var noticeAdapter: NoticeRecyclerAdapter
    private lateinit var filterNoticeListObserver: Observer<Int>

    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val layout = inflater.inflate(R.layout.fragment_notice, container, false)

        noticeAdapter = getNoticeAdapter()

        viewModel = ViewModelProviders.of(parentFragment!!.parentFragment!!).get(NoticeVM::class.java)

        layout.rv_notice.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = noticeAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel!!.IS_NOTICE_FRAGMENT_VISIBLE = true
        viewModel!!.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE = false

        filterNoticeListObserver = Observer {
            viewModel!!.loadFilterLocalNoticeData()
        }
        viewModel!!.noticeList.observe(this, Observer {
            noticeAdapter.setData(it!!)
        })

        return layout
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (viewModel!=null) {
            if (isVisibleToUser) {
                viewModel!!.IS_NOTICE_FRAGMENT_VISIBLE = true
                viewModel!!.IS_EXPIRED_NOTICE_FRAGMENT_VISIBLE = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel!!.FLAG_APPLY_NOTICE_FILTER.observe(this, filterNoticeListObserver)
        viewModel!!.loadLocalAllNotice()
    }

    override fun onStop() {
        super.onStop()
        viewModel!!.FLAG_APPLY_NOTICE_FILTER.removeObserver(filterNoticeListObserver)
    }

    private fun getNoticeAdapter() =
            NoticeRecyclerAdapter(object : RecyclerViewClickListener {
                override fun onClick(view: View, position: Int) {
                    val notice = viewModel!!.noticeList.value!![position]
                    val intent = Intent().putExtra(NoticeDetailActivity.NOTICE_OBJECT, notice)
                            .putExtra(NoticeDetailActivity.IS_FROM_NOTIFICATION_INTENT, false)
                            .setClass(activity!!, NoticeDetailActivity::class.java)

                    startActivity(intent)

                }
            })

}
