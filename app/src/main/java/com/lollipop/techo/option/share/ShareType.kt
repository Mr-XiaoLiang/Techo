package com.lollipop.techo.option.share

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lollipop.techo.R
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
    SaveImage(R.drawable.ic_baseline_save_24, R.color.shareSaveImage, R.string.shareSaveImage),

    /**
     * 微信好友
     */
    WeChat(R.drawable.ic_wechat, R.color.shareWechat, R.string.shareWechat),

    /**
     * 微信朋友圈
     */
    Moments(R.drawable.ic_moments, R.color.shareWechat, R.string.shareMoments),

    /**
     * QQ好友
     */
    QQ(R.drawable.ic_qq, R.color.shareQQ, R.string.shareQQ),

    /**
     * QQ空间
     */
    QZone(R.drawable.ic_qzone, R.color.shareQQ, R.string.shareQzone),

    /**
     * 新浪微博
     */
    WeiBo(R.drawable.ic_weibo, R.color.shareWeibo, R.string.shareWeibo);

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