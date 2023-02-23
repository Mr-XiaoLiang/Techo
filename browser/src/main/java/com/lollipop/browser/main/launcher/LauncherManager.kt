package com.lollipop.browser.main.launcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.browser.R
import com.lollipop.browser.utils.FileInfoManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object LauncherManager : FileInfoManager() {

    private const val LAUNCHER_FILE_NAME = "launcher.ltf"
    private const val LAUNCHER_IMAGE_DIR_NAME = "launcher"
    private const val LAUNCHER_BACKGROUND_WIDTH = 96
    private const val LAUNCHER_BACKGROUND_HEIGHT = 122
    private const val LAUNCHER_LOGO_SIZE = 96

    private const val KEY_LABEL = "label"
    private const val KEY_ICON = "icon"
    private const val KEY_BACKGROUND = "background"
    private const val KEY_BACKGROUND_COLOR = "color"
    private const val KEY_URL = "url"

    private var launcherBackgroundSize = Size(LAUNCHER_BACKGROUND_WIDTH, LAUNCHER_BACKGROUND_HEIGHT)
    private var launcherLogoSize = Size(LAUNCHER_LOGO_SIZE, LAUNCHER_LOGO_SIZE)

    private var launcherFile: File? = null
    private var launcherImageDir: File? = null

    private var isInit = false

    private val cacheList = ArrayList<LauncherInfo>()

    private val idMap = HashMap<Int, Int>()

    override fun init(context: Context) {
        if (isInit && launcherFile != null && launcherImageDir != null) {
            return
        }
        launcherFile = File(context.filesDir, LAUNCHER_FILE_NAME)
        launcherImageDir = File(context.filesDir, LAUNCHER_IMAGE_DIR_NAME)
        val displayMetrics = context.resources.displayMetrics
        launcherBackgroundSize = Size(
            getDp(displayMetrics, LAUNCHER_BACKGROUND_WIDTH),
            getDp(displayMetrics, LAUNCHER_BACKGROUND_HEIGHT)
        )
        launcherLogoSize = Size(
            getDp(displayMetrics, LAUNCHER_LOGO_SIZE),
            getDp(displayMetrics, LAUNCHER_LOGO_SIZE)
        )
        cacheList.clear()
        isInit = true
    }

    private fun getDp(displayMetrics: DisplayMetrics, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics
        ).toInt()
    }

    fun load(context: Context, callback: (List<LauncherInfo>) -> Unit) {
        init(context)
        val file = launcherFile
        if (file == null) {
            callback(emptyList())
            return
        }
        if (cacheList.isNotEmpty()) {
            callback(ArrayList(cacheList))
            return
        }
        doAsync {
            val fileInfoList = loadFromFile()
            if (fileInfoList.isEmpty()) {
                val defaultInfo = loadDefaultInfo(context)
                cacheList.clear()
                cacheList.addAll(defaultInfo)
                save()
                onUI {
                    callback(defaultInfo)
                }
            } else {
                cacheList.clear()
                cacheList.addAll(fileInfoList)
                onUI {
                    callback(fileInfoList)
                }
            }
        }
    }

    private fun loadDefaultInfo(context: Context): List<LauncherInfo> {
        val launcherList = ArrayList<LauncherInfo>()
        DefaultLauncher.values().forEach { def ->
            val colorValue = def.background.map { ContextCompat.getColor(context, it) }
            launcherList.add(
                LauncherInfo(
                    id = createId(),
                    label = context.getString(def.label),
                    icon = createDrawableFile(context, def.icon, launcherLogoSize),
                    backgroundFile = null,
                    backgroundColor = colorValue,
                    url = def.url
                )
            )
        }
        return launcherList
    }

    private fun createId(): Int {
        val first = System.currentTimeMillis().and(0x8FFF).shl(16).toInt()
        var count = 10
        do {
            val end = Random.nextInt(0xFFFF)
            val id = first + end
            if (!idMap.containsKey(id)) {
                idMap[id] = 1
                return id
            }
            count--
        } while (count > 0)
        return 0
    }

    private fun save() {
        val file = launcherFile ?: return
        doAsync {
            val tempList = java.util.ArrayList(cacheList)
            val jsonArray = JSONArray()
            tempList.forEach {
                if (it.url.isNotEmpty()) {
                    jsonArray.put(it.toJson())
                }
            }
            writeToFile(jsonArray, file)
        }
    }

    fun add(
        label: String,
        icon: File?,
        backgroundFile: File?,
        backgroundColor: List<Int>,
        url: String,
    ) {
        add(label, icon, backgroundFile, backgroundColor, url, 0)
    }

    private fun add(
        label: String,
        icon: File?,
        backgroundFile: File?,
        backgroundColor: List<Int>,
        url: String,
        index: Int
    ) {
        val info = LauncherInfo(
            id = createId(),
            label = label,
            icon = icon,
            backgroundFile = backgroundFile,
            backgroundColor = backgroundColor,
            url = url
        )
        if (index < 0) {
            cacheList.add(0, info)
        } else if (index > cacheList.size) {
            cacheList.add(info)
        } else {
            cacheList.add(index, info)
        }
        save()
    }

    fun remove(launcherInfo: LauncherInfo) {
        val info = cacheList.find { launcherInfo.id == it.id } ?: return
        cacheList.remove(info)
        save()
        doAsync {
            info.icon?.delete()
            info.backgroundFile?.delete()
        }
    }

    private fun loadFromFile(): List<LauncherInfo> {
        val file = launcherFile ?: return emptyList()
        val result = ArrayList<LauncherInfo>()
        readArrayFromFile(file, {}) { obj ->
            val info = obj.toInfo()
            if (info != null) {
                result.add(info)
            }
        }
        return result
    }

    private fun LauncherInfo.toJson(): JSONObject {
        val info = this
        val colorArray = JSONArray()
        info.backgroundColor.forEach {
            colorArray.put(colorToString(it))
        }
        return JSONObject()
            .put(KEY_LABEL, info.label)
            .put(KEY_ICON, info.icon?.path ?: "")
            .put(KEY_BACKGROUND, info.backgroundFile?.path ?: "")
            .put(KEY_URL, info.url)
            .put(KEY_BACKGROUND_COLOR, colorArray)
    }

    private fun colorToString(color: Int): String {
        val builder = StringBuilder("#")
        val alpha = getColorHex(Color.alpha(color))
        if (alpha.length < 2) {
            builder.append("0")
        }
        builder.append(alpha)

        val red = getColorHex(Color.red(color))
        if (red.length < 2) {
            builder.append("0")
        }
        builder.append(red)

        val green = getColorHex(Color.green(color))
        if (green.length < 2) {
            builder.append("0")
        }
        builder.append(green)

        val blue = getColorHex(Color.blue(color))
        if (blue.length < 2) {
            builder.append("0")
        }
        builder.append(blue)

        return builder.toString()
    }

    private fun getColorHex(value: Int): String {
        val color = min(255, max(0, value))
        return color.toString(16)
    }

    private fun stringToColor(value: String): Int? {
        if (value.isEmpty()) {
            return null
        }
        try {
            return Color.parseColor(value)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    private fun JSONObject.toInfo(): LauncherInfo? {
        val obj = this
        if (obj.length() < 1 || !obj.has(KEY_URL)) {
            return null
        }
        val colorArray = obj.optJSONArray(KEY_BACKGROUND_COLOR)
        val colors = ArrayList<Int>()
        if (colorArray != null && colorArray.length() > 0) {
            for (i in 0 until colorArray.length()) {
                val color = stringToColor(colorArray.optString(i, ""))
                if (color != null) {
                    colors.add(color)
                }
            }
        }
        return LauncherInfo(
            id = createId(),
            label = obj.optString(KEY_LABEL),
            icon = obj.optString(KEY_ICON).optFile(),
            backgroundFile = obj.optString(KEY_BACKGROUND).optFile(),
            backgroundColor = colors,
            url = obj.optString(KEY_URL),
        )
    }

    private fun createDrawableFile(context: Context, @DrawableRes resId: Int, size: Size): File? {
        val dir = launcherImageDir ?: return null
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val file = File(dir, createFileName())
        writeToFile(drawable, size, file)
        return file
    }

    private fun createFileName(): String {
        val timeValue = System.currentTimeMillis().toString(16)
        val randomValue = Random.nextInt(9999).toString(16)
        return "${timeValue}_${randomValue}"
    }

    private fun writeToFile(drawable: Drawable, size: Size, file: File) {
        var outputStream: OutputStream? = null
        try {
            file.parentFile?.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, size.width, size.height)
            val canvas = Canvas(bitmap)
            drawable.draw(canvas)
            val fileOutputStream = FileOutputStream(file)
            outputStream = fileOutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
            fileOutputStream.flush()
            bitmap.recycle()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    fun move(info: LauncherInfo, index: Int) {
        val currentInfo = if (info.id != 0) {
            cacheList.find { info.id == it.id }
        } else {
            null
        }
        if (currentInfo != null) {
            cacheList.remove(currentInfo)
            if (index > cacheList.size) {
                cacheList.add(currentInfo)
            } else if (index < 0) {
                cacheList.add(0, currentInfo)
            } else {
                cacheList.add(index, currentInfo)
            }
        } else {
            add(info.label, info.icon, info.backgroundFile, info.backgroundColor, info.url, index)
        }
    }

    private enum class DefaultLauncher(
        @StringRes
        val label: Int,
        @DrawableRes
        val icon: Int,
        @ColorRes
        val iconTint: Int,
        @DrawableRes
        val background: IntArray,
        val url: String
    ) {

        Bing(
            label = R.string.label_bing,
            icon = R.drawable.ic_bing,
            iconTint = R.color.gray_0,
            background = intArrayOf(
                R.color.logo_bing_1,
                R.color.logo_bing_2,
                R.color.logo_bing_3,
                R.color.logo_bing_4
            ),
            url = "https://www.bing.com"
        ),

        Baidu(
            label = R.string.label_baidu,
            icon = R.drawable.ic_baidu,
            iconTint = R.color.gray_0,
            background = intArrayOf(R.color.logo_baidu_1, R.color.logo_baidu_2),
            url = "https://www.baidu.com"
        ),

        Google(
            label = R.string.label_google,
            icon = R.drawable.ic_google,
            iconTint = R.color.gray_0,
            background = intArrayOf(
                R.color.logo_google_1,
                R.color.logo_google_2,
                R.color.logo_google_3,
                R.color.logo_google_4
            ),
            url = "https://www.google.com"
        ),

    }


}