package com.ty.tybusbase.gesture

import com.adl.base.bean.PointF
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import kotlin.math.abs
import kotlin.math.max

class SkeletonGestureHelper2(val gestureListener: GestureListener?) {

    private val leftSkeletonGesture by lazy { SkeletonGesture() }
    private val rightSkeletonGesture by lazy { SkeletonGesture() }

    init {
        leftSkeletonGesture.setGestureCallback { gestureCommand ->
            gestureListener?.onGesture(gestureCommand, GestureType.TYPE_LEFT)
        }
        rightSkeletonGesture.setGestureCallback { gestureCommand ->
            gestureListener?.onGesture(gestureCommand, GestureType.TYPE_RIGHT)
        }
    }

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
        //左手检测
        leftSkeletonGesture.updateSkeleton(ski, true)
        //右手检测
        rightSkeletonGesture.updateSkeleton(ski, false)
    }

    fun release() {
        leftSkeletonGesture.release()
        rightSkeletonGesture.release()
    }

    inner class SkeletonGesture {
        //初始化标志位
        private var initFlag = false

        //开始检测手势标志位
        private var gestureStartFlag = false

        //开始确认标识
        private var confirmFlag = false

        //手势检测起始点
        private var gestureStartPoint = PointF(Float.MAX_VALUE, Float.MAX_VALUE)

        //手势移动距离
        private var gestureMoveDistance = 0f

        //头部位置
        private var headY = 0f

        //手势开始的下阈值
        private var downThreshold = 0f

        //手势回调，1：左，2：右
        private var gestureCallback: ((GestureCommand) -> Unit)? = null

        //确认手势回调，true：确认，false：取消
        private var gestureConfirmCallback: ((Boolean) -> Unit)? = null

        //手势检测间隔时间
        private var gestureDetectIntervalTime = 500L

        //上一次检测手势完成的时间
        private var preGestureTime = 0L

        //上一次检测举手时间
        private var preHandsUpTime = 0L

        //开始检测手势的时间
        private var startCheckTime = 0L

        //手势起始点重置时间
        private var gestureDetectResetTime = 1500L

        //检测确认和取消的手势，连续检测到该帧数，则认为手势结束;防止骨骼抖动带来的影响
        private var handsUpFrameCount = 10

        //当前检测确认和取消的手势帧数
        private var currentHandsUpFrame = 0

        fun setGestureCallback(callback: ((GestureCommand) -> Unit)?) {
            gestureCallback = callback
        }

        fun refreshParams(
            anotherShoulder: AdlPoseKeyPoint,
            centralShoulder: AdlPoseKeyPoint,
        ) {
            val shoulderWidth = abs(anotherShoulder.x - centralShoulder.x)
            gestureMoveDistance = shoulderWidth * 0.5f


            //肩到下颚点的X倍为头的位置
            headY = centralShoulder.y - shoulderWidth * 1f

            downThreshold = centralShoulder.y + shoulderWidth * 1f
        }

        /**
         * centralShoulder 中枢肩; 例、如果是检测左手，则传左肩；anotherShoulder传右肩
         * anotherShoulder 另一个肩
         */
        fun updateSkeleton(skeleton: AdlPoseSkeleton?, isLeft: Boolean) {
            if (gestureCallback == null || skeleton == null) {
                return
            }
            //刷新参数
            refreshParams(
                if (isLeft) skeleton.LShoulder else skeleton.RShoulder,
                if (isLeft) skeleton.RShoulder else skeleton.LShoulder
            )
            //检测确认手势
            checkHandsUpGesture(skeleton, isLeft.not())
            checkGesture(
                if (isLeft) skeleton.RWrist else skeleton.LWrist,
                if (isLeft) skeleton.RShoulder else skeleton.LShoulder
            )
        }

        private fun checkGesture(wrist: AdlPoseKeyPoint, centralShoulder: AdlPoseKeyPoint) {
            val gestureStartX = centralShoulder.x
            val gestureStartY = centralShoulder.y
            //开始检测手势
            if (gestureStartFlag) {
                //向上
//                if (wrist.y - gestureStartY < -gestureMoveDistance) {
//                    handleGestureCallback(GestureCommand.UP)
//                }
//
//                //向下
//                if (wrist.y - gestureStartY > gestureMoveDistance) {
//                    handleGestureCallback(GestureCommand.DOWN)
//                }

                //向左
                if (wrist.x - gestureStartX > gestureMoveDistance) {
                    handleGestureCallback(GestureCommand.RIGHT)
                }

                //向右
                if (wrist.x - gestureStartX < -gestureMoveDistance) {
                    handleGestureCallback(GestureCommand.LEFT)
                }

                //超过一定时间未检测到手势，重新取起始点
//                if (System.currentTimeMillis() - startCheckTime > gestureDetectResetTime) {
//                    startCheckTime = System.currentTimeMillis()
//                    gestureStartPoint.x = wrist.x
//                    gestureStartPoint.y = wrist.y
//                }

                //如果手势不在检测范围，则取消检测
                if (wrist.y > downThreshold || wrist.y < headY) {
                    gestureStartPoint.x = Float.MAX_VALUE
                    gestureStartPoint.y = Float.MAX_VALUE
                    gestureStartFlag = false
                }
                return
            }

            if (System.currentTimeMillis() - preGestureTime > gestureDetectIntervalTime
                && wrist.y < downThreshold
                && wrist.y > headY
            ) {
                gestureStartPoint.x = wrist.x
                gestureStartPoint.y = (downThreshold + headY) / 2
                startCheckTime = System.currentTimeMillis()
                gestureStartFlag = true
            }
        }

        private fun checkHandsUpGesture(skeleton: AdlPoseSkeleton, isLeft: Boolean) {
            //检测冷却时间内，不处理
            if (System.currentTimeMillis() - preHandsUpTime < gestureDetectIntervalTime) return
            //检测是否是确认手势开始
            if (isHandsUp(skeleton, isLeft)) {
                currentHandsUpFrame++
                if (currentHandsUpFrame >= handsUpFrameCount) {
                    handsUpGestureCallback()
                }
            } else {
                currentHandsUpFrame = max(0, currentHandsUpFrame - 1)
            }
        }

        /**
         * 举手回调
         */
        private fun handsUpGestureCallback() {
            currentHandsUpFrame = 0
            preHandsUpTime = System.currentTimeMillis()
            preGestureTime = preHandsUpTime
            gestureCallback?.invoke(GestureCommand.HANDS_UP)
        }

        //手势回调
        private fun handleGestureCallback(value: GestureCommand) {
            gestureCallback?.invoke(value)
            preGestureTime = System.currentTimeMillis()
            gestureStartFlag = false
        }

        private fun handleGestureConfirmCallback(value: Boolean) {
            currentHandsUpFrame = 0
            gestureConfirmCallback?.invoke(value)
            preGestureTime = System.currentTimeMillis()
            gestureStartFlag = false
        }

        fun isHandsUp(skeleton: AdlPoseSkeleton?, isLeft: Boolean): Boolean {
            if (skeleton == null) return false

            val wrist = if (isLeft) skeleton.lWrist else skeleton.rWrist
            val elbow = if (isLeft) skeleton.lElbow else skeleton.rElbow
            val shoulder = if (isLeft) skeleton.lShoulder else skeleton.rShoulder
            val hip = if (isLeft) skeleton.lHip else skeleton.rHip

            if (!EuHelper.notNull(wrist, elbow, shoulder, hip)) return false

            // 手腕 < 手肘 < 肩膀
            if (wrist.y < elbow.y && elbow.y < shoulder.y) {
                val angle1 = EuHelper.calculateAngle(elbow, wrist, shoulder)
                val angle2 = EuHelper.calculateAngle(shoulder, wrist, hip)
                return EuHelper.inRange(angle1, 120.0, 180.0) && EuHelper.inRange(
                    angle2,
                    80.0,
                    180.0
                )
            }
            return false
        }

        fun reset() {
            initFlag = false
            gestureStartFlag = false
            gestureStartPoint.x = Float.MAX_VALUE
            gestureStartPoint.y = Float.MAX_VALUE
            gestureMoveDistance = 0f
            headY = 0f
            gestureCallback = null
            gestureConfirmCallback = null
            preGestureTime = 0L
            currentHandsUpFrame = 0
            startCheckTime = 0L
        }

        fun release() {
            reset()
        }
    }
}

interface GestureListener {
    fun onGesture(command: GestureCommand, type: GestureType)
}

enum class GestureType {
    TYPE_LEFT,
    TYPE_RIGHT
}

//手势指令
enum class GestureCommand {
    UP,
    DOWN,
    LEFT,
    RIGHT,

    //举手
    HANDS_UP,
}