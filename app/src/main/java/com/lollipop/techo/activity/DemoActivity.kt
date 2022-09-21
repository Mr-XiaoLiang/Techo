package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.log
import com.lollipop.palette.ColorHistoryHelper
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.fragment.RichTextOptionFragment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

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
            SingleFragmentActivity.start<RichTextOptionFragment>(this) {
                RichTextOptionFragment.createArguments(this, TechoItem.Text().apply {
                    value = string
                })
            }
        }
    }

}