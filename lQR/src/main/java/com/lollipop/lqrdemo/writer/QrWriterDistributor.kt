package com.lollipop.lqrdemo.writer

import android.graphics.Canvas

class QrWriterDistributor : QrWriter() {

    companion object {
        var DEFAULT_ALIGNMENT_WRITER: Class<QrAlignmentWriter>? = null
        var DEFAULT_DATA_POINT_WRITER: Class<QrDataPointWriter>? = null
        var DEFAULT_POSITION_DETECTION_WRITER: Class<QrPositionDetectionWriter>? = null
    }

    private val defaultWriterWrapper = WriterWrapper(null)
    private val writerWrapper = WriterWrapper(defaultWriterWrapper)

    init {
        onDefaultWriterChanged()
    }

    override fun onDraw(canvas: Canvas) {
        writerWrapper.alignment?.onDraw(canvas)
        writerWrapper.positionDetection?.onDraw(canvas)
        writerWrapper.dataPoint?.onDraw(canvas)
    }

    fun onDefaultWriterChanged() {
        defaultWriterWrapper.alignment = DEFAULT_ALIGNMENT_WRITER?.newInstance()
        defaultWriterWrapper.dataPoint = DEFAULT_DATA_POINT_WRITER?.newInstance()
        defaultWriterWrapper.positionDetection = DEFAULT_POSITION_DETECTION_WRITER?.newInstance()
    }

    fun setAlignmentWriter(writer: QrAlignmentWriter) {
        writerWrapper.alignment = writer
    }
    fun setDataPointWriter(writer: QrDataPointWriter) {
        writerWrapper.dataPoint = writer
    }
    fun setPositionDetectionWriter(writer: QrPositionDetectionWriter) {
        writerWrapper.positionDetection = writer
    }

    override fun onBoundsChanged() {}

    private class WriterWrapper(val base: WriterWrapper?) {
        private var alignmentWriter: QrAlignmentWriter? = null
        private var dataPointWriter: QrDataPointWriter? = null
        private var positionDetectionWriter: QrPositionDetectionWriter? = null

        var alignment: QrAlignmentWriter?
            get() {
                return alignmentWriter ?: base?.alignment
            }
            set(value) {
                alignmentWriter = value
            }

        var dataPoint: QrDataPointWriter?
            get() {
                return dataPointWriter ?: base?.dataPoint
            }
            set(value) {
                dataPointWriter = value
            }

        var positionDetection: QrPositionDetectionWriter?
            get() {
                return positionDetectionWriter ?: base?.positionDetection
            }
            set(value) {
                positionDetectionWriter = value
            }

    }

}