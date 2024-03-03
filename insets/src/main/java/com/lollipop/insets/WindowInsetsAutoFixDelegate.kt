package com.lollipop.insets

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lollipop.insets.WindowInsetsAutoFixMode.ALLOW
import com.lollipop.insets.WindowInsetsAutoFixMode.BLOCK

object WindowInsetsAutoFixDelegate {

    private var application: Application? = null
    private var fixMode = BLOCK

    /**
     * @param app 被注册的Application
     * 避免反复注册，咱们会对比一下，同时这里的比较是全等，也就是希望对比内存中的对象
     * 一般情况下，一个进程会有一个单独的Application。因此同一个Application的情况下就是同一个进程的
     * @param mode 自动绑定时，使用的规则。
     * 虽然同一个Application下我们只注册一次，但是咱们的模式是允许多次修改的，
     * 但是多次修改时，已经被修改的Activity我们不做恢复
     */
    fun init(app: Application, mode: WindowInsetsAutoFixMode) {
        //
        //
        if (app !== application) {
            application?.unregisterActivityLifecycleCallbacks(ActivityRegister)
            app.registerActivityLifecycleCallbacks(ActivityRegister)
            application = app
        }
        fixMode = mode
    }

    private fun fixActivityWindowInsets(activity: Activity) {
        when (fixMode) {
            BLOCK -> {
                fixInsetsByBlockMode(activity)
            }

            ALLOW -> {
                fixInsetsByAllowMode(activity)
            }
        }
    }

    private fun fixInsetsByBlockMode(activity: Activity) {
        if (activity is WindowInsetsAutoFixBlock) {
            // 黑名单模式时，发现处于黑名单，则不继续执行
            return
        }
        WindowInsetsHelper.fitsSystemWindows(activity)
    }

    private fun fixInsetsByAllowMode(activity: Activity) {
        if (activity !is WindowInsetsAutoFixAllow) {
            // 白名单模式时，发现不处于白名单，则不继续执行
            return
        }
        WindowInsetsHelper.fitsSystemWindows(activity)
    }

    private object ActivityRegister : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // 虽然我们更希望在创建的时候就设置它的的Flag,但是会有兼容性问题
            // 因为部分国产手机上，Flag需要在setContentView之后设置生效，
            // 但是这个方法的回掉时机，却是Activity的onCreate中，
            // 也就是会在真正执行的子类Activity的super.onCreate中触发。
            // 这个时机太早了，所以我们需要将时机延后
        }

        override fun onActivityStarted(activity: Activity) {
            // 由于我们不能在onActivityCreated中执行绑定，所以我们在后面一个生命周期中触发
            fixActivityWindowInsets(activity)
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

    }

}