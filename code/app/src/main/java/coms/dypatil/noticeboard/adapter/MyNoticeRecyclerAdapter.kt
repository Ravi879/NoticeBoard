package coms.dypatil.noticeboard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.util.DateUtil
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.item_my_notice_row.view.*


class MyNoticeRecyclerAdapter(private val clickListener: RecyclerViewClickListener) : RecyclerView.Adapter<MyNoticeRecyclerAdapter.NoticeViewHolder>() {

    private var noticeData = arrayListOf<Notice>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val context = App.getAppContext
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.item_my_notice_row, parent, false)
        return NoticeViewHolder(layout, clickListener)
    }


    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(noticeData[position])
    }

    override fun getItemCount(): Int = noticeData.size


    fun setData(newData: ArrayList<Notice>) {
        if (noticeData.size > 0) {
            val postDiffCallback = PostDiffCallback(noticeData, newData)
            val diffResult = DiffUtil.calculateDiff(postDiffCallback)

            noticeData.clear()
            noticeData.addAll(newData)
            diffResult.dispatchUpdatesTo(this)
        } else {
            // first initialization
            noticeData = newData
            notifyDataSetChanged()
        }
    }

    class NoticeViewHolder(private val layoutView: View, private val clickListener: RecyclerViewClickListener) : RecyclerView.ViewHolder(layoutView), View.OnClickListener {

        private var txtTitle: TextView = layoutView.txt_title
        private var txtDescription: TextView = layoutView.txt_description
        private var txtIssueDate: TextView = layoutView.txt_issue_date
        private var txtLastDate: TextView = layoutView.txt_last_date


        fun bind(noticeAndFaculty: Notice) {
            with(noticeAndFaculty) {
                txtTitle.text = title
                txtDescription.text = description
                txtIssueDate.text = DateUtil.getDateFromMillis(issueDate!!)
                txtLastDate.text = DateUtil.getDateFromMillis(lastDate!!)
            }
            layoutView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (adapterPosition!=RecyclerView.NO_POSITION) {
                clickListener.onClick(view!!, adapterPosition)
            }
        }
    }

    internal inner class PostDiffCallback(private val oldPosts: List<Notice>, private val newPosts: List<Notice>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldPosts.size
        }

        override fun getNewListSize(): Int {
            return newPosts.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPosts[oldItemPosition].fbNoticeId===newPosts[newItemPosition].fbNoticeId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPosts[oldItemPosition]==newPosts[newItemPosition]
        }
    }


}