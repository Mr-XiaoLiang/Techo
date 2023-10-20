package com.lollipop.lqrdemo.creator.layer

import com.lollipop.lqrdemo.creator.writer.QrWriterLayerType

class DefaultAlignmentWriterLayer : AlignmentWriterLayer() {
    override val layerType: Array<QrWriterLayerType> = arrayOf(QrWriterLayerType.ALIGNMENT)
}