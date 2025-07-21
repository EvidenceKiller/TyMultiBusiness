package com.ty.tybusbase.base

import android.os.Bundle

interface IBaseView {
    fun initView(savedInstanceState: Bundle?)
    fun initData()
    fun initObserver()
    fun initLayoutId(): Int
    fun showLoading()
    fun dismissLoading()
}