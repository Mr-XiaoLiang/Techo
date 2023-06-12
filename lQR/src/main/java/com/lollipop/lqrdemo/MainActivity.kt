package com.lollipop.lqrdemo

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.base.util.registerResult
import com.lollipop.filechooser.FileChooseResult
import com.lollipop.filechooser.FileChooser
import com.lollipop.filechooser.FileMime
import com.lollipop.lqrdemo.base.ScanResultActivity
import com.lollipop.lqrdemo.creator.content.ContentBuilderActivity
import com.lollipop.lqrdemo.databinding.ActivityMainBinding
import com.lollipop.lqrdemo.other.AppSettings
import com.lollipop.lqrdemo.other.PrivacyAgreementActivity
import com.lollipop.pigment.Pigment
import com.lollipop.qr.BarcodeHelper
import com.lollipop.qr.comm.BarcodeResult
import com.lollipop.qr.view.FocusAnimationHelper

class MainActivity : ScanResultActivity() {

    companion object {
        private const val REQUEST_PERMISSION_CAMERA = 233
    }

    private val binding: ActivityMainBinding by lazyBind()

    private val qrReaderHelper = BarcodeHelper.createCameraReader(this)

    private val focusAnimationHelper = FocusAnimationHelper {
        binding.focusView
    }

    private val resultBackPressHandler = BackPressHandler(false) {
        resumeScan()
    }

    private val drawerBackPressHandler = BackPressHandler(false) {
        closeDrawer()
    }

    private val fileChooser = FileChooser.registerChooserLauncher(this, ::onChooseFile)

    private val privacyAgreementLauncher = registerResult(PrivacyAgreementActivity.LAUNCHER) {
        if (it != true) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        resultBackPressHandler.bindTo(this)
        binding.contentPanel.fixInsetsByPadding(WindowInsetsEdge.ALL)
        initCamera()
        initView()
    }

    private fun initCamera() {
        qrReaderHelper.bindContainer(binding.previewContainer)
        qrReaderHelper.addOnFocusChangedListener(focusAnimationHelper)
        bindResult(qrReaderHelper)
    }

    private fun initView() {
        bindByBack(binding.backButton)
        binding.menuButton.onClick {
            binding.drawerLayout.open()
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            override fun onDrawerOpened(drawerView: View) {
                drawerBackPressHandler.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerBackPressHandler.isEnabled = false
            }

        })
        binding.flashBtn.onClick {
            val torch = !qrReaderHelper.torch
            qrReaderHelper.torch = torch
            binding.flashBtn.isChecked = torch
        }
        binding.flashBtn.isChecked = qrReaderHelper.torch

        binding.galleryBtn.onClick {
            fileChooser.launch().localOnly().type(FileMime.Image.ALL).start()
        }

        binding.createBtn.onClick {
            Toast.makeText(this, "在做了在做了", Toast.LENGTH_SHORT).show()
        }
        binding.permissionView.onClick {
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        }

        updateCreateButtonState()

        bindSelectionView(binding.resultImageView, ImageView.ScaleType.CENTER_CROP)

        resumeScan()
    }

    private fun closeDrawer() {
        binding.drawerLayout.close()
    }

    private fun updateCreateButtonState() {
        binding.createBtn.isEnabled = !fromExternal
        binding.createBtn.alpha = if (fromExternal) {
            0.5F
        } else {
            1F
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.contentPanel.setBackgroundColor(pigment.backgroundColor)
        binding.flashBtn.tint(ColorStateList.valueOf(pigment.onBackgroundTitle))
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.menuButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
        binding.previewWindowView.color = pigment.backgroundColor
        binding.focusView.color = pigment.primaryColor
        binding.resultImageView.color = pigment.primaryColor
        binding.resultImageView.tintNavigateIcon(ColorStateList.valueOf(pigment.primaryColor))
        binding.galleryBtnContent.setBackgroundColor(pigment.secondaryColor)
        binding.galleryBtnText.setTextColor(pigment.onSecondaryTitle)
        binding.galleryBtnIcon.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
        binding.createBtnContent.setBackgroundColor(pigment.secondaryColor)
        binding.createBtnText.setTextColor(pigment.onSecondaryTitle)
        binding.createBtnIcon.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
        binding.permissionBtnText.setBackgroundColor(pigment.secondaryColor)
        binding.permissionBtnText.setTextColor(pigment.onSecondaryTitle)
    }

    private fun onChooseFile(file: FileChooseResult) {
        when (file) {
            FileChooseResult.Empty -> {
                // 忽略空返回
            }

            is FileChooseResult.Multiple -> {
                if (!file.isEmpty()) {
                    openPhotoScanPage(file[0])
                }
            }

            is FileChooseResult.Single -> {
                openPhotoScanPage(file.uri)
            }
        }
    }

    private fun openPhotoScanPage(path: Uri) {
        if (fromExternal) {
            PhotoScanActivity.startForResult(this, path)
        } else {
            PhotoScanActivity.start(this, path)
        }
    }

    override fun onBarcodeScanResult(result: BarcodeResult) {
        if (!result.isEmpty) {
            qrReaderHelper.analyzerEnable = false
            qrReaderHelper.analyzerBitmap?.let {
                if (!it.isRecycled) {
                    onUI {
                        binding.resultImageView.setImageBitmap(it)
                        binding.resultImageView.onCodeResult(
                            Size(it.width, it.height),
                            result.list
                        )
                        binding.resultImageView.isVisible = true
                        setBackButtonVisible(true)
                        if (result.list.size == 1) {
                            // 只有一个的时候默认选中
                            onCodeSelected(result.list[0])
                        }
                    }
                }
            }
            resultBackPressHandler.isEnabled = true
        }
    }

    private fun resumeScan() {
        resultBackPressHandler.isEnabled = false
        binding.resultImageView.setImageDrawable(null)
        binding.resultImageView.isVisible = false
        qrReaderHelper.analyzerEnable = true
        setBackButtonVisible(false)
    }

    private fun setBackButtonVisible(isVisible: Boolean) {
        if (isVisible) {
            closeDrawer()
        }
        binding.backButton.isVisible = isVisible
        binding.menuButton.isVisible = !isVisible
    }

    override fun onResume() {
        super.onResume()
        val selfPermission = PermissionChecker.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        )
        binding.permissionView.isVisible = selfPermission != PermissionChecker.PERMISSION_GRANTED
        closeDrawer()
        if (!AppSettings.default.isAgreePrivacyAgreement) {
            privacyAgreementLauncher.launch(null)
        }
    }

}