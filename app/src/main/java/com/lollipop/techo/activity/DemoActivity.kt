package com.lollipop.techo.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.registerResult
import com.lollipop.techo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    private val recorderLauncher = registerResult(
        RecorderActivity.LAUNCHER
    ) {
        Toast.makeText(this, it?.path ?: "null", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.openButton.setOnClickListener {
            recorderLauncher.launch(false)
        }
    }

}