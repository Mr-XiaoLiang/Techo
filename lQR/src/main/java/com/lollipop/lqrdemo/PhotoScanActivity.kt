package com.lollipop.lqrdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.ScanResultActivity
import com.lollipop.lqrdemo.databinding.ActivityPhotoScanBinding
import com.lollipop.pigment.Pigment
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import kotlin.random.Random

class PhotoScanActivity : ScanResultActivity() {

    companion object {

        const val REQUEST_CODE_SCAN = 5018

        fun start(context: Context, photo: Uri) {
            context.startActivity(Intent(context, PhotoScanActivity::class.java).apply {
                data = photo
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
        }

        fun startForResult(activity: Activity, photo: Uri) {
            activity.startActivityForResult(
                Intent(activity, PhotoScanActivity::class.java).apply {
                    action = ACTION_SCAN
                    data = photo
                },
                REQUEST_CODE_SCAN
            )
        }

    }

    private val binding: ActivityPhotoScanBinding by lazyBind()

    private val barcodeReader = BarcodeHelper.createLocalReader(this)

    private val readTag = Random.nextInt().toString(16)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
        binding.progressIndicator.show()
        bindResult(barcodeReader)
        bindSelectionView(binding.resultImageView, ImageView.ScaleType.FIT_CENTER)
        bindByBack(binding.backButton)
        loadData()
    }

    override fun createContentView(): View {
        return binding.root
    }

    private fun loadData() {
        val data = intent.data
        if (data != null) {
            Glide.with(this).load(data).into(binding.resultImageView)
            barcodeReader.read(this, data, readTag)
        }
    }

    override fun onBarcodeScanResult(result: BarcodeResult) {
        binding.progressIndicator.hide()
        if (!result.isEmpty) {
            binding.resultImageView.onCodeResult(
                result.info.getSize(),
                result.list
            )
            binding.resultImageView.isVisible = true
            binding.backButton.isVisible = true
            if (result.list.size == 1) {
                // 只有一个的时候默认选中
                onCodeSelected(result.list[0])
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeReader.removeOnBarcodeScanResultListener(this)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.resultImageView.color = pigment.primaryColor
        binding.resultImageView.tintNavigateIcon(ColorStateList.valueOf(pigment.primaryColor))
        binding.progressIndicator.setIndicatorColor(pigment.primaryColor, pigment.secondaryColor)
    }

}