package com.ty.bus.multi.core.ui.sport.twelvegbsport

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ty.bus.multi.R
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.dialog.CommonTipDialog
import com.ty.tybusbase.entity.DisplayType
import com.ty.tybusbase.expand.getDisplayType
import com.ty.tybusbase.expand.toJson
import com.ty.tybusbase.expand.toObject
import com.ty.tybusbase.grpc.CollaborationCmdEnum
import com.ty.tybusbase.grpc.CollaborationStatusEnum
import com.ty.tybusbase.grpc.GrpcCmdCode
import com.ty.tybusbase.grpc.GrpcCollaborationSportInfo
import com.ty.tybusbase.grpc.GrpcCollaborationStatus
import java.util.concurrent.atomic.AtomicInteger

/**
 * 十二人双摇国标 gRPC 协同工具类（详情页预检）
 *
 * - [registerCollaboration]：进页即 QUERY → 占用弹窗 → FORCE_JOIN（Common 详情沿用）
 * - [bindForDeferredStart] / [queryThenDecide] / [forceJoin]：进页只绑定，点开始再 QUERY/弹窗/FORCE_JOIN（Multi 详情）
 */
object TwelveSportGbGrpcCollaborationUtils {

    private const val TAG = "ZXN_TEST"
    private const val COLLABORATION_STATUS_TIMEOUT_MS = 3000L
    private const val TEST_COUNT = 12
    private const val DEFERRED_DIALOG_TAG = "TwelveDeferredTipDialog"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var collaborationStatusTimeoutRunnable: Runnable? = null
    private var deferredQueryTimeoutRunnable: Runnable? = null

    @Volatile
    private var leftSubStatus: CollaborationStatusEnum? = null
    @Volatile
    private var rightSubStatus: CollaborationStatusEnum? = null

    private val lock = Any()

    @Volatile
    private var hasNavigatedToSport = false
    @Volatile
    private var isWaitingForCollaboration = false

    @Volatile
    private var isWaitingForDeferredQuery = false
    @Volatile
    private var hasForceJoinedDeferred = false

    private var currentSportId: String? = null
    private var currentSportSkuId: String = ""
    private var leftScreenIp: String = ""
    private var rightScreenIp: String = ""

    private var deferredLeftIp: String = ""
    private var deferredRightIp: String = ""

    // ==================== Legacy（Common 详情进页 FORCE_JOIN） ====================

    fun registerCollaboration(
        activity: AppCompatActivity,
        sportId: String,
        sportSkuId: String,
        leftSubScreenIp: String,
        rightSubScreenIp: String,
        resultCallback: ((Boolean, Boolean) -> Unit)? = null
    ) {
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::registerCollaboration: $sportId, sku=$sportSkuId")

        currentSportId = sportId
        currentSportSkuId = sportSkuId
        leftScreenIp = leftSubScreenIp
        rightScreenIp = rightSubScreenIp

        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COLLABORATION_STATUS, sportId) { data ->
            Log.d(TAG, "TwelveSportGbGrpcCollaborationUtils::registerCollaboration: data $data")
            if (!isWaitingForCollaboration) {
                return@registerServerHandler
            }
            val status = data.payload.toObject<GrpcCollaborationStatus>()?.status
            val displayType = data.from.getDisplayType(leftSubScreenIp, rightSubScreenIp)
            val statusEnum = CollaborationStatusEnum.fromCode(status ?: -1)
            Log.d(
                TAG,
                "TwelveSportGbGrpcCollaborationUtils::registerCollaboration: 收到 $displayType 协同状态：$statusEnum"
            )

            synchronized(lock) {
                when (displayType) {
                    DisplayType.LEFT -> leftSubStatus = statusEnum
                    DisplayType.RIGHT -> rightSubStatus = statusEnum
                    else -> {}
                }
            }

            if (leftSubStatus == null || rightSubStatus == null) {
                return@registerServerHandler
            }

            cancelCollaborationStatusTimeout()

            val isLeftValid = leftSubStatus == CollaborationStatusEnum.JOINABLE
            val isRightValid = rightSubStatus == CollaborationStatusEnum.JOINABLE
            if (isLeftValid && isRightValid) {
                if (!hasNavigatedToSport) {
                    hasNavigatedToSport = true
                    isWaitingForCollaboration = false
                    forceIntoCollaboration(resultCallback)
                    Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::registerCollaboration: both JOINABLE, FORCE_JOIN")
                    leftSubStatus = null
                    rightSubStatus = null
                }
            } else {
                activity.runOnUiThread {
                    if (activity.isFinishing || activity.isDestroyed || activity.supportFragmentManager.isStateSaved) {
                        return@runOnUiThread
                    }
                    val existing = activity.supportFragmentManager.findFragmentByTag("CommonTipDialog")
                    if (existing != null && existing.isAdded) {
                        return@runOnUiThread
                    }
                    CommonTipDialog()
                        .setContent(activity.getString(R.string.sub_screen_sport_ongoing_tip))
                        .setCallback {
                            if (!hasNavigatedToSport) {
                                isWaitingForCollaboration = false
                                hasNavigatedToSport = true
                                forceIntoCollaboration(resultCallback)
                                Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::registerCollaboration: user confirm FORCE_JOIN")
                                leftSubStatus = null
                                rightSubStatus = null
                            }
                        }
                        .show(activity.supportFragmentManager, "CommonTipDialog")
                }
            }
        }
        queryCollaborationStatus(resultCallback)
    }

    fun unregisterCollaboration() {
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::unregisterCollaboration: $currentSportId")
        finishCollaboration()
        cancelCollaborationStatusTimeout()
        currentSportId?.let {
            GrpcManager.unregisterHandler(it)
        }
        hasNavigatedToSport = false
        isWaitingForCollaboration = false
    }

    // ==================== Deferred（Multi 详情点开始再 FORCE_JOIN） ====================

    /**
     * 进详情只绑定 handler 与副屏 IP，不 QUERY、不 FORCE_JOIN、不弹窗。
     */
    fun bindForDeferredStart(
        activity: AppCompatActivity,
        sportId: String,
        sportSkuId: String,
        leftSubScreenIp: String,
        rightSubScreenIp: String
    ) {
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::bindForDeferredStart: $sportId, sku=$sportSkuId")
        currentSportId = sportId
        currentSportSkuId = sportSkuId
        leftScreenIp = leftSubScreenIp
        rightScreenIp = rightSubScreenIp
        deferredLeftIp = leftSubScreenIp
        deferredRightIp = rightSubScreenIp
        hasForceJoinedDeferred = false
        isWaitingForDeferredQuery = false

        GrpcManager.registerServerHandler(GrpcCmdCode.CMD_COLLABORATION_STATUS, sportId) { data ->
            if (!isWaitingForDeferredQuery) {
                return@registerServerHandler
            }
            val status = data.payload.toObject<GrpcCollaborationStatus>()?.status
            val displayType = data.from.getDisplayType(deferredLeftIp, deferredRightIp)
            val statusEnum = CollaborationStatusEnum.fromCode(status ?: -1)
            Log.d(
                TAG,
                "TwelveSportGbGrpcCollaborationUtils::bindForDeferredStart: 收到 $displayType 协同状态：$statusEnum"
            )
            synchronized(lock) {
                when (displayType) {
                    DisplayType.LEFT -> leftSubStatus = statusEnum
                    DisplayType.RIGHT -> rightSubStatus = statusEnum
                    else -> {}
                }
            }
            if (leftSubStatus == null || rightSubStatus == null) {
                return@registerServerHandler
            }
            cancelDeferredQueryTimeout()
            isWaitingForDeferredQuery = false
            handleDeferredQueryResult(activity)
        }
    }

    /**
     * 点「开始锻炼」时调用：QUERY；双端 JOINABLE 直接 [onReadyToJoin]；
     * 否则弹窗，确认后 [onReadyToJoin]，取消 [onCancel]；超时 [onFail]。
     */
    fun queryThenDecide(
        activity: AppCompatActivity,
        onReadyToJoin: () -> Unit,
        onCancel: (() -> Unit)? = null,
        onFail: ((Boolean, Boolean) -> Unit)? = null
    ) {
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::queryThenDecide")
        deferredOnReadyToJoin = onReadyToJoin
        deferredOnCancel = onCancel
        deferredOnFail = onFail
        leftSubStatus = null
        rightSubStatus = null
        isWaitingForDeferredQuery = true
        cancelDeferredQueryTimeout()
        deferredQueryTimeoutRunnable = Runnable {
            deferredQueryTimeoutRunnable = null
            if (!isWaitingForDeferredQuery) return@Runnable
            isWaitingForDeferredQuery = false
            val leftOk = leftSubStatus != null
            val rightOk = rightSubStatus != null
            Log.w(
                TAG,
                "TwelveSportGbGrpcCollaborationUtils::queryThenDecide: timeout leftOk=$leftOk, rightOk=$rightOk"
            )
            deferredOnFail?.invoke(leftOk, rightOk)
            clearDeferredCallbacks()
        }
        mainHandler.postDelayed(deferredQueryTimeoutRunnable!!, COLLABORATION_STATUS_TIMEOUT_MS)
        sendCollaborationCommand(currentSportSkuId, leftScreenIp, CollaborationCmdEnum.QUERY)
        sendCollaborationCommand(currentSportSkuId, rightScreenIp, CollaborationCmdEnum.QUERY)
    }

    fun forceJoin(onResult: ((Boolean, Boolean) -> Unit)? = null) {
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::forceJoin")
        forceIntoCollaboration { leftOk, rightOk ->
            if (leftOk && rightOk) {
                hasForceJoinedDeferred = true
            }
            onResult?.invoke(leftOk, rightOk)
        }
    }

    /**
     * 注销 deferred 绑定；仅当已 FORCE_JOIN 或显式要求时发 FINISH。
     */
    fun unbindDeferred(sendFinish: Boolean = false) {
        Log.i(
            TAG,
            "TwelveSportGbGrpcCollaborationUtils::unbindDeferred: sendFinish=$sendFinish, hasForceJoined=$hasForceJoinedDeferred"
        )
        cancelDeferredQueryTimeout()
        isWaitingForDeferredQuery = false
        if (sendFinish || hasForceJoinedDeferred) {
            finishCollaboration()
        }
        currentSportId?.let {
            GrpcManager.unregisterHandler(it)
        }
        hasForceJoinedDeferred = false
        clearDeferredCallbacks()
    }

    private var deferredOnReadyToJoin: (() -> Unit)? = null
    private var deferredOnCancel: (() -> Unit)? = null
    private var deferredOnFail: ((Boolean, Boolean) -> Unit)? = null

    private fun clearDeferredCallbacks() {
        deferredOnReadyToJoin = null
        deferredOnCancel = null
        deferredOnFail = null
    }

    private fun handleDeferredQueryResult(activity: AppCompatActivity) {
        val isLeftValid = leftSubStatus == CollaborationStatusEnum.JOINABLE
        val isRightValid = rightSubStatus == CollaborationStatusEnum.JOINABLE
        leftSubStatus = null
        rightSubStatus = null
        if (isLeftValid && isRightValid) {
            Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::queryThenDecide: both JOINABLE")
            val ready = deferredOnReadyToJoin
            clearDeferredCallbacks()
            ready?.invoke()
            return
        }
        Log.i(TAG, "TwelveSportGbGrpcCollaborationUtils::queryThenDecide: occupied, show dialog")
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed || activity.supportFragmentManager.isStateSaved) {
                clearDeferredCallbacks()
                return@runOnUiThread
            }
            val existing = activity.supportFragmentManager.findFragmentByTag(DEFERRED_DIALOG_TAG)
            if (existing != null && existing.isAdded) {
                return@runOnUiThread
            }
            CommonTipDialog()
                .setContent(activity.getString(R.string.sub_screen_sport_ongoing_tip))
                .setCallback {
                    val ready = deferredOnReadyToJoin
                    clearDeferredCallbacks()
                    ready?.invoke()
                }
                .setCancelCallback {
                    val cancel = deferredOnCancel
                    clearDeferredCallbacks()
                    cancel?.invoke()
                }
                .show(activity.supportFragmentManager, DEFERRED_DIALOG_TAG)
        }
    }

    private fun cancelDeferredQueryTimeout() {
        deferredQueryTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        deferredQueryTimeoutRunnable = null
    }

    // ==================== Shared ====================

    private fun queryCollaborationStatus(resultCallback: ((Boolean, Boolean) -> Unit)?) {
        resetCollaborationState()
        startCollaborationStatusTimeout(resultCallback)
        sendCollaborationCommand(currentSportSkuId, leftScreenIp, CollaborationCmdEnum.QUERY)
        sendCollaborationCommand(currentSportSkuId, rightScreenIp, CollaborationCmdEnum.QUERY)
    }

    private fun resetCollaborationState() {
        hasNavigatedToSport = false
        isWaitingForCollaboration = true
        leftSubStatus = null
        rightSubStatus = null
    }

    private fun startCollaborationStatusTimeout(resultCallback: ((Boolean, Boolean) -> Unit)?) {
        cancelCollaborationStatusTimeout()
        collaborationStatusTimeoutRunnable = Runnable {
            collaborationStatusTimeoutRunnable = null
            if (!isWaitingForCollaboration) return@Runnable
            isWaitingForCollaboration = false
            val leftOk = leftSubStatus != null
            val rightOk = rightSubStatus != null
            Log.w(
                TAG,
                "TwelveSportGbGrpcCollaborationUtils::startCollaborationStatusTimeout: leftTimeout=${!leftOk}, rightTimeout=${!rightOk}"
            )
            resultCallback?.invoke(leftOk, rightOk)
        }
        mainHandler.postDelayed(collaborationStatusTimeoutRunnable!!, COLLABORATION_STATUS_TIMEOUT_MS)
    }

    private fun cancelCollaborationStatusTimeout() {
        collaborationStatusTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        collaborationStatusTimeoutRunnable = null
    }

    private fun forceIntoCollaboration(resultCallback: ((Boolean, Boolean) -> Unit)? = null) {
        val done = AtomicInteger(0)
        var leftOk = false
        var rightOk = false
        fun tryNotify() {
            if (done.incrementAndGet() == 2) {
                resultCallback?.invoke(leftOk, rightOk)
            }
        }
        sendCollaborationCommand(
            currentSportSkuId, leftScreenIp, CollaborationCmdEnum.FORCE_JOIN, true,
            onSuccess = {
                leftOk = true
                tryNotify()
            },
            onFailure = { _, _ ->
                leftOk = false
                tryNotify()
            }
        )
        sendCollaborationCommand(
            currentSportSkuId, rightScreenIp, CollaborationCmdEnum.FORCE_JOIN, true,
            onSuccess = {
                rightOk = true
                tryNotify()
            },
            onFailure = { _, _ ->
                rightOk = false
                tryNotify()
            }
        )
    }

    private fun finishCollaboration() {
        sendCollaborationCommand(currentSportSkuId, leftScreenIp, CollaborationCmdEnum.FINISH)
        sendCollaborationCommand(currentSportSkuId, rightScreenIp, CollaborationCmdEnum.FINISH)
    }

    private fun sendCollaborationCommand(
        sportSkuId: String,
        clientIp: String,
        cmd: CollaborationCmdEnum,
        ack: Boolean = true,
        onSuccess: ((Long) -> Unit)? = null,
        onFailure: ((Long, String) -> Unit)? = null
    ) {
        val payloadJsonStr = GrpcCollaborationSportInfo(
            cmd = cmd.ordinal,
            sportSkuId = sportSkuId,
            testCount = TEST_COUNT,
            isDebug = BusiBaseConfig.getDebugEnable(),
            skeletonEnable = BusiBaseConfig.getSkeletonEnable()
        ).toJson()

        Log.i(
            TAG,
            "TwelveSportGbGrpcCollaborationUtils::sendCollaborationCommand: $clientIp, payload: $payloadJsonStr"
        )

        GrpcManager.sendToClientByIp(
            clientIp,
            GrpcCmdCode.CMD_COLLABORATION_SPORT,
            payloadJsonStr,
            ack,
            onSuccess,
            onFailure
        )
    }
}
