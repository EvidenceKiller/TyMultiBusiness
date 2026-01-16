package com.ty.bus.multi.core.dialog.identify

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.adl.service.web.SportPlanService
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.expand.Action1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IdentifyViewModel(application: Application) : BaseViewModel(application) {


    /**
     * 检查学生是否在计划内
     */
    fun checkStudent(accountId: String, planId: String?, callback: Action1<Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (planId == null) {
                withContext(Dispatchers.Main) {
                    callback.invoke(true)
                }
            } else {
                if (accountId == BusiBaseConstantsValue.VISITOR_USER_ID) {
                    withContext(Dispatchers.Main) {
                        callback.invoke(false)
                    }
                } else {
                    //掉接口判断学生是否在计划内
                    val result = SportPlanService.checkStudent(planId, accountId)
                    withContext(Dispatchers.Main) {
                        callback.invoke(result)
                    }
                }
            }
        }
    }


}