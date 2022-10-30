package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.registerResult
import com.lollipop.techo.databinding.ActivityDemoBinding
import com.lollipop.techo.dialog.AudioPlayerDialog
import kotlin.random.Random

class DemoActivity : AppCompatActivity() {

    private val binding: ActivityDemoBinding by lazyBind()

    private val recorderLauncher = registerResult(RecorderActivity.LAUNCHER) {
        Toast.makeText(this, it?.path ?: "null", Toast.LENGTH_SHORT).show()
        if (it != null) {
            AudioPlayerDialog(this, it.path).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.waveView.setWaveProvider {
            val fl = Random.nextFloat()
            fl - fl.toInt()
        }

        binding.startBtn.setOnClickListener {
            binding.waveView.start()
        }

        binding.stopBtn.setOnClickListener {
            binding.waveView.stop()
        }

        binding.openButton.setOnClickListener {
//            recorderLauncher.launch(false)
//            AudioPlayerDialog(this, "").show()
            startActivity(Intent(this, QrScanningActivity::class.java))
        }
    }

}