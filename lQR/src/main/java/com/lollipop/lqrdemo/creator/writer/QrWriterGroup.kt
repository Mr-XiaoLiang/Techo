package com.lollipop.lqrdemo.creator.writer

import android.graphics.Rect
import com.lollipop.lqrdemo.creator.writer.background.BackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix

class QrWriterGroup {

    private val writerArray = ArrayList<QrWriter>()

    private val bounds: Rect = Rect()

    private var bitMatrix: LBitMatrix? = null

    private var backgroundLayer: Class<out BackgroundWriterLayer>? = null

    private var contextProvider: QrWriter.ContextProvider? = null

    fun setContextProvider(provider: QrWriter.ContextProvider?) {
        this.contextProvider = provider
        writerArray.forEach {
            it.setContextProvider(provider)
        }
    }

    fun addWriter(writer: QrWriter) {
        val b = bounds
        writer.setBounds(b.left, b.top, b.right, b.bottom)
        writer.setBitMatrix(bitMatrix)
        writer.setBackground(backgroundLayer)
        writer.setContextProvider(contextProvider)
        writerArray.add(writer)
    }

    fun setBitMatrix(matrix: LBitMatrix?) {
        this.bitMatrix = matrix
        writerArray.forEach {
            it.setBitMatrix(matrix)
        }
    }

    fun setQrPointColor(dark: Int, light: Int) {
        writerArray.forEach {
            it.setQrPointColor(dark, light)
        }
    }

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        bounds.set(left, top, right, bottom)
        writerArray.forEach {
            it.setBounds(left, top, right, bottom)
        }
    }

    fun setBackground(layer: Class<out BackgroundWriterLayer>?) {
        backgroundLayer = layer
        writerArray.forEach {
            it.setBackground(layer)
        }
    }

    fun notifyBackgroundChanged() {
        writerArray.forEach {
            it.notifyBackgroundChanged()
        }
    }

    fun removeWriter(writer: QrWriter) {
        writerArray.remove(writer)
    }

    fun removeAt(index: Int) {
        if (index >= 0 && index < writerArray.size) {
            writerArray.removeAt(index)
        }
    }

}