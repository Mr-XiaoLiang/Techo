package com.lollipop.guide

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment

/**
 * @author lollipop
 * @date 2021/5/18 23:18
 * 蒙层引导的工具类
 */
class GuideHelper(private val rootGroup: ViewGroup) {

    companion object {
        fun with(activity: Activity): GuideHelper {
            return with(activity.window.decorView as ViewGroup, false)
        }

        fun with(fragment: Fragment): GuideHelper {
            return with(findRootGroup(fragment.view!!), true)
        }

        fun with(viewGroup: ViewGroup, isFindRoot: Boolean): GuideHelper {
            return if (isFindRoot) {
                GuideHelper(findRootGroup(viewGroup))
            } else {
                GuideHelper(viewGroup)
            }
        }

        fun with(view: View): GuideHelper {
            return with(findRootGroup(view), false)
        }

        private fun findRootGroup(view: View): ViewGroup {
            var target: View = view
            var viewGroup: ViewGroup? = null
            do {
                if (isGuideParent(target)) {
                    viewGroup = target as ViewGroup
                }
                val parent = target.parent
                if (parent is View) {
                    target = parent
                }
            } while (parent != null)
            if (viewGroup == null) {
                throw RuntimeException("Root view not found")
            }
            return viewGroup
        }

        private fun isGuideParent(view: View): Boolean {
            return (view is FrameLayout
                    || view is ConstraintLayout
                    || view is RelativeLayout
                    || view is CoordinatorLayout)
        }

    }

    class Builder() {
        fun addStep(step: GuideStep) {

        }
    }

}