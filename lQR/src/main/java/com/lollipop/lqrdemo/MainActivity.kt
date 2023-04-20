package com.lollipop.lqrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowInsetsHelper.initWindowFlag(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}