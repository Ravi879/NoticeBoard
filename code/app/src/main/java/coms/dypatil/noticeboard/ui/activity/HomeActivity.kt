package coms.dypatil.noticeboard.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.ui.fragment.*
import coms.dypatil.noticeboard.util.NetworkUtil
import coms.dypatil.noticeboard.util.replaceFragment
import coms.dypatil.noticeboard.viewmodel.HomeVM
import coms.dypatil.noticeboard.viewmodel.contracts.NetworkStateCallBack
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.layout_nav_drawer_header.view.*
import java.io.File

class HomeActivity : AppCompatActivity(), NetworkStateCallBack {

    private var mDrawerLayout: DrawerLayout? = null
    private lateinit var homeVM: HomeVM
    private lateinit var toolbar: Toolbar
    private lateinit var networkSnackBar: Snackbar
    private var isTabletLayout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeVM = ViewModelProviders.of(this).get(HomeVM::class.java)

        setUpToolBar()

        if (homeVM.currentFragmentTag!=null)
            updateFragmentContainer(homeVM.currentFragmentTag!!)
        else
            updateFragmentContainer(HomeFragment.tag)

        setUpNavDrawer(toolbar = toolbar)
        setUpNavView()

        networkSnackBar = NetworkUtil.getNetworkSnackBar(root_container)

    }


    override fun onStart() {
        super.onStart()
        setNavHeaderUserDetails()
        if (App.isProfilePicChanged) {
            setNavHeaderIcon()
        }
        App.isNetworkAvailable.observe(this, NetworkUtil.getNetworkObserver(this))
    }

    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout?.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpNavDrawer(toolbar: Toolbar) {
        if (findViewById<View>(R.id.drawer_layout)!=null) {
            mDrawerLayout = findViewById(R.id.root_layout)
            isTabletLayout = false
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationIcon(R.drawable.ic_drawer_menu_white)
        } else {
            isTabletLayout = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toast.makeText(this, "inside if", Toast.LENGTH_SHORT).show()
                window.apply {
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    statusBarColor = ContextCompat.getColor(context, R.color.prestoDarker)
                }
            }
        }
    }

    private fun setUpNavView() {
        val navigationView: NavigationView = navigation_view

        setNavHeader()

        val visibility = App.preference.spUserType=="faculty"

        val navMenu = navigationView.menu
        navMenu.findItem(R.id.nav_item_my_notice).isVisible = visibility

        navMenu.findItem(R.id.nav_item_home).isChecked = true

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->

            if (!isTabletLayout)
                mDrawerLayout?.run { if (isDrawerOpen(GravityCompat.START)) closeDrawer(GravityCompat.START) }

            if (item.itemId!=R.id.nav_item_about)
                item.isChecked = true

            when (item.itemId) {
                R.id.nav_item_home -> updateFragmentContainer(HomeFragment.tag)

                R.id.nav_item_faculty -> updateFragmentContainer(FacultyListFragment.tag)

                R.id.nav_item_student -> updateFragmentContainer(StudentListFragment.tag)

                R.id.nav_item_my_notice -> updateFragmentContainer(MyNoticeFragment.tag)

                R.id.nav_item_profile -> updateFragmentContainer(UserProfileFragment.tag)

                R.id.nav_item_about -> showAboutDialog()

                else -> return@setNavigationItemSelectedListener false
            }

            return@setNavigationItemSelectedListener true
        }
    }

    private fun updateFragmentContainer(tag: String) {
        val fragment: Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if (fragment==null) {
            homeVM.currentFragmentTag = tag
            when (tag) {
                HomeFragment.tag -> {
                    replaceFragment(R.id.root_container, HomeFragment.newInstance(), tag)
                    toolbar.visibility = View.VISIBLE
                }
                UserProfileFragment.tag -> {
                    replaceFragment(R.id.root_container, UserProfileFragment.newInstance(), tag)
                    toolbar.visibility = View.GONE
                }
                MyNoticeFragment.tag -> {
                    replaceFragment(R.id.root_container, MyNoticeFragment.newInstance(), tag)
                    toolbar.visibility = View.VISIBLE
                }
                FacultyListFragment.tag -> {
                    replaceFragment(R.id.root_container, FacultyListFragment.newInstance(), tag)
                    toolbar.visibility = View.VISIBLE
                }
                StudentListFragment.tag -> {
                    replaceFragment(R.id.root_container, StudentListFragment.newInstance(), tag)
                    toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onNetworkStateChange(isAvailable: Boolean) {
        when (isAvailable) {
            true -> networkSnackBar.dismiss()
            else -> networkSnackBar.show()
        }
    }

    override
    fun onBackPressed() {
        if (homeVM.currentFragmentTag!=HomeFragment.tag) {
            updateFragmentContainer(HomeFragment.tag)
            navigation_view.menu.findItem(R.id.nav_item_home).isChecked = true
            return
        }

        if (isTabletLayout) {
            super.onBackPressed()
        } else {
            mDrawerLayout?.run {
                if (isDrawerOpen(GravityCompat.START)) closeDrawer(GravityCompat.START)
                else super.onBackPressed()
            }
        }
    }

    private fun setUpToolBar() {
        toolbar = findViewById(R.id.app_bar)
        setSupportActionBar(toolbar)
    }

    private lateinit var navHeaderLayout: View

    private fun setNavHeader() {
        navHeaderLayout = navigation_view.inflateHeaderView(R.layout.layout_nav_drawer_header)
        setNavHeaderUserDetails()
        setNavHeaderIcon()
    }

    private fun setNavHeaderUserDetails() {
        navHeaderLayout.txt_user_name.text = homeVM.getUserName()
        navHeaderLayout.txt_email.text = homeVM.getUserDepartment()
    }

    private fun setNavHeaderIcon() {
        val profilePicPath: File = homeVM.getProfilePic()
        if (profilePicPath.exists()) {
            val requestOptions = RequestOptions()
                    .signature(ObjectKey(App.preference.spProfilePicSignature))
            Glide.with(this)
                    .load(profilePicPath)
                    .apply(requestOptions)
                    .into(navHeaderLayout.img_nav_header_profile_pic)
        }
    }

    private fun showAboutDialog() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_about, null)

        dialogLayout.btn_share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareSubText = "Get the Noticeboard App"
            val shareBodyText = "hey, check out Noticeboard App. I'm finding it very useful.\nGet it from here:\n https://github.com/Ravi879/NoticeBoard/raw/master/Noticeboard.apk"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText)
            startActivity(Intent.createChooser(shareIntent, "Share With"))
        }

        val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogLayout)
        dialogBuilder.show()
    }

}