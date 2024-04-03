package com.lollipop.punch2.data

import android.graphics.Color

/**
 * @date: 2019/01/20 13:23
 * @author: lollipop
 * 打卡标签的数据Bean
 */
data class FlagInfo(
    var id: String = "",
    var name: String = "",
    var color: Int = Color.WHITE,
    var position: Int = 0,
    var disable: Boolean = false,
    var isEmpty: Boolean = false,
)