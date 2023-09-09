package com.lollipop.lqrdemo.creator.background

sealed class BackgroundCorner(
    val leftTop: Radius,
    val rightTop: Radius,
    val rightBottom: Radius,
    val leftBottom: Radius
) {

    class Cut(
        leftTop: Radius,
        rightTop: Radius,
        rightBottom: Radius,
        leftBottom: Radius
    ) : BackgroundCorner(leftTop, rightTop, rightBottom, leftBottom)

    class Round(
        leftTop: Radius,
        rightTop: Radius,
        rightBottom: Radius,
        leftBottom: Radius
    ) : BackgroundCorner(leftTop, rightTop, rightBottom, leftBottom)

    class Squircle(
        leftTop: Radius,
        rightTop: Radius,
        rightBottom: Radius,
        leftBottom: Radius
    ) : BackgroundCorner(leftTop, rightTop, rightBottom, leftBottom)

    object None : BackgroundCorner(
        Radius.None,
        Radius.None,
        Radius.None,
        Radius.None
    )

    sealed class Radius {

        class Absolute(val value: Float) : Radius()
        class Weight(val value: Float) : Radius()

        object None : Radius()

    }

}