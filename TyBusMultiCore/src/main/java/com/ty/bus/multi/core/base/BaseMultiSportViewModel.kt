package com.ty.bus.multi.core.base

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adl.auth.core.AdlAuthFactory
import com.adl.base.log.AdlLogger
import com.adl.service.common.IDefine
import com.adl.service.entity.BestRankTopEntity
import com.adl.service.outer.AdlService
import com.adl.service.outer.UserCaller
import com.adl.service.web.SportStatisticsService
import com.aipower.modulecommon.expand.getString
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseSportViewModel
import com.ty.tybusbase.constants.ConstantsValue
import com.ty.tybusbase.helper.TimeOutHelper
import com.ty.tybusbase.utils.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseMultiSportViewModel(application: Application) : BaseSportViewModel(application) {
    var matchId: String? = null
    val bestRankLD = MutableLiveData<BestRankTopEntity>()
    val timeOutLD = MutableLiveData<Boolean>()
    var measureUnit: String = getString(R.string.ge)


    val timeOutHelper = TimeOutHelper({
        ToastUtil.getInstance(
            getApplication(),
            getString(R.string.sport_time_out)
        ).show()
        timeOutLD.postValue(true)
    }, ConstantsValue.STARTED_TIME_OUT)

    override fun onCreate() {
        super.onCreate()
        sportInfo?.measureUnitCode?.let {
            try {
                measureUnit = AdlService.getService().dictDao.queryDictLabel(
                    IDefine.DirctSportUnit,
                    it
                )
            } catch (e: Exception) {
                AdlLogger.e(
                    "查询单位报错：sportName: ${sportInfo?.sportSkuName} unitCode: ${it} 错误日志：$e"
                )
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取今日最佳
     */
    fun getBestRankTop() {
        viewModelScope.launch(Dispatchers.IO) {
            val authFac = AdlAuthFactory.create(getApplication())
            val org = UserCaller.instance().currentOrg
            val bestRankTop = SportStatisticsService.bestRankTop(
                authFac.appCode,
                sportInfo?.sportSkuId,
                org?.orgId,
                null,
                null,
                null,
                null,
                null,
                0,
                1,
                1
            )
            if (!bestRankTop.isNullOrEmpty()) {
                bestRankLD.postValue(bestRankTop[0])
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        timeOutHelper.cancel()
    }
}