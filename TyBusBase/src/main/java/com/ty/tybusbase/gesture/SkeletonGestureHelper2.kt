package com.ty.tybusbase.gesture

import com.adl.base.bean.PointF
import com.adl.lib.pose.AdlPoseKeyPoint
import com.adl.lib.pose.AdlPoseSkeleton
import com.adl.sport.general.EuHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

        //手势判定需要移动的距离
        private var gestureNeedMoveDistance = 0f

        //手势判定实时移动的距离
        private var gestureMoveDistance = 0f

        //手势起始点Y的坐标
        private var gestureStartPointY = 0f

        //手势上一次X坐标
        private var gesturePrePointX = 0f

        //手势Y轴区间移动最大距离，超过此距离，则不判定为手势左右
        private var gestureYMoveDistance = 0f

        //头部位置
        private var headY = 0f

        //手势开始的下阈值
        private var downThreshold = 0f

        //手势回调，1：左，2：右
        private var gestureCallback: ((GestureCommand) -> Unit)? = null

        //确认手势回调，true：确认，false：取消
        private var gestureConfirmCallback: ((Boolean) -> Unit)? = null

        //手势检测间隔时间
        private var gestureDetectIntervalTime = 300L

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
            hipY: Float
        ) {
            val shoulderWidth = abs(anotherShoulder.x - centralShoulder.x)
            gestureNeedMoveDistance = shoulderWidth * 0.5f


            //肩到下颚点的X倍为头的位置
            headY = centralShoulder.y - shoulderWidth * 1f

            downThreshold = hipY - shoulderWidth

            gestureYMoveDistance = (downThreshold - headY) * 0.8f
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
                if (isLeft) skeleton.RShoulder else skeleton.LShoulder,
                min(skeleton.lHip.y,skeleton.rHip.y)
            )
            //检测确认手势
            checkHandsUpGesture(skeleton, isLeft.not())
            checkGesture(
                if (isLeft) skeleton.RWrist else skeleton.LWrist,
                if (isLeft) skeleton.RShoulder else skeleton.LShoulder,
                isLeft
            )
        }

        private fun checkGesture(
            wrist: AdlPoseKeyPoint,
            centralShoulder: AdlPoseKeyPoint,
            isLeft: Boolean
        ) {
            //开始检测手势
            if (gestureStartFlag) {
                //如果手势不在检测范围，则取消检测
                if (wrist.y > downThreshold || wrist.y < headY) {
                    gestureStartPoint.x = Float.MAX_VALUE
                    gestureStartPoint.y = Float.MAX_VALUE
                    gestureStartFlag = false
//                    LogUtil.d("手势取消")
                    return
                }

                //赋值Y坐标
                if (gestureMoveDistance == 0f) {
                    gestureStartPointY = centralShoulder.y
                }

                gestureMoveDistance += wrist.x - gesturePrePointX

                //Y轴移动的距离
                val movedYDistance = abs(wrist.y - gestureStartPointY)

                //手势终点到中心控制点的距离
                val centralToEndDistance = abs(centralShoulder.x - wrist.x)
//                LogUtil.d("每次加的距离：${wrist.x - gesturePrePointX},总距离：${gestureMoveDistance}")
//                LogUtil.d("当前点：${wrist.x},上一个点: ${gesturePrePointX} 起始点x: ${gestureStartPoint.x},中轴点：${centralShoulder.x}")
                gesturePrePointX = wrist.x
                //如果手势Y轴移动距离超过阈值，则手势检测无效
                //如果手势终点到中心控制点的距离小于阈值（需要移动的距离的80%），则手势检测无效
                if (movedYDistance > gestureYMoveDistance
                    || centralToEndDistance < gestureNeedMoveDistance * 0.8f
                ) {
                    return
                }
//                LogUtil.d("手势检测：移动距离${gestureMoveDistance} 起始点x: ${gestureStartPoint.x},中轴点：${centralShoulder.x}")
//                LogUtil.d("手势检测：${if (gestureMoveDistance > gestureNeedMoveDistance) "向右" else if (gestureMoveDistance < -gestureNeedMoveDistance) "向左" else "未移动"}")

                //向右
                if (isLeft && gestureMoveDistance > gestureNeedMoveDistance) {
                    handleGestureCallback(GestureCommand.RIGHT)
                }

                //向左
                if (isLeft.not() && gestureMoveDistance < -gestureNeedMoveDistance) {
                    handleGestureCallback(GestureCommand.LEFT)
                }
                return
            }

            //是否在中轴点附近
            val isCloseCentral = abs(centralShoulder.x - wrist.x) < gestureNeedMoveDistance * 0.3f
            //满足此条件，开始检测手势
            //时间间隔满足，并且，在检测范围之内
            if (System.currentTimeMillis() - preGestureTime > gestureDetectIntervalTime
                && wrist.y < downThreshold
                && wrist.y > headY
                && isCloseCentral
            ) {
                gestureStartPoint.x = wrist.x
                gestureStartPoint.y = (downThreshold + headY) / 2
                startCheckTime = System.currentTimeMillis()
                gestureMoveDistance = 0f
                gestureStartFlag = true
                gesturePrePointX = wrist.x
//                LogUtil.d("手势开始;起始点坐标：${gesturePrePointX}")
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
            gestureMoveDistance = 0f
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
            gestureNeedMoveDistance = 0f
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