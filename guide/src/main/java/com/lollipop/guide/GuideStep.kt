package com.lollipop.guide

import android.view.View

/**
 * @author lollipop
 * @date 2021/5/19 22:06
 * 引导步骤的基础类
 */
open class GuideStep(
    val target: View,
    val info: String
)