package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import com.github.ybq.android.spinkit.SpinKitView
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.adapter.FacultyRecyclerAdapter
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Faculty
import coms.dypatil.noticeboard.ui.activity.UserListProfileActivity
import coms.dypatil.noticeboard.util.AdapterUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.replaceFragment
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.FacultyListVM
import coms.dypatil.noticeboard.viewmodel.contracts.RecyclerViewClickListener
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.fragment_user_list.view.*
import kotlinx.android.synthetic.main.dialog_filter.view.*
import kotlinx.android.synthetic.main.layout_user_list.view.*

class FacultyListFragment : Fragment() {

    private var twoPane: Boolean = false
    private var viewModel: FacultyListVM? = null

    private lateinit var facultyAdapter: FacultyRecyclerAdapter

    private lateinit var loadingIndicator: SpinKitView

    private var alertDialog: AlertDialog? = null
    private lateinit var spnDeptFilter: Spinner
    private lateinit var spnYearFilter: Spinner
    private var spnSelectionHack = true

    companion object {
        const val tag = "FacultyListFragment"
        fun newInstance(): FacultyListFragment {
            return FacultyListFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setMenuVisibility(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        (activity!! as AppCompatActivity).supportActionBar?.title = "Faculty"

        val layout = inflater.inflate(R.layout.fragment_user_list, container, false)
        loadingIndicator = layout.loading_indicator

        viewModel = ViewModelProviders.of(this).get(FacultyListVM::class.java)
        viewModel!!.response.observe(this, Observer { processResponse(it!!) })

        facultyAdapter = getFacultyAdapter()

        if (layout.findViewById<View>(R.id.user_detail_container)!=null)
            twoPane = true

        with(layout.rv_user_list) {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = facultyAdapter
        }

        viewModel!!.facultyList.observe(this, Observer { list: ArrayList<Faculty>? ->
            facultyAdapter.setData(list!!, App.isNetworkAvailable.value!!)
        })

        filterFacultyList()

        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

            R.id.action_filter -> {
                filterFacultyList()
                return true
            }

        }
        return false
    }

    private fun getFilterDialog(): AlertDialog {
        val builder = AlertDialog.Builder(activity!!)
        val layoutView = layoutInflater.inflate(R.layout.dialog_filter, null)

        spnDeptFilter = layoutView.spn_department
        spnYearFilter = layoutView.spn_designation

        spnDeptFilter.adapter = AdapterUtil.getAdapter(viewModel!!.spnDeptList)
        spnYearFilter.adapter = AdapterUtil.getAdapter(viewModel!!.spnDesiList[0])

        spnDeptFilter.onItemSelectedListener = spnListen()
        spnYearFilter.onItemSelectedListener = spnListen()

        val spnObserver = getDeptSpnObserver(spnYearFilter)
        viewModel!!.mFilterDeptIndex.observe(this, spnObserver)

        builder.setView(layoutView)
                .setPositiveButton("Filter") { dialog, _ ->
                    if (App.isNetworkAvailable.value!!)
                        viewModel!!.getFBFacultyData()
                    else
                        viewModel!!.getLocalFacultyData()

                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setOnDismissListener {
                    viewModel!!.mFilterDeptIndex.removeObserver(spnObserver)
                }

        return builder.create()
    }

    private fun getDeptSpnObserver(spnDesiFilter: Spinner): Observer<Int?> = Observer { position: Int? ->
        spnDesiFilter.adapter = viewModel!!.getSpnDesiAdapter(position!!)
    }

    private fun filterFacultyList() {
        if (alertDialog!=null) {
            val spnObserver = getDeptSpnObserver(spnYearFilter)
            viewModel!!.mFilterDeptIndex.observe(this, spnObserver)

            val deptIndex = viewModel!!.mFilterDeptIndex.value ?: 0
            val desiIndex = viewModel!!.mFilterDesiIndex

            spnDeptFilter.setSelection(deptIndex)
            spnSelectionHack = false
            spnYearFilter.setSelection(desiIndex)

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
                            viewModel!!.mFilterDesi = parent.getItemAtPosition(position).toString()
                            viewModel!!.mFilterDesiIndex = position
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

    private fun getFacultyAdapter(): FacultyRecyclerAdapter =
            FacultyRecyclerAdapter(object : RecyclerViewClickListener {
                override fun onClick(view: View, position: Int) {
                    val faculty: Faculty = viewModel!!.facultyList.value!![position]

                    if (twoPane) {
                        val userListProfileFragment = UserListProfileFragment().apply {
                            arguments = Bundle().also { bundle ->
                                bundle.putString(UserListProfileFragment.FACULTY_ID, faculty.fbUserId)
                                bundle.putString(UserListProfileFragment.IS_FROM, "faculty")
                            }
                        }
                        replaceFragment(R.id.user_detail_container, userListProfileFragment, UserListProfileFragment.TAG)
                    } else {
                        val intent = Intent().putExtra(UserListProfileFragment.FACULTY_ID, faculty.fbUserId)
                                .putExtra(UserListProfileFragment.IS_FROM, "faculty")
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
        activity!!.toast(errorMsg)
    }


}