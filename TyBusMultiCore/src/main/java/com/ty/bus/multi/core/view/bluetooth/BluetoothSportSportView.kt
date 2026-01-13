package com.ty.bus.multi.core.view.bluetooth

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.adl.service.entity.StudentEntity
import com.ty.bus.multi.R
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.dialog.identify.IdentifyDialog
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.bus.multi.core.view.bluetooth.BluetoothSportCardView.BluetoothSportCardClickListener
import com.ty.bus.multi.core.view.sixforwardflexion.ForwardFlexionSportCardView
import com.ty.bus.multi.databinding.ViewBluetoothSportSportBinding
import com.ty.tybusbase.dialog.CommonTipDialog
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

    private val identifiedMap by lazy { HashMap<Int, StudentEntity>() }

    var listener: ResultViewBtnClickListener? = null

    private val NEXT_COUNT_DOWN_TIME = 5 //倒计时时间

    private var nextCountDownTime = NEXT_COUNT_DOWN_TIME //倒计时时间

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    //保存蓝牙连接状态
    private var blueConnectList = HashMap<Int, Boolean>()

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

    private val cardClickListener = object : BluetoothSportCardClickListener {
        override fun onIdentifyClick(index: Int) {
            IdentifyDialog().setIdentifiedMap(identifiedMap)
                .setBlueConnectList(blueConnectList)
                .setIdentifyResultListener {
                    handleIdentifyResult(it)
                }
                .show((context as FragmentActivity).supportFragmentManager, "identify")
        }

        override fun onAddClick(index: Int) {
        }

        override fun onDeleteClick(index: Int) {
            CommonTipDialog()
                .setCallback {
                    identifiedMap.remove(index)
                    updateStartBtn()
                    changeState(index, BluetoothSportCardState.CONNECTED)
                }.setContent(getString(R.string.busi_multi_delete_user))
                .show((context as FragmentActivity).supportFragmentManager, "tip")

        }
    }

    init {
        cardMap.put(1, mBinding.blueSportCard1)
        cardMap.put(2, mBinding.blueSportCard2)
        cardMap.put(3, mBinding.blueSportCard3)
        blueConnectList.put(1, false)
        blueConnectList.put(2, false)
        blueConnectList.put(3, false)
        initView()
    }

    private fun initView() {
        mBinding.blueSportCard1.setSportCardClickListener(cardClickListener)
        mBinding.blueSportCard2.setSportCardClickListener(cardClickListener)
        mBinding.blueSportCard3.setSportCardClickListener(cardClickListener)
        mBinding.tvStartTest.setOnClickListener {
            listener?.onStartClick()
        }
        mBinding.tvRetest.setOnClickListener {
            listener?.onRetestClick()
        }
        mBinding.tvNext.setOnClickListener {
            listener?.onNextClick()
        }
        mBinding.tvEndTest.setOnClickListener {
            listener?.onEndTestClick()
        }
        mBinding.tvFinish.setOnClickListener {
            (context as Activity).finish()
        }

    }

    fun changeState(index: Int, state: BluetoothSportCardState) {
        if (state == BluetoothSportCardState.CONNECTED) {
            blueConnectList.put(index, true)
        }
        if (state == BluetoothSportCardState.DISCONNECTED) {
            blueConnectList.put(index, false)
        }
        cardMap[index]?.changeState(state)
    }

    private fun handleIdentifyResult(stuMap: HashMap<Int, StudentEntity>) {
        stuMap.forEach { (index, value) ->
            setAccountInfo(index, value)
            changeState(index, BluetoothSportCardState.PREPARED)
        }
        updateStartBtn()
    }

    private fun updateStartBtn() {
        if (identifiedMap.isNotEmpty()) {
            mBinding.tvStartTest.isEnabled = true
            mBinding.tvStartTest.setBackgroundResource(R.drawable.bg_round_r50_gradient_red)
        } else {
            mBinding.tvStartTest.isEnabled = false
            mBinding.tvStartTest.setBackgroundResource(R.drawable.bg_c4_r73_shape)
        }
    }

    fun addResult(index: Int, result: String) {
        cardMap[index]?.addResult(result)
    }

    fun setAccountInfo(index: Int, studentEntity: StudentEntity) {
        cardMap[index]?.setAccountInfo(studentEntity)
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