package com.lollipop.techo.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.util.TextSelectedHelper

class DemoActivity : AppCompatActivity(),
    TextSelectedHelper.OnSelectedRangChangedListener,
    TextSelectedHelper.TextLayoutProvider {

    private val binding: ActivityDemoBinding by lazyBind()
    private var selectedPrinter: TextSelectedHelper.Painter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val printer = TextSelectedHelper.printer()
            .setColor(Color.RED).setRadius(5F).setLayoutProvider(this).build()
        selectedPrinter = printer
        binding.backgroundView.setImageDrawable(printer)
        TextSelectedHelper.selector().onSelectedChanged(this).bind(binding.valueView)
    }

    override fun onSelectedRangChanged(start: Int, end: Int) {
        selectedPrinter?.setSelectedRang(start, end)
        binding.backgroundView.invalidate()
    }

    override fun getTextLayout(): Layout? {
        return binding.valueView.layout
    }

}