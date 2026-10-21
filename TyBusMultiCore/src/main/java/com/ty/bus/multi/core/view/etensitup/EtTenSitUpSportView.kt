package com.ty.bus.multi.core.view.etensitup

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.BaseMultiSportViewNoAnimation
import com.ty.bus.multi.databinding.EtTenSitUpSportViewBinding
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.helper.SpeechWrapper
import java.util.concurrent.Executors

/**
 * 副屏10人仰卧起坐运动面板（与副屏10人跳绳 EtTenSportSportView 对应）。
 *
 * 与跳绳面板的区别：
 * - 道次卡片使用横版卡片 MoreLandscapeSportCardSportView（仰卧起坐专用），2/4/4 三排布局且整体垂直居中
 * - 其余行为（倒计时提示、开始按钮、时间栏、3-2-1大数字倒计时）与跳绳面板保持一致
 */
class EtTenSitUpSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    BaseMultiSportViewNoAnimation(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var startNowCallback: Action? = null

    private val mBinding: EtTenSitUpSportViewBinding

    init {
        mBinding = EtTenSitUpSportViewBinding.inflate(LayoutInflater.from(context), this, true)
        initView()
    }

    private fun initView() {
        cardMap[0] = mBinding.mlSportView1
        cardMap[1] = mBinding.mlSportView2
        cardMap[2] = mBinding.mlSportView3
        cardMap[3] = mBinding.mlSportView4
        cardMap[4] = mBinding.mlSportView5
        cardMap[5] = mBinding.mlSportView6
        cardMap[6] = mBinding.mlSportView7
        cardMap[7] = mBinding.mlSportView8
        cardMap[8] = mBinding.mlSportView9
        cardMap[9] = mBinding.mlSportView10
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
        mBinding.tvStartNow.visibility = View.VISIBLE
    }

    fun readyGo(block: Action, speak: Boolean) {
        getCountDownNoticeTv().text = startedText
        countDownTime = 4L
        countTimer = object : CountDownTimer(countDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTime--
                val text = if (countDownTime == 0L) {
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
        getCountDownGoTv().visibility = View.VISIBLE
        mBinding.tvStartNow.visibility = View.GONE
    }

    fun visibilityStartBtn(visibility: Int) {
        mBinding.tvStartNow.visibility = visibility
    }

    override fun onStarted() {
        super.onStarted()
        mBinding.llTime.visibility = View.VISIBLE
    }

    override fun reset() {
        super.reset()
        mBinding.llTime.visibility = View.GONE
    }
}
