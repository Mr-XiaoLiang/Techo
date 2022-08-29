package com.lollipop.techo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivitySingleFragmentBinding

class SingleFragmentActivity : AppCompatActivity() {

    companion object {

        private const val PARAMS_FRAGMENT_NAME = "PARAMS_FRAGMENT_NAME"
        private const val PARAMS_FRAGMENT_ARGUMENTS = "PARAMS_FRAGMENT_ARGUMENTS"

        fun start(context: Context, clazz: Class<out Fragment>, arguments: Bundle) {
            context.startActivity(
                Intent(context, SingleFragmentActivity::class.java).apply {
                    if (context !is AppCompatActivity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    putExtra(PARAMS_FRAGMENT_NAME, clazz.name)
                    putExtra(PARAMS_FRAGMENT_ARGUMENTS, arguments)
                }
            )
        }

        inline fun <reified T : Fragment> start(context: Context, builder: Bundle.() -> Unit) {
            start(context, T::class.java, Bundle().apply(builder))
        }

    }

    private val binding: ActivitySingleFragmentBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
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

}