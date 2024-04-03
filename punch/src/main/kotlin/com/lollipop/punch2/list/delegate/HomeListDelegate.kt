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
        doAsync {
            val result = db.installPunch(info, year, month, day)
            invokeOnActive {
                dataList.removeAt(index)
                when (result) {
                    is DBHelper.BoolResult.Error -> {
                        setResult(OpResult.Error(ErrorType.PUNCH))
                    }

                    DBHelper.BoolResult.FALSE -> {
                        setResult(OpResult.Error(ErrorType.PUNCH))
                    }

                    DBHelper.BoolResult.TRUE -> {
                        setResult(OpResult.ItemRemoved(index, 1))
                    }
                }
            }
        }
    }

    private fun reloadAllFlags() {
        startLoading()
        doAsync {
            val result = db.selectFlag(year, month, day)
            invokeOnActive {
                when (result) {
                    is DBHelper.ListResult.Error -> {
                        // 保证UI和数据是在同步处理的
                        dataList.clear()
                        setResult(OpResult.Error(ErrorType.LIST_ALL))
                    }

                    is DBHelper.ListResult.Success -> {
                        dataList.clear()
                        dataList.addAll(result.data)
                        setResult(OpResult.FullUpdate)
                    }
                }
            }
        }
    }

    private fun startLoading() {
        setResult(OpResult.Loading)
    }

    private fun setResult(result: OpResult) {
        invokeOnActive {
            callback.onDelegateCallback(result)
        }
    }

    fun interface Callback {
        fun onDelegateCallback(result: OpResult)
    }

    sealed class OpResult {

        data object Loading : OpResult()

        data object FullUpdate : OpResult()

        class ItemRemoved(val position: Int, val count: Int) : OpResult()

        class Error(val type: ErrorType) : OpResult()

    }

    enum class ErrorType {
        LIST_ALL,
        LIST_MORE,
        PUNCH,
        REMOVE
    }

}