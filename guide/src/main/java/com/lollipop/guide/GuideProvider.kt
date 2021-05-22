package com.lollipop.guide

/**
 * @author lollipop
 * @date 2021/5/22 19:10
 * 蒙层呈现器
 */
interface GuideProvider {

    fun support(step: GuideStep): Boolean

    fun onBoundsChange(left: Int, top: Int, right: Int, bottom: Int)

    fun onTargetChange(step: GuideStep, left: Int, top: Int, right: Int, bottom: Int)

    fun onAnimation(progress: Float)

}