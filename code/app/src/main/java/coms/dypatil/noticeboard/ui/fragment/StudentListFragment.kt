package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
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
import androidx.recyclerview.widget.DefaultItemAnimator
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.adapter.StudentRecyclerAdapter
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Student
import coms.dypatil.noticeboard.ui.activity.UserListProfileActivity
import coms.dypatil.noticeboard.util.AdapterUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.replaceFragment
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.StudentListVM
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.layout_user_list.view.*


class StudentListFragment : Fragment() {

    private var twoPane: Boolean = false
    private var viewModel: StudentListVM? = null

    private lateinit var studentAdapter: StudentRecyclerAdapter

    private var alertDialog: AlertDialog? = null
    private lateinit var spnDeptFilter: Spinner
    private lateinit var spnDesiFilter: Spinner
    private var spnSelectionHack = true

    companion object {
        const val tag = "StudentListFragment"
        fun newInstance(): StudentListFragment {
            return StudentListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setMenuVisibility(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity!! as AppCompatActivity).supportActionBar?.title = "Student"

        val layout = inflater.inflate(R.layout.fragment_user_list, container, false)

        viewModel = ViewModelProviders.of(this).get(StudentListVM::class.java)
        viewModel!!.response.observe(this, Observer { processResponse(it!!) })

        studentAdapter = getStudentAdapter()

        if (layout.findViewById<View>(R.id.user_detail_container)!=null) {
            twoPane = true
        }

        with(layout.rv_user_list) {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = studentAdapter
        }

        viewModel!!.studentList.observe(this, Observer { list: ArrayList<Student>? ->
            studentAdapter.setData(list!!, App.isNetworkAvailable.value!!)
        })

        filterStudentList()
        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
            when (item!!.itemId) {

                R.id.action_filter -> {
                    filterStudentList()
                    true
                }

                else -> false

            }

    private fun getFilterDialog(): AlertDialog {
        val builder = AlertDialog.Builder(activity!!)
        val layoutView = layoutInflater.inflate(R.layout.dialog_filter, null)

        spnDeptFilter = layoutView.spn_department
        spnDesiFilter = layoutView.spn_designation

        spnDeptFilter.adapter = AdapterUtil.getAdapter(viewModel!!.spnDeptList)
        spnDesiFilter.adapter = AdapterUtil.getAdapter(viewModel!!.spnYearList[0])

        spnDeptFilter.onItemSelectedListener = spnListen()
        spnDesiFilter.onItemSelectedListener = spnListen()

        val spnObserver = getDeptSpnObserver(spnDesiFilter)
        viewModel!!.mFilterDeptIndex.observe(this, spnObserver)

        builder.setView(layoutView)
                .setPositiveButton("Filter") { dialog, _ ->
                    if (App.isNetworkAvailable.value!!)
                        viewModel!!.getFBStudentData()
                    else
                        viewModel!!.getStudentData()

                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setOnDismissListener {
                    viewModel!!.mFilterDeptIndex.removeObserver(spnObserver)
                }

        return builder.create()
    }

    private fun getDeptSpnObserver(spnDesiFilter: Spinner): Observer<Int?> = Observer { position: Int? ->
        spnDesiFilter.adapter = viewModel!!.getSpnYearAdapter(position!!)
    }


    private fun filterStudentList() {

        if (alertDialog!=null) {
            val spnObserver = getDeptSpnObserver(spnDesiFilter)
            viewModel!!.mFilterDeptIndex.observe(this, spnObserver)

            val deptIndex = viewModel!!.mFilterDeptIndex.value ?: 0
            val desiIndex = viewModel!!.mFilterYearIndex

            spnDeptFilter.setSelection(deptIndex)
            spnSelectionHack = false
            spnDesiFilter.setSelection(desiIndex)

            alertDialog!!.show()
            return
        }

        alertDialog = getFilterDialog()
        alertDialog!!.show()
    }


    private fun spnListen(): AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (parent!=null && view!=null) {
                when (parent.id) {

                    R.id.spn_department -> {
                        viewModel!!.mFilterDeptIndex.value = position
                    }
                    R.id.spn_designation -> {
                        if (spnSelectionHack) {
                            viewModel!!.mFilterYear = parent.getItemAtPosition(position).toString()
                            viewModel!!.mFilterYearIndex = position
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

    private fun getStudentAdapter(): StudentRecyclerAdapter =
            StudentRecyclerAdapter(object : RecyclerViewClickListener {
                override fun onClick(view: View, position: Int) {
                    val student: Student = viewModel!!.studentList.value!![position]

                    if (twoPane) {
                        val userListProfileFragment = UserListProfileFragment().apply {
                            arguments = Bundle().also { bundle ->
                                bundle.putString(UserListProfileFragment.FACULTY_ID, student.fbUserId)
                                bundle.putString(UserListProfileFragment.IS_FROM, "student")
                            }
                        }
                        replaceFragment(R.id.user_detail_container, userListProfileFragment, UserListProfileFragment.TAG)
                    } else {
                        val intent = Intent().putExtra(UserListProfileFragment.FACULTY_ID, student.fbUserId)
                                .putExtra(UserListProfileFragment.IS_FROM, "student")
                                .setClass(activity!!, UserListProfileActivity::class.java)
                        startActivity(intent)

                    }
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
        loading_indicator.visibility = View.VISIBLE
    }

    private fun renderDataState(msg: String?) {
        loading_indicator.visibility = View.GONE
        msg?.let { activity!!.toast(it) }
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        loading_indicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(tag, " StudentListFragment Error " + throwable.toString())
        activity!!.toast(errorMsg)
    }

}