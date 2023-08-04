package com.lollipop.lqrdemo

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.lollipop.base.util.ShareSheet
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.base.util.registerResult
import com.lollipop.faceicon.FaceIcons
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.creator.QrBackgroundFragment
import com.lollipop.lqrdemo.creator.QrContentInputPopupWindow
import com.lollipop.lqrdemo.creator.QrContentValueFragment
import com.lollipop.lqrdemo.creator.QrCreatorHelper
import com.lollipop.lqrdemo.creator.QrCreatorPreviewDrawable
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.creator.content.ContentBuilderActivity
import com.lollipop.lqrdemo.databinding.ActivityCreatorBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

class CreatorActivity : ColorModeActivity(), QrContentValueFragment.Callback,
    OnCodeContentChangedListener, QrCreatorHelper.OnLoadStatusChangedListener {

    companion object {
        private const val STATE_QR_VALUE = "STATE_QR_VALUE"
    }

    private val binding: ActivityCreatorBinding by lazyBind()

    private val creatorHelper = QrCreatorHelper(this, ::notifyQrChanged, ::onQrCheckResult)

    private val contentBuilderLauncher = registerResult(ContentBuilderActivity.LAUNCHER) {
        creatorHelper.contentValue = it ?: ""
    }

    private val previewDrawable = QrCreatorPreviewDrawable(creatorHelper.previewWriter)

    private var isLoading = false

    private val log by lazyLogD()

    private var currentCheckResult = QrCreatorHelper.CheckResult.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.HEADER)
        bindByBack(binding.backButton)
        binding.panelGroup.fixInsetsByPadding(WindowInsetsEdge.BOTTOM)
        binding.subpageGroup.adapter = SubPageAdapter(this)
        TabLayoutMediator(
            binding.tabLayout,
            binding.subpageGroup,
            true
        ) { tab, position ->
            tab.setText(SubPage.values()[position].tab)
        }.attach()
        creatorHelper.addContentChangedListener(this)
        creatorHelper.addLoadStatusChangedListener(this)
        binding.previewImageView.setImageDrawable(previewDrawable)
        binding.saveBtn.onClick {
            saveQrBitmap()
        }
        binding.resultCheckBtn.onClick {
            onCheckResultClick()
        }
        onLoadStatusChanged(false)

        creatorHelper.contentValue = savedInstanceState?.getString(STATE_QR_VALUE, "") ?: ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_QR_VALUE, creatorHelper.contentValue)
    }

    private fun notifyQrChanged() {
        previewDrawable.invalidateSelf()
    }

    private fun onQrCheckResult(result: QrCreatorHelper.CheckResult) {
        this.currentCheckResult = result
        val faceIcon = when (result) {
            QrCreatorHelper.CheckResult.SUCCESSFUL -> {
                FaceIcons.HAPPY
            }

            QrCreatorHelper.CheckResult.ERROR -> {
                FaceIcons.SADNESS
            }

            QrCreatorHelper.CheckResult.EMPTY -> {
                FaceIcons.CALM
            }
        }
        binding.faceIconView.nextFace(faceIcon)
    }

    private fun onCheckResultClick() {
        val message = when (currentCheckResult) {
            QrCreatorHelper.CheckResult.SUCCESSFUL -> R.string.message_qr_check_result_success
            QrCreatorHelper.CheckResult.ERROR -> R.string.message_qr_check_result_error
            QrCreatorHelper.CheckResult.EMPTY -> R.string.message_qr_check_result_empty
        }
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(R.string.known) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveQrBitmap() {
        if (creatorHelper.contentValue.isEmpty()) {
            return
        }
        if (isLoading) {
            return
        }
        startLoading()
        doAsync({
            stopLoading()
            Toast.makeText(
                this, R.string.toast_error_create_qr_bitmap, Toast.LENGTH_SHORT
            ).show()
        }) {
            val result = creatorHelper.createShareQrBitmap()
            val bitmap = result.getOrNull()
            if (bitmap == null) {
                onUI {
                    stopLoading()
                    Toast.makeText(
                        this, R.string.toast_error_create_qr_bitmap, Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val uri = QrCreatorHelper.saveToMediaStore(this, bitmap)
                onUI {
                    stopLoading()
                    if (uri == null) {
                        Toast.makeText(
                            this, R.string.toast_error_save_qr_bitmap, Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        ShareSheet.shareImage(this, uri)
                    }
                }
            }
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)

        binding.saveBtn.setBackgroundColor(pigment.secondaryVariant)
        binding.saveBtnText.setTextColor(pigment.onSecondaryTitle)
        binding.saveBtnIcon.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)

        BlendMode.flow(pigment.primaryColor)
            .blend(pigment.backgroundColor, remember = false) {
                binding.faceIconView.setBackgroundColor(it)
            }
            .blend(pigment.onBackgroundTitle, remember = false) {
                binding.faceIconView.color = it
            }
        binding.panelGroup.setBackgroundColor(pigment.extreme)
        binding.tabLayout.setTabTextColors(pigment.onBackgroundBody, pigment.primaryColor)
        binding.tabLayout.setSelectedTabIndicatorColor(pigment.primaryColor)
        binding.tabLayout.tabRippleColor = ColorStateList.valueOf(
            BlendMode.blend(pigment.primaryColor, pigment.backgroundColor, 0.8F)
        )
    }

    override fun onCodeContentChanged(value: String) {
        log("onCodeContentChanged： $value")
        findTypedFragment<OnCodeContentChangedListener>()?.onCodeContentChanged(value)
        val empty = value.isEmpty()
        binding.saveBtn.isEnabled = !empty
        binding.saveBtn.alpha = if (empty) {
            0.2F
        } else {
            1F
        }
        binding.previewImageView.isInvisible = empty
    }

    override fun onLoadStatusChanged(isLading: Boolean) {
        log("onLoadStatusChanged： $isLading")
        if (isLading) {
            startLoading()
        } else {
            stopLoading()
        }
    }

    private fun startLoading() {
        isLoading = true
        binding.contentLoadingView.show()
    }

    private fun stopLoading() {
        isLoading = false
        binding.contentLoadingView.hide()
    }

    private fun openBuildPage() {
        log("openBuildPage")
        contentBuilderLauncher.launch(null)
    }

    private fun findFragment(position: Int = -1): Fragment? {
        val pager2 = binding.subpageGroup
        val adapter = pager2.adapter ?: return null
        val itemCount = adapter.itemCount
        if (itemCount < 1) {
            return null
        }
        val pagePosition = if (position < 0 || position >= itemCount) {
            pager2.currentItem
        } else {
            position
        }
        if (pagePosition < 0 || pagePosition >= itemCount) {
            return null
        }
        val itemId = adapter.getItemId(pagePosition)
        return supportFragmentManager.findFragmentByTag("f$itemId")
    }

    private inline fun <reified T : Any> findTypedFragment(position: Int = -1): T? {
        val fragment = findFragment(position) ?: return null
        if (fragment is T) {
            return fragment
        }
        return null
    }

    override fun getQrContentInfo(): String {
        log("getQrContentInfo: ${creatorHelper.contentValue}")
        return creatorHelper.contentValue
    }

    override fun requestChangeContent() {
        log("requestChangeContent")
        QrContentInputPopupWindow.show(
            this,
            getQrContentInfo(),
            ::openBuildPage
        ) {
            creatorHelper.contentValue = it
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private class SubPageAdapter(
        fragmentActivity: FragmentActivity,
    ) : FragmentStateAdapter(fragmentActivity) {

        private val pageInfo = SubPage.values()

        override fun getItemCount(): Int {
            return pageInfo.size
        }

        override fun createFragment(position: Int): Fragment {
            return pageInfo[position].fragment.newInstance()
        }

    }

    private enum class SubPage(
        val tab: Int,
        val fragment: Class<out Fragment>,
    ) {

        CONTENT(R.string.tab_content, QrContentValueFragment::class.java),
        BACKGROUND(R.string.tab_background, QrBackgroundFragment::class.java),
        // POSITION_DETECTION(R.string.tab_position_detection,QrPositionDetectionFragment::class.java),
        // ALIGNMENT(R.string.tab_alignment, QrAlignmentFragment::class.java),
        // DATA_POINT(R.string.tab_data_point, QrDataPointFragment::class.java),

    }

}