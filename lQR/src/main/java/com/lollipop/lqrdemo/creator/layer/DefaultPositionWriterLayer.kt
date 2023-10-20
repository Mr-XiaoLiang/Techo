package com.lollipop.lqrdemo.creator.layer

import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

class DefaultPositionWriterLayer : PositionWriterLayer() {
    override val layerType: Array<QrWriterLayerType> = arrayOf(QrWriterLayerType.POSITION)
}