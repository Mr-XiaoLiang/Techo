package com.lollipop.base.request

import android.app.Activity
import android.content.Intent
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 2021/5/11 23:32
 */
class RequestHelper private constructor(private val requestOwner: RequestOwner): RequestLauncher {

    companion object {
        private const val DEFAULT_REQUEST_ID = 233

        fun with(activity: Activity): RequestHelper {
            return RequestHelper(ActivityRequestOwner(activity))
        }

        fun with(fragment: Fragment): RequestHelper {
            return RequestHelper(FragmentRequestOwner(fragment))
        }

    }

    private var requestCode = DEFAULT_REQUEST_ID

    private val requestCallbackList = SparseArray<RequestCallback>()

    private val permissionCallbackList = SparseArray<PermissionCallback>()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val requestCallback = requestCallbackList[requestCode]
        if (requestCallback != null) {
            requestCallbackList.remove(requestCode)
            requestCallback.onActivityResult(RequestResult(resultCode, data))
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionCallback = permissionCallbackList[requestCode]
        if (permissionCallback != null) {
            permissionCallbackList.remove(requestCode)
            permissionCallback.onPermissionsResult(PermissionResult(permissions, grantResults))
        }
    }

    override fun requestActivity(intent: Intent, callback: RequestCallback) {
        requestOwner.startActivityForResult(intent, pendingCallback(callback))
    }

    override fun requestPermission(permission: Array<String>, callback: PermissionCallback) {
        requestOwner.requestPermissions(permission, pendingCallback(callback))
    }

    private fun pendingCallback(callback: RequestCallback): Int {
        val requestId = generateRequestId()
        requestCallbackList.put(requestId, callback)
        return requestId
    }

    private fun pendingCallback(callback: PermissionCallback): Int {
        val requestId = generateRequestId()
        permissionCallbackList.put(requestId, callback)
        return requestId
    }

    private fun generateRequestId(): Int {
        if (requestCode >= Int.MAX_VALUE || requestCode < 0) {
            requestCode = 0
        }
        val startCode = requestCode
        do {
            if (requestCallbackList[requestCode] == null) {
                return requestCode
            }
            requestCode++
        } while (startCode != requestCode)
        throw RuntimeException("No available request code was found")
    }

    private interface RequestOwner {
        fun startActivityForResult(intent: Intent, requestCode: Int)
        fun requestPermissions(permission: Array<String>, requestCode: Int)
    }

    private class ActivityRequestOwner(private val activity: Activity): RequestOwner {
        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            activity.startActivityForResult(intent, requestCode)
        }

        override fun requestPermissions(permission: Array<String>, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, permission, requestCode)
        }
    }

    private class FragmentRequestOwner(private val fragment: Fragment): RequestOwner {
        override fun startActivityForResult(intent: Intent, requestCode: Int) {
            fragment.startActivityForResult(intent, requestCode)
        }

        override fun requestPermissions(permission: Array<String>, requestCode: Int) {
            fragment.requestPermissions(permission, requestCode)
        }
    }

}