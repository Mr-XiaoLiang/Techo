package com.lollipop.web.util

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.FrameLayout
import com.lollipop.web.listener.CustomViewListener
import java.lang.ref.WeakReference

class HorizontalCustomViewHelper(activity: Activity) : CustomViewListener {

    private val activityReference = WeakReference(activity)
    private var customView: WeakReference<View>? = null
    private var viewCallback: WeakReference<CustomViewListener.CustomViewCallback>? = null
    private var activityStatus: ActivityStatus? = null

    private fun getActivity(): Activity? {
        return activityReference.get()
    }

    override fun onShowCustomView(view: View?, callback: CustomViewListener.CustomViewCallback) {
        val activity = getActivity()
        if (activity == null) {
            callback.onCustomViewHidden()
            return
        }
        if (customView?.get() != null) {
            onHideCustomView()
        } else {
            saveActivityStatus()
        }
        showCustomView(view)
        viewCallback = WeakReference(callback)
    }

    override fun onHideCustomView() {
        customView?.get()?.let { view ->
            view.parent?.let { parent ->
                if (parent is ViewManager) {
                    parent.removeView(view)
                }
            }
        }
        customView = null
        viewCallback?.get()?.onCustomViewHidden()
        viewCallback = null
        restoreActivityStatus()
    }

    private fun saveActivityStatus() {
        val activity = getActivity() ?: return
        activityStatus = ActivityStatus(
            activity.window.attributes.flags,
            activity.requestedOrientation
        )
    }

    private fun restoreActivityStatus() {
        val status = activityStatus ?: return
        activityStatus = null
        val activity = getActivity() ?: return
        activity.window.setFlags(status.flags, status.flags)
        activity.requestedOrientation = status.requestedOrientation
    }

    private fun showCustomView(view: View?) {
        view ?: return
        val activity = getActivity() ?: return
        val group = FrameLayout(activity)
        group.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        group.setBackgroundColor(Color.BLACK)
        val decorView = activity.window.decorView
        if (decorView is ViewManager) {
            decorView.addView(
                group, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        customView = WeakReference(group)
    }

    private class ActivityStatus(
        val flags: Int,
        val requestedOrientation: Int
    )

}