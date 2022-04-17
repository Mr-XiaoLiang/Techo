package com.lollipop.techo.activity

import android.os.Bundle
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityRecorderBinding

class RecorderActivity : BaseActivity() {

    private val binding: ActivityRecorderBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}