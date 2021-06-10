package com.lollipop.guide.impl

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import com.lollipop.guide.GuideProvider
import com.lollipop.guide.GuideStep

/**
 * @author lollipop
 * @date 2021/6/1 23:10
 * 默认的引导提供者
 */
class DefaultGuideProvider: GuideProvider() {

    companion object {
        val DEFAULT_GUIDE_STYLE = GuideStyle(
            backgroundColor = Color.BLACK and 0xFFFFFF or 0xAA000000.toInt(),
            cornerDp = 10,
            marginVerticalDp = 10,
            marginHorizontalDp = 10,
            paddingVerticalDp = 10,
            paddingHorizontalDp = 10,
            spaceToTargetDp = 10
        )

        var guideStyle = DEFAULT_GUIDE_STYLE
    }

    override fun support(step: GuideStep): Boolean {
        return true
    }

    override fun onGuideBoundsChanged(left: Int, top: Int, right: Int, bottom: Int) {
        TODO("Not yet implemented")
    }

    override fun onTargetChange(step: GuideStep) {
        TODO("Not yet implemented")
    }

    private class GuideView(context: Context): FrameLayout(context) {
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            TODO("Not yet implemented")
        }
    }

    data class GuideStyle(
        val backgroundColor: Int,
        val cornerDp: Int,
        val marginVerticalDp: Int,
        val marginHorizontalDp: Int,
        val paddingVerticalDp: Int,
        val paddingHorizontalDp: Int,
        val spaceToTargetDp: Int
    )

}