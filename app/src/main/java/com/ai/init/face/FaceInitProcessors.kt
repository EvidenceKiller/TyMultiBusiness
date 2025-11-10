package com.ai.init.face

import com.ai.init.InitProcessors

/**
 * @author zhouwenhua
 * @date 2023/9/19
 * @describe  人员信息获取与人脸处理相关任务队列处理类
 */
class FaceInitProcessors : InitProcessors(3) {
    init {
        reload()
    }

    fun reload() {
        //按照顺序添加
        //先网络获取人脸库
        addToProcessors(FaceGetProcessor())
        //再初始化人脸sdk
        addToProcessors(FaceSdkInitProcessor())
        //最后再人脸特征入库
        addToProcessors(FaceLoadProcessor())
    }

    /**
     * 改变网络获取人员信息更新模式
     * @param getAll Boolean 是否是全量更新
     */
    fun changeFaceGetMode(getAll: Boolean): FaceInitProcessors {
        if (list.isEmpty()) return this
        (list[0] as FaceGetProcessor).isGetAll = getAll
        return this
    }

    /**
     * 网络获取人员信息失败时候是否会停止顺序向下执行
     * @param isFailedContinue Boolean 是否失败之后继续执行，默认是true
     * @return FaceInitProcessors
     */
    fun changeFaceGetNecessary(isFailedContinue: Boolean): FaceInitProcessors {
        if (list.isEmpty()) return this
        (list[0] as FaceGetProcessor).isFailedContinue = isFailedContinue
        return this
    }

    /**
     * 停止网络获取人员信息
     * @return FaceInitProcessors
     */
    fun stop(): FaceInitProcessors {
        if (list.isEmpty()) return this
        list.forEach { it.stop() }
        return this
    }

    /**
     * 获取当前处理队列数
     * @return Int 队列数
     */
    fun getListSize(): Int = list.size
}