package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.fragment.RichTextOptionFragment

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.passiveScrollView.addListener { contentHeight, height ->
            val offset = contentHeight - height
            if (offset > 0) {
                binding.scrollSlider.valueFrom = 0F
                binding.scrollSlider.valueTo = offset.toFloat()
            }
        }
        binding.scrollSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.passiveScrollView.scrollTo(0, value.toInt())
            }
        }
        val stringBuilder = StringBuilder()
        for (i in 0..20) {
            stringBuilder.append("测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容")
        }
        val string = stringBuilder.toString()
        binding.testTextView.text = string
        binding.testTextView2.text = string

        binding.openButton.setOnClickListener {
            SingleFragmentActivity.start<RichTextOptionFragment>(this) {
                RichTextOptionFragment.createArguments(this, TechoItem.Text().apply {
                    value = string
                })
            }
        }
    }

}