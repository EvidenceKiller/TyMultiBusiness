package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.MeetGroupDetailsEntity
import com.ty.bus.multi.core.base.BaseMultiSportViewModel
import com.ty.tybusbase.utils.SportTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkipLikeEightViewModel(application: Application) : BaseMultiSportViewModel(application) {

    private var countDownTimer: SportTimer? = null

    /** 倒计时剩余秒数 */
    val countDownSecondsLD = MutableLiveData<Long>()

    /** 倒计时结束事件 */
    val countDownFinishLD = MutableLiveData<Unit>()

    /** 头像列表数据 */
    val avatarListLD = MutableLiveData<List<AvatarItem>>()

    var studentList: ArrayList<MeetGroupDetailsEntity>? = null
    /**
     * 加载头像列表数据
     */
    fun loadPerson() {
        val avatarUrl = "https://f.nezhasport.com/V2/busi/userFaceImgUrl/2025-12-11/1765450309969_7c033eb3.png"
        val totalCount = 15 // 实际总数，可从接口获取
        val items = mutableListOf<AvatarItem>()

        // 前9个固定显示头像
        repeat(kotlin.math.min(totalCount, 9)) {
            items.add(AvatarItem(url = avatarUrl))
        }

        // 超出10个时，最后一个显示+X
        if (totalCount > 10) {
            items.add(AvatarItem(showMore = true, moreCount = totalCount - 10))
        } else if (totalCount == 10) {
            items.add(AvatarItem(url = avatarUrl))
        }

        avatarListLD.postValue(items)
    }

    /**
     * 开始倒计时
     */
    fun startCountDown(totalSeconds: Long) {
        countDownTimer?.cancel()
        countDownTimer = SportTimer.build {
            mode = SportTimer.Mode.COUNT_DOWN
            totalTimeSeconds = totalSeconds
            callback = object : SportTimer.Callback {
                override fun onTick(time: Long) {
                    countDownSecondsLD.postValue(time)
                }

                override fun onFinish() {
                    countDownFinishLD.postValue(Unit)
                }
            }
        }
        countDownTimer?.start()
    }

    fun startRecord(){
        if (videoRecorder.isStarting) {
            return
        }
        getVideoPath()
        viewModelScope.launch(Dispatchers.IO) {
            val size = 1920 to 1080
            videoRecorder.onStart(videoFile, size.first, size.second)
        }
    }


    fun stopRecord(){
        videoRecorder.onStop()
    }

    /**
     * 停止倒计时
     */
    fun stopCountDown() {
        countDownTimer?.cancel()
    }

    /**
     * 释放资源
     */
    fun onRelease() {
        stopCountDown()
    }
}