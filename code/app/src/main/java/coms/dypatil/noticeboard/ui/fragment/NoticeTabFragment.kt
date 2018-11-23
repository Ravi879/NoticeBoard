package coms.dypatil.noticeboard.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.adapter.NoticeTabPagerAdapter
import kotlinx.android.synthetic.main.fragment_notice_tab.view.*


class NoticeTabFragment : Fragment() {

    companion object {

        const val tag = "NoticeTabFragment"

        fun newInstance(): NoticeTabFragment {
            return NoticeTabFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val layout: View = inflater.inflate(R.layout.fragment_notice_tab, container, false)
        val mViewPager: ViewPager = layout.viewPager

        val tabOnGoing = getString(R.string.tab_notice_ongoing)
        val tabExpire = getString(R.string.tab_notice_expired)

        val tabs = arrayOf(tabOnGoing, tabExpire)

        mViewPager.adapter = NoticeTabPagerAdapter(childFragmentManager, tabs)
        mViewPager.offscreenPageLimit = 0

        layout.tabLayout.setupWithViewPager(mViewPager)

        return layout
    }

}