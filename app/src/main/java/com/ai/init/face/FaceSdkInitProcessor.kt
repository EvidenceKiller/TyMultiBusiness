package com.ai.init.face

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.ai.init.InitProcessor
import com.google.gson.Gson
import com.ty.identifty.stface.StParameter
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.utils.ApplicationUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

/**
 * @author zhouwenhua
 * @date 2023/9/18
 * @describe 人脸sdk初始化处理类
 */
class FaceSdkInitProcessor : InitProcessor() {
    init {
        isFailedContinue = true
    }

    private val faceCompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var retryCount = 1 //重试次数

    private var retryDownloadFileCount = 5 // 重试下载授权文件次数

    private var mHandler = Handler(Looper.getMainLooper())

    @SuppressLint("CheckResult")
    override fun apply(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        //先判断有没有成功过
        if (FaceDetectorHelper.getInstance().isInitOk()) {
            initStatus = true
            callBack(true, true, "人脸初始化成功")
            return
        }

        //先检查signcode是否存在，存在则使用本地的,不存在则拉取服务端的鉴权配置
        //
        val myJson = getJson(ApplicationUtil.getContext(), "param.json")
        val gson = Gson()
        val mStParameter =
            gson.fromJson(myJson, StParameter::class.java)
        val basePath: String = mStParameter.params.common.basePath
        val licenseSignCodeFile = "$basePath${mStParameter.params.common.licenseSignCode}"
        val newLicSignCodeFile = "$basePath${mStParameter.params.common.newLicenseSignCode}"
        val file = File(licenseSignCodeFile)
        val newFile = File(newLicSignCodeFile)

        //直接初始化人脸
        initFace(basePath, callBack)
    }

    private fun initFace(
        basePath: String,
        callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit,
        uploadCallback: () -> Unit = {}
    ) {
        FaceDetectorHelper.getInstance().init(ApplicationUtil.getContext()) {
            if (it) {
                initStatus = true
                callBack(true, true, "人脸引擎初始化成功")
                uploadCallback.invoke()
            } else {
                //删除，facelic文件重试
                if (retryCount > 0) {
                    retryCount--
                    deleteFacelic(basePath)
                    apply(callBack)
                } else {
                    initStatus = false
                    callBack(false, true, "人脸引擎初始化失败")
                }
            }
        }
    }

    private fun deleteFacelic(basePath: String) {
        val file = File(basePath)
        file.deleteRecursively()
    }

    private fun getJson(context: Context, fileName: String): String? {
        val stringBuilder = StringBuilder()
        val assetManager = context.assets
        try {
            val bufferedReader =
                BufferedReader(InputStreamReader(assetManager.open(fileName), "utf-8"))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            bufferedReader.close()
        } catch (var7: IOException) {
            var7.printStackTrace()
        }
        return stringBuilder.toString()
    }

    override fun stop() {
        faceCompositeDisposable.dispose()
        faceCompositeDisposable.clear()
        mHandler.removeCallbacksAndMessages(null)
    }

}