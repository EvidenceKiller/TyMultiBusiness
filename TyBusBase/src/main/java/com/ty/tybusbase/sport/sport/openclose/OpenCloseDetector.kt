package com.ty.tybusbase.sport.sport.openclose

import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.adl.sport.general.EuHelper.calculateAngle
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.sport.base.CommonEuSingleBase
import com.ty.tybusbase.sport.listener.SingleEuBaseCallBack
import kotlin.math.abs
import kotlin.math.max

/**
 * @ProjectName: SportBS
 * @Date: 2025/1/6
 * @Desc: 开合跳改版
 */
open class OpenCloseDetector(singleEuBaseCallBack: SingleEuBaseCallBack?) :
    CommonEuSingleBase(singleEuBaseCallBack) {

    /**
     * 举手标记
     */
    private var isHandUp = 0

    /**
     * 张开腿标记
     */
    private var isOpenLeg = 0

    /**
     * 检测状态
     */
    protected var checkTag = false

    /**
     * 关键点位计数
     */
    protected var sumTag = 0

    /**
     * 关键位置的检测(第二关键点)
     */
    private var subTag = false

    /**
     * 一次有效计分中，的最大脚踝之间的距离
     */
    private var maxAnkleDistance = 0f

    /**
     * 一次有效计分中,手举的最高位置
     */
    private var maxHandDistance = 0f

    override fun onSkeleton(skeleton: AdlPoseSkeleton?) {
        if (skeleton == null) return
        singleEuBaseCallBack?.onSkeleton(skeleton)
        if (checkScore(skeleton)) return
    }

    fun checkScore(skeleton: AdlPoseSkeleton): Boolean {
        val lElbow = skeleton.lElbow
        val rElbow = skeleton.rElbow
        val rShoulder: AdlPoseKeyPoint = skeleton.rShoulder
        val lShoulder: AdlPoseKeyPoint = skeleton.lShoulder
        val rWrist: AdlPoseKeyPoint = skeleton.rWrist
        val lWrist: AdlPoseKeyPoint = skeleton.lWrist
        val chin = skeleton.chin
        val lAnkle: AdlPoseKeyPoint = skeleton.lAnkle
        val rAnkle: AdlPoseKeyPoint = skeleton.rAnkle
        val rHip: AdlPoseKeyPoint = skeleton.rHip
        val lHip: AdlPoseKeyPoint = skeleton.lHip

        if (!EuHelper.notNull(
                *arrayOf<AdlPoseKeyPoint>(
                    lElbow,
                    rElbow,
                    lWrist,
                    rWrist,
                    rShoulder,
                    lShoulder,
                    rHip,
                    lHip,
                    lAnkle,
                    rAnkle
                )
            )
        ) {
            mCallback?.onSpeaker("请保持正面面向屏屏幕")
            return false
        }
        // 手腕 肩 髋的夹角 必须为大于100°的钝角  1道和5道 降低要求 一侧夹角大于100度即可
        // 双腿张开至少与肩膀同宽
        val LWSH = calculateAngle(
            skeleton.getLShoulder(),
            skeleton.getLWrist(),
            skeleton.getLHip()
        )
        val RWSH = calculateAngle(
            skeleton.getRShoulder(),
            skeleton.getRWrist(),
            skeleton.getRHip()
        )
        val DSX: Float = Math.abs(skeleton.getLShoulder().x - skeleton.getRShoulder().x)
        val DAX: Float = Math.abs(skeleton.getLAnkle().x - skeleton.getRAnkle().x)
        val AXBigSX = DAX > DSX
        val LWYBigSY: Boolean = skeleton.getLWrist().y > skeleton.getLShoulder().y
        val RWYBigSY: Boolean = skeleton.getRWrist().y > skeleton.getRShoulder().y
        //第一阶段判断
        if ((!checkTag
                    && AXBigSX && LWSH > 100) && RWSH > 100
        ) {
            checkTag = true
            subTag = false
            sumTag++
        }
        //第一阶段满足后，计算分数
        if (checkTag) {
            val shoulderDistance = abs(skeleton.RShoulder.getX() - skeleton.LShoulder.getX())
            maxAnkleDistance =
                max(maxAnkleDistance, abs(skeleton.LAnkle.getX() - skeleton.RAnkle.getX()))
            maxHandDistance =
                max(maxHandDistance, max(abs(lWrist.y - lShoulder.y), abs(rWrist.y - rShoulder.y)))
            isOpenLeg =
                if (maxAnkleDistance >= shoulderDistance && maxAnkleDistance < shoulderDistance * 2) {
                    GOOD
                } else {
                    FINE
                }
            //如果手最高位置高于5倍下巴到肩的距离则GOOD
            isHandUp = if (maxHandDistance > shoulderDistance) {
                GOOD
            } else {
                FINE
            }
        }
        if ((!subTag
                    && checkTag && LWSH < 80) && RWSH < 80 && LWYBigSY
            && RWYBigSY
            && !AXBigSX
        ) {
            checkTag = false
            subTag = true
            sumTag++
        }
        if (sumTag >= 2 && !checkTag
            && subTag
        ) {
            result.singleSportLevel = judgeSportLevel()
            result.setMessage("完成")
            result.sportAchievement = 1
            singleEuBaseCallBack!!.onComplete(result)
            reset()
        }
        return true
    }

    @SportConstant.SingleSportLevel
    fun judgeSportLevel(): Int {
        var goodCount = 0
        if (isHandUp == GOOD) {
            goodCount += 1
        }
        if (isOpenLeg == GOOD) {
            goodCount += 1
        }
        //设置运动质量
        return if (goodCount >= 2) {
            SportConstant.SPORT_PERFECT
        } else if (goodCount == 1) {
            SportConstant.SPORT_TERRIFIC
        } else {
            SportConstant.SPORT_COME_ON
        }
    }

    /**
     * 注意这个方法在运动完成后只用在结果数据处理完成后才能调用,要不然可能会清空掉结果数据
     */
    override fun reset() {
        super.reset()
        isOpenLeg = 0
        isHandUp = 0
        maxAnkleDistance = 0f
        sumTag = 0
        maxHandDistance = 0f
    }

}