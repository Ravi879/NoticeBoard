package coms.dypatil.noticeboard.viewmodel.contracts

interface EditProfileContract : ProfileBuilderContract, NetworkStateCallBack {

    fun showConfirmationDialog()

}