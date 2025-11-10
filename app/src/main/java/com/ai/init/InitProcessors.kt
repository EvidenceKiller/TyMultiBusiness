package com.ai.init

import com.adl.base.common.AdlHandlerThread
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author zhouwenhua
 * @date 2023/9/19
 * @describe 初始化任务列表处理基类
 */
abstract class InitProcessors(size: Int = 0) {
    protected val list =
        if (size <= 0) mutableListOf<InitProcessor>() else ArrayList(size)

    private var isAllOk = true
    protected var listener: InitProcessor.InitProcessorListener? = null

    /**
     * 是否是最后的任务
     * @param processorsIndex Int 当前执行到的任务索引
     * @return Boolean
     */
    private fun isFinalProcessor(processorsIndex: Int) = list.size == processorsIndex + 1

    /**
     * 按照顺序初始化所有列表中的任务
     * @param listener InitProcessorListener?
     */
    fun initAll(listener: InitProcessor.InitProcessorListener?) {
        init(0, true, listener)
    }

    /**
     * 并行分别执行所有列表中的任务
     * @param listener InitProcessorListener?
     */
    fun initAllParallel(listener: InitProcessor.InitProcessorListener?) {
        init(0, false, listener)
    }

    /**
     * 从特定任务开始顺序初始化
     * @param processorsIndex Int 特定任务索引
     * @param isSerial Boolean
     * @param listener InitProcessor.InitProcessorListener?
     */
    fun init(
        processorsIndex: Int,
        isSerial: Boolean,
        listener: InitProcessor.InitProcessorListener?
    ) {
        this.listener = listener
        if (isSerial) {
            doApplySerial(processorsIndex)
        } else {
            doApplyParallel(processorsIndex)
        }
    }

    /**
     * 只执行特定的一个初始化任务
     * @param processorsIndex Int
     * @param listener InitProcessorListener
     */
    fun initOnly(processorsIndex: Int, listener: InitProcessor.InitProcessorListener?) {
        this.listener = listener
        if (processorsIndex < list.size && processorsIndex >= 0) {
            list[processorsIndex].apply { isOk, _, msg ->
                AdlHandlerThread.getUIHandler().post {
                    //回调初始化信息
                    this.listener?.progressCallBack(processorsIndex, isOk, msg)
                    this.listener?.completeCallBack(isOk)
                }
            }
        }
    }

    /**
     * 按照list顺序串行执行
     * @param i Int 开始的索引
     */
    private fun doApplySerial(i: Int) {
        if (i < list.size && i >= 0) {
            list[i].apply { isOk, isContinue, msg ->
                //只要有一个初始化失败就标记一下
                if (!isOk) {
                    isAllOk = false
                }
                //回调在主线程中执行
                AdlHandlerThread.getUIHandler().post {
                    //回调初始化信息
                    listener?.progressCallBack(i, isOk, msg)
                }

                //是否继续初始化调用
                if (isContinue && (i + 1) < list.size) {
                    //在主线程中执行
                    AdlHandlerThread.getUIHandler().post {
                        //继续下一个初始化调用
                        doApplySerial(i + 1)
                    }
                    return@apply
                }
                if (isFinalProcessor(i)) {
                    AdlHandlerThread.getUIHandler().post {
                        //回调结束信息
                        listener?.completeCallBack(isAllOk)
                    }
                    //结束就重置参数
                    isAllOk = true
                }
            }
        }
    }

    /**
     * 并行执行(前提是添加的processor处理都在非主线程)
     * @param index Int 开始的索引
     */
    private fun doApplyParallel(index: Int) {
        if (index < list.size && index >= 0) {
            val proIndex = AtomicInteger(0)
            for (i in index until list.size) {
                if (i < list.size) {
                    list[i].apply { isOk, _, msg ->
                        //只要有一个初始化失败就标记一下
                        if (!isOk) {
                            isAllOk = false
                        }
                        proIndex.addAndGet(1)
                        //回调在主线程中执行
                        AdlHandlerThread.getUIHandler().post {
                            //回调初始化信息
                            listener?.progressCallBack(i, isOk, msg)
                        }
                        if (proIndex.get() >= list.size) {
                            AdlHandlerThread.getUIHandler().post {
                                //回调结束信息
                                listener?.completeCallBack(isAllOk)
                            }
                            //结束就重置参数
                            isAllOk = true
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加初始化处理类
     * @param processor InitProcessor 初始化处理类
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun addToProcessors(processor: InitProcessor) {
        list.add(processor)
        //添加索引
        processor.processorsIndex = list.size - 1
    }

    /**
     * 移除监听
     */
    fun removeListener() {
        listener = null
    }

    /**
     * 移除所有任务队列
     */
    fun clear() {
        isAllOk = true
        list.clear()
    }
}