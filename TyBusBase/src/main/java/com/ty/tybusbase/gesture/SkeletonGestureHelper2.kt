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

@Deprecated("手势本身存在歧义，手势挥动无法统一；Deprecated in favor of SkeletonGestureHelper3")
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
//        leftSkeletonGesture.updateSkeleton(ski, true)
        //右手检测
        rightSkeletonGesture.updateSkeleton(ski, false)
    }

    fun release() {
        leftSkeletonGesture.release()
        rightSkeletonGesture.release()
    }

    inner class SkeletonGesture {

        // ---------- 可调参数 ----------
        private val SWIPE_THRESHOLD_RATIO = 0.6f    // 滑动距离阈值（肩宽倍数）
        private val REVERSE_RATIO = 0.3f            // 反向位移阈值（相对于滑动阈值）
        private val BACK_CENTER_RATIO = 0.2f        // 回归中心再开始的比例
        private val CONSISTENCY_FRAMES = 10          // 方向稳定帧数
        private val REVERSE_FRAMES = 2              // 反向判定帧数
        private val ARM_STRAIGHT_ANGLE = 150f       // 手臂伸直判定角度
        private val SHOULDER_STRAIGHT_ANGLE = 150f       // 手臂伸直判定角度
        private val ARM_UP_ANGLE = 20f              // 手臂是否抬起的角度
        private var GESTURE_DETECT_INTERVAL_TIME = 200L //手势检测间隔时间

        //检测确认和取消的手势，连续检测到该帧数，则认为手势结束;防止骨骼抖动带来的影响
        private var HANDS_UP_FRAMES = 8

        //初始化标志位
        private var initFlag = false

        //开始检测手势标志位
        private var gestureStartFlag = false

        //手势检测起始点
        private var gestureStartPoint = PointF(Float.MAX_VALUE, Float.MAX_VALUE)

        //手势判定需要移动的距离
        private var gestureNeedMoveDistance = 0f

        //手势判定实时移动的距离
        private var gestureMoveDistance = 0f

        //手势上一次X坐标
        private var gesturePrePointX = 0f

        //手势Y轴区间移动最大距离，超过此距离，则不判定为手势左右
        private var gestureYMoveDistance = 0f

        //头部位置
        private var headY = 0f

        //手势回调，1：左，2：右
        private var gestureCallback: ((GestureCommand) -> Unit)? = null

        //确认手势回调，true：确认，false：取消
        private var gestureConfirmCallback: ((Boolean) -> Unit)? = null

        //上一次检测手势完成的时间
        private var preGestureTime = 0L

        //上一次检测举手时间
        private var preHandsUpTime = 0L

        //开始检测手势的时间
        private var startCheckTime = 0L

        //当前检测确认和取消的手势帧数
        private var currentHandsUpFrame = 0

        //上一次产生的手势
        private var preGestureCommand: GestureCommand? = null

        //下一次候选手势；消抖判断
        private var candidateGestureCommand: GestureCommand? = null

        //产生手势时的手腕位置
        private var endGesturePoint = PointF(0f, 0f)

        //产生候选后的累计帧数
        private var candidateFrames = 0

        //候选手势后移动的距离
        private var candidateMoveDistance = 0f

        //候选手势后是否反向
        private var candidateReverse = false

        //反向帧数
        private var candidateReverseFrames = 0
        fun setGestureCallback(callback: ((GestureCommand) -> Unit)?) {
            gestureCallback = callback
        }

        fun refreshParams(
            anotherShoulder: AdlPoseKeyPoint,
            centralShoulder: AdlPoseKeyPoint,
        ) {
            val shoulderWidth = abs(anotherShoulder.x - centralShoulder.x)
            gestureNeedMoveDistance = shoulderWidth * SWIPE_THRESHOLD_RATIO
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
                if (isLeft) skeleton.RShoulder else skeleton.LShoulder,
                if (isLeft) skeleton.RElbow else skeleton.LElbow,
                if (isLeft) skeleton.RHip else skeleton.LHip
            )
        }

        private fun checkGesture(
            wrist: AdlPoseKeyPoint,
            centralShoulder: AdlPoseKeyPoint,
            elbow: AdlPoseKeyPoint,
            hip: AdlPoseKeyPoint,
        ) {
            // 手臂垂直判断
            val straight = isArmStraight(centralShoulder, elbow, wrist, hip)
            if (straight) {
                //如果手臂垂直，则不进行手势检测
                resetParam()
                return
            }
            if (gestureStartFlag) {

                gestureMoveDistance += wrist.x - gesturePrePointX

                //Y轴移动的距离
                val movedYDistance = abs(wrist.y - gestureStartPoint.y)

                //手势终点到中心控制点的距离
                val centralToEndDistance = abs(centralShoulder.x - wrist.x)
                gesturePrePointX = wrist.x
//                //如果手势终点到中心控制点的距离小于阈值（需要移动的距离的80%），则手势检测无效
//                if (centralToEndDistance < gestureNeedMoveDistance * 0.8f) {
//
//                    return
//                }

                //如果手势Y轴移动距离超过阈值，则手势检测无效
//                if (movedYDistance > gestureNeedMoveDistance * VALID_HEIGHT_RATIO) {
//                    ToastUtil.getInstance(ApplicationUtil.getContext(), "垂直距离超标手势无效")
//                        .show()
//                    resetParam()
//                    return
//                }

                //添加预选机制（消抖），输出用户最后的手势，蓄力过程应该屏蔽掉
                //向右
                if (candidateGestureCommand == null) {
                    if (gestureMoveDistance > gestureNeedMoveDistance) {
                        candidateGestureCommand = GestureCommand.RIGHT
                    }

                    //向左
                    if (gestureMoveDistance < -gestureNeedMoveDistance) {
                        candidateGestureCommand = GestureCommand.LEFT
                    }
                    //记录手腕位置
                    endGesturePoint.x = wrist.x
                    endGesturePoint.y = wrist.y
                } else {
                    candidateFrames++
                    val netDx = wrist.x - endGesturePoint.x
                    val reverse = (candidateGestureCommand == GestureCommand.RIGHT && netDx < 0) ||
                            (candidateGestureCommand == GestureCommand.LEFT && netDx > 0)
                    if (reverse) {
                        candidateReverseFrames++
                        if (candidateReverseFrames >= REVERSE_FRAMES) {
                            candidateReverse = true
                        }
                        candidateMoveDistance += abs(endGesturePoint.x - wrist.x)
                    }
                    //候选手势到达一致性帧数，判断是否候选手势为最终手势
                    //需要区分，比如往右蓄力，到达右判定点，继续往右，然后往左，一致性帧数到时，正好到产生候选的终点附近，则可能判定为候选有效，其实用户目的可能是向左的手势
                    //所以需要记录终点后的移动距离，如果这个距离小于阈值，则判定为最终手势
                    if (candidateFrames >= CONSISTENCY_FRAMES) {
                        //发生了反转才需要判断回拉距离，移动距离小于阈值，则判定为最终手势
                        if (candidateReverse.not() || candidateMoveDistance < gestureNeedMoveDistance * REVERSE_RATIO) {
                            handleGestureCallback(candidateGestureCommand!!)
                            preGestureCommand = candidateGestureCommand
                            resetParam()
                        } else {
                            //手势反转重新赋值起点，设当前手势点为起点
                            gestureStartPoint.x = wrist.x
                            gestureStartPoint.y = wrist.y
                            gestureMoveDistance =
                                if (candidateGestureCommand == GestureCommand.RIGHT) candidateMoveDistance * -1f else candidateMoveDistance
                            resetReverseParam()
                        }
                    }
                    endGesturePoint.x = wrist.x
                    endGesturePoint.y = wrist.y
                }
                return
            }

            //是否在中轴点附近
            var isCloseCentral =
                abs(centralShoulder.x - wrist.x) < gestureNeedMoveDistance * BACK_CENTER_RATIO
            //如果不满足，则再根据上次是什么手势，判断是否是骨骼点跳过了检测区域，例如上次是左滑，手势回拉太快，可能直接跳过检测区，拉到了检测区右边，导致无法检测
            if (isCloseCentral.not()) {
                when (preGestureCommand) {
                    GestureCommand.LEFT -> {
                        //已经移动到了检测区右边
                        if (wrist.x > centralShoulder.x) {
                            isCloseCentral = true
                        }
                    }

                    GestureCommand.RIGHT -> {
                        if (wrist.x < centralShoulder.x) {
                            isCloseCentral = true
                        }
                    }

                    else -> {}
                }
            }
            //满足此条件，开始检测手势
            //时间间隔满足，并且，在检测范围之内
            if (System.currentTimeMillis() - preGestureTime > GESTURE_DETECT_INTERVAL_TIME
                && isCloseCentral
            ) {
                gestureStartPoint.x = wrist.x
                //如果起始点的Y在
                gestureStartPoint.y = wrist.y
                startCheckTime = System.currentTimeMillis()
                gestureMoveDistance = 0f
                gestureStartFlag = true
                gesturePrePointX = wrist.x
            }
        }

        private fun checkHandsUpGesture(skeleton: AdlPoseSkeleton, isLeft: Boolean) {
            //检测冷却时间内，不处理
            if (System.currentTimeMillis() - preHandsUpTime < GESTURE_DETECT_INTERVAL_TIME) return
            //检测是否是确认手势开始
            if (isHandsUp(skeleton, isLeft)) {
                currentHandsUpFrame++
                if (currentHandsUpFrame >= HANDS_UP_FRAMES) {
                    handsUpGestureCallback()
                }
            } else {
                currentHandsUpFrame = max(0, currentHandsUpFrame - 1)
            }
        }

        private fun resetReverseParam() {
            candidateGestureCommand = null
            candidateFrames = 0
            candidateReverseFrames = 0
            candidateReverse = false
            candidateMoveDistance = 0f
        }

        private fun resetParam() {
            candidateGestureCommand = null
            gestureStartFlag = false
            gestureMoveDistance = 0f
            endGesturePoint.x = 0f
            endGesturePoint.y = 0f
            candidateGestureCommand = null
            candidateMoveDistance = 0f
            candidateFrames = 0
            candidateReverseFrames = 0
            candidateReverse = false
        }

        // ---------- 手臂垂直判定 ----------
        private fun isArmStraight(
            shoulder: AdlPoseKeyPoint,
            elbow: AdlPoseKeyPoint,
            smoothWrist: AdlPoseKeyPoint,
            hip: AdlPoseKeyPoint
        ): Boolean {
            // 计算肩-肘-腕的夹角
            val angle = EuHelper.calculateAngle(
                elbow, shoulder,
                smoothWrist
            )
            val a3 = EuHelper.calculateAngle(shoulder, hip, elbow)
            return angle >= ARM_STRAIGHT_ANGLE && a3 < ARM_UP_ANGLE
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

enum class GestureType {
    TYPE_LEFT,
    TYPE_RIGHT
}