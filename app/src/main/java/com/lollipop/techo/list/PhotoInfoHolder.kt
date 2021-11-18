package com.lollipop.techo.list

import android.content.Context
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.lollipop.base.util.bind
import com.lollipop.base.util.dp2px
import com.lollipop.gallery.Photo
import com.lollipop.techo.data.PhotoItem
import com.lollipop.techo.databinding.ItemEditGroupBinding
import com.lollipop.techo.databinding.ItemPhotoBinding
import java.util.*

/**
 * @author lollipop
 * @date 2021/11/18 21:42
 */
class PhotoInfoHolder(
    private val binding: ItemPhotoBinding,
    optionBinding: ItemEditGroupBinding
) : EditHolder(optionBinding) {

    companion object {
        fun create(group: ViewGroup): PhotoInfoHolder {
            val optionBinding = createItemView(group)
            return PhotoInfoHolder(getContentGroup(optionBinding).bind(true), optionBinding)
        }
    }

    init {
        binding.photoGridLayout.clipToOutline = true
        binding.photoGridLayout.outlineProvider = RoundOutline(7.dp2px.toFloat())
    }

    private val recycledViewPool = LinkedList<GridPhotoView>()

    fun bind(info: PhotoItem) {
        val photoSize = info.values.size
        val childCount = binding.photoGridLayout.childCount
        if (photoSize > childCount) {
            for (index in 0 until (photoSize - childCount)) {
                val photoView = if (recycledViewPool.isEmpty()) {
                    GridPhotoView(binding.photoGridLayout.context)
                } else {
                    recycledViewPool.removeFirst()
                }
                binding.photoGridLayout.addView(photoView)
            }
        } else {
            for (index in 0 until (childCount - photoSize)) {
                val child = binding.photoGridLayout.getChildAt(0)
                if (child is GridPhotoView) {
                    recycledViewPool.addLast(child)
                }
                binding.photoGridLayout.removeViewAt(0)
            }
        }
        info.values.forEachIndexed { index, photo ->
            val child = binding.photoGridLayout.getChildAt(index)
            if (child is GridPhotoView) {
                child.update(photo)
            }
        }
        binding.photoGridLayout.layoutStyle = info.style
    }

    private class RoundOutline(private val round: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline ?: return
            outline.setRoundRect(
                view.paddingLeft,
                view.paddingTop,
                view.width - view.paddingRight,
                view.height - view.paddingBottom,
                round
            )
        }
    }

    private class GridPhotoView(context: Context) : AppCompatImageView(context) {

        fun update(photo: Photo) {
            setImageDrawable(null)
            // 延迟到View展示出来再去加载
            post {
                Glide.with(this).load(photo.uri).into(this)
            }
        }

    }

}