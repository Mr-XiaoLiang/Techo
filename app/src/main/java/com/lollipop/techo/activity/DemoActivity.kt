package com.lollipop.techo.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.util.TextSelectedHelper

class DemoActivity : AppCompatActivity(),
    TextSelectedHelper.OnSelectedRangChangedListener{

    private val binding: ActivityDemoBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            .setLayoutProvider{ binding.valueView }
            .notifyInvalidate { binding.valueView.invalidate() }
            .bindTo(selector)
        selector.selectTarget = TextSelectedHelper.SelectTarget.START
    }

    override fun onSelectedRangChanged(start: Int, end: Int) {

    }

}