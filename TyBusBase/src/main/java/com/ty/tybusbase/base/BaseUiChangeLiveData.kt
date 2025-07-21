package com.ty.tybusbase.base


/**
 * @description:
 * @author: chenwei
 * @date :   2021/8/2 11:27
 */
class BaseUiChangeLiveData : BaseLiveData<Any>() {
    val showDialogEvent: BaseLiveData<String> by lazy { BaseLiveData() }
    val dismissDialogEvent: BaseLiveData<Void> by lazy { BaseLiveData() }
    val startActivityEvent: BaseLiveData<Map<String, Any>> by lazy {
        BaseLiveData()
    }
    val startContainerActivityEvent: BaseLiveData<Map<String, Any>> by lazy {
        BaseLiveData()
    }
    val finishEvent: BaseLiveData<Void> by lazy { BaseLiveData() }
    val onBackPressedEvent: BaseLiveData<Void> by lazy { BaseLiveData() }

    /**
     * 缺省状态回调
     */
    val showLoadingCallBackEvent: BaseLiveData<String> by lazy { BaseLiveData() }
    val disLoadingCallBackEvent: BaseLiveData<Void> by lazy { BaseLiveData() }
    val showErrorCallBackEvent: BaseLiveData<Void> by lazy { BaseLiveData() }
    val showEmptyCallBackEvent: BaseLiveData<Void> by lazy { BaseLiveData() }
    val showTimeoutCallBack: BaseLiveData<Void> by lazy { BaseLiveData() }
    val showNoNetworkCallBack: BaseLiveData<Void> by lazy { BaseLiveData() }
    val showSuccessCallBack: BaseLiveData<Void> by lazy { BaseLiveData() }

}