package com.lollipop.lqrdemo.writer.background

import com.lollipop.lqrdemo.creator.background.BackgroundInfo
import com.lollipop.lqrdemo.creator.background.BackgroundStore


class LocalBitmapBackgroundWriterLayer : BaseBitmapBackgroundWriterLayer() {

    override fun getPhotoPathFromStore(): String {
        return BackgroundStore.getByType<BackgroundInfo.Local>()?.file?.path ?: ""
    }


}