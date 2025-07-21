package com.ty.tybusbase.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

open class BaseViewModel(application: Application) : AndroidViewModel(application), IBaseViewModel {

    var showDialogFlag: BaseLiveData<Boolean> = BaseLiveData()

    val mBaseUcLiveData: BaseUiChangeLiveData by lazy { BaseUiChangeLiveData() }

    override fun onAny(owner: LifecycleOwner?, event: Lifecycle.Event?) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    override fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun onPause() {
    }

    open fun showEmpty() {
        mBaseUcLiveData.showEmptyCallBackEvent.postValue(null)
    }

    open fun showLoading(msg:String? = ""){
        mBaseUcLiveData.showLoadingCallBackEvent.postValue(msg)
    }

    open fun hideLoading(){
        mBaseUcLiveData.disLoadingCallBackEvent.postValue(null)
    }
}