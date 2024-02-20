package com.lollipop.techo.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lollipop.base.util.TxtHelper
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import java.io.File
import java.util.concurrent.ConcurrentHashMap

abstract class TechoTheme(
    val key: String,
) {

    companion object {

        var DEFAULT: Base = Base.LIGHT

        private val entries = ConcurrentHashMap<String, TechoTheme>()

        fun findBase(key: String): Base? {
            if (key == Base.LIGHT.key) {
                return Base.LIGHT
            }
            if (key == Base.DARK.key) {
                return Base.DARK
            }
            return null
        }

        fun find(key: String): TechoTheme {
            val base = findBase(key)
            if (base != null) {
                return base
            }
            return entries[key] ?: DEFAULT
        }

        fun valueOf(pigment: Pigment): Snapshot {
            return SimpleSnapshot(
                pigment.primaryColor,
                pigment.secondaryColor,
                pigment.blendMode
            )
        }

        fun putTheme(theme: TechoTheme) {
            if (theme is Base) {
                return
            }
            entries[theme.key] = theme
        }

        fun preloadProfile(context: Context, complete: () -> Unit) {
            doAsync {
                CustomTechoTheme.loadProfiles(context) { file ->
                    parse(file)
                }
                onUI {
                    complete()
                }
            }
        }

        fun parse(file: File): Result<Custom> {
            try {
                val content = TxtHelper.readFromFile(file)
                return parse(content)
            } catch (e: Throwable) {
                return Result.failure(e)
            }
        }

        fun parse(json: String): Result<Custom> {
            val customResult = CustomTechoTheme.parse(json)
            customResult.getOrNull()?.let {
                putTheme(it)
            }
            return customResult
        }

    }

    protected val blendModeWrapper = BlendModeWrapper()

    fun getPigment(): Snapshot {
        return getPigment(AppTheme.current)
    }

    protected abstract fun getPigment(pigment: Pigment): Snapshot

    sealed class Base(key: String) : TechoTheme(key) {

        abstract val isDarkMode: Boolean

        data object LIGHT : Base("light") {
            override val isDarkMode: Boolean = false

            override fun getPigment(pigment: Pigment): Snapshot {
                val primary = blendModeWrapper.appPrimary.toArgb()
                return SimpleSnapshot(
                    primary,
                    blendModeWrapper.appSecondary?.toArgb() ?: primary,
                    BlendMode.Light
                )
            }

        }

        data object DARK : Base("dark") {
            override val isDarkMode: Boolean = true
            override fun getPigment(pigment: Pigment): Snapshot {
                val primary = blendModeWrapper.appPrimary.toArgb()
                return SimpleSnapshot(
                    primary,
                    blendModeWrapper.appSecondary?.toArgb() ?: primary,
                    BlendMode.Dark
                )
            }

        }
    }

    class Custom(key: String, private val profile: Profile) : TechoTheme(key) {

        override fun getPigment(pigment: Pigment): Snapshot {
            return CustomSnapshot(profile, profile.base.getPigment(pigment))
        }

        interface Profile {
            val base: Base

            val primaryColor: Int?

            val secondaryColor: Int?

            /**
             * 主题色变体
             * 一般表示比主题色更深或者更浅的颜色，
             * 用于表达主题色，但是和主题色区分
             */
            val primaryVariant: Int?

            /**
             * 在主题色之上的内容的颜色
             */
            val onPrimaryTitle: Int?

            /**
             * 在主题色之上的内容的颜色
             */
            val onPrimaryBody: Int?

            /**
             * 次要颜色的变体
             * 一般是次要颜色的加深或是变浅
             * 用于表达次要颜色的同时和次要颜色区分
             */
            val secondaryVariant: Int?

            /**
             * 在次要颜色之上的内容的颜色
             */
            val onSecondaryTitle: Int?

            /**
             * 在次要颜色之上的内容的颜色
             */
            val onSecondaryBody: Int?

            /**
             * 背景色
             */
            val backgroundColor: Int?

            /**
             * 背景色之上的标题
             */
            val onBackgroundTitle: Int?

            /**
             * 背景色之上的内容体
             */
            val onBackgroundBody: Int?

            /**
             * 模式所对应的极致的颜色
             * 比如，在深色模式下，它会是黑色，浅色模式下，它会是白色
             */
            val extreme: Int?

            /**
             * 极致色彩的反转色
             */
            val extremeReversal: Int?

            /**
             * 极致色彩为背景时的标题颜色
             */
            val onExtremeTitle: Int?

            /**
             * 极致色彩为背景时的内容颜色
             */
            val onExtremeBody: Int?
        }

    }

    protected class BlendModeWrapper(
        private val custom: BlendMode? = null
    ) {
        fun get(): BlendMode {
            // 要么自定义的，要么app主题的
            return custom ?: AppTheme.current.blendMode
        }

        val appPrimary: Color
            get() {
                return AppTheme.current.primary
            }

        val appSecondary: Color?
            get() {
                return AppTheme.current.secondary
            }
    }

    interface Snapshot {
        val isDarkMode: Boolean

        val primaryColor: Int

        val secondaryColor: Int

        /**
         * 主题色变体
         * 一般表示比主题色更深或者更浅的颜色，
         * 用于表达主题色，但是和主题色区分
         */
        val primaryVariant: Int

        /**
         * 在主题色之上的内容的颜色
         */
        val onPrimaryTitle: Int

        /**
         * 在主题色之上的内容的颜色
         */
        val onPrimaryBody: Int

        /**
         * 次要颜色的变体
         * 一般是次要颜色的加深或是变浅
         * 用于表达次要颜色的同时和次要颜色区分
         */
        val secondaryVariant: Int

        /**
         * 在次要颜色之上的内容的颜色
         */
        val onSecondaryTitle: Int

        /**
         * 在次要颜色之上的内容的颜色
         */
        val onSecondaryBody: Int

        /**
         * 背景色
         */
        val backgroundColor: Int

        /**
         * 背景色之上的标题
         */
        val onBackgroundTitle: Int

        /**
         * 背景色之上的内容体
         */
        val onBackgroundBody: Int

        /**
         * 模式所对应的极致的颜色
         * 比如，在深色模式下，它会是黑色，浅色模式下，它会是白色
         */
        val extreme: Int

        /**
         * 极致色彩的反转色
         */
        val extremeReversal: Int

        /**
         * 极致色彩为背景时的标题颜色
         */
        val onExtremeTitle: Int

        /**
         * 极致色彩为背景时的内容颜色
         */
        val onExtremeBody: Int

        fun tint(fab: FloatingActionButton) {
            fab.backgroundTintList = ColorStateList.valueOf(secondaryColor)
            fab.imageTintList = ColorStateList.valueOf(onSecondaryBody)
        }

        fun tint(fab: ExtendedFloatingActionButton) {
            fab.backgroundTintList = ColorStateList.valueOf(secondaryColor)
            fab.iconTint = ColorStateList.valueOf(onSecondaryBody)
            fab.setTextColor(onSecondaryBody)
        }
    }

    open class SimpleSnapshot(
        private val primary: Int,
        private val secondary: Int,
        private val blendMode: BlendMode
    ) : Snapshot {
        override val isDarkMode: Boolean
            get() {
                return blendMode == BlendMode.Dark
            }

        override val primaryColor: Int by lazy {
            blendMode.original(primary)
        }

        override val secondaryColor: Int by lazy {
            blendMode.original(secondary)
        }

        /**
         * 主题色变体
         * 一般表示比主题色更深或者更浅的颜色，
         * 用于表达主题色，但是和主题色区分
         */
        override val primaryVariant: Int by lazy {
            blendMode.variant(primaryColor)
        }

        /**
         * 在主题色之上的内容的颜色
         */
        override val onPrimaryTitle: Int by lazy {
            blendMode.title(primaryColor)
        }

        /**
         * 在主题色之上的内容的颜色
         */
        override val onPrimaryBody: Int by lazy {
            blendMode.body(primaryColor)
        }

        /**
         * 次要颜色的变体
         * 一般是次要颜色的加深或是变浅
         * 用于表达次要颜色的同时和次要颜色区分
         */
        override val secondaryVariant: Int by lazy {
            blendMode.variant(secondaryColor)
        }

        /**
         * 在次要颜色之上的内容的颜色
         */
        override val onSecondaryTitle: Int by lazy {
            blendMode.title(secondaryColor)
        }

        /**
         * 在次要颜色之上的内容的颜色
         */
        override val onSecondaryBody: Int by lazy {
            blendMode.body(secondaryColor)
        }

        /**
         * 背景色
         */
        override val backgroundColor: Int by lazy {
            blendMode.background(primaryColor)
        }

        /**
         * 背景色之上的标题
         */
        override val onBackgroundTitle: Int by lazy {
            blendMode.title(backgroundColor)
        }

        /**
         * 背景色之上的内容体
         */
        override val onBackgroundBody: Int by lazy {
            blendMode.title(backgroundColor)
        }

        /**
         * 模式所对应的极致的颜色
         * 比如，在深色模式下，它会是黑色，浅色模式下，它会是白色
         */
        override val extreme: Int by lazy {
            blendMode.extreme
        }

        /**
         * 极致色彩的反转色
         */
        override val extremeReversal: Int by lazy {
            blendMode.extremeReversal
        }

        /**
         * 极致色彩为背景时的标题颜色
         */
        override val onExtremeTitle: Int by lazy {
            blendMode.title(extreme)
        }

        /**
         * 极致色彩为背景时的内容颜色
         */
        override val onExtremeBody: Int by lazy {
            blendMode.body(extreme)
        }
    }

    private class CustomSnapshot(
        private val profile: Custom.Profile,
        private val baseSnapshot: Snapshot
    ) : Snapshot {
        override val isDarkMode: Boolean
            get() = profile.base.isDarkMode
        override val primaryColor: Int
            get() {
                return profile.primaryColor ?: baseSnapshot.primaryColor
            }
        override val secondaryColor: Int
            get() {
                return profile.secondaryColor ?: baseSnapshot.secondaryColor
            }
        override val primaryVariant: Int
            get() {
                return profile.primaryVariant ?: baseSnapshot.primaryVariant
            }
        override val onPrimaryTitle: Int
            get() {
                return profile.onPrimaryTitle ?: baseSnapshot.onPrimaryTitle
            }
        override val onPrimaryBody: Int
            get() {
                return profile.onPrimaryBody ?: baseSnapshot.onPrimaryBody
            }
        override val secondaryVariant: Int
            get() {
                return profile.secondaryVariant ?: baseSnapshot.secondaryVariant
            }
        override val onSecondaryTitle: Int
            get() {
                return profile.onSecondaryTitle ?: baseSnapshot.onSecondaryTitle
            }
        override val onSecondaryBody: Int
            get() {
                return profile.onSecondaryBody ?: baseSnapshot.onSecondaryBody
            }
        override val backgroundColor: Int
            get() {
                return profile.backgroundColor ?: baseSnapshot.backgroundColor
            }
        override val onBackgroundTitle: Int
            get() {
                return profile.onBackgroundTitle ?: baseSnapshot.onBackgroundTitle
            }
        override val onBackgroundBody: Int
            get() {
                return profile.onBackgroundBody ?: baseSnapshot.onBackgroundBody
            }
        override val extreme: Int
            get() {
                return profile.extreme ?: baseSnapshot.extreme
            }
        override val extremeReversal: Int
            get() {
                return profile.extremeReversal ?: baseSnapshot.extremeReversal
            }
        override val onExtremeTitle: Int
            get() {
                return profile.onExtremeTitle ?: baseSnapshot.onExtremeTitle
            }
        override val onExtremeBody: Int
            get() {
                return profile.onExtremeBody ?: baseSnapshot.onExtremeBody
            }


    }

}