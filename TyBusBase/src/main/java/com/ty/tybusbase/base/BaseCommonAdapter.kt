package com.ty.tybusbase.base

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder


abstract class BaseCommonAdapter<T>(val resLayout:Int,data: MutableList<T>? = null) :
    BaseQuickAdapter<T, BaseViewHolder>(0, data) {

    //重写返回自定义 ViewHolder
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        //这里为了使用简洁性，使用反射来实例ViewBinding
        val view = LayoutInflater.from(parent.context).inflate(resLayout,parent,false)
        return BaseViewHolder(view)
    }
}