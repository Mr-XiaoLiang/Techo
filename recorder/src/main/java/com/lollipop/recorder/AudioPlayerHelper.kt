package com.lollipop.recorder

class AudioPlayerHelper {

    enum class State {
        /**
         * 异常了
         * 如果MediaPlayer进入了Error状态，可以通过调用reset()来恢复，使得MediaPlayer重新返回到Idle状态。
         */
        ERROR,

        /**
         * 结束了，不可使用了
         * 通过release()方法可以进入End状态，
         * 如果MediaPlayer对象进入了End状态，则不会在进入任何其他状态了。
         */
        END,

        /**
         * 空闲
         * 当创建一个新的MediaPlayer对象或者调用了其reset()方法时，该MediaPlayer对象处于idle状态
         */
        IDLE,

        /**
         * 已初始化
         * 使用setDataSource()后进入
         */
        INITIALIZED,

        /**
         * 准备中
         * 使用prepareAsync()进行异步初始化时进入
         */
        PREPARING,

        /**
         * 准备就绪
         * 使用prepare()结束后进入
         * 使用prepareAsync()并OnPreparedListener.onPrepared()被触发后进入
         */
        PREPARED,

        /**
         * 播放中
         * 使用了start()进入
         */
        STARTED,

        /**
         * 暂停中
         * 播放时使用pause()进入
         */
        PAUSED,

        /**
         * 停止
         * 就绪后使用stop()进入
         * 停止后需要重新准备
         */
        STOP,

        /**
         * 播放完毕
         * 如果没有设置循环播放，那么文件播放完成后进入
         * 可以使用start()再次播放
         */
        PLAYBACK_COMPLETED,
    }

    enum class Lifecycle {
        /**
         * 已销毁
         */
        DESTROYED,

        /**
         * 已创建
         */
        CREATED,

        /**
         * 已初始化
         */
        INITIALIZED,

        /**
         * 已就绪
         */
        PREPARED,

        /**
         * 已开始
         */
        STARTED
    }

}