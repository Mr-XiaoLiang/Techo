package com.lollipop.faceicon

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

class FaceBuilder {

    private val progressFaceIcon = ProgressFaceIcon()

    fun next(faceIcon: FaceIcon, progress: Float) {
        progressFaceIcon.next(faceIcon, progress)
    }

    fun progress(progress: Float) {
        progressFaceIcon.progress(progress)
    }

    fun rebuild() {
        progressFaceIcon.build()
    }

    fun buildPath(path: Path, width: Float, height: Float, offsetX: Float, offsetY: Float) {
        progressFaceIcon.buildPath(path, width, height, offsetX, offsetY)
    }

    fun buildPath(path: Path, bounds: Rect) {
        progressFaceIcon.buildPath(path, bounds)
    }

    fun buildPath(path: Path, bounds: RectF) {
        progressFaceIcon.buildPath(path, bounds)
    }

    private class ProgressFaceIcon : FaceIcon {
        private var currentFace: FaceIcon? = null
        private var nextFace: FaceIcon? = null
        private var progress: Float = 0F

        override val left = ProgressFaceEyes()
        override val right = ProgressFaceEyes()
        override val mouth = ProgressFaceMouth()

        fun next(faceIcon: FaceIcon, progress: Float) {
            currentFace = nextFace
            nextFace = faceIcon
            progress(progress)
        }

        fun progress(progress: Float) {
            this.progress = progress
            build()
        }

        fun build() {
            left.build(progress, currentFace?.left, nextFace?.left)
            right.build(progress, currentFace?.right, nextFace?.right)
            mouth.build(progress, currentFace?.mouth, nextFace?.mouth)
        }

        fun buildPath(path: Path, width: Float, height: Float, offsetX: Float, offsetY: Float) {
            val bounds = RectF()
            bounds.set(offsetX, offsetY, offsetX + width, offsetY + height)
            buildPath(path, bounds)
        }

        fun buildPath(path: Path, bounds: Rect) {
            buildPath(path, RectF(bounds))
        }

        fun buildPath(path: Path, bounds: RectF) {
            path.reset()
            path.fillType = Path.FillType.WINDING
            left.buildPath(path, bounds)
            right.buildPath(path, bounds)
            mouth.buildPath(path, bounds)
        }

    }

    private class PathBuilder(
        val path: Path,
        var current: FaceIcon.Point,
        val bounds: RectF
    ) {

        companion object {
            fun moveTo(
                path: Path,
                current: FaceIcon.Point,
                bounds: Rect
            ): PathBuilder {
                return moveTo(path, current, RectF(bounds))
            }

            fun moveTo(
                path: Path,
                current: FaceIcon.Point,
                bounds: RectF
            ): PathBuilder {
                val builder = PathBuilder(path, current, RectF(bounds))
                builder.moveTo(current)
                return builder
            }
        }

        private val width: Float = bounds.width()
        private val height: Float = bounds.height()
        private val offsetX: Float = bounds.left
        private val offsetY: Float = bounds.top

        fun cubicTo(
            fromDirection: Direction,
            endDirection: Direction,
            end: FaceIcon.Point
        ): PathBuilder {
            val from = current
            current = end
            val fromX = getX(from)
            val fromY = getY(from)
            val endX = getX(end)
            val endY = getY(end)
            path.cubicTo(
                fromDirection.getX(fromX, endX, false),
                fromDirection.getY(fromY, endY, false),
                endDirection.getX(fromX, endX, true),
                endDirection.getY(fromY, endY, true),
                endX,
                endY
            )
            return this
        }

        fun close(): PathBuilder {
            path.close()
            return this
        }

        fun lineTo(
            end: FaceIcon.Point
        ): PathBuilder {
            current = end
            path.lineTo(getX(end), getY(end))
            return this
        }

        fun moveTo(
            end: FaceIcon.Point
        ) {
            current = end
            path.moveTo(getX(end), getY(end))
        }

        private fun getX(point: FaceIcon.Point): Float {
            return point.x * width + offsetX
        }

        private fun getY(point: FaceIcon.Point): Float {
            return point.y * height + offsetY
        }

        sealed class Direction {

            abstract fun getX(from: Float, end: Float, isEnd: Boolean): Float

            abstract fun getY(from: Float, end: Float, isEnd: Boolean): Float

            protected fun supported(
                weight: Float,
                from: Float,
                end: Float,
                isEnd: Boolean
            ): Float {
                return if (isEnd) {
                    (end - from) * (1 - weight) + from
                } else {
                    (end - from) * (weight) + from
                }
            }

            protected fun unsupported(
                from: Float,
                end: Float,
                isEnd: Boolean
            ): Float {
                return if (isEnd) {
                    end
                } else {
                    from
                }
            }

            class Horizontal(val weight: Float) : Direction() {

                override fun getX(from: Float, end: Float, isEnd: Boolean): Float {
                    return supported(weight, from, end, isEnd)
                }

                override fun getY(from: Float, end: Float, isEnd: Boolean): Float {
                    return unsupported(from, end, isEnd)
                }

            }

            class Vertical(val weight: Float) : Direction() {
                override fun getX(from: Float, end: Float, isEnd: Boolean): Float {
                    return unsupported(from, end, isEnd)
                }

                override fun getY(from: Float, end: Float, isEnd: Boolean): Float {
                    return supported(weight, from, end, isEnd)
                }

            }

            class Inclined(val weightX: Float, val weightY: Float) : Direction() {
                override fun getX(from: Float, end: Float, isEnd: Boolean): Float {
                    return supported(weightX, from, end, isEnd)
                }

                override fun getY(from: Float, end: Float, isEnd: Boolean): Float {
                    return supported(weightY, from, end, isEnd)
                }
            }

        }

    }

    private class ProgressFaceEyes : FaceIcon.Eyes {

        override val left = ProgressFacePoint()
        override val top = ProgressFacePoint()
        override val right = ProgressFacePoint()
        override val bottom = ProgressFacePoint()

        fun build(progress: Float, from: FaceIcon.Eyes?, end: FaceIcon.Eyes?) {
            left.build(progress, from?.left, end?.left)
            top.build(progress, from?.top, end?.top)
            right.build(progress, from?.right, end?.right)
            bottom.build(progress, from?.bottom, end?.bottom)
        }

        fun buildPath(path: Path, bounds: RectF) {
            PathBuilder.moveTo(path, left, bounds)
                .cubicTo(
                    PathBuilder.Direction.Vertical(0.5F),
                    PathBuilder.Direction.Horizontal(0.5F),
                    bottom
                )
                .cubicTo(
                    PathBuilder.Direction.Horizontal(0.5F),
                    PathBuilder.Direction.Vertical(0.5F),
                    right
                )
                .cubicTo(
                    PathBuilder.Direction.Vertical(0.5F),
                    PathBuilder.Direction.Horizontal(0.5F),
                    top
                )
                .cubicTo(
                    PathBuilder.Direction.Horizontal(0.5F),
                    PathBuilder.Direction.Vertical(0.5F),
                    left
                )
                .close()
        }

    }

    private class ProgressFaceMouth : FaceIcon.Mouth {

        override val leftTop = ProgressFacePoint()
        override val leftBottom = ProgressFacePoint()
        override val middleTop = ProgressFacePoint()
        override val middleBottom = ProgressFacePoint()
        override val rightTop = ProgressFacePoint()
        override val rightBottom = ProgressFacePoint()

        fun build(progress: Float, from: FaceIcon.Mouth?, end: FaceIcon.Mouth?) {
            leftTop.build(progress, from?.leftTop, end?.leftTop)
            leftBottom.build(progress, from?.leftBottom, end?.leftBottom)
            middleTop.build(progress, from?.middleTop, end?.middleTop)
            middleBottom.build(progress, from?.middleBottom, end?.middleBottom)
            rightTop.build(progress, from?.rightTop, end?.rightTop)
            rightBottom.build(progress, from?.rightBottom, end?.rightBottom)
        }

        fun buildPath(path: Path, bounds: RectF) {
            PathBuilder.moveTo(path, leftTop, bounds)
                .cubicTo(
                    PathBuilder.Direction.Vertical(0.5F),
                    PathBuilder.Direction.Horizontal(0.5F),
                    middleTop
                )
                .cubicTo(
                    PathBuilder.Direction.Horizontal(0.5F),
                    PathBuilder.Direction.Vertical(0.5F),
                    rightTop
                )
                .lineTo(rightBottom)
                .cubicTo(
                    PathBuilder.Direction.Vertical(0.5F),
                    PathBuilder.Direction.Horizontal(0.5F),
                    middleBottom
                )
                .cubicTo(
                    PathBuilder.Direction.Horizontal(0.5F),
                    PathBuilder.Direction.Vertical(0.5F),
                    leftBottom
                )
                .close()
        }

    }

    private class ProgressFacePoint : FaceIcon.Point {

        override var x: Float = 0F
            private set
        override var y: Float = 0F
            private set

        fun build(progress: Float, from: FaceIcon.Point?, end: FaceIcon.Point?) {
            if (from != null && end != null) {
                x = (end.x - from.x) * progress + from.x
                y = (end.y - from.y) * progress + from.y
            } else {
                x = (from?.x) ?: (end?.x) ?: 0F
                y = (from?.y) ?: (end?.y) ?: 0F
            }
        }

    }

}