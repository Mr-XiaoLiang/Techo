package com.lollipop.qr

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.util.Size
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewManager
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class CameraBarcodeReader(
    lifecycleOwner: LifecycleOwner
) : BarcodeReader(lifecycleOwner) {

    companion object {
        private const val AUTO_FOCUS_DURATION = 3 * 1000L
        private const val METERING_MODE = (FocusMeteringAction.FLAG_AF
            .or(FocusMeteringAction.FLAG_AE)
            .or(FocusMeteringAction.FLAG_AWB))
    }

    private var container: FrameLayout? = null

    private var previewView: PreviewView? = null

    private val resultTag: String by lazy {
        System.identityHashCode(this@CameraBarcodeReader).toString(16)
    }

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

    fun bindContainer(layout: FrameLayout) {
        // 更换容器
        switchContainer(layout)
        // 将Camera的生命周期和Activity绑定在一起（设定生命周期所有者），这样就不用手动控制相机的启动和关闭。
        val cameraProviderFuture = ProcessCameraProvider.getInstance(layout.context)
        cameraProviderFuture.addListener({
            bindPreview(cameraProviderFuture.get())
        }, mainExecutor)
    }

    fun addOnFocusChangedListener(listener: OnCameraFocusChangedListener) {
        this.onFocusChangedListenerList.add(listener)
    }

    fun removeOnFocusChangedListener(listener: OnCameraFocusChangedListener) {
        this.onFocusChangedListenerList.remove(listener)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val view = previewView ?: return
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(view.surfaceProvider)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

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

    override fun onLifecycleStateChanged() {
        if (isResumed) {
            onResumed()
        }
    }

    private fun onResumed() {
        if (autoFocus) {
            findFocus()
        }
    }

    private fun findFocus() {
        mainExecutor.cancel(autoFocusTask)
        if (!isResumed) {
            return
        }
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
                if (future.isCancelled) {
                    return@addListener
                }
                if (future.isDone) {
                    val result = future.get()
                    onFocusResult(result.isFocusSuccessful, focusLocation[0], focusLocation[1])
                }
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
        imageAnalysis.setAnalyzer(analyzerExecutor, codeAnalyzer)
        return imageAnalysis
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
        layout.addView(
            view,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
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

        BarcodeScanning.getClient(options).process(inputImage)
            .addOnSuccessListener { list ->
                onDecodeSuccess(list, resultTag)
            }.addOnCompleteListener {
                imageProxy.close()
            }.addOnCanceledListener {
                imageProxy.close()
            }
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

}