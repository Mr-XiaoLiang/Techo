package com.lollipop.techo.edit.base

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import com.lollipop.base.request.PermissionFlow
import com.lollipop.base.request.RequestLauncher
import com.lollipop.base.request.startPermissionFlow
import com.lollipop.base.util.onClick
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.edit.PanelController
import com.lollipop.techo.util.AnimationHelper

/**
 * @author lollipop
 * @date 2021/12/18 20:15
 */
abstract class EditDelegate<T : TechoItem> {

    private var controller: PanelController? = null

    private var panelView: View? = null

    private var currentInfo: T? = null

    private val animationHelper = AnimationHelper(onUpdate = ::onAnimationUpdate).apply {
        onStart {
            onAnimationStart(it)
            showOnAnimationStart()
        }
        onEnd { progress, preload ->
            onAnimationEnd(progress)
            if (!preload && isAnimationClosed) {
                hideOnAnimationEnd()
            }
        }
    }

    protected val context: Activity?
        get() {
            return controller?.context
        }

    protected val requestLauncher: RequestLauncher?
        get() {
            return controller?.requestLauncher
        }

    protected val isAnimationOpened: Boolean
        get() {
            return animationHelper.progressIs(AnimationHelper.PROGRESS_MAX)
        }

    protected val isAnimationClosed: Boolean
        get() {
            return animationHelper.progressIs(AnimationHelper.PROGRESS_MIN)
        }

    var isChangedValue = false
        private set

    protected open val animationEnable = false

    fun setController(controller: PanelController?) {
        this.controller = controller
    }

    fun open(info: T) {
        currentInfo = info
        isChangedValue = false
        panelView?.post {
            onOpen(info)
            doAnimation(true)
        }
    }

    fun close() {
        onClose()
        this.controller = null
        panelView?.post {
            doAnimation(false)
        }
    }

    fun getPanelView(container: ViewGroup): View {
        panelView?.let {
            return it
        }
        val newView = onCreateView(container)
        this.panelView = newView
        onViewCreated(newView)
        if (animationEnable) {
            animationHelper.preload()
        }
        return newView
    }

    protected abstract fun onCreateView(container: ViewGroup): View

    protected open fun onViewCreated(view: View) {}

    protected open fun onOpen(info: T) {}

    protected open fun onClose() {}

    protected fun notifyDataChanged() {
        isChangedValue = true
    }

    private fun doAnimation(isOpen: Boolean) {
        if (animationEnable) {
            if (isOpen) {
                animationHelper.open(true)
            } else {
                animationHelper.close(true)
            }
        } else {
            panelView?.isInvisible = !isOpen
        }
    }

    protected open fun onAnimationUpdate(progress: Float) {}

    protected open fun onAnimationStart(progress: Float) {}

    protected open fun onAnimationEnd(progress: Float) {}

    private fun showOnAnimationStart() {
        panelView?.isInvisible = false
    }

    private fun hideOnAnimationEnd() {
        panelView?.isInvisible = true
    }

    protected fun resetAnimationProgress(progress: Float) {
        animationHelper.reset(progress)
    }

    fun destroy() {
        animationHelper.destroy()
    }

    protected fun getCurrentInfo(): T? {
        return currentInfo
    }

    protected inline fun tryChangeInfo(callback: (T) -> Unit) {
        getCurrentInfo()?.let(callback)
    }

    protected fun clickToClose(vararg views: View) {
        views.forEach {
            it.onClick {
                callClose()
            }
        }
    }

    protected fun keepWithClick(vararg views: View) {
        views.forEach {
            it.onClick {
                // 点击事件设置，但是不响应
            }
        }
    }

    /**
     * 按照进度来做透明度动画
     * @param progress 0：完全透明，1：完全显示
     * @param views 被改变的View
     */
    protected fun animationAlpha(progress: Float, vararg views: View) {
        views.forEach {
            it.alpha = progress
        }
    }

    /**
     * 按照进度来做上移动画
     * 它会从容器底部移动出来
     * @param progress 0：完全隐藏、View位于容器底部边界之外，1：View布局时原本的位置
     * @param views 被改变的View
     */
    protected fun animationUp(progress: Float, vararg views: View) {
        views.forEach { target ->
            target.parent?.isViewGroup { parent ->
                target.translationY = (parent.height - target.top) * (1F - progress)
            }
        }
    }

    /**
     * 按照进度来做下移动画
     * 它会从容器顶部移动出来
     * @param progress 0：完全隐藏、View位于容器顶部边界之外，1：View布局时原本的位置
     * @param views 被改变的View
     */
    protected fun animationDown(progress: Float, vararg views: View) {
        views.forEach { target ->
            target.translationY = target.bottom * (progress - 1F)
        }
    }

    private fun Any.isViewGroup(run: (ViewGroup) -> Unit) {
        val any = this
        if (any is ViewGroup) {
            run(any)
        }
    }

    protected open fun onDestroy() {}

    fun callClose() {
        controller?.callClose(this)
    }

    protected fun startPermissionFlow(callback: (PermissionFlow?) -> Unit) {
        val activity = context
        if (activity == null) {
            callback(null)
            return
        }
        if (requestLauncher == null && activity !is RequestLauncher) {
            callback(null)
            return
        }
        callback(activity.startPermissionFlow(requestLauncher))
    }

}