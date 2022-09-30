package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.registerResult
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.fragment.RichTextOptionFragment

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    private val richTextOptionLauncher = registerResult(
        RichTextOptionFragment.LAUNCHER
    ) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val stringBuilder = StringBuilder()
        for (i in 0..20) {
            stringBuilder.append("测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容")
        }
        val string = stringBuilder.toString()
        binding.openButton.setOnClickListener {
            richTextOptionLauncher.launch(
                RichTextOptionFragment.Request(
                    1,
                    TechoItem.Text().apply {
                        value = string
                    }
                )
            )
        }
    }

}