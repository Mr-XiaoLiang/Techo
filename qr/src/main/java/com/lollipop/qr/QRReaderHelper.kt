package com.lollipop.qr

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Size
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lollipop.qr.BarcodeType.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit


class QRReaderHelper(
    private val lifecycleOwner: LifecycleOwner
) {

    companion object {
        private const val AUTO_FOCUS_DURATION = 3 * 1000L
        private const val METERING_MODE = (FocusMeteringAction.FLAG_AF
            .or(FocusMeteringAction.FLAG_AE)
            .or(FocusMeteringAction.FLAG_AWB))
    }

    private var container: FrameLayout? = null

    private var previewView: PreviewView? = null

    var previewMode = QrPreviewMode.PERFORMANCE
        set(value) {
            field = value
            previewView?.implementationMode = value.mode
        }

    var scaleType = QrPreviewScaleType.FILL_CENTER
        set(value) {
            field = value
            previewView?.scaleType = value.type
        }

    var scanFormat = BarcodeFormat.values()

    private val mainExecutor = MainExecutor()
    private var analyzerExecutor: SingleExecutor? = null

    private var camera: Camera? = null

    var torch: Boolean = false
        set(value) {
            field = value
            camera?.cameraControl?.enableTorch(value)
        }

    private var focusPoint: PointF? = null

    var autoFocus = true
        set(value) {
            field = value
            camera?.let {
                findFocus()
            }
        }

    var focusByTouch = true
        set(value) {
            field = value
            if (!value) {
                focusPoint = null
            }
        }

    var analyzerEnable = true

    @SuppressLint("UnsafeOptInUsageError")
    private val codeAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        try {
            if (analyzerEnable) {
                scan(imageProxy)
            } else {
                // after done, release the ImageProxy object
                imageProxy.close()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private val autoFocusTask = Runnable {
        findFocus()
    }

    private val onFocusChangedListenerList = ArrayList<OnCameraFocusChangedListener>()

    private val onBarcodeScanResultListener = ArrayList<OnBarcodeScanResultListener>()

    fun bindContainer(layout: FrameLayout) {
        // 更换容器
        if (switchContainer(layout)) {
            return
        }
        // 将Camera的生命周期和Activity绑定在一起（设定生命周期所有者），这样就不用手动控制相机的启动和关闭。
        val cameraProviderFuture = ProcessCameraProvider.getInstance(layout.context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, mainExecutor)
    }

    fun addOnFocusChangedListener(listener: OnCameraFocusChangedListener) {
        this.onFocusChangedListenerList.add(listener)
    }

    fun removeOnFocusChangedListener(listener: OnCameraFocusChangedListener) {
        this.onFocusChangedListenerList.remove(listener)
    }

    fun addOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.add(listener)
    }

    fun removeOnBarcodeScanResultListener(listener: OnBarcodeScanResultListener) {
        this.onBarcodeScanResultListener.remove(listener)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val view = previewView ?: return
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(view.surfaceProvider)
        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            buildImageAnalysis(),
            preview
        )
        camera?.cameraControl?.enableTorch(torch)
        if (autoFocus) {
            findFocus()
        }
    }

    private fun findFocus() {
        mainExecutor.cache(autoFocusTask)
        mainExecutor.delay(AUTO_FOCUS_DURATION, autoFocusTask)
        val view = previewView ?: return
        val cameraControl = camera?.cameraControl ?: return
        val focusLocation = getFocusLocation(view)
        val future = cameraControl.startFocusAndMetering(
            FocusMeteringAction.Builder(
                view.meteringPointFactory.createPoint(focusLocation[0], focusLocation[1]),
                METERING_MODE
            ).setAutoCancelDuration(AUTO_FOCUS_DURATION, TimeUnit.MILLISECONDS)
                .build()
        )
        future.addListener({
            try {
                val result = future.get()
                onFocusResult(result.isFocusSuccessful, focusLocation[0], focusLocation[1])
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    private fun onFocusResult(isSuccessful: Boolean, x: Float, y: Float) {
        onFocusChangedListenerList.forEach { it.onCameraFocusChanged(isSuccessful, x, y) }
    }

    private fun getFocusLocation(view: View): FloatArray {
        val point = focusPoint
        if (point != null) {
            return floatArrayOf(point.x, point.y)
        }
        return floatArrayOf(view.width * 0.5F, view.height * 0.5F)
    }

    private fun buildImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            // 保持一定的分辨率，避免过高分辨率造成的解析压力，过低的分辨率无法分析
            .setTargetResolution(Size(1280, 720))
            // 只取最后一帧
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(getAnalyzerExecutor(), codeAnalyzer)
        return imageAnalysis
    }

    private fun getAnalyzerExecutor(): Executor {
        val executor = analyzerExecutor
        if (executor != null && !executor.isDestroy) {
            return executor
        }
        val newExecutor = SingleExecutor(lifecycleOwner, "AnalyzerExecutor")
        analyzerExecutor = newExecutor
        return newExecutor
    }

    private fun switchContainer(layout: FrameLayout): Boolean {
        container?.removeAllViews()
        val oldPreview = previewView
        if (oldPreview != null) {
            oldPreview.parent?.let {
                if (it is ViewManager) {
                    it.removeView(oldPreview)
                }
            }
        }
        val view = oldPreview ?: createPreviewView(layout.context)
        previewView = view
        container = layout
        layout.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        return view == oldPreview
    }

    private fun bindFocusTouchListener(view: View) {
        view.setOnTouchListener(PreviewTouchHelper(view.context, ::onPreviewTap))
    }

    private fun onPreviewTap(x: Float, y: Float) {
        if (focusByTouch) {
            val point = focusPoint ?: PointF()
            point.set(x, y)
            focusPoint = point
            findFocus()
        }
    }

    @ExperimentalGetImage
    private fun scan(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        // Pass image to an ML Kit Vision API
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val array = scanFormat.map { it.code }.toIntArray()
        val first = if (array.isEmpty()) {
            Barcode.FORMAT_ALL_FORMATS
        } else {
            array[0]
        }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(first, *array)
            .build()

        val scanner = BarcodeScanning.getClient(options)
        scanner.process(inputImage)
            .addOnSuccessListener { list ->
                onDecodeSuccess(list)
            }.addOnCompleteListener {
                imageProxy.close()
            }.addOnCanceledListener {
                imageProxy.close()
            }
    }

    private fun onDecodeSuccess(list: List<Barcode>) {
        val resultList = list.map { code ->
            BarcodeResultWrapper(
                parseBarcode(code),
                BarcodeResultBuilder.createCodeInfoBy(code)
            )
        }
        mainExecutor.execute {
            onBarcodeScanResultListener.forEach { it.onBarcodeScanResult(ArrayList(resultList)) }
        }
    }

    private fun parseBarcode(code: Barcode): BarcodeResult {
        return when (findBarcodeType(code.valueType)) {
            UNKNOWN -> {
                BarcodeResult.Unknown
            }
            CONTACT_INFO -> {
                BarcodeResultBuilder.createContactInfoBy(code)
            }
            EMAIL -> {
                BarcodeResultBuilder.createEmailBy(code)
            }
            ISBN -> {
                BarcodeResult.Isbn
            }
            PHONE -> {
                BarcodeResultBuilder.createPhoneBy(code)
            }
            PRODUCT -> {
                BarcodeResult.Product
            }
            SMS -> {
                BarcodeResultBuilder.createSmsBy(code)
            }
            TEXT -> {
                BarcodeResult.Text
            }
            URL -> {
                BarcodeResultBuilder.createUrlBy(code)
            }
            WIFI -> {
                BarcodeResultBuilder.createWifiBy(code)
            }
            GEO -> {
                BarcodeResultBuilder.createGeoBy(code)
            }
            CALENDAR_EVENT -> {
                BarcodeResultBuilder.createCalendarEventBy(code)
            }
            DRIVER_LICENSE -> {
                BarcodeResultBuilder.createDriverLicenseBy(code)
            }
        }
    }

    private fun findBarcodeType(code: Int): BarcodeType {
        values().forEach {
            if (it.code == code) {
                return it
            }
        }
        return UNKNOWN
    }

    private fun createPreviewView(context: Context): PreviewView {
        val view = PreviewView(context)
        view.implementationMode = previewMode.mode
        view.scaleType = scaleType.type
        bindFocusTouchListener(view)
        return view
    }

    private class PreviewTouchHelper(
        context: Context,
        private val onTap: (x: Float, y: Float) -> Unit
    ) : GestureDetector.OnGestureListener,
        View.OnTouchListener {

        private val gestureDetector = GestureDetector(context, this)

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent?) {

        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            e ?: return false
            onTap(e.x, e.y)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return false
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

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

    private class MainExecutor : Executor {

        private val handler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable?) {
            command ?: return
            handler.post(command)
        }

        fun delay(delay: Long, command: Runnable) {
            handler.postDelayed(command, delay)
        }

        fun cache(command: Runnable) {
            handler.removeCallbacks(command)
        }

    }
}