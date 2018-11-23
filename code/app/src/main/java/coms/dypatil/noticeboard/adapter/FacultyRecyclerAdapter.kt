package coms.dypatil.noticeboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.util.FileUtil
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.item_faculty_row.view.*

class FacultyRecyclerAdapter(private val clickListener: RecyclerViewClickListener) : RecyclerView.Adapter<FacultyRecyclerAdapter.FacultyViewHolder>() {

    private var facultyData = arrayListOf<Faculty>()
    private var isFromFB: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = inflater.inflate(R.layout.item_faculty_row, parent, false)
        return FacultyViewHolder(layout, clickListener)
    }


    override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
        holder.bind(facultyData[position], isFromFB)
    }

    override fun getItemCount(): Int = facultyData.size


    fun setData(newData: ArrayList<Faculty>, isFromFB: Boolean) {
        this.isFromFB = isFromFB
        if (facultyData.size > 0) {
            val postDiffCallback = PostDiffCallback(facultyData, newData)
            val diffResult = DiffUtil.calculateDiff(postDiffCallback)

            facultyData.clear()
            facultyData.addAll(newData)
            diffResult.dispatchUpdatesTo(this)
        } else {
            facultyData.addAll(newData)
        }
        notifyDataSetChanged()
    }


    class FacultyViewHolder(private val layout: View, private val clickListener: RecyclerViewClickListener) : RecyclerView.ViewHolder(layout), View.OnClickListener {

        private var txtName = layout.txt_issuer_name
        private var txtDesignation = layout.txt_designation
        private var imgProfilePic = layout.img_circle_profile_pic

        fun bind(faculty: Faculty, isFromFB: Boolean) {

            txtName.text = faculty.name
            txtDesignation.text = faculty.designation
            if (isFromFB) {
                Glide.with(imgProfilePic)
                        .load(faculty.profilePicUrl!!)
                        .into(imgProfilePic)
            } else {
                val profilePicName = ProfileBuilderUtil.getProfilePicNameFromUrl(faculty.profilePicUrl!!)
                val file = FileUtil.getProfilePicFile("faculty", profilePicName)
                if (file.exists()) {
                    Glide.with(imgProfilePic)
                            .load(file)
                            .into(imgProfilePic)
                }
            }
            layout.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            clickListener.onClick(view!!, adapterPosition)
        }
    }

    internal inner class PostDiffCallback(private val oldList: List<Faculty>, private val newList: List<Faculty>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].fbUserId===newList[newItemPosition].fbUserId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition]==newList[newItemPosition]
        }

/*
        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
*/
    }


}