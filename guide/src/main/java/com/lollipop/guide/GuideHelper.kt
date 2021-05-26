package com.lollipop.guide

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
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
class GuideHelper(private val option: Option) : GuideManager {

    companion object {

        private val globalProviderList = ArrayList<Class<out GuideProvider>>()

        fun addGlobalGuideProvider(clazz: Class<out GuideProvider>) {
            if (globalProviderList.contains(clazz)) {
                return
            }
            globalProviderList.add(clazz)
        }

        fun with(activity: Activity): Builder {
            return with(activity.window.decorView as ViewGroup, false)
        }

        fun with(fragment: Fragment): Builder {
            return with(findRootGroup(fragment.view!!), true)
        }

        fun with(viewGroup: ViewGroup, isFindRoot: Boolean): Builder {
            return if (isFindRoot) {
                Builder(findRootGroup(viewGroup))
            } else {
                Builder(viewGroup)
            }
        }

        fun with(view: View): Builder {
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

    private val defaultProviderList = ArrayList<GuideProvider>()

    private val context: Context
        get() {
            return option.rootGroup.context
        }

    private val guideRoot = FrameLayout(context)

    private var currentProvider: GuideProvider? = null

    private var currentStep: GuideStep? = null

    private val animator: ValueAnimator by lazy {
        ValueAnimator()
    }

    fun show() {
        val rootGroup = option.rootGroup
        if (guideRoot.parent != rootGroup
            || rootGroup.indexOfChild(guideRoot) != rootGroup.childCount - 1
        ) {
            guideRoot.parent?.let { parent ->
                if (parent is ViewManager) {
                    parent.removeView(guideRoot)
                }
            }
            rootGroup.addView(
                guideRoot,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        guideRoot.post {
            nextStep()
        }
    }

    fun dismiss() {

    }

    override fun nextStep() {

    }

    private fun findProvider(step: GuideStep): GuideProvider? {
        option.providerLis.forEach {
            if (it.support(step)) {
                return it
            }
        }
        for (index in 0 until globalProviderList.size) {
            if (defaultProviderList.size > index) {
                val provider = defaultProviderList[index]
                if (provider.support(step)) {
                    return provider
                }
            } else {
                val provider = globalProviderList[index].newInstance()
                defaultProviderList.add(provider)
                if (provider.support(step)) {
                    return provider
                }
            }
        }
        return null
    }

    class Option(
        val rootGroup: ViewGroup,
        val stepList: List<GuideStep>,
        val providerLis: List<GuideProvider>,
    )

    class Builder(private val rootGroup: ViewGroup) {

        private val stepList = ArrayList<GuideStep>()

        private val providerList = ArrayList<GuideProvider>()

        fun addStep(step: GuideStep): Builder {
            stepList.add(step)
            return this
        }

        fun addProvider(provider: GuideProvider): Builder {
            providerList.add(provider)
            return this
        }

        fun show() {
            GuideHelper(Option(rootGroup, stepList, providerList)).show()
        }

    }

}