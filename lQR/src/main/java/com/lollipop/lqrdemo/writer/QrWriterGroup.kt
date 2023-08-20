package com.lollipop.lqrdemo.writer

import android.graphics.Rect
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.creator.background.BackgroundGravity
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.BitmapBackgroundWriterLayer
import com.lollipop.qr.writer.LBitMatrix

class QrWriterGroup {

    private val writerArray = ArrayList<QrWriter>()

    private val bounds: Rect = Rect()

    private var bitMatrix: LBitMatrix? = null

    private var backgroundLayer: Class<out BackgroundWriterLayer>? = null

    private var contextProvider: QrWriter.ContextProvider? = null

    private var backgroundCorner: BackgroundCorner? = null

    fun setContextProvider(provider: QrWriter.ContextProvider?) {
        this.contextProvider = provider
        writerArray.forEach {
            it.setContextProvider(provider)
        }
    }

    fun setBackgroundCorner(corner: BackgroundCorner?) {
        this.backgroundCorner = corner
        writerArray.forEach {
            it.setBackgroundCorner(corner)
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

    fun setBackgroundPhoto(url: String) {
        writerArray.forEach {
            it.setBackgroundPhoto(url)
        }
    }

    fun setBackgroundGravity(gravity: BackgroundGravity) {
        writerArray.forEach {
            it.setBackgroundGravity(gravity)
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