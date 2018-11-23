package coms.dypatil.noticeboard.ui.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.domain.service.FBConfigService
import coms.dypatil.noticeboard.ui.fragment.UserProfileFragment
import coms.dypatil.noticeboard.util.*
import coms.dypatil.noticeboard.util.AdapterUtil.Companion.getAdapter
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.viewmodel.EditProfileVM
import coms.dypatil.noticeboard.viewmodel.contracts.EditProfileContract
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), EditProfileContract {

    private lateinit var editProfileVM: EditProfileVM

    private lateinit var networkSnackBar: Snackbar

    companion object {
        private const val PROFILE_IMG_REQUEST_CODE: Int = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        setToolbar()

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        editProfileVM = ViewModelProviders.of(this).get(EditProfileVM::class.java)
        editProfileVM.contract = this
        if (editProfileVM.profileBuilderUtil==null)
            editProfileVM.profileBuilderUtil = ProfileBuilderUtil(editProfileVM.contract, isEditProfile = true)

        spn_department.adapter = getAdapter(editProfileVM.getAllDeptList())
        spn_designation.adapter = getAdapter(editProfileVM.getTempDesiList())
        spn_year.adapter = getAdapter(editProfileVM.getYearList())

        val bundle: Bundle? = intent.extras
        if (bundle!!.containsKey(UserProfileFragment.profileData)) {
            editProfileVM.userProfile = bundle.getParcelable(UserProfileFragment.profileData)!!
            setFormFields(editProfileVM.userProfile)
        }

        editProfileVM.deptIndex.observe(this, editProfileVM.getDeptObserver())
        editProfileVM.desiIndex.observe(this, editProfileVM.getDesiObserver())
        editProfileVM.response.observe(this, Observer { processResponse(it!!) })

    }

    override fun onStart() {
        super.onStart()
        spn_department.onItemSelectedListener = spnListen()
        spn_designation.onItemSelectedListener = spnListen()
        spn_year.onItemSelectedListener = spnListen()
    }

    override fun onStop() {
        super.onStop()
        spn_department.onItemSelectedListener = null
        spn_designation.onItemSelectedListener = null
        spn_year.onItemSelectedListener = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==PROFILE_IMG_REQUEST_CODE && resultCode==Activity.RESULT_OK && data!=null) {
            editProfileVM.isProfilePicChange = true
            val bitmap = editProfileVM.getProfilePicBitmap(data.data!!)
            img_user_profile_pic.setImageBitmap(bitmap)
        }
    }

    fun btnClick(view: View) {
        when (view.id) {
            R.id.img_user_profile_pic -> startActivityForResult(editProfileVM.getImagePickerIntent(), PROFILE_IMG_REQUEST_CODE)
            R.id.img_btn_calendar -> getDatePickerDialog().show()
            R.id.btn_save_profile -> {
                if (!App.isNetworkAvailable.value!!) {
                    toast("Unable to save changes,Please turn on internet connection.")
                    return
                }

                editProfileVM.userRegistration.apply {
                    firstName = edt_first_name.getString()
                    lastName = edt_last_name.getString()
                    email = edt_email.getString()
                    gender = if (rbtn_male.isChecked) "Male" else "Female"
                    dob = edt_dob.getString()
                    phoneNo = edt_phone_no.getString()
                    password = edt_password.getString()

                    if (editProfileVM.isDeptSpnChange)
                        department = editProfileVM.getAllDeptList()[editProfileVM.deptIndex.value!!]
                    if (editProfileVM.isDesiSpnChange)
                        designation = editProfileVM.desiIndex.value!!

                    if (editProfileVM.isYearSpnChange)
                        year = editProfileVM.yearIndex

                    userType = if (!layout_year.isVisible) "faculty" else "student"

                    profilePicUrl = editProfileVM.userProfile.profilePicUrl

                    mimeType = editProfileVM.profilePicMimeType

                }.let { userRegistration ->
                    editProfileVM.userRegistration = userRegistration
                    editProfileVM.validateProfileDetails(userRegistration)
                }

            }
        }
    }

    override fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
                .setTitle("Update Confirmation")
                .setMessage("You are no longer able to modify your previously posted notice, due to changes in department or designation.")
                .setPositiveButton("OK") { _, _ ->
                    editProfileVM.apply {
                        startProfileUpdateProcess()
                    }
                }.setNegativeButton("CANCEL") { dialog: DialogInterface, _ ->
                    dialog.cancel()
                }
        builder.show()
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

    override fun setSpnYearVisibility(visibility: Int) {
        layout_year.visibility = visibility
    }

    override fun setDesignationAdapter(adapter: ArrayAdapter<String>) {
        spn_designation.adapter = adapter
    }

    override fun setYearAdapter(adapter: ArrayAdapter<String>) {
        spn_year.adapter = adapter
    }

    private fun spnListen(): AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        var flagSpnHack = false
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (!flagSpnHack) {
                flagSpnHack = true
                return
            }

            if (parent!=null && view!=null && flagSpnHack) {
                when (parent.id) {
                    R.id.spn_department -> {
                        editProfileVM.isDeptSpnChange = true
                        editProfileVM.deptIndex.value = position
                    }
                    R.id.spn_designation -> {
                        editProfileVM.isDesiSpnChange = true
                        editProfileVM.desiIndex.value = parent.getItemAtPosition(position).toString()
                    }
                    R.id.spn_year -> {
                        editProfileVM.isYearSpnChange = true
                        editProfileVM.yearIndex = parent.getItemAtPosition(position).toString()
                    }
                }
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
        }

    }

    private fun setFormFields(profile: UserProfile) {
        val name: List<String> = profile.name!!.split(" ")
        edt_first_name.setText(name[0])
        edt_last_name.setText(name[1])
        edt_email.setText(profile.email)
        when (profile.gender) {
            "Male" -> rbtn_male.isChecked = true
            "Female" -> rbtn_female.isChecked = true
        }
        edt_dob.setText(profile.dob)
        edt_phone_no.setText(profile.phoneNo)
        //password = edt_password.getString()

        edt_password.setText(profile.password)

        if (profile.profilePicFile!!.exists()) {
            val requestOption = RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
            Glide.with(this)
                    .load(profile.profilePicFile!!)
                    .apply(requestOption)
                    .into(img_user_profile_pic)
        } else {
            toast("Profile pic is not found")
        }

        val deptIndex = editProfileVM.getUserDeptIndex(profile.department!!)
        spn_department.setSelection(deptIndex)

        val desiIndex = editProfileVM.getUserDesiIndex(profile.designation!!)
        spn_designation.setSelection(desiIndex)

        editProfileVM.userRegistration.department = profile.department
        editProfileVM.userRegistration.designation = profile.designation




        if (profile.year!=null) {
            setSpnYearVisibility(View.VISIBLE)
            val index: Int = editProfileVM.getUserYearIndex(profile.designation!!, profile.year!!)
            editProfileVM.userRegistration.year = profile.year
            spn_year.setSelection(index)
        } else {
            setSpnYearVisibility(View.GONE)
        }
    }


    private fun setToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.app_bar)
        toolbar.title = ""
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


    private fun renderLoadingState() {
        disableWindowTouch()
        loading_indicator.visibility = View.VISIBLE
    }

    private fun renderDataState(msg: String) {
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        toast(msg)
        finish()
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        throwable.printStackTrace()
        if (throwable is FirebaseAuthUserCollisionException) {
            toast("Unable to save changes")
            invalidCredential("email", "Email address already register")
            Log.v(tag, " EditProfileActivity Error  $throwable")
        }

        toast(errorMsg)
    }

    private fun disableWindowTouch() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}