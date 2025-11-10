package com.ai.ui.init

import android.os.Bundle
import com.adl.base.activity.NextWriter
import com.adl.service.outer.AdlService
import com.adl.service.outer.UserCaller
import com.ai.init.InitManager
import com.ai.test.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ty.bus.multi.demo.databinding.ActivityInitBinding
import com.ty.tybusbase.base.BaseActivity

class InitActivity : BaseActivity<ActivityInitBinding, InitViewModel>() {

    private var a = 0
    var startTime: Long = 0
    var endTime: Long = 0
    private var processCount = 0
    override fun initObserver() {
        viewModel.tipsData.observe(this) { msg -> mBinding.tvInit.text = "应用初始化中...$msg" }
        viewModel.progressData.observe(this) { index ->
            val afterProgress: Int = mBinding.seekBar.progress + index
            mBinding.seekBar.setProgress(afterProgress, true)
            mBinding.seekBar.setSecondaryProgress(afterProgress)
        }
        mBinding.logoClick.setOnClickListener {
            synchronized(this@InitActivity) {
                if (a == 0) {
                    startTime = System.currentTimeMillis()
                }
                if (a < 9) {
                    a++
                } else {
                    endTime = System.currentTimeMillis()
                    if (endTime - startTime <= 10000) {
                        a = 0
                        val ft =
                            supportFragmentManager.beginTransaction()
                        val fragment =
                            supportFragmentManager.findFragmentByTag("out_dialog")
                        if (null != fragment) {
                            ft.remove(fragment)
                        }
                        finish()
                    } else {
                        a = 0
                    }
                }
            }
        }

        // 人脸处理

        // 人脸处理
        viewModel.bindLiveData.observe(this) {
            processCount++
            nextActivity()
        }

    }

    override fun initData() {
        //设定最大值，人脸相关初始化处理队列加上一个算法模型处理任务
        //设定最大值，人脸相关初始化处理队列加上一个算法模型处理任务
        mBinding.seekBar.setMax(InitManager.faceInitProcessors.getListSize() + 1)
        //初始化人脸识别相关数据与sdk
        viewModel.startInit()
    }

    override fun initView(savedInstanceState: Bundle?) {
//加载Logo
        val org = UserCaller.instance().currentOrg
        if (org != null) {
            Glide.with(this)
                .load("${AdlService.getService().facePrefix}${org.orglogo}")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mBinding.logoClick)
        }
        mBinding.seekBar.isEnabled = false
    }

    private fun nextActivity() {
        if (processCount < 2) return
        // 跳转到首页
        NextWriter.with(this, MainActivity::class.java).closeCurrent(true).go()
        finish()
    }

}