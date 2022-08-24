package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {

    }

}