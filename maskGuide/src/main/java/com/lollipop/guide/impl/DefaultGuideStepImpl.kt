package com.lollipop.guide.impl

import android.view.View
import com.lollipop.guide.GuideStep

/**
 * @author lollipop
 * @date 2021/6/17 22:22
 * 椭圆形的引导步骤
 */
open class OvalGuideStep(
    target: View,
    info: String
) : GuideStep(target, info)

/**
 * @author lollipop
 * @date 2021/6/17 22:22
 * 椭圆形的引导步骤
 */
open class RoundRectGuideStep(
    target: View,
    info: String
) : GuideStep(target, info)