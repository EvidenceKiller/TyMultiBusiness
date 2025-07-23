package com.ty.bus.multi.core.base

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.aipower.modulecommon.expand.Action
import com.aipower.modulepk.base.ISportCardView
import com.aipower.modulepk.ui.fivesport.MultiSportCardViewState
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.helper.SpeechWrapper
import java.util.concurrent.Executors

abstract class BaseMultiSportViewNoAnimation(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    protected val cardMap = HashMap<Int, ISportCardView>()
    protected val bottomShadowViewMap = HashMap<Int, View>()
    protected val preparedMap by lazy { SparseArray<Int>() }

    private var countDownTime = 4L // 倒计时，单位S
    private var countTimer: CountDownTimer? = null


    abstract fun getCountDownNoticeTv(): TextView

    abstract fun getCountDownGoTv(): TextView

    open fun setCountDownNotice(notice: String) {
        getCountDownNoticeTv().visibility = VISIBLE
        getCountDownNoticeTv().text = notice
    }

    /**
     * 切换卡片状态
     */
    fun changeState(number: Int, state: MultiSportCardViewState) {
        cardMap[number]?.changeState(state)
        when (state) {
            MultiSportCardViewState.NOBODY -> {
                preparedMap.remove(number)
            }

            MultiSportCardViewState.PREPARED -> {
                preparedMap.put(number, number)
            }

            else -> {}
        }
    }

    fun setUserInfo(p: Pair<Int, AccountInfo>) {
        cardMap[p.first]?.setUserInfo(p.second.userName, p.second.userName)
    }

    fun setAccountInfo(person: SparseArray<MultiSportResult>) {
        person.forEach { _, value ->
            preparedMap.put(value.number, value.number)
            cardMap[value.number]?.setAccountInfo(value.identifyResult)
            cardMap[value.number]?.changeState(MultiSportCardViewState.IDENTIFIED)
        }
    }

    /**
     * 切换卡片到无人状态
     */
    fun toNobody(number: Int) {
        cardMap[number]?.toNobody()
    }

    /**
     * 倒计时准备开始
     */
    open fun readyGo(block: Action) {
        getCountDownNoticeTv().text =
            context.getString(R.string.race_start_cheer)
        countTimer = object : CountDownTimer(countDownTime * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTime--
                var text = if (countDownTime == 0L) {
                    context.getString(R.string.start)
                } else {
                    countDownTime.toString()
                }
                getCountDownGoTv().text = text
                Executors.newSingleThreadExecutor().execute {
                    SpeechWrapper.speakAfterStop(text)
                }
            }

            override fun onFinish() {
                block.invoke()
                onStarted()
            }

        }.start()
        getCountDownGoTv().visibility = VISIBLE
    }

    /**
     * 比赛已经开始
     */
    open fun onStarted() {
        getCountDownGoTv().visibility = GONE
        getCountDownNoticeTv().visibility = GONE
        //比赛开始，切换卡片状态
        for (index in cardMap.values.indices) {
            //该位置无人
            if (preparedMap.get(index, null) == null) {
                changeState(index, MultiSportCardViewState.NOBODY_SPORT)
            } else {
                changeState(index, MultiSportCardViewState.SPORTING)
            }
        }
    }

    /**
     * 更新成绩
     */
    fun updateScore(result: MutableList<GroupSportBaseMan>) {
        for (index in result.indices) {
            val skipMan = result[index]
            if (skipMan.score != 0) {
                cardMap[skipMan.positionNum]?.updateScore(skipMan.score)
            }
        }
    }

    open fun updateTime(time: String) {
        cardMap.values.forEach {
            it.setTime(time)
        }
    }

    fun release() {
        countTimer?.cancel()
    }

    open fun changeTimeTextBg(res: Int) {
        cardMap.values.forEach {
            it.setTimeBackground(res)
        }
    }

}