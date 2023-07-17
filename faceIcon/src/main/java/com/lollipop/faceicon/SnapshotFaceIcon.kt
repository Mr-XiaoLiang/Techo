package com.lollipop.faceicon

class SnapshotFaceIcon(
    override val left: FaceIcon.Eyes,
    override val right: FaceIcon.Eyes,
    override val mouth: FaceIcon.Mouth
) : FaceIcon {

    companion object {

        fun valueOf(faceIcon: FaceIcon): SnapshotFaceIcon {
            return SnapshotFaceIcon(
                left = faceIcon.left.snapshot(),
                right = faceIcon.right.snapshot(),
                mouth = faceIcon.mouth.snapshot()
            )
        }

        private fun FaceIcon.Point.snapshot(): SnapshotFacePoint {
            return SnapshotFacePoint.valueOf(this)
        }

        private fun FaceIcon.Eyes.snapshot(): SnapshotFaceEyes {
            return SnapshotFaceEyes.valueOf(this)
        }

        private fun FaceIcon.Mouth.snapshot(): SnapshotFaceMouth {
            return SnapshotFaceMouth.valueOf(this)
        }
    }

    class SnapshotFaceEyes(
        override val left: FaceIcon.Point,
        override val top: FaceIcon.Point,
        override val right: FaceIcon.Point,
        override val bottom: FaceIcon.Point
    ) : FaceIcon.Eyes {

        companion object {
            fun valueOf(eyes: FaceIcon.Eyes): SnapshotFaceEyes {
                return SnapshotFaceEyes(
                    left = eyes.left.snapshot(),
                    top = eyes.top.snapshot(),
                    right = eyes.right.snapshot(),
                    bottom = eyes.bottom.snapshot(),
                )
            }
        }

    }

    class SnapshotFaceMouth(
        override val leftTop: FaceIcon.Point,
        override val leftBottom: FaceIcon.Point,
        override val middleTop: FaceIcon.Point,
        override val middleBottom: FaceIcon.Point,
        override val rightTop: FaceIcon.Point,
        override val rightBottom: FaceIcon.Point
    ) : FaceIcon.Mouth {
        companion object {
            fun valueOf(mouth: FaceIcon.Mouth) : SnapshotFaceMouth {
                return SnapshotFaceMouth(
                    leftTop = mouth.leftTop.snapshot(),
                    leftBottom = mouth.leftBottom.snapshot(),
                    middleTop = mouth.middleTop.snapshot(),
                    middleBottom = mouth.middleBottom.snapshot(),
                    rightTop = mouth.rightTop.snapshot(),
                    rightBottom = mouth.rightBottom.snapshot(),
                )
            }
        }
    }

    class SnapshotFacePoint(override val x: Float, override val y: Float) : FaceIcon.Point {

        companion object {
            fun valueOf(point: FaceIcon.Point): SnapshotFacePoint {
                return SnapshotFacePoint(point.x, point.y)
            }
        }

    }

}