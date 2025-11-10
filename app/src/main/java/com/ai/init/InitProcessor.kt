package com.ai.init

/**
 * @author zhouwenhua
 * @date 2023/9/15
 * @describe 初始化处理基类
 */
abstract class InitProcessor {

    /**
     * 初始化状态，默认失败(可以不用上它)
     */
    var initStatus: Boolean = false

    /**
     * 处理类失败之后是否继续向下传递执行其他处理类工作
     */
    var isFailedContinue = true

    /**
     * 在处理列表中的索引默认-1是没有添加到处理列表中(可以不用上它)
     */
    var processorsIndex: Int = -1

    /**
     * 初始化执行方法
     * @param callBack (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit 初始化回调
     */
    abstract fun apply(callBack: (isOk: Boolean, isContinue: Boolean, msg: String) -> Unit)

    open fun stop(){}
    /**
     * 初始化进度监听
     */
    interface InitProcessorListener {
        /**
         * 初始化进度回调
         * @param index Int 当前任务列表索引
         * @param isOk Boolean 是否成功初始化
         * @param msg String 返回的信息
         */
        fun progressCallBack(index: Int, isOk: Boolean, msg: String)

        /**
         * 任务列表完成回调
         * @param isAllOk Boolean 是否都成功初始化了
         */
        fun completeCallBack(isAllOk: Boolean)
    }
}