package com.lollipop.techo.option.share

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lollipop.techo.option.item.Option

/**
 * @author lollipop
 * @date 2021/12/30 20:57
 * 分享类型
 */
enum class ShareType(
    /**
     * 操作图标
     */
    @DrawableRes
    val icon: Int,

    /**
     * 背景色
     */
    @ColorRes
    val theme: Int,

    /**
     * 操作名称
     */
    @StringRes
    val shareName: Int,
) {
    /**
     * 保存图片
     */
    SaveImage(0, 0, 0),

    /**
     * 微信好友
     */
    WeChat(0, 0, 0),

    /**
     * 微信朋友圈
     */
    Moments(0, 0, 0),

    /**
     * QQ好友
     */
    QQ(0, 0, 0),

    /**
     * QQ空间
     */
    QZone(0, 0, 0),

    /**
     * 新浪微博
     */
    WeiBo(0, 0, 0);

    companion object {
        private const val BASE_ID = -10000

        fun findByOptionId(id: Int): ShareType? {
            val index = id - BASE_ID
            val values = values()
            if (index in values.indices) {
                return values[index]
            }
            return null
        }
    }

    fun option(): Option {
        return Option(icon, theme, shareName, BASE_ID + ordinal)
    }

}