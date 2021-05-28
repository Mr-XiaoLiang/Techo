package com.lollipop.guide

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
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

    private var stepIndex = 0

    private var isFirstStep = true

    private val animator: ValueAnimator by lazy {
        ValueAnimator()
    }

    private val guideBounds = Rect()

    private val targetBounds = Rect()

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

    fun destroy() {

    }

    override fun nextStep() {
        if (currentStep != null) {
            dismiss()
            return
        }
        val oldProvider = currentProvider
        do {
            val step = option.stepList[stepIndex]
            currentStep = step
            if (currentProvider?.support(step) == true) {
                break
            }
            currentProvider = findProvider(step)
            if (currentProvider == null) {
                stepIndex++
            }
        } while (stepIndex < option.stepList.size && currentProvider == null)
        val provider = currentProvider ?: return destroy()
        val step = currentStep ?: return destroy()
        var needUpdateBounds = checkGuideBounds()
        var needAnimation = isFirstStep
        isFirstStep = false
        if (oldProvider != provider) {
            needUpdateBounds = true
        }
        if (needUpdateBounds) {
            provider.onBoundsChange(
                guideBounds.left,
                guideBounds.top,
                guideBounds.right,
                guideBounds.bottom
            )
        }
        checkTargetBounds()
        provider.onTargetChange(
            step,
            targetBounds.left,
            targetBounds.top,
            targetBounds.right,
            targetBounds.bottom
        )
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

    private fun checkGuideBounds(): Boolean {
        val rootLocation = IntArray(2)
        val guideLocation = IntArray(2)
        option.rootGroup.getLocationInWindow(rootLocation)
        guideRoot.getLocationInWindow(guideLocation)

        val snapshot = BoundsSnapshot.create(guideBounds)

        guideBounds.set(0, 0, guideRoot.width, guideRoot.height)

        guideBounds.offset(
            guideLocation[0] - rootLocation[0],
            guideLocation[1] - rootLocation[1]
        )

        return snapshot.isInconsistent(guideBounds)
    }

    private fun checkTargetBounds(): Boolean {
        val step = currentStep
        if (step == null) {
            targetBounds.set(0, 0, 0, 0)
            return true
        }
        val groupLocation = IntArray(2)
        val targetLocation = IntArray(2)

        guideRoot.getLocationInWindow(groupLocation)
        step.target.getLocationInWindow(targetLocation)

        val snapshot = BoundsSnapshot.create(targetBounds)

        targetBounds.set(0, 0, step.target.width, step.target.height)
        targetBounds.offset(
            targetLocation[0] - groupLocation[0],
            targetLocation[1] - groupLocation[1]
        )

        return snapshot.isInconsistent(targetBounds)
    }

    class BoundsSnapshot(
        private val left: Int,
        private val top: Int,
        private val right: Int,
        private val bottom: Int
    ) {

        companion object {
            fun create(rect: Rect): BoundsSnapshot {
                return BoundsSnapshot(rect.left, rect.top, rect.right, rect.bottom)
            }
        }

        fun isInconsistent(rect: Rect): Boolean {
            return (rect.left != left
                    || rect.top != top
                    || rect.right != right
                    || rect.bottom != bottom)
        }

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