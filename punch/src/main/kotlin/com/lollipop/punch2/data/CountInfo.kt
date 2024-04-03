package com.lollipop.punch2.data

import android.graphics.Color

/**
 * @date: 2019/02/06 15:22
 * @author: lollipop
 * 统计结果的数据
 */
data class CountInfo(
    var count: Int = 0,
    var name: String = "",
    var color: Int = Color.WHITE,
    var year: Int = 0,
    var month: Int = 0,
) {

    companion object {

        val timeComparator = Comparator<CountInfo> { o1, o2 ->
            val t1 = o1.year * 100 + o1.month
            val t2 = o2.year * 100 + o2.month
            return@Comparator t1 - t2
        }

    }

}