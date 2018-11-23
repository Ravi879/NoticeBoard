package coms.dypatil.noticeboard.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import coms.dypatil.noticeboard.R
import coms.dypatil.noticeboard.App
import coms.dypatil.noticeboard.domain.model.UserProfile
import coms.dypatil.noticeboard.ui.activity.EditProfileActivity
import coms.dypatil.noticeboard.util.toast
import coms.dypatil.noticeboard.viewmodel.UserProfileVM
import coms.dypatil.noticeboard.viewmodel.contracts.UserProfileContract
import kotlinx.android.synthetic.main.fragment_user_profile.view.*
import kotlinx.android.synthetic.main.layout_user_profile.*
import kotlinx.android.synthetic.main.layout_user_profile.view.*

class UserProfileFragment : Fragment(), UserProfileContract {

    companion object {
        const val tag = "UserProfileFragment"
        fun newInstance(): UserProfileFragment {
            return UserProfileFragment()
        }

        const val profileData = "key_profile_data"
    }

    private var userProfileVM: UserProfileVM? = null
    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        setMenuVisibility(false)
    }

    override fun onCreateView(inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        layout = inflater.inflate(R.layout.fragment_user_profile, container, false)

        userProfileVM = ViewModelProviders.of(this).get(UserProfileVM::class.java)
        userProfileVM!!.contract = this

        setEditProfileBtnListener()

        return layout
    }

    override fun onStart() {
        super.onStart()
        Log.v("NoticeBoard_KT", "onResume")
        userProfileVM!!.loadProfile()
    }


    private fun setEditProfileBtnListener() {
        layout.img_btn_edit_profile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            userProfileVM!!.userProfile!!.password = App.preference.spUserPassword
            intent.putExtra(profileData, userProfileVM!!.userProfile)
            activity!!.startActivity(intent)
        }
    }

    override fun showProfileDetails(userProfile: UserProfile) {
        layout.txt_user_name.text = userProfile.name
        layout.txt_email.text = userProfile.email
        layout.txt_gender.text = userProfile.gender
        layout.txt_dob.text = userProfile.dob
        layout.txt_phone_no.text = userProfile.phoneNo
        layout.txt_designation.text = userProfile.designation
        layout.txt_department.text = userProfile.department
        layout.txt_year.text = userProfile.year

        if (userProfile.profilePicFile!=null && userProfile.profilePicFile!!.exists()) {
            Log.v("NoticeBoard_KT", "spProfilePicSignature.........................  " + App.preference.spProfilePicSignature)
            val requestOptions =
                    RequestOptions()
                            .signature(ObjectKey(App.preference.spProfilePicSignature))
            Glide.with(context!!)
                    .load(userProfile.profilePicFile!!)
                    .apply(requestOptions)
                    .into(layout.img_background_profile_pic)
            Glide.with(context!!)
                    .load(userProfile.profilePicFile)
                    .apply(requestOptions)
                    .into(layout.img_circle_profile_pic)

        } else {
            activity!!.toast("Profile pic not found")
        }

    }

    override fun setYearVisibility(visibility: Int) {
        img_year.visibility = visibility
        txt_year.visibility = visibility
    }

}












