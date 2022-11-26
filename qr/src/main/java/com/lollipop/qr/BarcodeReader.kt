package com.lollipop.qr

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor

abstract class BarcodeReader(
    protected val lifecycleOwner: LifecycleOwner
) {

    companion object {
        @JvmStatic
        fun yuv420888ToBitmap(inputImage: InputImage): Bitmap? {
            if (inputImage.format == ImageFormat.YUV_420_888.code) {
                val image = inputImage.mediaImage ?: return null
                return YuvToRgbConverter.yuv420888ToRgb(image)
            }
            return null
        }
    }

    protected var currentStatus = Lifecycle.State.DESTROYED
        private set

    protected val isResumed: Boolean
        get() {
            return currentStatus.isAtLeast(Lifecycle.State.RESUMED)
        }

    private val lifecycleObserver = LifecycleEventObserver { source, _ ->
        currentStatus = source.lifecycle.currentState
        onLifecycleStateChanged()
    }

    var scanFormat = BarcodeFormat.values()

    protected open val resultByEmpty = true

    private val onBarcodeScanResultListener = ArrayList<OnBarcodeScanResultListener>()

    protected val mainExecutor = MainExecutor()
    private var analyzerExecutorImpl: SingleExecutor? = null

    protected val analyzerExecutor: Executor
        get() {
            return getOrCreateAnalyzerExecutor()
        }

    init {
        currentStatus = lifecycleOwner.lifecycle.currentState
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    protected open fun onLifecycleStateChanged() {}

    private fun getOrCreateAnalyzerExecutor(): Executor {
        val executor = analyzerExecutorImpl
        if (executor != null && !executor.isDestroy) {
            return executor
        }
        val newExecutor = SingleExecutor(lifecycleOwner, "AnalyzerExecutor")
        analyzerExecutorImpl = newExecutor
        return newExecutor
    }

    fun addOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.add(listener)
    }

    fun removeOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.remove(listener)
    }

    protected fun onDecodeSuccess(list: List<Barcode>, tag: String) {
        if (!resultByEmpty && list.isEmpty()) {
            return
        }
        val resultList = list.map { code ->
            BarcodeWrapper(parseBarcode(code), BarcodeResultBuilder.createCodeDescribeBy(code))
        }

        onUI {
            onBarcodeScanResultListener.forEach {
                it.onBarcodeScanResult(BarcodeResult(ArrayList(resultList), tag))
            }
        }
    }

    protected fun doAsync(command: Runnable) {
        analyzerExecutor.execute(command)
    }

    protected fun onUI(command: Runnable) {
        mainExecutor.execute(command)
    }

    private fun parseBarcode(code: Barcode): BarcodeInfo {
        return when (findBarcodeType(code.valueType)) {
            BarcodeType.UNKNOWN -> {
                BarcodeInfo.Unknown
            }
            BarcodeType.CONTACT_INFO -> {
                BarcodeResultBuilder.createContactBy(code)
            }
            BarcodeType.EMAIL -> {
                BarcodeResultBuilder.createEmailBy(code)
            }
            BarcodeType.ISBN -> {
                BarcodeInfo.Isbn
            }
            BarcodeType.PHONE -> {
                BarcodeResultBuilder.createPhoneBy(code)
            }
            BarcodeType.PRODUCT -> {
                BarcodeInfo.Product
            }
            BarcodeType.SMS -> {
                BarcodeResultBuilder.createSmsBy(code)
            }
            BarcodeType.TEXT -> {
                BarcodeInfo.Text
            }
            BarcodeType.URL -> {
                BarcodeResultBuilder.createUrlBy(code)
            }
            BarcodeType.WIFI -> {
                BarcodeResultBuilder.createWifiBy(code)
            }
            BarcodeType.GEO -> {
                BarcodeResultBuilder.createGeoBy(code)
            }
            BarcodeType.CALENDAR_EVENT -> {
                BarcodeResultBuilder.createCalendarEventBy(code)
            }
            BarcodeType.DRIVER_LICENSE -> {
                BarcodeResultBuilder.createDriverLicenseBy(code)
            }
        }
    }

    private fun findBarcodeType(code: Int): BarcodeType {
        BarcodeType.values().forEach {
            if (it.code == code) {
                return it
            }
        }
        return BarcodeType.UNKNOWN
    }


    private class SingleExecutor(lifecycleOwner: LifecycleOwner, threadName: String) : Executor {

        private val thread = HandlerThread(threadName).apply {
            start()
        }

        private val handler = Handler(thread.looper)

        var isDestroy = false
            private set

        init {
            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event.targetState == Lifecycle.State.DESTROYED) {
                        destroy()
                    }
                }
            })
        }

        override fun execute(command: Runnable?) {
            if (isDestroy) {
                return
            }
            command ?: return
            handler.post(command)
        }

        fun destroy() {
            if (isDestroy) {
                return
            }
            isDestroy = true
            thread.quitSafely()
        }
    }

    protected class MainExecutor : Executor {

        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable?) {
            command ?: return
            handler.post(command)
        }

        fun delay(delay: Long, command: Runnable) {
            handler.postDelayed(command, delay)
        }

        fun cancel(command: Runnable) {
            handler.removeCallbacks(command)
        }

    }

}