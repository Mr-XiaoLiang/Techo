package com.lollipop.punch2.list.delegate

import android.content.Context
import androidx.activity.ComponentActivity
import com.lollipop.punch2.data.DBHelper
import com.lollipop.punch2.data.FlagInfo
import com.lollipop.punch2.utils.doAsync
import java.util.Calendar

class HomeListDelegate(
    private val context: Context,
    private val callback: Callback
) : LiveDelegate() {

    companion object {
        fun create(activity: ComponentActivity, callback: Callback): HomeListDelegate {
            val delegate = HomeListDelegate(activity, callback)
            delegate.bind(activity.lifecycle)
            return delegate
        }
    }

    private val dataList = ArrayList<FlagInfo>()

    var year: Int = 0
        private set
    var month: Int = 0
        private set
    var day: Int = 0
        private set

    val data: List<FlagInfo>
        get() {
            return dataList
        }

    private val db by lazy {
        DBHelper.read(context)
    }

    private val calendar = Calendar.getInstance()

    init {
        calendar.timeInMillis = System.currentTimeMillis()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        day = calendar.get(Calendar.DAY_OF_MONTH)
    }

    override fun onStart() {
        reloadAllFlags()
    }

    fun changeDate(y: Int, m: Int, d: Int) {
        this.year = y
        this.month = m
        this.day = d
        reloadAllFlags()
    }

    fun punchFlag(index: Int) {
        if (index < 0 || index >= data.size) {
            return
        }
        val info = data[index]
        val op = OpResult.ItemRemoved(index, 1)
        setResult(op, ModeState.LOADING)
        doAsync {
            val result = db.installPunch(info, year, month, day)
            invokeOnActive {
                dataList.removeAt(index)
                when (result) {
                    is DBHelper.BoolResult.Error -> {
                        setResult(op, ModeState.ERROR)
                    }

                    DBHelper.BoolResult.FALSE -> {
                        setResult(op, ModeState.ERROR)
                    }

                    DBHelper.BoolResult.TRUE -> {
                        setResult(op, ModeState.IDLE)
                    }
                }
            }
        }
    }

    private fun reloadAllFlags() {
        startLoading(OpResult.ContentList)
        doAsync {
            val result = db.selectFlag(year, month, day)
            invokeOnActive {
                when (result) {
                    is DBHelper.ListResult.Error -> {
                        // 保证UI和数据是在同步处理的
                        dataList.clear()
                        setResult(OpResult.ContentList, ModeState.ERROR)
                    }

                    is DBHelper.ListResult.Success -> {
                        dataList.clear()
                        dataList.addAll(result.data)
                        setResult(OpResult.ContentList, ModeState.IDLE)
                    }
                }
            }
        }
    }

    private fun startLoading(op: OpResult) {
        setResult(op, ModeState.LOADING)
    }

    private fun setResult(result: OpResult, state: ModeState) {
        putState(result, ModeState.LOADING)
        callback.onDelegateCallback(result, state)
    }

    fun interface Callback {
        fun onDelegateCallback(result: OpResult, state: ModeState)
    }

    sealed class OpResult : ModeOption {

        data object ContentList : OpResult()

        class ItemRemoved(val position: Int, val count: Int) : OpResult()

    }

    enum class ErrorType {
        LIST_ALL,
        LIST_MORE,
        PUNCH,
        REMOVE
    }

}