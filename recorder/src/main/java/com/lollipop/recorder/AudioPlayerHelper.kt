package com.lollipop.recorder

import android.media.MediaPlayer
import android.util.Log

class AudioPlayerHelper : MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {

    companion object {
        private const val TAG = "AudioPlayerHelper"
    }

    var mediaPlayer: MediaPlayer? = null
        private set

    private var state = State.END

    val lifecycle: Lifecycle
        get() {
            return when (state) {
                State.ERROR, State.END -> {
                    Lifecycle.DESTROYED
                }
                State.IDLE -> {
                    Lifecycle.CREATED
                }
                State.INITIALIZED, State.PREPARING, State.STOP -> {
                    Lifecycle.INITIALIZED
                }
                State.PREPARED, State.PAUSED, State.PLAYBACK_COMPLETED -> {
                    Lifecycle.PREPARED
                }
                State.STARTED -> {
                    Lifecycle.STARTED
                }
            }
        }

    private val listenerList = ArrayList<OnPlayerStateChangedListener>()

    private val seekCompleteListenerList = ArrayList<OnSeekCompleteListener>()

    private fun init() {
        if (mediaPlayer == null || !lifecycle.isAtLeast(Lifecycle.CREATED)) {
            try {
                mediaPlayer?.release()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            val newPlayer = MediaPlayer()
            mediaPlayer = newPlayer
            newPlayer.setOnErrorListener(this)
            newPlayer.setOnPreparedListener(this)
            newPlayer.setOnCompletionListener(this)
            newPlayer.setOnSeekCompleteListener(this)
            changeState(State.IDLE)
        }
    }

    fun setDataSource(initBuilder: (MediaPlayer) -> Unit) {
        init()
        if (lifecycle.isAtLeast(Lifecycle.INITIALIZED)) {
            Log.w(TAG, "setDataSource 需要在 Lifecycle.INITIALIZED 之前")
            return
        }
        val player = mediaPlayer ?: return
        player.let(initBuilder)
        changeState(State.PREPARING)
        tryRun {
            player.prepareAsync()
        }
    }

    fun start() {
        if (!lifecycle.isAtLeast(Lifecycle.PREPARED)) {
            Log.w(TAG, "start() 需要在 Lifecycle.PREPARED 之后")
            return
        }
        val player = mediaPlayer ?: return
        changeState(State.STARTED)
        tryRun {
            player.start()
        }
    }

    fun pause() {
        if (!lifecycle.isAtLeast(Lifecycle.STARTED)) {
            Log.w(TAG, "pause() 需要在 Lifecycle.STARTED 之后")
            return
        }
        val player = mediaPlayer ?: return
        changeState(State.PAUSED)
        tryRun {
            player.pause()
        }
    }

    fun stop() {
        if (!lifecycle.isAtLeast(Lifecycle.PREPARED)) {
            Log.w(TAG, "stop() 需要在 Lifecycle.PREPARED 之后")
            return
        }
        val player = mediaPlayer ?: return
        changeState(State.STOP)
        tryRun {
            player.stop()
        }
    }

    fun seekTo(millisecond: Long, mode: SeekMode = SeekMode.SEEK_PREVIOUS_SYNC) {
        if (!lifecycle.isAtLeast(Lifecycle.PREPARED)) {
            Log.w(TAG, "seekTo() 需要在 Lifecycle.PREPARED 之后")
            return
        }
        val player = mediaPlayer ?: return
        tryRun {
            player.seekTo(millisecond, mode.flag)
        }
    }

    var isLooping: Boolean
        get() {
            tryRun {
                if (lifecycle.isAtLeast(Lifecycle.CREATED)) {
                    return mediaPlayer?.isLooping ?: false
                }
            }
            Log.w(TAG, "isLooping 需要在 Lifecycle.CREATED 之后")
            return false
        }
        set(value) {
            if (!lifecycle.isAtLeast(Lifecycle.CREATED)) {
                Log.w(TAG, "isLooping 需要在 Lifecycle.CREATED 之后")
                return
            }
            val player = mediaPlayer ?: return
            tryRun {
                player.isLooping = value
            }
        }

    fun release() {
        val player = mediaPlayer ?: return
        changeState(State.END)
        try {
            player.release()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }

    fun reset() {
        if (!lifecycle.isAtLeast(Lifecycle.INITIALIZED)) {
            Log.w(TAG, "reset() 需要在 Lifecycle.INITIALIZED 之后")
            return
        }
        val player = mediaPlayer ?: return
        changeState(State.IDLE)
        tryRun {
            player.reset()
        }
    }

    val duration: Int
        get() {
            tryRun {
                if (lifecycle.isAtLeast(Lifecycle.INITIALIZED)) {
                    return mediaPlayer?.duration ?: 0
                }
            }
            return 0
        }

    val currentPosition: Int
        get() {
            tryRun {
                if (lifecycle.isAtLeast(Lifecycle.PREPARED)) {
                    return mediaPlayer?.currentPosition ?: 0
                }
            }
            return 0
        }

    private inline fun tryRun(run: () -> Unit) {
        try {
            run()
        } catch (e: Throwable) {
            e.printStackTrace()
            changeState(State.ERROR)
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (mp != null && mp === mediaPlayer) {
            changeState(State.ERROR)
            return true
        }
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mp != null && mp === mediaPlayer) {
            changeState(State.PLAYBACK_COMPLETED)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (mp != null && mp === mediaPlayer) {
            changeState(State.PREPARED)
        }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        if (mp != null && mp === mediaPlayer) {
            seekCompleteListenerList.forEach { it.onSeekComplete(this) }
        }
    }

    private fun changeState(newState: State) {
        this.state = newState
        listenerList.forEach { it.onAudioPlayerStateChanged(this, newState) }
    }

    fun addStateChangedListener(listener: OnPlayerStateChangedListener) {
        this.listenerList.add(listener)
    }

    fun removeStateChangedListener(listener: OnPlayerStateChangedListener) {
        this.listenerList.remove(listener)
    }

    fun addSeekCompleteListener(listener: OnSeekCompleteListener) {
        seekCompleteListenerList.add(listener)
    }

    fun removeSeekCompleteListener(listener: OnSeekCompleteListener) {
        seekCompleteListenerList.remove(listener)
    }

    fun interface OnPlayerStateChangedListener {
        fun onAudioPlayerStateChanged(helper: AudioPlayerHelper, state: State)
    }

    fun interface OnSeekCompleteListener {
        fun onSeekComplete(helper: AudioPlayerHelper)
    }

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

    enum class SeekMode(val flag: Int) {
        /**
         * This mode is used with [.seekTo] to move media position to
         * a sync (or key) frame associated with a data source that is located
         * right before or at the given time.
         *
         * @see .seekTo
         */
        SEEK_PREVIOUS_SYNC(MediaPlayer.SEEK_PREVIOUS_SYNC),

        /**
         * This mode is used with [.seekTo] to move media position to
         * a sync (or key) frame associated with a data source that is located
         * right after or at the given time.
         *
         * @see .seekTo
         */
        SEEK_NEXT_SYNC(MediaPlayer.SEEK_NEXT_SYNC),

        /**
         * This mode is used with [.seekTo] to move media position to
         * a sync (or key) frame associated with a data source that is located
         * closest to (in time) or at the given time.
         *
         * @see .seekTo
         */
        SEEK_CLOSEST_SYNC(MediaPlayer.SEEK_CLOSEST_SYNC),

        /**
         * This mode is used with [.seekTo] to move media position to
         * a frame (not necessarily a key frame) associated with a data source that
         * is located closest to or at the given time.
         *
         * @see .seekTo
         */
        SEEK_CLOSEST(MediaPlayer.SEEK_CLOSEST)
    }

    private inline fun <reified T : Enum<*>> T.isAtLeast(o: T): Boolean {
        val t = this
        return t.ordinal >= o.ordinal
    }

}