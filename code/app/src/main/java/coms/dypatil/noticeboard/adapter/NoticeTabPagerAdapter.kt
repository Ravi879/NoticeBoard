package coms.dypatil.noticeboard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import coms.dypatil.noticeboard.ui.fragment.ExpiredNoticeFragment
import coms.dypatil.noticeboard.ui.fragment.NoticeFragment


class NoticeTabPagerAdapter(fm: FragmentManager, private val tabNames: Array<String>) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment? {

        when (position) {
            0 -> return NoticeFragment.newInstance()
            1 -> return ExpiredNoticeFragment.newInstance()
        }

        return null
    }

    override fun getCount(): Int = tabNames.size

    override fun getPageTitle(position: Int): CharSequence = tabNames[position]


}
