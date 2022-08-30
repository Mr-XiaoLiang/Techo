package liang.lollipop.ltabview.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Created by lollipop on 2018/2/2.
 * @author Lollipop
 * Tint用的工具类
 */
internal object TintUtil {

    fun tintText(view:TextView,vararg beans: TintBean){
        TintUtil.TintTextBuilder.with(view).addAll(*beans).tint()
    }

    fun tintWith(view: TextView): TintTextBuilder {
        return TintUtil.TintTextBuilder.with(view)
    }

    class TintBean(private val str:CharSequence,private val color:Int){

        fun value(): CharSequence{
            return str
        }

        fun color(): Int{
            return color
        }

        fun length():Int{
            return str.length
        }

    }

    fun tintDrawable(drawable: Drawable): TintDrawableBuilder {
        return TintDrawableBuilder(drawable)
    }

    fun tintDrawable(context: Context,resId:Int): TintDrawableBuilder {
        return TintUtil.TintDrawableBuilder.whitResId(context, resId)
    }

    fun tintDrawable(context: Context,bitmap: Bitmap): TintDrawableBuilder {
        return TintUtil.TintDrawableBuilder.whitBitmap(context, bitmap)
    }

    fun colorTransition(start: Int, end: Int, progress: Float, ignoreAlpha: Boolean = true): Int {
        val startColor = start.splitColor()
        val endColor = end.splitColor()
        val difference = IntArray(startColor.size) { i -> (endColor[i] - startColor[i]) }
        for (i in 0 until difference.size) {
            difference[i] = (difference[i] * progress + startColor[i]).toInt()
        }
        if (ignoreAlpha) {
            return Color.rgb(difference[1], difference[2], difference[3])
        }
        return Color.argb(difference[0], difference[1], difference[2], difference[3])
    }

    fun Int.splitColor(): IntArray {
        return intArrayOf(Color.alpha(this), Color.red(this),
            Color.green(this), Color.blue(this))
    }

    class TintTextBuilder private constructor(private val view: TextView){

        private val tintBranArray = ArrayList<TintBean>()

        companion object {
            fun with(view: TextView): TintTextBuilder {
                return TintTextBuilder(view)
            }
        }

        fun add(bean: TintBean): TintTextBuilder {
            tintBranArray.add(bean)
            return this
        }

        fun add(value:CharSequence,color: Int): TintTextBuilder {
            return add(TintBean(value,color))
        }

        fun add(value:String,color: Int): TintTextBuilder {
            return add(TintBean(value,color))
        }

        fun addAll(vararg beans: TintBean): TintTextBuilder {
            tintBranArray.addAll(beans)
            return this
        }

        fun tint(){
            if(tintBranArray.isEmpty()){
                view.text = ""
                return
            }
            val strBuilder = StringBuilder()
            for(str in tintBranArray){
                strBuilder.append(str.value())
            }
            val spannable = SpannableStringBuilder(strBuilder.toString())
            var index = 0
            for(color in tintBranArray){
                if(color.length() < 1){
                    continue
                }
                spannable.setSpan(ForegroundColorSpan(color.color()),index,index+color.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                index += color.length()
            }

            view.text = spannable
        }

    }

    /**
     * 对资源进行渲染的工具类
     */
    class TintDrawableBuilder(private val drawable: Drawable){

        private var colors: ColorStateList = ColorStateList.valueOf(Color.BLACK)

        companion object {

            fun whitResId(context: Context,resId:Int): TintDrawableBuilder {
                val wrappedDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    context.resources.getDrawable(resId, context.theme)
                } else {
                    context.resources.getDrawable(resId)
                }
                return TintDrawableBuilder(wrappedDrawable)
            }

            fun whitBitmap(context: Context,bitmap: Bitmap): TintDrawableBuilder {
                val wrappedDrawable = BitmapDrawable(context.resources, bitmap)
                return TintDrawableBuilder(wrappedDrawable)
            }

        }

        fun mutate(): TintDrawableBuilder {
            drawable.mutate()
            return this
        }

        fun setColor(color: Int): TintDrawableBuilder {
            colors = ColorStateList.valueOf(color)
            return this
        }

        fun setColor(color: ColorStateList): TintDrawableBuilder {
            colors = color
            return this
        }

        fun tint(): Drawable{
            DrawableCompat.setTintList(drawable, colors)
            return drawable
        }

    }

}