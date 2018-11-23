package coms.dypatil.noticeboard.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.ui.fragment.UserListProfileFragment
import coms.dypatil.noticeboard.util.DateUtil
import coms.dypatil.noticeboard.util.NetworkUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.tag
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.NoticeDetailVM
import coms.dypatil.noticeboard.viewmodel.contracts.NetworkStateCallBack
import kotlinx.android.synthetic.main.activity_notice_detail.*

class NoticeDetailActivity : AppCompatActivity(), NetworkStateCallBack {

    companion object {
        const val NOTICE_OBJECT = "notice_object"
        const val IS_FROM_NOTIFICATION_INTENT = "from_notification"
    }

    var notice: Notice? = null
    private lateinit var viewModel: NoticeDetailVM
    private lateinit var networkSnackBar: Snackbar
    private var isFromNotificationIntent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_detail)

        setUpToolbar()

        viewModel = ViewModelProviders.of(this).get(NoticeDetailVM::class.java)

        viewModel.response.observe(this, Observer { processResponse(it!!) })


        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        val bundle = intent.extras!!
        if (bundle.containsKey(NOTICE_OBJECT)) {
            notice = bundle.getParcelable(NOTICE_OBJECT)
            isFromNotificationIntent = bundle.getBoolean(IS_FROM_NOTIFICATION_INTENT)
            setUpFields(notice!!)
            viewModel.setNoticeIsOpen(notice!!.fbNoticeId)
        } else {
            finish()
        }
    }

    private fun setUpFields(notice: Notice) {
        txt_title.text = notice.title
        txt_description.text = notice.description
        txt_date.text = DateUtil.getDateFromMillis(notice.issueDate!!)
        txt_last_date.text = DateUtil.getDateFromMillis(notice.lastDate!!)
        txt_issuer_name.text = notice.issuerName
    }

    fun btnClick(view: View) {
        val intent = Intent(this, UserListProfileActivity::class.java)
                .putExtra(UserListProfileFragment.FACULTY_ID, notice!!.issuerId)
                .putExtra(UserListProfileFragment.IS_FROM, "faculty")
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (notice!!.issuerId==viewModel.fbUserId) {
            menuInflater.inflate(R.menu.menu_edit_notice, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    if (isFromNotificationIntent)
                        startHomeActivity()
                    else
                        navigateUpTo(Intent(this, HomeActivity::class.java))

                    true
                }

                R.id.action_edit_notice -> {
                    val intent = Intent(this, CreateNotice::class.java)
                            .putExtra(CreateNotice.NOTICE, notice)
                    startActivity(intent)
                    true
                }

                R.id.action_delete_notice -> {
                    if (App.isNetworkAvailable.value!!) {
                        getDeletionConformationDialog().show()
                    } else {
                        toast("No internet connection available")
                    }
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }

    private fun getDeletionConformationDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want to delete this notice?")
                .setPositiveButton("DELETE") { _: DialogInterface?, _: Int ->
                    viewModel.deleteNotice(notice!!.fbNoticeId)
                }.setNegativeButton("CANCEL") { dialog: DialogInterface?, _: Int ->
                    dialog!!.dismiss()
                }
        return builder.create()
    }

    private fun setUpToolbar() {
        val toolbar = app_bar as Toolbar
        toolbar.title = "Notice"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }


    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> renderDataState(response.data!!)
            Status.ERROR -> renderErrorState(response.error!!, response.data!!)
        }
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onNetworkStateChange(isAvailable: Boolean) {
        when (isAvailable) {
            true -> networkSnackBar.dismiss()
            else -> networkSnackBar.show()
        }
    }

    private fun renderLoadingState() {
        loading_indicator.visibility = View.VISIBLE
    }

    private fun renderDataState(msg: String) {
        loading_indicator.visibility = View.GONE
        toast(msg)
        finish()
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        loading_indicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(tag, "Error " + throwable.toString())
        toast(errorMsg)
    }

    override fun onBackPressed() {
        if (isFromNotificationIntent)
            startHomeActivity()
        else
            super.onBackPressed()
    }

}