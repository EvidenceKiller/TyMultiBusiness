package com.ty.bus.multi.core.ui.zwtqqset

import android.os.Bundle
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
        }

        mBinding.ivNumberAdd.setOnClickListener {
            if (testPeopleCount == maxPeopleCount) {
                return@setOnClickListener
            }
            testPeopleCount++
            mBinding.tvNumber.text = testPeopleCount.toString()
            MMKVUtil.encode(MultiCoreConstant.ZWTQQ_TEST_PEOPLE_NUMBER, testPeopleCount)
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

        }

    }

}