package com.ai.ui.init

import android.app.Application
import com.ai.init.InitManager
import com.ai.init.InitProcessor
import com.ty.tybusbase.base.BaseLiveData
import com.ty.tybusbase.base.BaseViewModel

class InitViewModel(application: Application):BaseViewModel(application) {
    var bindLiveData: BaseLiveData<Boolean> = BaseLiveData()
    var tipsData: BaseLiveData<String> = BaseLiveData()

    var progressData: BaseLiveData<Int> = BaseLiveData()
    /**
     * 初始化人脸识别相关数据与sdk
     */
    fun startInit() {
        InitManager.faceInitProcessors.changeFaceGetMode(false)
            .initAll(object : InitProcessor.InitProcessorListener{
                override fun progressCallBack(index: Int, isOk: Boolean, msg: String) {
                    tipsData.value = msg
                    progressData.postValue(1)
                }

                override fun completeCallBack(isAllOk: Boolean) {
                    //不管成不成功都会返回，因为到设置页面可以重新加载初始化
                    bindLiveData.setValue(true)
                }
            })

    }

    override fun onDestroy() {
        super.onDestroy()
        InitManager.faceInitProcessors.removeListener()
    }
}