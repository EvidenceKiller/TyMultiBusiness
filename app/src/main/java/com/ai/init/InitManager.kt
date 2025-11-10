package com.ai.init

import com.ai.init.face.FaceInitProcessors


/**
 * @author zhouwenhua
 * @date 2023/9/15
 * @describe 初始化任务队列管理类
 */
object InitManager {
    /**
     * 人脸初始化任务处理队列
     */
    val faceInitProcessors = FaceInitProcessors()
        get() {
            //因为faceInitProcessors队列可能被清空，所以每次使用都要判断下是否清空，清空了就重新装载
            synchronized(field) {
                if (field.getListSize() == 0) {
                    field.reload()
                }
            }
            return field
        }


    /**
     * 移除所有回调监听
     */
    fun removeListener() {
        faceInitProcessors.removeListener()
    }

    /**
     * 清空初始化队列并移除监听
     */
    fun clear() {
        removeListener()
        faceInitProcessors.stop().clear()
    }

    /**
     * 重新装载初始化处理队列
     */
    fun reload() {
        clear()
        faceInitProcessors.reload()
    }
}