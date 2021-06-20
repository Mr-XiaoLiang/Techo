package com.lollipop.guide

import android.animation.Animator
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
import com.lollipop.base.listener.BackPressListener
import com.lollipop.base.provider.BackPressProvider
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.cleanWindowInsetHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.guide.impl.DefaultGuideProvider
import kotlin.math.abs

/**
 * @author lollipop
 * @date 2021/5/18 23:18
 * 蒙层引导的工具类
 */
class GuideHelper(private val option: Option) : GuideManager,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener,
    BackPressListener {

    companion object {

        private val globalProviderList = ArrayList<Class<out GuideProvider>>().apply {
            add(DefaultGuideProvider::class.java)
        }

        private const val ANIMATION_MAX = 1F
        private const val ANIMATION_MIN = 0F
        private const val ANIMATION_DURATION = 500L

        fun addGlobalGuideProvider(clazz: Class<out GuideProvider>) {
            if (globalProviderList.contains(clazz)) {
                return
            }
            globalProviderList.add(clazz)
        }

        fun with(activity: Activity): Builder {
            val backPressProvider = if (activity is BackPressProvider) {
                activity
            } else {
                null
            }
            return with(activity.window.decorView as ViewGroup, false, backPressProvider)
        }

        fun with(fragment: Fragment): Builder {
            val backPressProvider = if (fragment is BackPressProvider) {
                fragment
            } else {
                null
            }
            return with(findRootGroup(fragment.view!!), true, backPressProvider)
        }

        fun with(
            viewGroup: ViewGroup,
            isFindRoot: Boolean,
            backPressProvider: BackPressProvider?
        ): Builder {
            return if (isFindRoot) {
                Builder(findRootGroup(viewGroup), backPressProvider)
            } else {
                Builder(viewGroup, backPressProvider)
            }
        }

        fun with(view: View, backPressProvider: BackPressProvider?): Builder {
            return with(findRootGroup(view), false, backPressProvider)
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
        ValueAnimator().apply {
            addUpdateListener(this@GuideHelper)
            addListener(this@GuideHelper)
        }
    }

    private val guidePaddings = Rect()

    private val targetBounds = Rect()

    private var animationForStart = true

    private var animationProgress = 0F

    private val windowInsetsSize = Rect()

    fun show() {
        val rootGroup = option.rootGroup
        option.backPressProvider?.let {
            it.removeBackPressListener(this)
            it.addBackPressListener(this)
        }
        if (guideRoot.parent != rootGroup
            || rootGroup.indexOfChild(guideRoot) != rootGroup.childCount - 1
        ) {
            guideRoot.parent?.let { parent ->
                if (parent is ViewManager) {
                    parent.removeView(guideRoot)
                }
            }

            guideRoot.fitsSystemWindows = true
            guideRoot.fixInsetsByPadding { _, _, insets ->
                val insetsValue = WindowInsetsHelper.getInsetsValue(insets)
                windowInsetsSize.set(
                    insetsValue.left,
                    insetsValue.top,
                    insetsValue.right,
                    insetsValue.bottom
                )
                if (checkGuideBounds()) {
                    currentProvider?.setGuideBounds(
                        guideRoot.width,
                        guideRoot.height,
                        guidePaddings.left,
                        guidePaddings.top,
                        guidePaddings.right,
                        guidePaddings.bottom
                    )
                }
                insets
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
        if (currentStep != null) {
            val provider = currentProvider
            if (provider != null && provider.supportAnimation) {
                doAnimation(false)
                return
            }
        }
        stepIndex++
        if (stepIndex >= option.stepList.size) {
            destroy()
            return
        }
        nextStep()
    }

    fun destroy() {
        animator.cancel()
        guideRoot.cleanWindowInsetHelper()
        option.backPressProvider?.removeBackPressListener(this)
        guideRoot.parent?.let {
            if (it is ViewManager) {
                it.removeView(guideRoot)
            }
        }
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
            if (!targetInBounds(step.target)) {
                currentProvider = null
                stepIndex++
            } else {
                if (currentProvider?.support(step) == true) {
                    break
                }
                currentProvider = findProvider(step)
                if (currentProvider == null) {
                    stepIndex++
                }
            }
        } while (stepIndex < option.stepList.size && currentProvider == null)
        val provider = currentProvider ?: return destroy()
        val step = currentStep ?: return destroy()
        var needUpdateBounds = checkGuideBounds()
        isFirstStep = false
        if (oldProvider != provider) {
            needUpdateBounds = true
            oldProvider?.bindGuideManager(null)
            oldProvider?.destroy()
            oldProvider?.view?.let {
                guideRoot.removeView(it)
            }

            val view = provider.getView(guideRoot)
            guideRoot.addView(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        provider.bindGuideManager(this)
        if (needUpdateBounds) {
            provider.setGuideBounds(
                guideRoot.width,
                guideRoot.height,
                guidePaddings.left,
                guidePaddings.top,
                guidePaddings.right,
                guidePaddings.bottom
            )
        }
        checkTargetBounds()
        provider.setTargetBounds(
            targetBounds.left,
            targetBounds.top,
            targetBounds.right,
            targetBounds.bottom
        )
        provider.updateGuideStep(step)
        if (provider.supportAnimation) {
            provider.getView(guideRoot).post {
                doAnimation(true)
            }
        }
    }

    private fun onAnimationEnd() {
        if (!animationForStart) {
            currentStep = null
            dismiss()
        }
    }

    private fun doAnimation(isStart: Boolean) {
        animationForStart = isStart
        val endValue = if (isStart) {
            ANIMATION_MAX
        } else {
            ANIMATION_MIN
        }
        val duration = ((abs(animationProgress - endValue)
                / (ANIMATION_MAX - ANIMATION_MIN))
                * ANIMATION_DURATION).toLong()
        animator.cancel()
        animator.setFloatValues(animationProgress, endValue)
        animator.duration = duration
        animator.start()
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
        val snapshot = BoundsSnapshot.create(guidePaddings)
        guidePaddings.set(windowInsetsSize)
        return snapshot.isInconsistent(guidePaddings)
    }

    private fun targetInBounds(target: View): Boolean {
        val groupLocation = IntArray(2)
        val targetLocation = IntArray(2)
        guideRoot.getLocationInWindow(groupLocation)
        target.getLocationInWindow(targetLocation)

        val targetBounds = Rect(0, 0, target.width, target.height)
        targetBounds.offset(targetLocation[0], targetLocation[1])

        val guideRangeHorizontal = groupLocation[0]..(groupLocation[0] + guideRoot.width)
        val guideRangeVertical = groupLocation[1]..(groupLocation[1] + guideRoot.height)
        if (targetBounds.top in guideRangeVertical) {
            if (targetBounds.left in guideRangeHorizontal) {
                return true
            }
            if (targetBounds.right in guideRangeHorizontal) {
                return true
            }
        }
        if (targetBounds.bottom in guideRangeVertical) {
            if (targetBounds.left in guideRangeHorizontal) {
                return true
            }
            if (targetBounds.right in guideRangeHorizontal) {
                return true
            }
        }
        return false
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
        val backPressProvider: BackPressProvider?
    )

    class Builder(
        private val rootGroup: ViewGroup,
        val backPressProvider: BackPressProvider?
    ) {

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
            GuideHelper(Option(rootGroup, stepList, providerList, backPressProvider)).show()
        }

    }

    override fun onAnimationStart(animation: Animator?) {
        if (animation == animator) {
            currentProvider?.onAnimationStart(animationForStart)
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (animation == animator) {
            currentProvider?.onAnimationEnd(animationForStart)
            onAnimationEnd()
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == animator) {
            animationProgress = animation.animatedValue as Float
            currentProvider?.onAnimation(animationForStart, animationProgress)
        }
    }

    override fun onBackPressed(): Boolean {
        if (stepIndex < option.stepList.size) {
            nextStep()
            return true
        }
        return false
    }

}