package com.lollipop.techo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.vScrollBar.contentWeight = 0.3F
        binding.hScrollBar.contentWeight = 0.3F
        binding.hScrollBar.addListener {
            binding.hValueView.text = it.toString()
        }
        binding.vScrollBar.addListener {
            binding.vValueView.text = it.toString()
        }
    }

}