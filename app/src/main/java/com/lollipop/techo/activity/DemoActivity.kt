package com.lollipop.techo.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.util.TextSelectedHelper
import com.lollipop.techo.util.VectorHelper

class DemoActivity : AppCompatActivity(),
    TextSelectedHelper.OnSelectedRangeChangedListener {

    private val binding: ActivityDemoBinding by lazyBind()

    private val clipData = "M38,21V18V17.923C38,14.217 38,11.334 37.698,9.089C37.39,6.799 36.752,5.024 35.364,3.636C33.976,2.248 32.201,1.61 29.911,1.302C27.666,1 24.783,1 21.077,1L21,1H18L17.923,1C14.217,1 11.334,1 9.089,1.302C6.799,1.61 5.024,2.248 3.636,3.636C2.248,5.024 1.61,6.799 1.302,9.089C1,11.334 1,14.217 1,17.923L1,18V21L1,21.077C1,24.783 1,27.666 1.302,29.911C1.61,32.201 2.248,33.976 3.636,35.364C5.024,36.752 6.799,37.39 9.089,37.698C11.334,38 14.217,38 17.923,38H18H21H21.077C24.783,38 27.666,38 29.911,37.698C32.201,37.39 33.976,36.752 35.364,35.364C36.752,33.976 37.39,32.201 37.698,29.911C38,27.666 38,24.783 38,21.077V21Z"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val selector = TextSelectedHelper.selector()
            .onSelectedChanged(this)
            .bind(binding.valueView)
        binding.valueView.background = TextSelectedHelper.printer()
            .setColor(Color.RED)
            .halfRadius()
            .setLayoutProvider { binding.valueView }
            .notifyInvalidate { binding.valueView.invalidate() }
            .bindTo(selector)
        selector.selectTarget = TextSelectedHelper.SelectTarget.START

        VectorHelper.outline(39,39, clipData).bindTo(binding.backgroundView)

        binding.testView.fixInsetsByMargin(WindowInsetsHelper.Edge.ALL)
    }

    override fun onSelectedRangeChanged(start: Int, end: Int) {

    }

}