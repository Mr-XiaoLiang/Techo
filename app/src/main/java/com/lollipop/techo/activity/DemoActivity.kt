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
        test()
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

    private fun test() {
        val srcList = ArrayList<Int>()
        for (index in 0..9) {
            srcList.add(index + 1000)
        }
        ColorHistoryHelper.colorHistory.clear()
        ColorHistoryHelper.colorHistory.addAll(srcList)
        log(Arrays.toString(ColorHistoryHelper.colorHistory.toArray()))

        val outputStream = ByteArrayOutputStream()
        ColorHistoryHelper.write(outputStream)
        val value = outputStream.toString()
        log(value)

        ColorHistoryHelper.colorHistory.clear()
        log(Arrays.toString(ColorHistoryHelper.colorHistory.toArray()))

        val inputStream = ByteArrayInputStream(value.toByteArray())
        ColorHistoryHelper.read(inputStream)
        log(Arrays.toString(ColorHistoryHelper.colorHistory.toArray()))

    }

}