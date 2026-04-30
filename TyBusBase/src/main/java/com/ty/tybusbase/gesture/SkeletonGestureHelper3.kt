package com.ty.tybusbase.gesture

import com.adl.base.bean.PointF
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import com.ty.tybusbase.utils.ApplicationUtil
import com.ty.tybusbase.utils.ToastUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SkeletonGestureHelper3(val gestureListener: GestureListener?,val isMirror: Boolean = false) {
    private val leftSkeletonGesture by lazy { SingleHandGesture(true) }
    private val rightSkeletonGesture by lazy { SingleHandGesture(false) }

    /**
     * 更新骨骼数据
     *
     */
    fun updateSkeleton(ski: AdlPoseSkeleton) {
        if (gestureListener == null
            || ski.RShoulder == null
            || ski.LShoulder == null
            || ski.lElbow == null
            || ski.rElbow == null
            || ski.RWrist == null
            || ski.LWrist == null
        ) {
            return
        }

        leftSkeletonGesture.updateSkeleton(ski)
        rightSkeletonGesture.updateSkeleton(ski)

    }


    fun release() {
    }

    inner class SingleHandGesture(private val isLeft: Boolean) {
        // ---------- 可调参数 ----------
        private val RAISE_ARM_UP_RATIO = 0.15f        // 回归中心再开始的比例
        private val CONSISTENCY_FRAMES = 10          // 方向稳定帧数
        private val ARM_STRAIGHT_ANGLE = 150f       // 手臂伸直判定角度
        private val SHOULDER_STRAIGHT_ANGLE = 150f       // 手臂伸直判定角度
        private val RAISE_ARM_UP_ANGLE = 90f              // 手臂是否抬起的角度
        private val RAISE_ARM_UP_ANGLE_MIN =
            RAISE_ARM_UP_ANGLE - RAISE_ARM_UP_ANGLE * RAISE_ARM_UP_RATIO //手臂抬起角度最小值
        private val RAISE_ARM_UP_ANGLE_MAX =
            RAISE_ARM_UP_ANGLE + RAISE_ARM_UP_ANGLE * RAISE_ARM_UP_RATIO //手臂抬起角度最大值
        private var GESTURE_DETECT_INTERVAL_TIME = 400L //手势检测间隔时间
        private var GESTURE_HANDSUP_INTERVAL_TIME = 1000L //举手检测间隔时间

        //上一次检测手势完成的时间
        private var preGestureTime = 0L

        //上一次检测举手时间
        private var preHandsUpTime = 0L

        //当前检测确认和取消的手势帧数
        private var currentHandsUpFrame = 0

        //下一次候选手势；消抖判断
        private var candidateGestureCommand: GestureCommand? = null

        //产生候选后的累计帧数
        private var candidateFrames = 0

        //检测确认和取消的手势，连续检测到该帧数，则认为手势结束;防止骨骼抖动带来的影响
        private var HANDS_UP_FRAMES = 8

        fun updateSkeleton(skeleton: AdlPoseSkeleton?) {
            if (skeleton == null) {
                return
            }
            //检测确认手势
            checkHandsUpGesture(skeleton)
            checkGesture(skeleton)
        }

        fun isHandsUp(skeleton: AdlPoseSkeleton?): Boolean {
            if (skeleton == null) return false

            val wrist = if (isLeft) skeleton.lWrist else skeleton.rWrist
            val elbow = if (isLeft) skeleton.lElbow else skeleton.rElbow
            val shoulder = if (isLeft) skeleton.lShoulder else skeleton.rShoulder
            val hip = if (isLeft) skeleton.lHip else skeleton.rHip

            if (!EuHelper.notNull(wrist, elbow, shoulder, hip)) return false

            // 手腕 < 手肘 < 肩膀
            if (wrist.y < elbow.y && elbow.y < shoulder.y) {
                val angle1 = EuHelper.calculateAngle(elbow, wrist, shoulder)
                val angle2 = EuHelper.calculateAngle(shoulder, elbow, hip)
                return EuHelper.inRange(
                    angle1,
                    ARM_STRAIGHT_ANGLE.toDouble(),
                    180.0
                ) && EuHelper.inRange(
                    angle2,
                    SHOULDER_STRAIGHT_ANGLE.toDouble(),
                    180.0
                )
            }
            return false
        }

        private fun checkHandsUpGesture(skeleton: AdlPoseSkeleton) {
            //检测冷却时间内，不处理
            if (System.currentTimeMillis() - preHandsUpTime < GESTURE_HANDSUP_INTERVAL_TIME) return
            //检测是否是确认手势开始
            if (isHandsUp(skeleton)) {
                currentHandsUpFrame++
                if (currentHandsUpFrame >= HANDS_UP_FRAMES) {
                    handsUpGestureCallback()
                }
            } else {
                currentHandsUpFrame = max(0, currentHandsUpFrame - 1)
            }
        }

        private fun checkGesture(skeleton: AdlPoseSkeleton) {
            if (System.currentTimeMillis() - preGestureTime < GESTURE_DETECT_INTERVAL_TIME) {
                return
            }
            val wrist = if (isLeft) skeleton.lWrist else skeleton.rWrist
            val elbow = if (isLeft) skeleton.lElbow else skeleton.rElbow
            val shoulder = if (isLeft) skeleton.lShoulder else skeleton.rShoulder
            val hip = if (isLeft) skeleton.lHip else skeleton.rHip

            // 计算肩-肘-腕的夹角
            val armAngle = EuHelper.calculateAngle(elbow, shoulder, wrist)
            // 计算臀-肩-肘的夹角
            val angleA = EuHelper.calculateAngle(shoulder, hip, elbow)

            if (candidateGestureCommand == null) {
                if ((armAngle in ARM_STRAIGHT_ANGLE..180f)
                    && (angleA in RAISE_ARM_UP_ANGLE_MIN..RAISE_ARM_UP_ANGLE_MAX)
                ) {
                    candidateGestureCommand = GestureCommand.RIGHT
                }
            } else {
                candidateFrames++
                if (candidateFrames >= CONSISTENCY_FRAMES) {
                    if ((armAngle in ARM_STRAIGHT_ANGLE..180f)
                        && (angleA in RAISE_ARM_UP_ANGLE_MIN..RAISE_ARM_UP_ANGLE_MAX)
                    ) {
                        handleGestureCallback()
                    }
                    resetParam()
                }
            }
        }

        /**
         * 举手回调
         */
        private fun handsUpGestureCallback() {
            currentHandsUpFrame = 0
            preHandsUpTime = System.currentTimeMillis()
            preGestureTime = preHandsUpTime
            gestureListener?.onGesture(
                GestureCommand.HANDS_UP,
                if (isLeft xor isMirror) GestureType.TYPE_RIGHT else GestureType.TYPE_LEFT
            )

        }

        //手势回调
        private fun handleGestureCallback() {
            gestureListener?.onGesture(
                if (isLeft xor isMirror) GestureCommand.RIGHT else GestureCommand.LEFT,
                if (isLeft xor isMirror) GestureType.TYPE_RIGHT else GestureType.TYPE_LEFT
            )
            preGestureTime = System.currentTimeMillis()
        }

        private fun resetParam() {
            candidateGestureCommand = null
            candidateFrames = 0
        }
    }
}