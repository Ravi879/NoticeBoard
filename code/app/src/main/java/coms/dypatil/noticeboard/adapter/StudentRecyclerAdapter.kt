package coms.dypatil.noticeboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.util.FileUtil
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.item_student_row.view.*


class StudentRecyclerAdapter(private val clickListener: RecyclerViewClickListener) : RecyclerView.Adapter<StudentRecyclerAdapter.StudentViewHolder>() {

    private var studentData = arrayListOf<Student>()
    private var isFromFB: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = inflater.inflate(R.layout.item_student_row, parent, false)
        return StudentViewHolder(layout, clickListener)
    }


    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(studentData[position], isFromFB)
    }

    override fun getItemCount(): Int = studentData.size


    fun setData(newData: ArrayList<Student>, isFromFB: Boolean) {
        this.isFromFB = isFromFB

        if (studentData.size > 0) {
            val postDiffCallback = PostDiffCallback(studentData, newData)
            val diffResult = DiffUtil.calculateDiff(postDiffCallback)

            studentData.clear()
            studentData.addAll(newData)
            diffResult.dispatchUpdatesTo(this)
        } else {
            // first initialization
            studentData.addAll(newData)
        }
        notifyDataSetChanged()
    }


    class StudentViewHolder(private val layout: View, private val clickListener: RecyclerViewClickListener) : RecyclerView.ViewHolder(layout), View.OnClickListener {

        private var txtName = layout.txt_issuer_name
        private val txtYear = layout.txt_year
        private var imgProfilePic = layout.img_circle_profile_pic

        fun bind(student: Student, isFromFB: Boolean) {
            txtName.text = student.name
            txtYear.text = student.year
            if (isFromFB) {
                Glide.with(imgProfilePic)
                        .load(student.profilePicUrl!!)
                        .into(imgProfilePic)
            } else {
                val profilePicName = ProfileBuilderUtil.getProfilePicNameFromUrl(student.profilePicUrl!!)
                val file = FileUtil.getProfilePicFile("student", profilePicName)
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

    internal inner class PostDiffCallback(private val oldList: List<Student>, private val newList: List<Student>) : DiffUtil.Callback() {

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
    }


}