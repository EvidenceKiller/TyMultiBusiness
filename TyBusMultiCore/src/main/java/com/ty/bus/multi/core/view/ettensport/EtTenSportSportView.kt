package com.ty.bus.multi.core.view.ettensport

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ty.tybusbase.expand.Action
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.databinding.EtTenSportSportViewBinding
import com.ty.tybusbase.helper.SpeechWrapper
import java.util.concurrent.Executors

class EtTenSportSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportViewNoAnimation(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var startNowCallback: Action? = null

    private val mBinding: EtTenSportSportViewBinding

    init {
        mBinding = EtTenSportSportViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    private fun initView() {
        cardMap[0] = mBinding.sportView1
        cardMap[1] = mBinding.sportView2
        cardMap[2] = mBinding.sportView3
        cardMap[3] = mBinding.sportView4
        cardMap[4] = mBinding.sportView5
        cardMap[5] = mBinding.sportView6
        cardMap[6] = mBinding.sportView7
        cardMap[7] = mBinding.sportView8
        cardMap[8] = mBinding.sportView9
        cardMap[9] = mBinding.sportView10
        mBinding.tvStartNow.setOnClickListener {
            startNowCallback?.invoke()
        }
    }

    override fun getCountDownNoticeTv(): TextView = mBinding.tvCountDownNotice

    override fun getCountDownGoTv(): TextView = mBinding.tvCountDownGo
    override fun changeTimeTextBg(res: Int) {
        mBinding.llTime.setBackgroundResource(R.mipmap.pic_time_warm_more)
    }

    override fun updateTime(time: String) {
        mBinding.tvTime.text = time
    }

    override fun setCountDownNotice(notice: String) {
        super.setCountDownNotice(notice)
        mBinding.tvStartNow.visibility = VISIBLE
    }

    fun readyGo(block: Action, speak: Boolean) {
        getCountDownNoticeTv().text = startedText
        countDownTime = 4L
        countTimer = object : CountDownTimer(countDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTime--
                var text = if (countDownTime == 0L) {
                    context.getString(R.string.start)
                } else {
                    countDownTime.toString()
                }
                getCountDownGoTv().text = text
                if (speak) {
                    Executors.newSingleThreadExecutor().execute {
                        SpeechWrapper.speakAfterStop(text)
                    }
                }
            }

            override fun onFinish() {
                block.invoke()
                onStarted()
            }

        }.start()
        getCountDownGoTv().visibility = VISIBLE
        mBinding.tvStartNow.visibility = GONE
    }

    fun visibilityStartBtn(visibility: Int) {
        mBinding.tvStartNow.visibility = visibility
    }

    override fun onStarted() {
        super.onStarted()
        mBinding.llTime.visibility = VISIBLE
    }

    override fun reset() {
        super.reset()
        mBinding.llTime.visibility = GONE
    }
}
