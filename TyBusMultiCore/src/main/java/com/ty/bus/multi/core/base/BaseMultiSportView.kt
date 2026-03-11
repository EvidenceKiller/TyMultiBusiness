package com.ty.bus.multi.core.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.forEach
import com.adl.sport.general.groupsportbase.GroupSportBaseMan
import com.ty.bus.multi.R
import com.ty.bus.multi.core.entity.MultiSportResult
import com.ty.bus.multi.core.view.common.SportCardSportView
import com.ty.tybusbase.entity.AccountInfo
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.helper.SpeechWrapper
import java.util.concurrent.Executors

@SuppressLint("UseSparseArrays")
abstract class BaseMultiSportView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    protected val cardMap = HashMap<Int, SportCardSportView>()
    protected val bottomShadowViewMap = HashMap<Int, View>()
    protected val preparedMap by lazy { SparseArray<Int>() }

    private var countDownTime = 4L // 倒计时，单位S
    private var countTimer: CountDownTimer? = null

    abstract fun setCountDownNotice(notice: String)

    abstract fun getCountDownNoticeTv(): TextView

    abstract fun getCountDownGoTv(): TextView

    abstract fun needCardAnimation(): Boolean

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
                doTranslateAnimation(cardMap[number], number)
                doTranslateShadowAnimation(bottomShadowViewMap[number])
            }

            else -> {}
        }
    }

    fun setUserInfo(p: Pair<Int, AccountInfo>) {
        cardMap[p.first]?.setUserInfo(
            p.second.studentEntity!!.accountName,
            p.second.studentEntity?.userFaceImgUrl
        )
        cardMap[p.first]?.setAccountInfo(p.second)
    }

    fun setAccountInfo(person: SparseArray<out MultiSportResult>) {
        person.forEach { _, value ->
            preparedMap.put(value.number, value.number)
            cardMap[value.number]?.setAccountInfo(value.accountInfo)
            cardMap[value.number]?.changeState(MultiSportCardViewState.IDENTIFIED)
        }
    }

    fun setPrecastAccountInfo(person: SparseArray<out MultiSportResult>) {
        person.forEach { _, value ->
            preparedMap.put(value.number, value.number)
            cardMap[value.number]?.setPrecastAccountInfo(value.accountInfo)
            cardMap[value.number]?.changeState(MultiSportCardViewState.PRECAST_PERSON)
        }
    }


    /**
     * 切换卡片到无人状态
     */
    fun toNobody(number: Int) {
        cardMap[number]?.toNobody()
        preparedMap.remove(number)
    }

    /**
     * 切换卡片到standby状态
     */
    fun toStandby(number: Int) {
        cardMap[number]?.toStandby()
    }

    /**
     * 切换卡片到识别状态
     */
    fun toIdentifying(number: Int) {
        cardMap[number]?.toIdentifying()
    }

    /**
     * 切换卡片到准备中状态（已识别到人）
     */
    fun toPrepared(number: Int, accountInfo: AccountInfo) {
        val skipCardView = cardMap[number] ?: return
        skipCardView.toPrepared(accountInfo)
        preparedMap.put(number, number)
        doTranslateAnimation(skipCardView, number)
        doTranslateShadowAnimation(bottomShadowViewMap[number])
    }

    /**
     * 离开切换到运动状态
     */
    fun leaveToSporting(number: Int) {
        cardMap[number]?.leaveToSporting()
    }

    /**
     * 运动状态切换到离开状态
     */
    fun sportingToLeave(number: Int) {
        cardMap[number]?.sportingToLeave()
    }

    /**
     * 切换到干扰模式
     */
    fun sportingToInterference(number: Int) {
        cardMap[number]?.sportingToInterference()
    }

    /**
     * 干扰切换到运动模式
     */
    fun interferenceToSporting(number: Int) {
        cardMap[number]?.interferenceToSporting()
    }

    fun toIdentifyFail(number: Int) {
        cardMap[number]?.toIdentifyFail()
    }

    fun toPrecastPrepare(number: Int) {
        cardMap[number]?.toPrecastPrepare()
    }

    /**
     * 倒计时准备开始
     */
    fun readyGo(block: Action) {
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
    fun onStarted() {
        getCountDownGoTv().visibility = GONE
        //比赛开始，切换卡片状态
        for (index in cardMap.values.indices) {
            val cardSportView = cardMap[index]
            //该位置无人
            if (preparedMap.get(index, null) == null) {
                cardSportView?.toNobodySport()
                doTranslateAnimation(cardSportView, index)
                doTranslateShadowAnimation(bottomShadowViewMap[index])
            } else {
                cardSportView?.toSporting()
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

    fun updateTime(time: String) {
        cardMap.values.forEach {
            it.setTime(time)
        }
    }

    fun release() {
        countTimer?.cancel()
    }

    fun changeTimeTextBg(res: Int) {
        cardMap.values.forEach {
            it.setTimeBackground(res)
        }
    }

    /**
     * 执行卡片平移动画
     */
    private fun doTranslateAnimation(view: View?, number: Int) {
        if (view == null || !needCardAnimation()) return
        //计算动画移动距离
        val yDelta = if (number % 2 == 1) {
            -80f
        } else {
            100f
        }
        val moveAnimation = TranslateAnimation(0f, 0f, 0f, yDelta)
        moveAnimation.duration = 500
        moveAnimation.fillAfter = true
        view.startAnimation(moveAnimation)
    }

    /**
     * 执行卡片下面阴影部分的动画
     */
    private fun doTranslateShadowAnimation(view: View?) {
        view?.post {
            val moveAnimation = TranslateAnimation(0f, 0f, 0f, -216f)
            moveAnimation.duration = 500
            moveAnimation.fillAfter = true
            view.startAnimation(moveAnimation)
        }
    }

}