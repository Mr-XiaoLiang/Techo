package com.lollipop.techo.activity

import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.isResumed
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider
import com.lollipop.pigment.PigmentProviderHelper
import com.lollipop.techo.LApplication
import com.lollipop.techo.fragment.BaseFragment

abstract class BaseActivity : AppCompatActivity(), PigmentPage, PigmentProvider,
    BaseFragment.OnBackPressedCallback {

    private var isDarkMode = false

    private val pigmentProviderHelperInner = PigmentProviderHelper()

    override val pigmentProviderHelper: PigmentProviderHelper
        get() = pigmentProviderHelperInner

    override fun onStart() {
        super.onStart()
        updateStatusBar()
    }

    protected fun bindByBack(vararg view: View) {
        view.forEach {
            it.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        isDarkMode = pigment.blendMode == BlendMode.Dark
        updateStatusBar()
        pigmentProviderHelperInner.onDecorationChanged(pigment)
    }

    override val currentPigment: Pigment?
        get() {
            return pigmentProviderHelper.currentPigment
        }

    private fun updateStatusBar() {
        WindowInsetsHelper.getController(this).isAppearanceLightStatusBars = !isDarkMode
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isResumed()) {
            checkNightMode(Pigment.isNightMode(newConfig.uiMode))
        }
    }

    private fun checkNightMode(nightMode: Boolean) {
        if (nightMode != isDarkMode) {
            val app = application
            if (app is LApplication) {
                app.fetchPigment()
            }
        }
    }

    override fun notifyFragmentBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

}