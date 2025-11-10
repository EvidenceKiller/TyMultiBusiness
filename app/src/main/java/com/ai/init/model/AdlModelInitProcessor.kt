package com.ai.init.model

import com.adl.base.file.AdlFileHelper
import com.adl.base.log.AdlLogger
import com.adl.mount.core.TsModelInfo
import com.adl.mount.core.TsModelRes
import com.adl.mount.core.TsModelResGroup
import com.adl.mount.core.TsModelResPool
import com.ai.init.InitProcessor
import com.ty.tybusbase.constants.ModelIdConstants
import com.ty.tybusbase.utils.DeviceUtil
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.RxUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.io.FileInputStream

/**
 * @author zhouwenhua
 * @date 2023/9/18
 * @describe 初始化模型文件处理类
 */
class AdlModelInitProcessor : InitProcessor() {
    var disposable: Disposable? = null

    override fun apply(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        disposable?.dispose()
        Observable.create { obser ->
            if (DeviceUtil.isTsRk3588()) {
                TsModelResPool.instance()
                    .loadModelGroup(
                        listOf(
                            TsModelResGroup(
                                false,
                                0L,
                                "_ts_normal_000"
                            ).addModelRes(TsModelRes(ModelIdConstants.COMMON_BODY_MODEL_ID, 2))
                                .addModelRes(TsModelRes(ModelIdConstants.COMMON_POSE_MODEL_ID, 3))
                        ), object :
                            TsModelResPool.OnLoadModelCallbackSample() {

                            override fun onStart() {
                                LogUtil.d("开始加载通用模型")
                            }

                            override fun onSucceed(list: MutableList<TsModelInfo>?) {
                                obser.onNext(true)
                                obser.onComplete()
                            }

                        })

            } else {
                obser.onNext(true)
                obser.onComplete()
            }
        }.compose(RxUtils.schedulersTransformer()).subscribe(object : Observer<Boolean> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onError(e: Throwable) {
                initStatus = false
                callBack(false, true, e.message.toString())
            }

            override fun onComplete() {
                disposable?.dispose()
                disposable = null
            }

            override fun onNext(t: Boolean) {
                initStatus = true
                AdlLogger.d("加载算法模型成功!!!!")
                try {
                    callBack(
                        true,
                        true,
                        "模型版本${
                            AdlFileHelper.readStream(
                                FileInputStream(File("/sdcard/com.adl.sport/deploy/version"))
                            )
                        }"
                    )
                } catch (e: Exception) {
                    callBack(
                        true,
                        true,
                        "模型加载成功"
                    )
                }
            }

        })
    }
}