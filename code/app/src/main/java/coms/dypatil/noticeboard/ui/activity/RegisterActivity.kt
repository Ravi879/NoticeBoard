package coms.dypatil.noticeboard.ui.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.model.UserRegistration
import coms.dypatil.noticeboard.domain.service.FBConfigService
import coms.dypatil.noticeboard.util.*
import coms.dypatil.noticeboard.util.AdapterUtil.Companion.getAdapter
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.viewmodel.RegisterVM
import coms.dypatil.noticeboard.viewmodel.contracts.RegisterContract
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), RegisterContract {

    companion object {
        private const val PROFILE_PIC_REQUEST_CODE: Int = 1
    }

    private lateinit var viewModel: RegisterVM
    private lateinit var networkSnackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setToolbar()

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        viewModel = ViewModelProviders.of(this).get(RegisterVM::class.java)
        viewModel.contract = this

        if (viewModel.profileBuilderUtil==null)
            viewModel.profileBuilderUtil = ProfileBuilderUtil(viewModel.contract, isEditProfile = false)

        viewModel.deptIndex.observe(this, viewModel.getDeptObserver())
        viewModel.desiIndex.observe(this, viewModel.getDesiObserver())

        viewModel.response.observe(this, Observer { processResponse(it!!) })

        spn_department.adapter = getAdapter(viewModel.getAllDeptList())
        spn_designation.adapter = getAdapter(viewModel.getTempDesiList())
        spn_year.adapter = getAdapter(viewModel.getYearList())

    }

    override fun onStart() {
        super.onStart()
        spn_department.onItemSelectedListener = spnListen()
        spn_designation.onItemSelectedListener = spnListen()
        spn_year.onItemSelectedListener = spnListen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==PROFILE_PIC_REQUEST_CODE && resultCode==Activity.RESULT_OK && data!=null) {
            edt_dob.error = null
            val bitmap: Bitmap = viewModel.getProfilePicBitmap(data.data!!)
            img_user_profile.setImageBitmap(bitmap)
        }
    }

    private fun spnListen(): AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            if (parent!=null && view!=null) {
                setSpnItemSelectedTextColor((parent.getChildAt(0) as TextView))

                when (parent.id) {

                    R.id.spn_department -> viewModel.deptIndex.value = position

                    R.id.spn_designation -> viewModel.desiIndex.value = parent.getItemAtPosition(position).toString()

                    R.id.spn_year -> viewModel.yearIndex = parent.getItemAtPosition(position).toString()

                }
            }
        }

        private fun setSpnItemSelectedTextColor(textView: TextView) {
            val color = ContextCompat.getColor(applicationContext, R.color.blue_light)
            textView.setTextColor(color)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }
    }

    fun btnClick(view: View) {
        when (view.id) {
            R.id.img_user_profile -> startActivityForResult(viewModel.getImagePickerIntent(), PROFILE_PIC_REQUEST_CODE)
            R.id.img_btn_calendar -> getDatePickerDialog().show()
            R.id.btn_sign_up -> {
                if (!App.isNetworkAvailable.value!!) {
                    toast("Unable to sign up,Please turn on internet connection.")
                    return
                }

                UserRegistration().apply {
                    firstName = edt_first_name.getString()
                    lastName = edt_last_name.getString()
                    email = edt_email.getString()
                    gender = if (rbtn_male.isChecked) "Male" else "Female"
                    dob = edt_dob.getString()
                    phoneNo = edt_phone_no.getString()
                    password = edt_password.getString()
                    department = viewModel.getAllDeptList()[viewModel.deptIndex.value!!]
                    designation = viewModel.desiIndex.value!!
                    year = viewModel.yearIndex

                    userType = if (!layout_year.isVisible) "faculty" else "student"

                    profilePicUri = viewModel.profilePicUri
                    mimeType = viewModel.profilePicMimeType
                }.let { userRegistration ->
                    viewModel.startSignUpProcess(userRegistration)
                }

            }
        }
    }

    override fun invalidCredential(field: String, errorMsg: String) {
        val editText: EditText? = when (field) {
            "first_name" -> edt_first_name
            "last_name" -> edt_last_name
            "email" -> edt_email
            "phone_no" -> edt_phone_no
            "password" -> edt_password
            else -> null
        }
        if (editText!=null) {
            editText.error =
                    if (editText.getString().length <= 0)
                        "Not allowed to empty."
                    else
                        errorMsg
        }
        if (field=="profilePic") toast(errorMsg)

        if (field=="dob") toast(errorMsg)
    }

    private fun setToolbar() {
        val toolbar = app_bar as Toolbar
        toolbar.title = "Register"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun getDatePickerDialog(): DatePickerDialog {
        val currentDate: Map<String, Int> = DateUtil.getCurrentDate()

        return DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _: DatePicker, year, month, dayOfMonth ->
            val date: String = DateUtil.formatDate(d = dayOfMonth, m = month, y = year - 1900)
            edt_dob.setText(date)
        }, currentDate[DateUtil.YEAR]!!, currentDate[DateUtil.MONTH]!!, currentDate[DateUtil.DAY]!!)

    }

    override fun setSpnYearVisibility(visibility: Int) {
        findViewById<LinearLayout>(R.id.layout_year).visibility = visibility
    }

    override fun setDesignationAdapter(adapter: ArrayAdapter<String>) {
        spn_designation.adapter = adapter
    }

    override fun setYearAdapter(adapter: ArrayAdapter<String>) {
        spn_year.adapter = adapter
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


    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> renderDataState(response.data!!)
            Status.ERROR -> renderErrorState(response.error!!, response.data!!)
        }
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
        Log.v(tag, "Error " + throwable.toString())
        toast(errorMsg)
    }

    private fun showWaitMsg() {
        toast("Please wait sign up process is running, Do not press back button")
    }

    private fun disableWindowTouch() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onBackPressed() {
        if (viewModel.response.value?.status==Status.LOADING)
            showWaitMsg()
        else
            super.onBackPressed()
    }

}
