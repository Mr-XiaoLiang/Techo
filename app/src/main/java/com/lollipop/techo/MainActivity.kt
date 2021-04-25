package com.lollipop.techo

import android.os.Bundle
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity() {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}