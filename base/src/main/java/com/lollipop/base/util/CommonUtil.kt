package com.lollipop.base.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.io.*
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * @author lollipop
 * @date 2020/6/9 23:17
 * 通用的全局工具方法
 */
object CommonUtil {

    /**
     * 全局的打印日志的关键字
     */
    var logTag = "Techo"

    /**
     * 异步线程池
     */
    private val threadPool: Executor by lazy {
        Executors.newScheduledThreadPool(2)
    }

    /**
     * 主线程的handler
     */
    private val mainThread: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * 异步任务
     */
    fun <T> doAsync(task: Task<T>) {
        threadPool.execute(task.runnable)
    }

    /**
     * 主线程
     */
    fun <T> onUI(task: Task<T>) {
        mainThread.post(task.runnable)
    }

    /**
     * 主线程
     */
    fun <T> tryUI(task: Task<T>) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            task.runnable.run()
        } else {
            mainThread.post(task.runnable)
        }
    }

    /**
     * 延迟任务
     */
    fun <T> delay(delay: Long, task: Task<T>) {
        mainThread.postDelayed(task.runnable, delay)
    }

    /**
     * 移除任务
     */
    fun <T> remove(task: Task<T>) {
        mainThread.removeCallbacks(task.runnable)
    }

    /**
     * 将一组对象打印合并为一个字符串
     */
    fun print(value: Array<out Any>): String {
        if (value.isEmpty()) {
            return ""
        }
        val iMax = value.size - 1
        val b = StringBuilder()
        var i = 0
        while (true) {
            b.append(value[i].toString())
            if (i == iMax) {
                return b.toString()
            }
            b.append(" ")
            i++
        }
    }

    /**
     * 包装的任务类
     * 包装的意义在于复用和移除任务
     * 由于Handler任务可能造成内存泄漏，因此在生命周期结束时，有必要移除任务
     * 由于主线程的Handler使用了全局的对象，移除不必要的任务显得更为重要
     * 因此包装了任务类，以任务类为对象来保留任务和移除任务
     */
    class Task<T>(
        private val target: T,
        private val err: ((Throwable) -> Unit) = {},
        private val run: T.() -> Unit
    ) {

        val runnable = Runnable {
            try {
                run(target)
            } catch (e: Throwable) {
                err(e)
            }
        }

        fun cancel() {
            remove(this)
        }

        fun run() {
            doAsync(this)
        }

        fun sync() {
            onUI(this)
        }

        fun delay(time: Long) {
            delay(time, this)
        }
    }

}

/**
 * 用于创建一个任务对象
 */
inline fun <reified T> T.task(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
) = CommonUtil.Task(this, err, run)

/**
 * 异步任务
 */
inline fun <reified T> T.doAsync(
    noinline err: ((Throwable) -> Unit) = { it.printStackTrace() },
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.doAsync(task)
    return task
}

/**
 * 主线程
 */
inline fun <reified T> T.onUI(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.onUI(task)
    return task
}

inline fun <reified T> T.tryUI(
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.tryUI(task)
    return task
}

/**
 * 延迟任务
 */
inline fun <reified T> T.delay(
    delay: Long,
    noinline err: ((Throwable) -> Unit) = {},
    noinline run: T.() -> Unit
): CommonUtil.Task<T> {
    val task = task(err, run)
    CommonUtil.delay(delay, task)
    return task
}

/**
 * 一个全局的打印Log的方法
 */
inline fun <reified T : Any> T.logD(tag: String = ""): (String) -> Unit {
    val logTag = "$tag ${this.javaClass.simpleName}@${System.identityHashCode(this)}"
    return { value ->
        value.splitLog {
            Log.d(CommonUtil.logTag, "$logTag -> $value")
        }
    }
}

inline fun <reified T : Any> T.lazyLogD(tag: String = "") = lazy { logD(tag) }

fun CharSequence.splitLog(callback: (CharSequence) -> Unit) {
    val value = this
    val splitLength = 500
    val length = value.length
    if (length <= splitLength) {
        callback(value)
        return
    }
    var startIndex = 0
    while (startIndex < length) {
        val endIndex = kotlin.math.min(startIndex + splitLength, length)
        callback(value.subSequence(startIndex, endIndex))
        startIndex = endIndex
    }
}

/**
 * 对一个输入框关闭键盘
 */
fun EditText.closeBoard() {
    //拿到InputMethodManager
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
        //如果window上view获取焦点 && view不为空
        if (imm.isActive) {
            //拿到view的token 不为空
            windowToken?.let { token ->
                imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }
}

/**
 * 对一个输入框请求键盘
 */
fun EditText.requestBoard(selected: Int = -1) {
    requestFocus()
    if (selected < 0) {
        setSelection(text.length)
    } else {
        setSelection(selected)
    }
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
    if (imm is InputMethodManager) {
        imm.showSoftInput(this, 0)
    }
}

/**
 * 对一个activity关闭键盘
 */
fun Activity.closeBoard() {
    //拿到InputMethodManager
    (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imm ->
        //如果window上view获取焦点 && view不为空
        if (imm.isActive) {
            currentFocus?.let { focus ->
                //拿到view的token 不为空
                focus.windowToken?.let { token ->
                    //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                    imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }
}

/**
 * 对一个颜色值设置它的透明度
 * 只支持#AARRGGBB格式排列的颜色值
 */
fun Int.changeAlpha(a: Int): Int {
    return this and 0xFFFFFF or ((a % 256) shl 24)
}

/**
 * 以浮点数的形式，以当前透明度为基础，
 * 调整颜色值的透明度
 */
fun Int.changeAlpha(f: Float): Int {
    return this.changeAlpha(((this ushr 24) * f).toInt().range(0, 255))
}

/**
 * 将一个浮点数，以dip为单位转换为对应的像素值
 */
val Float.dp2px: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )
    }

/**
 * 将一个浮点数，以sp为单位转换为对应的像素值
 */
val Float.sp2px: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )
    }

/**
 * 将一个整数，以dip为单位转换为对应的像素值
 */
val Int.dp2px: Int
    get() {
        return this.toFloat().dp2px.toInt()
    }

/**
 * 将一个整数，以sp为单位转换为对应的像素值
 */
val Int.sp2px: Int
    get() {
        return this.toFloat().sp2px.toInt()
    }

/**
 * 对一个浮点数做范围约束
 */
fun Float.range(min: Float, max: Float): Float {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

/**
 * 将一个字符串转换为颜色值
 * 只接受1～8位0～F之间的字符
 */
fun String.parseColor(): Int {
    val value = this
    if (value.isEmpty()) {
        return 0
    }
    return when (value.length) {
        1 -> {
            val v = (value + value).toInt(16)
            Color.rgb(v, v, v)
        }

        2 -> {
            val v = value.toInt(16)
            Color.rgb(v, v, v)
        }

        3 -> {
            val r = value.substring(0, 1)
            val g = value.substring(1, 2)
            val b = value.substring(2, 3)
            Color.rgb(
                (r + r).toInt(16),
                (g + g).toInt(16),
                (b + b).toInt(16)
            )
        }

        4, 5 -> {
            val a = value.substring(0, 1)
            val r = value.substring(1, 2)
            val g = value.substring(2, 3)
            val b = value.substring(3, 4)
            Color.argb(
                (a + a).toInt(16),
                (r + r).toInt(16),
                (g + g).toInt(16),
                (b + b).toInt(16)
            )
        }

        6, 7 -> {
            val r = value.substring(0, 2).toInt(16)
            val g = value.substring(2, 4).toInt(16)
            val b = value.substring(4, 6).toInt(16)
            Color.rgb(r, g, b)
        }

        8 -> {
            val a = value.substring(0, 2).toInt(16)
            val r = value.substring(2, 4).toInt(16)
            val g = value.substring(4, 6).toInt(16)
            val b = value.substring(6, 8).toInt(16)
            Color.argb(a, r, g, b)
        }

        else -> {
            Color.WHITE
        }
    }
}

/**
 * 对一个输入框做回车事件监听
 */
inline fun <reified T : EditText> T.onActionDone(noinline run: T.() -> Unit) {
    this.imeOptions = EditorInfo.IME_ACTION_DONE
    this.setOnEditorActionListener { _, actionId, event ->
        if (actionId.and(EditorInfo.IME_ACTION_DONE) != 0
            || event?.keyCode == KeyEvent.KEYCODE_ENTER
        ) {
            run.invoke(this)
            true
        } else {
            false
        }
    }
}

/**
 * 尝试将一个字符串转换为整形，
 * 如果发生了异常或者为空，那么将会返回默认值
 */
fun String.tryInt(def: Int): Int {
    try {
        if (TextUtils.isEmpty(this)) {
            return def
        }
        return this.toInt()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return def
}

/**
 * 一个整形的范围约束
 */
fun Int.range(min: Int, max: Int): Int {
    if (this < min) {
        return min
    }
    if (this > max) {
        return max
    }
    return this
}

/**
 * 从context中获取当前应用的版本名称
 */
fun Context.versionName(): String {
    return packageManager.getPackageInfo(packageName, 0).versionName
}

/**
 * 从context中获取当前应用的版本名称
 */
fun Context.versionCode(): Long {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    versionThen(Build.VERSION_CODES.P) {
        return packageInfo.longVersionCode
    }
    return packageInfo.versionCode.toLong()
}

/**
 * 将一段文本写入一个文件中
 * 它属于IO操作，这是一个耗时的任务，
 * 需要在子线程中执行
 */
fun String.writeTo(file: File) {
    try {
        if (file.exists()) {
            file.delete()
        } else {
            file.parentFile?.mkdirs()
        }
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        val buffer = ByteArray(2048)
        try {
            inputStream = ByteArrayInputStream(this.toByteArray(Charsets.UTF_8))
            outputStream = FileOutputStream(file)
            var length = inputStream.read(buffer)
            while (length >= 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.flush()
        } catch (ee: Throwable) {
            ee.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun interface SimpleViewClickCallback<V : View> {
    fun onClick(v: V)
}

inline fun <reified T : View> T.onClick(
    interval: Long = 300,
    callback: SimpleViewClickCallback<T>
) {
    setOnClickListener(SimpleIntervalClickListener(interval, this, callback))
}

class SimpleIntervalClickListener<V : View>(
    private val interval: Long = 0,
    target: V,
    private val callback: SimpleViewClickCallback<V>
) : View.OnClickListener {

    private var lastClickTime = 0L
    private val targetView = WeakReference(target)

    override fun onClick(v: View?) {
        val target = targetView.get() ?: return
        if (target != v) {
            return
        }
        val now = System.currentTimeMillis()
        if (interval < 0 || now - lastClickTime > interval) {
            lastClickTime = now
            callback.onClick(target)
        }
    }

}

fun Int.smallerThen(o: Int): Int {
    if (this > o) {
        return o
    }
    return this
}

fun Int.biggerThen(o: Int): Int {
    if (this < o) {
        return o
    }
    return this
}

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
inline fun versionThen(target: Int, callback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= target) {
        callback()
    }
}

fun View.setEmptyClick() {
    onClick { }
}

@SuppressLint("ClickableViewAccessibility")
fun View.setEmptyTouch() {
    setOnTouchListener { _, _ -> true }
}

/**
 * 是否已经被销毁
 */
fun LifecycleOwner.isDestroyed() = lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)

/**
 * 是否已经被创建
 */
fun LifecycleOwner.isCreated() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)

/**
 * 是否已经被初始化
 */
fun LifecycleOwner.isInitialized() = lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)

/**
 * 是否已经开始运行
 */
fun LifecycleOwner.isStarted() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

/**
 * 是否已经可见
 */
fun LifecycleOwner.isResumed() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

inline fun <reified T : Any> tryUse(info: T?, callback: (T) -> Unit) {
    info?.let(callback)
}

inline fun <reified T : Any> tryWith(info: T?, callback: T.() -> Unit) {
    info?.apply(callback)
}

