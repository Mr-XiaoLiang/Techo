package com.lollipop.techo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.ViewPropertyTransition
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.lazyLogD
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.pigment.Pigment
import com.lollipop.techo.data.RequestService
import com.lollipop.techo.databinding.ActivityHeaderBinding
import com.lollipop.techo.util.AppUtil
import com.lollipop.techo.util.BlurTransformation
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 带有头部View的Activity
 * @author Lollipop
 * @date 2021/04/25
 */
abstract class HeaderActivity : BaseActivity() {

    companion object {
        /**
         * 每次启动都保持不变吧
         */
        private var headerImageUrl = ""

        private fun getLocalImageName(): String {
            val day = 1000L * 60 * 60 * 24
            return (System.currentTimeMillis() / day).toString(16)
        }

        private fun getLocalImageFile(context: Context): WallpaperFile {
            val dir = File(context.filesDir, "wallpaper")
            dir.mkdirs()
            return WallpaperFile(File(dir, getLocalImageName()))
        }

        private fun saveLocalImage(context: Context, bitmap: Bitmap) {
            doAsync {
                val file = getLocalImageFile(context)
                val outputStream = FileOutputStream(file.file)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 80, outputStream)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
                }
            }
        }

        private fun saveLocalImage(
            wallpaperFile: WallpaperFile,
            sourceFile: File,
            complete: () -> Unit
        ) {
            doAsync {
                val destFile = wallpaperFile.file
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onUI {
                    complete()
                }
            }
        }

    }

    protected val scaffoldBinding: ActivityHeaderBinding by lazyBind()

    abstract val contentView: View

    open val floatingView: View? = null

    protected open val showBackArrow = true

    protected open val optionsMenu = 0

    private var isBlurHeader = AppUtil.isBlurHeader

    protected open val useCustomPigment = false

    private val log by lazyLogD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(scaffoldBinding.root)
        WindowInsetsHelper.fitsSystemWindows(this)
        scaffoldBinding.contentRoot.addView(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        floatingView?.let {
            scaffoldBinding.floatingRoot.addView(
                it,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        scaffoldBinding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        scaffoldBinding.appBar.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        scaffoldBinding.contentScrollView.fixInsetsByMargin(
            WindowInsetsEdge(
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ACCUMULATE,
                WindowInsetsEdgeStrategy.ORIGINAL
            )
        )
        scaffoldBinding.headerBackground.onClick {
            changeBlurState()
        }
        scaffoldBinding.optionButton.isVisible = optionsMenu != 0
        scaffoldBinding.optionButton.onClick {
            showOptionMenu(scaffoldBinding.optionButtonIcon)
        }
        loadHeader()
        hideLoading()
        setTitle("")
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        log("onDecorationChanged: $pigment")
        if (!useCustomPigment) {
            headerDarkMode(isDarkMode)
            scaffoldBinding.contentLoadingView.setIndicatorColor(
                pigment.primaryColor,
                pigment.secondaryColor,
                pigment.primaryVariant,
                pigment.secondaryVariant
            )
            scaffoldBinding.contentRoot.setBackgroundColor(pigment.backgroundColor)
//            Toast.makeText(this, "Dark mode = ${pigment.backgroundColor.toString(16)}", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun headerDarkMode(isDark: Boolean) {
        scaffoldBinding.headerBackgroundMask.isVisible = isDark
    }

    override fun setTitle(title: CharSequence?) {
        scaffoldBinding.titleView.isVisible = !TextUtils.isEmpty(title)
        scaffoldBinding.titleTextView.text = title ?: ""
    }

    protected fun showLoading() {
        scaffoldBinding.contentLoadingView.show()
    }

    override fun onResume() {
        super.onResume()
        if (isBlurHeader != AppUtil.isBlurHeader) {
            isBlurHeader = AppUtil.isBlurHeader
            loadHeader()
        }
    }

    private fun changeBlurState() {
        isBlurHeader = !isBlurHeader
        AppUtil.changeBlurHeader(this, isBlurHeader)
        loadHeader()
    }

    protected fun hideLoading() {
        scaffoldBinding.contentLoadingView.hide()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (optionsMenu != 0) {
            menuInflater.inflate(optionsMenu, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun showOptionMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        onCreateOptionsMenu(popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if (item == null) {
                    return false
                }
                return onOptionsItemSelected(item)
            }
        });
        popupMenu.show()
    }

    private fun loadHeader() {
        val imageUrl = headerImageUrl
        if (imageUrl.isNotEmpty()) {
            loadWallpaper(imageUrl)
            return
        }
        val wallpaperFile = getLocalImageFile(this)
        if (wallpaperFile.isExists) {
            val localUrl = wallpaperFile.path
            headerImageUrl = localUrl
            loadWallpaper(imageUrl)
            return
        }
        doAsync {
            val imageInfo = RequestService.getHeaderImageInfo()
            if (imageInfo.url.isNotEmpty()) {
                headerImageUrl = imageInfo.fullUrl
                loadAndCacheWallpaper(headerImageUrl)
            }
        }
    }

    private fun loadWallpaper(url: String) {
        var builder = Glide.with(scaffoldBinding.headerBackground)
            .load(url)
            .transition(GenericTransitionOptions.with(AlphaAnimator()))
        if (isBlurHeader) {
            builder = builder.apply(RequestOptions().transform(BlurTransformation.create()))
        }
        builder.into(scaffoldBinding.headerBackground)
    }

    private fun loadAndCacheWallpaper(url: String) {
        val wallpaperFile = getLocalImageFile(this)
        Glide.with(this)
            .downloadOnly()
            .load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>?,
                    isFirstResource: Boolean
                ): Boolean {
                    log("loadAndCacheWallpaper: failed, ${e?.message}")
                    e?.printStackTrace()
                    return false
                }

                override fun onResourceReady(
                    resource: File?,
                    model: Any?,
                    target: Target<File>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
                        if (it.exists()) {
                            saveLocalImage(wallpaperFile, resource) {
                                headerImageUrl = wallpaperFile.path
                                loadWallpaper(headerImageUrl)
                            }
                        }
                    }
                    return false
                }

            })
            .preload()
    }

    fun resultOk(callback: (Intent) -> Unit) {
        setResult(Activity.RESULT_OK, Intent().apply { callback(this) })
    }

    fun resultCanceled(callback: ((Intent) -> Unit)? = null) {
        setResult(Activity.RESULT_CANCELED, Intent().apply { callback?.invoke(this) })
    }

    private class AlphaAnimator : ViewPropertyTransition.Animator {
        override fun animate(view: View?) {
            view ?: return
            view.alpha = 0F
            view.animate()?.alphaBy(1F)?.start()
        }
    }

    private class WallpaperFile(val file: File) {
        val isExists: Boolean
            get() {
                return file.exists()
            }

        val path: String
            get() {
                return file.path
            }

    }

}