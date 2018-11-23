package coms.dypatil.noticeboard.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.domain.service.DownloadUserPicService
import coms.dypatil.noticeboard.util.FileUtil
import coms.dypatil.noticeboard.util.ProfileBuilderUtil
import coms.dypatil.noticeboard.util.loadingtracker.Response
import coms.dypatil.noticeboard.util.loadingtracker.Status
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.UserListProfileVM
import coms.dypatil.noticeboard.viewmodel.contracts.FacultyProfileContract
import kotlinx.android.synthetic.main.fragment_user_list_profile.view.*
import kotlinx.android.synthetic.main.layout_user_profile.view.*

class UserListProfileFragment : Fragment(), FacultyProfileContract {

    companion object {
        const val TAG = "UserListProfileFragment"

        const val IS_FROM = "activity"
        const val FACULTY_ID = "faculty_fb_user_id"
        const val STUDENT_ID = "student_fb_user_id"
    }

    private lateinit var viewModel: UserListProfileVM
    private lateinit var layout: View
    var fbUserId: String? = null
    private var isFrom: String? = null
    private lateinit var loadingIndicator: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fbUserId = arguments?.let { bundle ->
            when {
                bundle.containsKey(FACULTY_ID) -> bundle.getString(FACULTY_ID)

                bundle.containsKey(STUDENT_ID) ->
                    bundle.getString(STUDENT_ID)

                else -> null
            }
        }
        isFrom = arguments!!.getString(IS_FROM)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.findViewById(R.id.app_bar) as Toolbar).title = "Profile"
    }


    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        layout = inflater.inflate(R.layout.fragment_user_list_profile, container, false)
        layout.img_btn_edit_profile?.visibility = View.INVISIBLE

        viewModel = ViewModelProviders.of(this).get(UserListProfileVM::class.java)
        viewModel.contract = this

        loadingIndicator = layout.loading_indicator
        viewModel.response.observe(this, Observer { processResponse(it!!) })

        loadProfile(isFrom!!)

        if (isFrom=="student") {
            layout.txt_year.visibility = View.VISIBLE
            layout.img_year.visibility = View.VISIBLE
        } else {
            layout.txt_year.visibility = View.GONE
            layout.img_year.visibility = View.GONE
        }

        return layout
    }

    private fun loadProfile(isFrom: String) {
        when (isFrom) {
            "faculty" -> viewModel.getLocalFacultyProfile(fbUserId!!)
            "student" -> viewModel.getLocalStudentProfile(fbUserId!!)
        }
    }

    override fun profileNotFoundInRoom() {
        if (App.isNetworkAvailable.value!!) {
            when (isFrom) {
                "faculty" -> viewModel.getFBFacultyProfile(fbUserId!!)
                "student" -> viewModel.getFBStudentProfile(fbUserId!!)
            }
        } else {
            processResponse(Response.error(Throwable("Profile details not found in sqlite db"),
                    msg = "Profile is not available offline"))
        }
    }

    override fun showProfileDetails(userProfile: UserProfile) {
        userProfile.apply {
            layout.txt_user_name.text = name
            layout.txt_email.text = email
            layout.txt_department.text = department
            layout.txt_designation.text = designation
            layout.txt_dob.text = dob
            layout.txt_gender.text = gender
            layout.txt_phone_no.text = phoneNo
            layout.txt_year.text = year

            showProfilePic(userProfile)
        }
    }

    private fun showProfilePic(userProfile: UserProfile) {
        val userType = isFrom
        val requestOption = RequestOptions()
                .placeholder(AppCompatResources.getDrawable(App.getAppContext, R.drawable.ic_img_loading))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
        if (App.isNetworkAvailable.value!!) {
            val profilePic: RequestBuilder<Drawable>? = context?.let {
                Glide.with(it)
                        .load(userProfile.profilePicUrl)
                        .apply(requestOption)
            }
            profilePic?.into(layout.img_circle_profile_pic)
            profilePic?.into(layout.img_background_profile_pic)
            downloadProfilePic(App.getAppContext, userType!!, userProfile.profilePicUrl!!)
        } else {
            val profilePicName = ProfileBuilderUtil.getProfilePicNameFromUrl(userProfile.profilePicUrl!!)
            val imgFile = FileUtil.getProfilePicFile(userType!!, profilePicName)
            if (imgFile.exists()) {
                val profilePic: RequestBuilder<Drawable>? = Glide.with(layout.img_circle_profile_pic)
                        .load(imgFile)
                        .apply(requestOption)
                profilePic?.into(layout.img_circle_profile_pic)
                profilePic?.into(layout.img_background_profile_pic)
            }
        }
    }

    private fun downloadProfilePic(context: Context, userType: String, profilePicUrl: String) {
        val intent = Intent(context, DownloadUserPicService::class.java).apply {
            putExtra(DownloadUserPicService.USER_TYPE, userType)
            putExtra(DownloadUserPicService.PROFILE_PIC_URL, profilePicUrl)
        }
        context.startService(intent)
    }

    private fun processResponse(response: Response) {
        when (response.status) {
            Status.LOADING -> renderLoadingState()
            Status.SUCCESS -> renderLoadingSuccess()
            Status.ERROR -> renderErrorState(response.error!!, response.data!!)
        }
    }

    private fun renderLoadingState() {
        loadingIndicator.visibility = View.VISIBLE
    }

    private fun renderLoadingSuccess() {
        loadingIndicator.visibility = View.GONE
    }

    private fun renderErrorState(throwable: Throwable, errorMsg: String) {
        loadingIndicator.visibility = View.GONE
        throwable.printStackTrace()
        Log.v(coms.dypatil.noticeboard.util.tag, " CreateNotice Error " + throwable.toString())
        activity!!.toast(errorMsg)
    }


}