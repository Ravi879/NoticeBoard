package coms.dypatil.noticeboard.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

class PermissionUtil(val a: Activity) {
    private val tag = "PermissionUtil"

    private var permissionDialog: AlertDialog? = null
    private val PERMISSION_REQUEST_CODE = 1

    private val activityWeakReference = WeakReference<Activity>(a)

    companion object {
        const val WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }

    fun isPermissionGranted(checkPermission: String) {
        val activity = activityWeakReference.get()

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(activity!!, checkPermission)==PackageManager.PERMISSION_GRANTED) {
                Log.v(tag, "PermissionUtil storage permission granted")
            } else {
                ActivityCompat.requestPermissions(activity,
                        arrayOf(checkPermission),
                        PERMISSION_REQUEST_CODE)
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(tag, "PermissionUtil is granted")
        }
    }

    fun onRequestResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray, checkPermission: String) {
        val activity = activityWeakReference.get()
        if (requestCode==PERMISSION_REQUEST_CODE) {
            if (grantResults[0]==PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= 23) {
                    Log.v(tag, "Boolean " + activity?.shouldShowRequestPermissionRationale(checkPermission))
                    if (!activity!!.shouldShowRequestPermissionRationale(checkPermission)) {
                        showPermissionDialog("OPEN SETTINGS", checkPermission)
                    } else if (permissions[0]==checkPermission) {
                        showPermissionDialog("TRY AGAIN",checkPermission = checkPermission)
                    }
                }
            } else if (grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                if (permissionDialog!=null)
                    permissionDialog!!.cancel()
            }
        }
    }

    private fun showPermissionDialog(btnText: String, checkPermission: String) {
        val activity: Activity? = activityWeakReference.get()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
                .setTitle("Oops, there was a problem")
                .setMessage("Noticeboard requires storage permission for storing media files, So you can see it offline")
                .setCancelable(false)
                .setPositiveButton(btnText) { _: DialogInterface?, _: Int ->
                    permissionDialog!!.cancel()
                    if (btnText=="TRY AGAIN") {
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(checkPermission),
                                PERMISSION_REQUEST_CODE)
                    } else {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.data = uri
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                        activity.startActivity(intent)
                    }
                }
        permissionDialog = builder.create()
        permissionDialog!!.show()
    }
}