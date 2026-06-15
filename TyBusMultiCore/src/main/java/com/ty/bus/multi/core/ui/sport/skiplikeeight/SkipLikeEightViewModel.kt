package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.outer.AdlService
import com.adl.service.web.SportMeetingService
import com.ty.bus.multi.core.base.BaseMultiSportViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.entity.SportResult
import com.ty.tybusbase.utils.LogUtil
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
    val avatarListLD = MutableLiveData<MutableList<AvatarItem>>()

    var meetGroupDetailsEntity: MeetGroupDetailsEntity? = null


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

    fun startRecord() {
        if (videoRecorder.isStarting) {
            return
        }
        getVideoPath()
        viewModelScope.launch(Dispatchers.IO) {
            val size = 1920 to 1080
            videoRecorder.onStart(videoFile, size.first, size.second)
        }
    }

    fun getMeetUserList() {
        viewModelScope.launch(Dispatchers.IO) {
            val userList = SportMeetingService.getSportMeetUserList(
                meetGroupDetailsEntity?.classId,
                recordMeet?.meetSportSkuId,
                recordMeet?.meetId
            )
            if (userList.isNullOrEmpty().not()) {
                // 前9个固定显示头像
                val items = mutableListOf<AvatarItem>()
                val totalCount = userList.size
                repeat(kotlin.math.min(totalCount, 10)) {
                    items.add(AvatarItem(url = AdlService.getService().facePrefix + userList[it].accountAvatar))
                }
                // 超出10个时，最后一个显示+X
                if (totalCount > 10) {
                    items.add(AvatarItem(showMore = true, moreCount = totalCount - 10))
                }
                avatarListLD.postValue(items)
            }
        }
    }

    fun stopRecord() {
        videoRecorder.onStop()
    }

    /**
     * 停止倒计时
     */
    fun stopCountDown() {
        countDownTimer?.cancel()
    }

    fun uploadResult(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 结束上传成绩，目前八字为集体赛，只传一条成绩
            meetGroupDetailsEntity?.let { meetGroupDetail ->
                val sportResult = SportResult()
                sportResult.sportDuration = sportInfo!!.endValue.toInt()
                sportResult.maxComboCount = 0
                sportResult.sportAchievement = count
                sportResult.perfectCount = 0
                sportResult.goodCount = 0
                sportResult.comeOnCount = 0
                sportResult.perfectScore = 0
                sportResult.goodScore = 0
                sportResult.comeOnScore = 0
                sportResult.startTime = startSportTime
                sportResult.endTime = endSportTime
                sportResult.sportResult = count.toString()
                sportResult.videoCover = videoCover
                sportResult.videoFile = videoFile
                recordMeet?.let {
                    it.meetNumberCode = meetGroupDetail.numberCode
                    it.meetRunwayCode = meetGroupDetail.runwayCode
                    it.serialNumber = meetGroupDetail.serialCode
                    it.meetNumberCode = meetGroupDetail.numberCode
                    it.meetRunwayCode = meetGroupDetail.runwayCode
                    it.classId = meetGroupDetail.classId
                }
                uploadSportResult(
                    sceneId ?: SceneConstants.FREE,
                    sportResult,
                    null,
                    recordMeeting = recordMeet
                )

            }
        }
    }

    /**
     * 释放资源
     */
    fun onRelease() {
        stopCountDown()
    }
}