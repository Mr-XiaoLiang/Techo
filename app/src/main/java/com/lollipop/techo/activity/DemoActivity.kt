package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.util.TextSelectedHelper

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        TextSelectedHelper.create().bind(binding.valueView)
    }

}