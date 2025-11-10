package com.ai.init.face

import androidx.camera.core.impl.utils.ContextUtil
import com.adl.service.common.RequestCallback
import com.adl.service.common.RequestResult
import com.adl.service.common.RequestUserCallback
import com.adl.service.outer.UserCaller
import com.ai.init.InitProcessor
import com.ty.tybusbase.utils.ApplicationUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * @author zhouwenhua
 * @date 2023/9/15
 * @describe 网络获取人脸库处理类
 */
class FaceGetProcessor : InitProcessor() {
    var isGetAll = true
    private val faceCompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun apply(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        getUserFace(callBack)
    }

    private fun getUserFace(netFaceCallBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        val userCallback = object : RequestUserCallback(ApplicationUtil.getContext(), true) {
            override fun onResult(result: RequestResult?) {
                //请求完毕后，获取一下学生数据，如果数据库没有数据，再请求一下全量数据
                //已存在BUG，更新数据后，把数据库数据清空了
                UserCaller.instance().queryAllStudentCount(object : RequestCallback() {
                    override fun onResult(result: RequestResult) {
                        val count = result.content
                        if (count is Int && count == 0) {
                            //请求全量数据
                            UserCaller.instance().syncAll(object :
                                RequestUserCallback(ApplicationUtil.getContext(), false) {
                                override fun onResult(result: RequestResult?) {
                                    netFaceCallBack(
                                        true,
                                        true,
                                        "获取用户数据成功"
                                    )
                                }

                                override fun nextStatus(status: Int, msg: String?) {

                                }

                            })
                        } else {
                            initStatus = true
                            netFaceCallBack(
                                true,
                                true,
                                "获取用户数据成功"
                            )
                        }
                    }
                })

            }

            override fun nextStatus(status: Int, msg: String?) {
            }

        }
        if (isGetAll) {
            UserCaller.instance().syncAll(userCallback)
        } else {
            UserCaller.instance()
                .syncBaseInfo(userCallback)
        }
    }

    /**
     * 停止网络获取
     */
    override fun stop() {
        faceCompositeDisposable.clear()
    }
}