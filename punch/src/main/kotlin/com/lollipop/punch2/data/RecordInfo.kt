package com.lollipop.punch2.data

import android.graphics.Color

/**
 * @date: 2019/01/20 13:21
 * @author: lollipop
 * 打卡记录的数据
 */
class RecordInfo {

    /** 记录的id */
    var id = ""
    /** 类型的id */
    var flagId = ""
    /** 打卡名称 */
    var name = ""
    /** 颜色 */
    var color = Color.WHITE
    /** 打卡的时间戳 */
    var timestamp = System.currentTimeMillis()
    /** 打卡日期 YYYYmmdd */
    var days = 0
}