package com.lollipop.base.request

import android.app.Activity
import android.util.ArraySet
import androidx.core.app.ActivityCompat
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.ui.BaseFragment
import java.util.HashSet

/**
 * @author lollipop
 * @date 2021/5/12 22:08
 */
class PermissionFlow(private val activity: Activity, private val requestLauncher: RequestLauncher) {

    companion object {
        fun with(activity: BaseActivity): PermissionFlow {
            return PermissionFlow(activity, activity)
        }

        fun with(fragment: BaseFragment): PermissionFlow {
            return PermissionFlow(fragment.activity!!, fragment)
        }
    }

    private var rationaleCallback: RationaleCallback? = null

    private var permissionsList = HashSet<String>()

    fun onNeedRationale(callback: RationaleCallback): PermissionFlow {
        this.rationaleCallback = callback
        return this
    }

    fun permissionIs(vararg permissions: String): PermissionFlow {
        permissionsList.addAll(permissions)
        return this
    }

    fun request(permissionCallback: PermissionCallback) {
        val shouldShowRationale = ArrayList<String>()
        val permissionsArray = permissionsList.toTypedArray()
        permissionsArray.forEach {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, it)) {
                shouldShowRationale.add(it)
            }
        }
        val rationaleDialog = rationaleCallback
        if (rationaleDialog != null && shouldShowRationale.isNotEmpty()) {
            shouldShowRationale.forEach {
//                rationaleDialog.showRationale(it, )
            }
        } else {

        }
//        requestLauncher.requestPermission(, permissionCallback)
    }

    private class RequestTask(
        private val permission: String,
        private val requestLauncher: RequestLauncher,
        private val callback: PermissionCallback
    ) {
        fun run() {
            requestLauncher.requestPermission(arrayOf(permission), callback)
        }
    }

    fun interface PermissionFeedback {
        fun onFeedback(isAllowed: Boolean)
    }

    fun interface RationaleCallback {
        fun showRationale(permissions: String, feedback: PermissionFeedback)
    }

}