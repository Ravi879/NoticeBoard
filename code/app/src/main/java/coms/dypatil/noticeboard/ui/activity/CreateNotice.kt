package coms.dypatil.noticeboard.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.data.datastore.persistence.entity.Notice
import coms.dypatil.noticeboard.domain.service.FBConfigService
import coms.dypatil.noticeboard.util.*
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.viewmodel.CreateNoticeVM
import coms.dypatil.noticeboard.viewmodel.contracts.CreateNoticeContract
import kotlinx.android.synthetic.main.activity_create_notice.*

class CreateNotice : AppCompatActivity(), CreateNoticeContract {

    private lateinit var viewModel: CreateNoticeVM
    private var notice: Notice? = null

    private lateinit var networkSnackBar: Snackbar

    companion object {
        const val NOTICE = "notice_object"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notice)

        setUpToolBar()

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        viewModel = ViewModelProviders.of(this).get(CreateNoticeVM::class.java)
        viewModel.createNoticeContract = this

        viewModel.response.observe(this, Observer { processResponse(it!!) })

        intent.extras?.let { bundle -> setNoticeFields(bundle) }
    }

    private fun setNoticeFields(bundle: Bundle) {
        if (bundle.containsKey(NOTICE)) {
            viewModel.isUpdateNotice = true
            notice = bundle.getParcelable(NOTICE)
            edt_notice_title.setText(notice!!.title)
            edt_notice_description.setText(notice!!.description)

            val date = DateUtil.getDateFromMillis(notice!!.lastDate!!)
            edt_notice_last_date.setText(date)

            btn_send_notice.text = getString(R.string.btn_update_notice)
        }
    }

    fun onClickButton(view: View) {
        when (view.id) {
            R.id.btn_send_notice -> {
                if (isNetworkNotAvailable())
                    return

                if (!viewModel.isUpdateNotice) {
                    viewModel.fbNotice.apply {
                        title = edt_notice_title.getString()
                        description = edt_notice_description.getString()
                        issueDate = System.currentTimeMillis()
                    }
                    viewModel.apply { startSendingProcess(fbNotice) }
                } else {
                    viewModel.fbNotice.apply {
                        title = edt_notice_title.text.toString()
                        description = edt_notice_description.text.toString()
                        issueDate = notice!!.issueDate
                        issuerId = notice!!.issuerId
                        lastDate = notice!!.lastDate
                    }
                    viewModel.apply { updateNotice(fbNotice, notice!!.fbNoticeId) }
                }

            }

            R.id.btn_calender ->
                getDatePickerDialog().show()
        }

    }

    private fun isNetworkNotAvailable(): Boolean =
            if (!App.isNetworkAvailable.value!!) {
                toast("Unable to send notice,Please turn on internet connection.")
                true
            } else
                false


    override fun invalidCredential(field: String, errorMsg: String) {
        val editText: EditText? = when (field) {
            "title" -> edt_notice_title
            "description" -> edt_notice_description
            "lastDate" -> edt_notice_last_date
            else -> null
        }
        if (editText!=null) {
            editText.error = errorMsg
            if (field!="lastDate") editText.requestFocus()
        }
    }

    private fun getDatePickerDialog(): DatePickerDialog {
        val currentDate: Map<String, Int> = DateUtil.getCurrentDate()

        return DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _: DatePicker, year, month, dayOfMonth ->
            val lastDate: Long? = DateUtil.getMillis(dayOfMonth, month, year)
            if (lastDate==null) {
                toast("Please select the valid last date for notice.")
                return@OnDateSetListener
            }
            viewModel.fbNotice.lastDate = lastDate
            val date = DateUtil.formatDate(dayOfMonth, month, year - 1900)
            edt_notice_last_date.setText(date)

        }, currentDate[DateUtil.YEAR]!!, currentDate[DateUtil.MONTH]!!, currentDate[DateUtil.DAY]!!)

    }

    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> renderDataState(response.data!!)
            Status.ERROR -> renderErrorState(response.error!!, response.data!!)
        }
    }

    override fun onNetworkStateChange(isAvailable: Boolean) {
        when (isAvailable) {
            true -> {
                networkSnackBar.dismiss()
                val intent = Intent(applicationContext, FBConfigService::class.java)
                startService(intent)
            }
            else -> networkSnackBar.show()
        }
    }

    private fun setUpToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.app_bar)
        toolbar.title = "Create Notice"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun renderLoadingState() {
        disableWindowTouch()
        loading_indicator.visibility = View.VISIBLE
    }

    private fun renderDataState(msg: String) {
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        toast(msg)
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(tag, " CreateNotice Error " + throwable.toString())
        toast(errorMsg)
    }

    private fun showWaitMsg() {
        toast("Please wait until notice is posted, Do not press back button")
    }

    override fun onBackPressed() {
        if (viewModel.response.value?.status==Status.LOADING)
            showWaitMsg()
        else
            super.onBackPressed()
    }


    private fun disableWindowTouch() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}