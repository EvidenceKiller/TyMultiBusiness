package com.ai.ui.gesture

import android.annotation.SuppressLint
import android.os.Bundle
import com.adl.mount.impl.TsSingleProcessor
import com.adl.mount.impl.TsSportFrameSingle
import com.adl.processor.SportProcessorStatus
import com.ty.bus.multi.demo.databinding.ActivityGestureBinding
import com.ty.tybusbase.base.BaseSingleIdentifyActivity
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.gesture.GestureCommand
import com.ty.tybusbase.gesture.GestureListener
import com.ty.tybusbase.gesture.GestureType
import com.ty.tybusbase.gesture.SkeletonGestureHelper3
import com.ty.tybusbase.utils.LogUtil

class GestureActivity : BaseSingleIdentifyActivity<ActivityGestureBinding, BaseSportViewModel>() {
    private var leftCount = 0
    private var rightCount = 0
    private var handsUpCount = 0

    private val gestureHelper by lazy {
        SkeletonGestureHelper3(object : GestureListener {
            override fun onGesture(command: GestureCommand, type: GestureType) {
                gestureCallback(command, type)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    @Synchronized
    private fun gestureCallback(command: GestureCommand, type: GestureType) {
        LogUtil.d("gestureCallback: $command, $type")
        runOnUiThread {
            when (command) {
                GestureCommand.HANDS_UP -> {
                    handsUpCount++
                }

                GestureCommand.LEFT -> {
                    leftCount++
                }

                GestureCommand.RIGHT -> {
                    rightCount++
                }

                else -> {}
            }
            mBinding.tvLeft.text = "左滑次数：$leftCount"
            mBinding.tvRight.text = "右滑次数：$rightCount"
            mBinding.tvHandsUp.text = "举手次数：$handsUpCount"
        }
    }

    override fun createEuBase(): TsSingleProcessor {

        return object : TsSingleProcessor {
            override fun onAnalyzer(frame: TsSportFrameSingle?) {
                frame?.skeleton?.let { gestureHelper.updateSkeleton(it) }
            }

            override fun onInit() {
            }

            override fun onReset() {
            }

            override fun onRelease() {
            }

            override fun version(): String? = ""

        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        tsSportLifecycle.toSporting()
        processorSingle.changeTo(SportProcessorStatus.Sporting)
    }
}