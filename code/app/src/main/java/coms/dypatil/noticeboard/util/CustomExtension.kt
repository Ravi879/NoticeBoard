package coms.dypatil.noticeboard.util

import android.app.Activity
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot

fun String.getList(delimiter: Char): MutableList<String> = this.split(delimiter) as MutableList<String>

fun EditText.getString(): String = this.text.toString().trim()

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

//add fragment
fun AppCompatActivity.addFragment(frameId: Int, fragment: Fragment, tag: String) {
    supportFragmentManager.inTransaction { add(frameId, fragment, tag) }
}

//replace fragment
//for activity
fun AppCompatActivity.replaceFragment(frameId: Int, fragment: Fragment, tag: String, addToBackStack: Boolean = false) {
    if (addToBackStack)
        supportFragmentManager.inTransaction { replace(frameId, fragment, tag).addToBackStack(tag) }
    else
        supportFragmentManager.inTransaction { replace(frameId, fragment, tag) }
}

//for fragment
fun Fragment.replaceFragment(frameId: Int, fragment: Fragment, tag: String, addToBackStack: Boolean = false) {
    if (addToBackStack)
        childFragmentManager.inTransaction { replace(frameId, fragment, tag).addToBackStack(tag) }
    else
        childFragmentManager.inTransaction { replace(frameId, fragment, tag) }
}

//if list is null then initialize otherwise adding all items to list
operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
    val value: ArrayList<T> = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}


fun DataSnapshot.getString(key: String): String = this.child(key).value.toString()


//Toast
fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, text, duration).apply { show() }
}

fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(this, text, duration).apply { show() }
}
