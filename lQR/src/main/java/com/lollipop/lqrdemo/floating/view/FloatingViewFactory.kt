package com.lollipop.lqrdemo.floating.view

interface FloatingViewFactory {

    fun create(callback: FloatingActionInvokeCallback): FloatingView

}