package com.ai.test

import android.os.Bundle
import com.adl.base.speech.AdlTextSpeaker
import com.ty.tybusbase.config.UINavConfig
import com.ty.bus.multi.core.UIMultiNavigation
import com.ty.bus.multi.core.UIMultiNavigation.goFiveSkipRope
import com.ty.bus.multi.core.UIMultiNavigation.goTenSportRope
import com.ty.bus.multi.core.UIMultiNavigation.goThreeSitReach
import com.ty.bus.multi.demo.databinding.AcMainBinding
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel

class MainActivity : BaseActivity<AcMainBinding, BaseViewModel>() {
    override fun initObserver() {
    }

    override fun initData() {
        AdlTextSpeaker.init(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.bnt0.setOnClickListener { goFiveSkipRope(this, UINavConfig()) }

        mBinding.bnt11.setOnClickListener { goTenSportRope(this, UINavConfig()) }

        mBinding.bnt10.setOnClickListener { goThreeSitReach(this, UINavConfig()) }

        mBinding.btn10RegionSet.setOnClickListener { UIMultiNavigation.goMultiRegionSetting(this,10) }
    }
}
