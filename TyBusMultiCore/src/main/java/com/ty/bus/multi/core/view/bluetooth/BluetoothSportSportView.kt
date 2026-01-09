package com.ty.bus.multi.core.view.bluetooth

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.bus.multi.R
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.bus.multi.core.view.sixforwardflexion.ForwardFlexionSportCardView
import com.ty.bus.multi.databinding.ViewBluetoothSportSportBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.getString

class BluetoothSportSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewBluetoothSportSportBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var cardMap = HashMap<Int, BluetoothSportCardView>()

    var listener: ResultViewBtnClickListener? = null

    private val NEXT_COUNT_DOWN_TIME = 5 //倒计时时间

    private var nextCountDownTime = NEXT_COUNT_DOWN_TIME //倒计时时间

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private val runnable by lazy {
        object : Runnable {
            override fun run() {
                nextCountDownTime--
                if (nextCountDownTime > 0) {
                    mHandler.postDelayed(this, 1000)
                } else {
                    next()
                }
                updateNextText(nextCountDownTime)
            }
        }
    }

    init {
        cardMap.put(1, mBinding.blueSportCard1)
        cardMap.put(2, mBinding.blueSportCard2)
        cardMap.put(3, mBinding.blueSportCard3)
    }

    fun changeState(index: Int, state: BluetoothSportCardState) {
        cardMap[index]?.changeState(state)
    }

    fun addResult(index: Int, result: String) {
        cardMap[index]?.addResult(result)
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    fun sportFinish() {
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        mBinding.tvRetest.visibility = VISIBLE
        mBinding.tvNext.visibility = VISIBLE
        updateNextText(NEXT_COUNT_DOWN_TIME)
        mHandler.postDelayed(runnable, 1000)
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
    }

    /**
     * 重置，恢复初始状态（下一组）
     */
    fun next() {
        mHandler.removeCallbacks(runnable)
        listener?.onNextClick()
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacks(runnable)
    }
}