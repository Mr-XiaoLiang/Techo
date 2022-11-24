package com.lollipop.base.ui

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.request.RequestCallback
import com.lollipop.base.request.RequestHelper
import com.lollipop.base.request.RequestLauncher
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentPage
import com.lollipop.pigment.PigmentProvider
import com.lollipop.pigment.PigmentProviderHelper

/**
 * @author lollipop
 * @date 4/16/21 22:02
 * 基础的Activity
 * 提供基础的实现和能力
 */
open class BaseActivity : AppCompatActivity(),
    RequestLauncher,
    PigmentPage,
    PigmentProvider {

    override val pigmentProviderHelper = PigmentProviderHelper()

    override val requestHelper: RequestHelper by lazy {
        RequestHelper.with(this)
    }

    private fun getSelf(): BaseActivity {
        return this
    }

    override fun requestPigment(page: PigmentPage) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                notifyBackPress()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected inline fun <reified T : Activity> requestActivity(
        paramsProvider: ((Intent) -> Unit) = {},
        callback: RequestCallback
    ) {
        requestActivity(
            Intent(this, T::class.java).apply(paramsProvider),
            callback
        )
    }

    fun notifyBackPress() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        requestHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        pigmentProviderHelper.onDecorationChanged(pigment)
    }

}