package com.lollipop.pigment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

class PigmentActivityHelper(
    private val appStateChangedListener: (onBackground: Boolean) -> Unit
) : Application.ActivityLifecycleCallbacks, PigmentPage {

    private var activePigmentPageList = ArrayList<WeakReference<PigmentPage>>()
    private var pigment: Pigment? = null

    private var appOnBackground = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        setAppMode(false)
        if (activity is PigmentPage) {
            activePigmentPageList.add(WeakReference(activity))
            pigment?.let {
                activity.onDecorationChanged(it)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        val removed = HashSet<WeakReference<PigmentPage>>()
        activePigmentPageList.forEach {
            val page = it.get()
            if (page == null || page === activity) {
                removed.add(it)
            }
        }
        activePigmentPageList.removeAll(removed)
        setAppMode(activePigmentPageList.isEmpty())
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onDecorationChanged(pigment: Pigment) {
        this.pigment = pigment
        activePigmentPageList.forEach {
            it.get()?.onDecorationChanged(pigment)
        }
    }

    override val currentPigment: Pigment?
        get() {
            return pigment
        }

    private fun setAppMode(onBackground: Boolean) {
        if (appOnBackground != onBackground) {
            appOnBackground = onBackground
            appStateChangedListener(onBackground)
        }
    }

}