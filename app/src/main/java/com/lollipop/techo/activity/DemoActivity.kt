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
        binding.passiveScrollView.setOnContentChangedListener { contentHeight, height ->
            binding.scrollSlider.valueFrom = 0F
            binding.scrollSlider.valueTo = (contentHeight - height).toFloat()
        }
        binding.scrollSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.passiveScrollView.offsetTo(value.toInt())
            }
        }
        val stringBuilder = StringBuilder()
        for (i in 0..500) {
            stringBuilder.append(i)
            stringBuilder.append("\n")
        }
        binding.testTextView.text = stringBuilder.toString()

        binding.openButton.setOnClickListener {
            SingleFragmentActivity.start<RichTextOptionFragment>(this) {
                RichTextOptionFragment.createArguments(this, TechoItem.Text().apply {
                    value = "测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容测试内容"
                })
            }
        }
    }

}