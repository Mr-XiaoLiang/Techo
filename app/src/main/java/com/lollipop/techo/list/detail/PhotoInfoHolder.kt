package com.lollipop.techo.list.detail

import android.content.Context
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.lollipop.base.util.dp2px
import com.lollipop.gallery.Photo
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ItemPhotoBinding
import java.util.*

/**
 * @author lollipop
 * @date 2021/11/18 21:42
 */
class PhotoInfoHolder(
    view: EditItemView<ItemPhotoBinding>
) : EditHolder<ItemPhotoBinding>(view) {

    companion object {
        fun create(group: ViewGroup): PhotoInfoHolder {
            return PhotoInfoHolder(group.bindContent())
        }
    }

//    init {
//        binding.content.photoGridLayout.clipToOutline = true
//        binding.content.photoGridLayout.outlineProvider = RoundOutline(7.dp2px.toFloat())
//    }

    private val recycledViewPool = LinkedList<GridPhotoView>()

    fun bind(info: TechoItem.Photo) {
        update()
        val photoSize = info.values.size
        val childCount = binding.content.photoGridLayout.childCount
        if (photoSize > childCount) {
            for (index in 0 until (photoSize - childCount)) {
                val photoView = if (recycledViewPool.isEmpty()) {
                    GridPhotoView(binding.content.photoGridLayout.context)
                } else {
                    recycledViewPool.removeFirst()
                }
                binding.content.photoGridLayout.addView(photoView)
            }
        } else {
            for (index in 0 until (childCount - photoSize)) {
                val child = binding.content.photoGridLayout.getChildAt(0)
                if (child is GridPhotoView) {
                    recycledViewPool.addLast(child)
                }
                binding.content.photoGridLayout.removeViewAt(0)
            }
        }
        info.values.forEachIndexed { index, photo ->
            val child = binding.content.photoGridLayout.getChildAt(index)
            if (child is GridPhotoView) {
                child.update(photo)
            }
        }
        binding.content.photoGridLayout.layoutStyle = info.style
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
            // ?????????View????????????????????????
            post {
                Glide.with(this).load(photo.uri).into(this)
            }
        }

    }

}