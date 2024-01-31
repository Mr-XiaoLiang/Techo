package com.lollipop.techo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivitySingleFragmentBinding

class SingleFragmentActivity : BaseActivity() {

    companion object {

        private const val PARAMS_FRAGMENT_NAME = "PARAMS_FRAGMENT_NAME"
        private const val PARAMS_FRAGMENT_ARGUMENTS = "PARAMS_FRAGMENT_ARGUMENTS"

        private fun createIntent(
            context: Context,
            clazz: Class<out Fragment>,
            arguments: Bundle
        ): Intent {
            return Intent(context, SingleFragmentActivity::class.java).apply {
                if (context !is AppCompatActivity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                putExtra(PARAMS_FRAGMENT_NAME, clazz.name)
                putExtra(PARAMS_FRAGMENT_ARGUMENTS, arguments)
            }
        }

        fun start(context: Context, clazz: Class<out Fragment>, arguments: Bundle) {
            context.startActivity(createIntent(context, clazz, arguments))
        }

        inline fun <reified T : Fragment> start(context: Context, builder: Bundle.() -> Unit) {
            start(context, T::class.java, Bundle().apply(builder))
        }

        fun startForResult(
            context: Activity,
            requestCode: Int,
            clazz: Class<out Fragment>,
            builder: Bundle
        ) {
            context.startActivityForResult(createIntent(context, clazz, builder), requestCode)
        }

        inline fun <reified T : Fragment> startForResult(
            context: Activity,
            requestCode: Int,
            builder: Bundle.() -> Unit
        ) {
            startForResult(context, requestCode, T::class.java, Bundle().apply(builder))
        }

    }

    private val binding: ActivitySingleFragmentBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        initFragment()
    }

    private fun initFragment() {
        val fragmentName = intent.getStringExtra(PARAMS_FRAGMENT_NAME) ?: ""
        if (fragmentName.isEmpty()) {
            finish()
            return
        }
        val arguments = intent.getBundleExtra(PARAMS_FRAGMENT_ARGUMENTS)
        try {
            val fragmentClass = Class.forName(fragmentName)
            val fragment = fragmentClass.newInstance()
            if (fragment !is Fragment) {
                finish()
                return
            }
            fragment.arguments = arguments
            bindFragment(fragment)
        } catch (e: Throwable) {
            e.printStackTrace()
            finish()
        }
    }

    private fun bindFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(binding.fragmentContainerView.id, fragment)
        transaction.commit()
    }

    abstract class LaunchContract<I, O> : ActivityResultContract<I, O>() {

        override fun createIntent(context: Context, input: I): Intent {
            return Intent(context, SingleFragmentActivity::class.java).apply {
                if (context !is AppCompatActivity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                putExtra(PARAMS_FRAGMENT_NAME, getTarget(input).name)
                putExtra(PARAMS_FRAGMENT_ARGUMENTS, createArguments(input))
            }
        }

        abstract fun createArguments(input: I): Bundle

        abstract fun getTarget(input: I): Class<out Fragment>

    }

}