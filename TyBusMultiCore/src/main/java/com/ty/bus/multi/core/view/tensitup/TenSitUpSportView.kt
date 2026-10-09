package com.ty.bus.multi.core.view.tensitup

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.ty.bus.multi.R
import com.ty.bus.multi.core.base.MultiSportCardViewState
import com.ty.bus.multi.core.callback.ResultViewBtnClickListener
import com.ty.bus.multi.core.view.common.MoreLandscapeSportCardSportView
import com.ty.bus.multi.databinding.TenSitUpSportViewBinding
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.getString
import com.ty.tybusbase.expand.gone
import com.ty.tybusbase.expand.visible

class TenSitUpSportView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var currentSportTimes = 0
    private var cardMap = HashMap<Int, MoreLandscapeSportCardSportView>()
    private var mBinding = TenSitUpSportViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private var isStarted = false
    private var startListener: Action? = null

    var listener: ResultViewBtnClickListener? = null

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    private var isShowNext = true

    private val NEXT_COUNT_DOWN_TIME = 10 //倒计时时间

    private var nextCountDownTime = NEXT_COUNT_DOWN_TIME //倒计时时间

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
        //顶部居中提示：背景为左右渐隐的蓝色渐变
        mBinding.tvTopHint.background = createTopHintGradient()
    }

    init {
        initView()
    }

    private fun initView() {
        cardMap.put(0, mBinding.mlSportView1)
        cardMap.put(1, mBinding.mlSportView2)
        cardMap.put(2, mBinding.mlSportView3)
        cardMap.put(3, mBinding.mlSportView4)
        cardMap.put(4, mBinding.mlSportView5)
        cardMap.put(5, mBinding.mlSportView6)
        cardMap.put(6, mBinding.mlSportView7)
        cardMap.put(7, mBinding.mlSportView8)
        cardMap.put(8, mBinding.mlSportView9)
        cardMap.put(9, mBinding.mlSportView10)
        mBinding.tvNext.setOnClickListener {
            next()
        }
        mBinding.tvFinish.setOnClickListener {
            listener?.onEndTestClick()
        }

        mBinding.tvRetest.setOnClickListener {
            retest()
        }

    }

    fun setStartListener(listener: Action) {
        startListener = listener
    }


    /**
     * 显示下一步按钮
     */
    fun showNextBtn() {
        isShowNext = true
    }

    private fun updateNextText(count: Int) {
        mBinding.tvNext.text = getString(R.string.next_count_down, count)
    }

    fun changeState(index: Int, state: MultiSportCardViewState) {
        if (isStarted && state == MultiSportCardViewState.PREPARED) {
            return
        }
        cardMap[index]?.changeState(state)
    }

    //运动已开始
    fun sportStarted() {
        isStarted = true
    }

    fun setSportingTips(text: String) {
        mBinding.tvTopHint.visible()
        mBinding.tvTopHint.text = text
    }
    fun sportFinish() {
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        mBinding.tvRetest.visibility = VISIBLE
        setSportingTips(getString(R.string.sport_finish))
        if (isShowNext) {
            mBinding.tvNext.visibility = VISIBLE
            updateNextText(NEXT_COUNT_DOWN_TIME)
            mHandler.postDelayed(runnable, 1000)
        }
        isStarted = false
    }

    fun updateResult(result: MutableList<GroupSportBaseMan>) {
        for (index in result.indices) {
            val skipMan = result[index]
            if (skipMan.score != 0) {
                cardMap[skipMan.positionNum]?.updateScore(skipMan.score)
            }
        }
    }

    fun addErrorMsg(index: Int, code: Int, msg: String?) {
//        cardMap[index]?.addErrorMsg(code, msg)
    }

    fun updateTime(time: String) {
        setSportingTips(getString(R.string.sport_countdown_time, time))
    }

    fun setAccountInfo(index: Int, accountInfo: AccountInfo) {
        cardMap[index]?.setAccountInfo(accountInfo)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacks(runnable)
    }

    /**
     * 重置，恢复初始状态（下一组）
     */
    fun next() {
        mHandler.removeCallbacks(runnable)
        listener?.onNextClick()
        isStarted = false
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        cardMap.forEach { (_, value) ->
            value.reset()
        }
        mBinding.tvTopHint.gone()
    }

    /**
     * 重测，当前人员继续测试
     */
    fun retest() {
        mHandler.removeCallbacks(runnable)
        nextCountDownTime = NEXT_COUNT_DOWN_TIME
        listener?.onRetestClick()
        mBinding.tvRetest.visibility = GONE
        mBinding.tvNext.visibility = GONE
        mBinding.tvTopHint.gone()
        cardMap.forEach { (_, value) ->
            value.reset()
        }
    }

    /**
     * 显示顶部居中提示
     */
    fun showTopHint(text: CharSequence) {
        mBinding.tvTopHint.text = text
        mBinding.tvTopHint.visibility = View.VISIBLE
    }

    /**
     * 隐藏顶部居中提示
     */
    fun hideTopHint() {
        mBinding.tvTopHint.visibility = View.GONE
    }

    /**
     * 顶部提示背景渐变：90deg（左→右），两端透明、中间蓝色
     * 色标较多（含 12%~88% 的平台段），XML 的 <gradient> 只支持三个色标，故用着色器还原
     */
    private fun createTopHintGradient(): Drawable = PaintDrawable().apply {
        shape = RectShape()
        shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                0f,
                TOP_HINT_GRADIENT_COLORS,
                TOP_HINT_GRADIENT_POSITIONS,
                Shader.TileMode.CLAMP
            )
        }
    }

    companion object {
        private val TOP_HINT_GRADIENT_COLORS = intArrayOf(
            Color.argb(0, 0, 92, 162),      //0%   rgba(0,92,162,0)
            Color.argb(82, 0, 96, 180),     //4%   rgba(0,96,180,0.32)
            Color.argb(204, 0, 99, 198),    //12%  rgba(0,99,198,0.8)
            Color.argb(242, 0, 99, 198),    //50%  rgba(0,99,198,0.95)
            Color.argb(204, 0, 99, 198),    //88%  rgba(0,99,198,0.8)
            Color.argb(82, 0, 97, 189),     //96%  rgba(0,97,189,0.32)
            Color.argb(0, 0, 92, 162)       //100% rgba(0,92,162,0)
        )

        private val TOP_HINT_GRADIENT_POSITIONS =
            floatArrayOf(0f, 0.04f, 0.12f, 0.5f, 0.88f, 0.96f, 1f)
    }

}
