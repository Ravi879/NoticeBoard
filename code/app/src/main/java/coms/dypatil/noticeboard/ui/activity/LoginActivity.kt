package coms.dypatil.noticeboard.ui.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.service.FBConfigService
import coms.dypatil.noticeboard.util.*
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.viewmodel.LoginVM
import coms.dypatil.noticeboard.viewmodel.contracts.LoginContract
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginContract {

    private lateinit var viewModel: LoginVM
    private var permissionUtil: PermissionUtil? = null

    private lateinit var networkSnackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isUserAlreadySignIN()?.let { intent ->
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_login)

        permissionUtil = PermissionUtil(this)

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        viewModel = ViewModelProviders.of(this).get(LoginVM::class.java)
        viewModel.loginContract = this
        viewModel.response.observe(this, Observer { processResponse(it!!) })

    }

    override fun onStart() {
        super.onStart()
        permissionUtil!!.isPermissionGranted(PermissionUtil.WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionUtil!!.onRequestResult(requestCode, permissions, grantResults, PermissionUtil.WRITE_EXTERNAL_STORAGE)
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

    override fun invalidCredential(field: String, errorMsg: String) {
        val editText: EditText? = when (field) {
            "email" -> edt_email
            "password" -> edt_password
            else -> null
        }
        if (editText!=null) {
            editText.setError(errorMsg, getEditTextErrorIcon())
            editText.requestFocus()
        }
    }

    fun onClickButton(view: View) {
        when (view.id) {
            R.id.btn_login -> {
                if (!App.isNetworkAvailable.value!!) {
                    toast("Unable to login,Please turn on internet connection.")
                    return
                }

                val email = edt_email.text.toString().trim()
                val password = edt_password.text.toString().trim()
                viewModel.startLoginProcess(email, password)
            }
            R.id.btn_sign_up -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GPSAvailability.isGooglePlayServicesAvailable(this)
    }

    private fun isUserAlreadySignIN(): Intent? {
        val userType = App.preference.spUserType
        return if (userType=="faculty" || userType=="student")
            Intent(this, HomeActivity::class.java)
        else
            null
    }


    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> loginSuccess(response.data!!)
            Status.ERROR -> loginError(response.error!!, response.data!!)
        }
    }

    private fun renderLoadingState() {
        disableWindowTouch()
        loading_indicator.visibility = View.VISIBLE
    }

    private fun loginSuccess(msg: String) {
        viewModel.subscribeMessageTopic()
        toast(msg)
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun loginError(throwable: Throwable, errorMsg: String) {
        enableWindowTouch()
        loading_indicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(tag, "LoginActivity Error " + throwable.toString())
        toast(errorMsg)
    }

    private fun disableWindowTouch() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun enableWindowTouch() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun getEditTextErrorIcon(): Drawable {
        val icon = AppCompatResources.getDrawable(this, R.drawable.ic_error_yellow)
        icon!!.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        return icon
    }

    private fun showWaitMsg(){
        toast("Please wait until login process is complete, Do not press back button")
    }

    override fun onBackPressed() {
        if(viewModel.response.value?.status == Status.LOADING)
            showWaitMsg()
        else
            super.onBackPressed()
    }

    override fun onDestroy() {
        permissionUtil = null
        super.onDestroy()
    }
}


