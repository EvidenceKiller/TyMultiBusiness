package com.ai.init.face

import com.adl.service.common.RequestCallback
import com.adl.service.common.RequestResult
import com.adl.service.entity.StudentEntity
import com.adl.service.entity.TeacherEntity
import com.adl.service.outer.UserCaller
import com.ai.init.InitProcessor
import com.ty.tybusbase.face.FaceDetectorHelper

/**
 * @author zhouwenhua
 * @date 2023/9/18
 * @describe 加载人脸特征值处理类
 */
class FaceLoadProcessor : InitProcessor() {
    override fun apply(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        if (!FaceDetectorHelper.getInstance().isInitOk()) {
            callBack(false, true, "人脸未初始化")
            return
        }
        UserCaller.instance().queryAllStudentWithFaceData(object : RequestCallback() {
            override fun onResult(result: RequestResult) {
                val stList = result.content as? List<StudentEntity>
                if (stList.isNullOrEmpty()) {
                    initStatus = false
                    loadTeacherFace(callBack)
                    return
                }

                FaceDetectorHelper.getInstance().loadStudentFace(stList,true){
                    initStatus = true
                    loadTeacherFace(callBack)
                }
            }
        })
    }

    fun loadTeacherFace(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit) {
        UserCaller.instance().queryAllTeacherWithFaceData(object : RequestCallback() {
            override fun onResult(result: RequestResult) {
                val tList = result.content as? List<TeacherEntity>
                if (tList.isNullOrEmpty()) {
                    initStatus = false
                    callBack(false, true, "数据库未查询到特证值")
                    return
                }
                FaceDetectorHelper.getInstance().loadTeacherFace(tList,false){
                    initStatus = true
                    callBack(true, true, "特征入库完成")
                }
            }

        })
    }
}