package com.ty.bus.multi.core.ui.zwtqqset

import android.os.Bundle
import com.adl.base.config.AdlUIConfig
import com.adl.base.overlay.OnRenderScene
import com.adl.sport.forwardflexion.config.FFlexionRoleConfig
import com.adl.sport.forwardflexion.processor.ForwardFlexionSportOverlay
import com.ty.bus.multi.core.constants.MultiCoreConstant
import com.ty.bus.multi.databinding.ActivityZwtqqSettingBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.utils.MMKVUtil


class ZwtqqSettingActivity : BaseActivity<ActivityZwtqqSettingBinding, BaseViewModel>() {

    private var testPeopleCount = 0
    private var testTimes = 0
    private var minPeopleCount = 1
    private var maxPeopleCount = 3

    private var minTestTimes = 1
    private var maxTestTimes = 3

    override fun initObserver() {

    }

    override fun initData() {
        testPeopleCount = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER,
            MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
        )
        testTimes = MMKVUtil.decodeInt(
            MultiCoreConstant.ZWTQQ_TEST_TIMES,
            MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
        )

        ForwardFlexionSportOverlay.previewWidth = 1920;
        ForwardFlexionSportOverlay.previewHeight = 1080;
    }

    override fun initView(savedInstanceState: Bundle?) {

        mBinding.tvNumber.text = testPeopleCount.toString()
        mBinding.tvTime.text = testTimes.toString()

        mBinding.llBack.setOnClickListener { finish() }

        mBinding.ivNumberMinus.setOnClickListener {
            if (testPeopleCount == minPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount--
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivNumberAdd.setOnClickListener {
            if (testPeopleCount == maxPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount++
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            updateOverlay()
        }

        mBinding.ivTimeMinus.setOnClickListener {
            if (testTimes == minTestTimes) {
                return@setOnClickListener
            }
            testTimes--
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
        }

        mBinding.ivTimeAdd.setOnClickListener {
            if (testTimes == maxTestTimes) {
                return@setOnClickListener
            }
            testTimes++
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
        }

        mBinding.tvReset.setOnClickListener {
            testPeopleCount = MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER_DEFAULT
            testTimes = MultiCoreConstant.ZWTQQ_TEST_TIMES_DEFAULT
            mBinding.tvNumber.text = testPeopleCount.toString()
            mBinding.tvTime.text = testTimes.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_TIMES, testTimes)
        }

        mBinding.tvConfirm.setOnClickListener {
            mBinding.overlay.saveConfig()
            finish()
        }
        initOverlay()
    }

    private fun initOverlay() {
        mBinding.overlay.setMarkerAreaNum(testPeopleCount)
        val uiConfig: FFlexionRoleConfig? = AdlUIConfig.instance()
            .readConfig(mBinding.overlay.configKey, FFlexionRoleConfig::class.java)
        mBinding.overlay.setConfig(uiConfig, OnRenderScene.StatusSportSet)
    }

    private fun updateOverlay() {
        mBinding.overlay.setMarkerAreaNum(testPeopleCount)
        mBinding.overlay.updateConfig()
        mBinding.overlay.refresh()
    }

}