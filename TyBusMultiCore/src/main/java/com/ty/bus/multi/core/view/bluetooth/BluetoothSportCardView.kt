package com.ty.bus.multi.core.view.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.adl.service.entity.StudentEntity
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.core.state.BluetoothSportCardState
import com.ty.bus.multi.databinding.ViewBluetoothSportCardBinding
import com.ty.tybusbase.config.BusiBaseConfig.getIdentityType
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible
import com.ty.tybusbase.helper.SpeechWrapper
import com.ty.tybusbase.utils.LogUtil

@SuppressLint("Recycle", "CustomViewStyleable")
class BluetoothSportCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mBinding = ViewBluetoothSportCardBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var preState = BluetoothSportCardState.IDLE

    var isSupportAdd = false //是否支持手动添加

    private var clickListener: BluetoothSportCardClickListener? = null
    private var cardIndex: Int = 0

    //成绩显示View
    private var resultView: AbstractBluetoothResultView? = null

    init {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.MoreSportCardSportView)
        val index = attributes.getInt(R.styleable.MoreSportCardSportView_pk_index, 1)
        mBinding.tvIndex.text = index.toString()
        cardIndex = index
        initView()
    }

    private fun initView() {
        mBinding.llFaceCardIdentify.setOnClickListener {
            clickListener?.onIdentifyClick(cardIndex)
        }
        mBinding.llClickAdd.setOnClickListener {
            if (isSupportAdd) {
                clickListener?.onAddClick(cardIndex)
            }
        }
        mBinding.ivDelete.setOnClickListener { clickListener?.onDeleteClick(cardIndex) }
    }

    fun setSportCardClickListener(listener: BluetoothSportCardClickListener) {
        clickListener = listener
    }


    /**
     * 切换状态
     */
    fun changeState(state: BluetoothSportCardState) {
        if (preState == state) {
            LogUtil.d("BluetoothSportCardView 状态未改变")
            return
        }
        LogUtil.d("BluetoothSportCardView ${mBinding.tvIndex.text} 状态切换: $preState -> $state")
        //退出上一状态
        when (preState) {
            BluetoothSportCardState.IDLE -> {
                exitIdle()
            }

            BluetoothSportCardState.PREPARED -> {
                exitPrepared()
            }

            BluetoothSportCardState.DISCONNECTED -> {
                exitDisconnected()
            }

            BluetoothSportCardState.CONNECTED -> {
                exitConnected()
            }

            BluetoothSportCardState.SPORTING -> {
                exitSporting()
            }

        }

        //进入新状态处理
        when (state) {
            BluetoothSportCardState.IDLE -> {
                enterIdle()
            }

            BluetoothSportCardState.PREPARED -> {
                enterPrepared()
            }

            BluetoothSportCardState.DISCONNECTED -> {
                enterDisconnected()
            }

            BluetoothSportCardState.CONNECTED -> {
                enterConnected()
            }

            BluetoothSportCardState.SPORTING -> {
                enterSporting()
            }
        }

        preState = state
    }

    private fun enterDisconnected() {
        mBinding.ivBlueIcon.visible()
        mBinding.tvBlueConnectState.visible()
        mBinding.ivBlueIcon.setBackgroundResource(R.mipmap.ic_blueteeth_disconnect)
        mBinding.tvBlueConnectState.text = getString(R.string.disconnected)
        mBinding.tvBlueConnectState.setTextColor(Color.parseColor("#F62227"))
        mBinding.tvIdleState.visible()
        mBinding.tvDisconnectTip.visible()

    }

    private fun exitDisconnected() {
        mBinding.tvIdleState.gone()
        mBinding.tvDisconnectTip.gone()
        mBinding.ivBlueIcon.gone()
        mBinding.tvBlueConnectState.gone()
    }

    private fun enterSporting() {
        mBinding.groupAccountInfo.visible()
        mBinding.rlResultContent.visible()

    }

    private fun exitSporting() {
        mBinding.groupAccountInfo.gone()
        mBinding.rlResultContent.gone()
    }

    private fun enterConnected() {
        mBinding.ivBlueIcon.visible()
        mBinding.tvBlueConnectState.visible()
        mBinding.ivBlueIcon.setBackgroundResource(R.mipmap.ic_blueteeth_connect)
        mBinding.tvBlueConnectState.text = getString(R.string.connected)
        mBinding.tvBlueConnectState.setTextColor(Color.parseColor("#06B522"))
        mBinding.llFaceCardIdentify.visible()
        mBinding.tvIdleState.visible()
        //判断识别类型设置文案
        val identityType = getIdentityType()
        when (identityType) {
            BusiBaseConstantsValue.IDENTITY_TYPE_FACE -> {
                mBinding.tvIdentifyType.text = getString(R.string.identify_face)
            }

            BusiBaseConstantsValue.IDENTITY_TYPE_FACE or BusiBaseConstantsValue.IDENTITY_TYPE_CARD -> {
                mBinding.tvIdentifyType.text = getString(R.string.identify_face_nfc)
            }

            BusiBaseConstantsValue.IDENTITY_TYPE_CARD -> {
                mBinding.tvIdentifyType.text = getString(R.string.identify_nfc)
            }
        }

        //支持手动添加，改变人脸卡片Item的高度和提示文案
        if (isSupportAdd) {
            mBinding.llClickAdd.visible()
            val par = mBinding.llFaceCardIdentify.layoutParams
            par.height = context.resources.getDimension(R.dimen.dp_120).toInt()
        }
    }

    private fun exitConnected() {
        mBinding.ivBlueIcon.gone()
        mBinding.tvBlueConnectState.gone()
        mBinding.llFaceCardIdentify.gone()
        mBinding.llClickAdd.gone()
        mBinding.tvIdleState.gone()
    }

    private fun enterPrepared() {
        mBinding.groupAccountInfo.visible()
        mBinding.groupPrepared.visible()

    }

    private fun exitPrepared() {
        mBinding.groupAccountInfo.gone()
        mBinding.groupPrepared.gone()

    }

    private fun enterIdle() {
        mBinding.tvIdleState.visible()
        mBinding.groupCardIdle.visible()
    }

    private fun exitIdle() {
        mBinding.tvIdleState.gone()
        mBinding.groupCardIdle.gone()
    }

    fun setAccountInfo(studentEntity: StudentEntity) {
        Glide.with(this)
            .load(studentEntity.userFaceImgUrl)
            .placeholder(R.mipmap.ic_head_default)
            .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
        mBinding.tvName.text = studentEntity.accountName
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addResult(result: String) {
        resultView?.addResult(result)
    }

    /**
     * 添加结果View
     */
    fun addResultView(resultView: AbstractBluetoothResultView) {
        this.resultView = resultView
        mBinding.rlResultContent.removeAllViews()
        mBinding.rlResultContent.addView(resultView)
    }

    fun speechResult() {
        val resultStr = resultView?.getSpeechResult() ?: ""
        if (resultStr.isNotEmpty()) {
            val speechStr = "${cardIndex}号，${mBinding.tvName.text},${resultStr}"
            SpeechWrapper.speech(speechStr)
        }
    }

    fun getSpeechResult():String {
        var resultStr = resultView?.getSpeechResult() ?: ""
        if (resultStr.isNotEmpty()) {
            resultStr = "${cardIndex}号，${mBinding.tvName.text},${resultStr}"
        }
        return resultStr
    }

    fun reset() {
        changeState(BluetoothSportCardState.IDLE)
    }

    interface BluetoothSportCardClickListener {
        fun onIdentifyClick(index: Int)
        fun onAddClick(index: Int)
        fun onDeleteClick(index: Int)
    }
}