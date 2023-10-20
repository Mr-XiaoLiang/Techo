package com.lollipop.lqrdemo.creator.layer

import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

class DefaultContentWriterLayer : ContentWriterLayer() {
    override val layerType: Array<QrWriterLayerType> = arrayOf(QrWriterLayerType.CONTENT)
}