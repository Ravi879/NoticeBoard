package coms.dypatil.noticeboard.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.service.FBConfigService
import coms.dypatil.noticeboard.ui.fragment.UserListProfileFragment
import coms.dypatil.noticeboard.util.NetworkUtil
import coms.dypatil.noticeboard.viewmodel.contracts.UserListProfileContract
import kotlinx.android.synthetic.main.activity_user_list_profile.*

class UserListProfileActivity : AppCompatActivity(), UserListProfileContract {

    private var fromActivity: String? = null

    private lateinit var networkSnackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list_profile)

        setUpToolbar()

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_layout)
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))

        val bundle: Bundle = intent.extras!!
        if (savedInstanceState==null) {
            if (bundle.containsKey(UserListProfileFragment.FACULTY_ID) || bundle.containsKey(UserListProfileFragment.STUDENT_ID)) {
                val facultyDetailFragment = UserListProfileFragment().apply {
                    arguments = bundle
                    fromActivity = arguments?.getString(UserListProfileFragment.IS_FROM)
                }
                supportFragmentManager.beginTransaction()
                        .replace(R.id.user_detail_container, facultyDetailFragment)
                        .commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                android.R.id.home -> {
                    navigateUpTo(Intent(this, HomeActivity::class.java))
                    true
                }
                else -> false
            }

    private fun setUpToolbar() {
        val toolbar = app_bar as Toolbar
        toolbar.title = "Profile"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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


}