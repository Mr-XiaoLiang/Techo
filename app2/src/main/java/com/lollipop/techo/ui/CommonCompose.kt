package com.lollipop.techo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier

/**
 * @author lollipop
 * @date 2021/9/29 21:52
 * 通用的Compose
 */

@Composable
fun NetworkImage(url: String, modifier: Modifier) {
    SideEffect {
//        url
    }
}

//@Composable
//fun NetworkImage(url: String?, modifier: Modifier) {
//
//    var image by remember { mutableStateOf<ImageAsset?>(null) }
//    var drawable by remember { mutableStateOf<Drawable?>(null) }
//
//    onCommit(url) {
//        val picasso = Picasso.get()
//
//        val target = object : Target {
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//                drawable = placeHolderDrawable
//            }
//
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
//                drawable = errorDrawable
//            }
//
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                image = bitmap?.asImageAsset()
//            }
//        }
//
//        picasso
//            .load(imagePath)
//            .placeHolder(R.drawable.placeholder).error(R.drawable.error)
//            .into(target)
//        onDispose {
//            image = null
//            drawable = null
//            picasso.cancelRequest(target)
//        }
//    }
//
//    if (image != null) {
//        Image(asset = image, modifier = modifier)
//    } else if (theDrawable != null) {
//        Canvas(modifier = modifier) {
//            drawIntoCanvas { canvas ->
//                drawable.draw(canvas.nativeCanvas)
//            }
//        }
//    }
//}