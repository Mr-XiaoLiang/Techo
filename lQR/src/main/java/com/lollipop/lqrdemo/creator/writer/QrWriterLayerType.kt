package com.lollipop.lqrdemo.creator.writer

/**
 * 孔位
 * 可以通过孔位的方式来分发和切换不同的绘制工具
 * 同一个绘制器可以支持多个孔位，同时也会占据多个孔位
 * 一旦他的某一个孔位被新的绘制器覆盖，那么它应该释放所有它已经占用的孔位
 */
enum class QrWriterLayerType {

    BACKGROUND,
    ALIGNMENT,
    CONTENT,
    POSITION,

}