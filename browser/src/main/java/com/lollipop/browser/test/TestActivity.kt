package com.lollipop.browser.test

import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.base.HeaderActivity
import com.lollipop.browser.databinding.ActivityTestBinding
import com.lollipop.filechooser.FileChooseResult
import com.lollipop.filechooser.FileChooser
import com.lollipop.filechooser.FileMime
import java.io.File

class TestActivity : HeaderActivity() {

    private val binding: ActivityTestBinding by lazyBind()

    override val contentView: View
        get() {
            return binding.root
        }

    private val fileChooser = FileChooser.registerChooserLauncher(this) {
        Log.d("fileChooser", it.toString())
        when (it) {
            is FileChooseResult.Single -> {
                Glide.with(this).load(File(it.uri.path ?: "")).into(binding.imageView)
            }
            is FileChooseResult.Multiple -> {
                Glide.with(this).load(File(it[0].path ?: "")).into(binding.imageView)
            }
            else -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.demoButton.setOnClickListener {
            fileChooser.launch().type(FileMime.Image.ALL).start()
        }
    }
}