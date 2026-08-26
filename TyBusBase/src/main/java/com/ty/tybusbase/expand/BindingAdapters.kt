package com.ty.tybusbase.expand

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.ty.tybusbase.R

/**
 * 自定义BindingAdapter，用于处理图片资源的绑定
 */
@BindingAdapter("android:src")
fun setImageResource(imageView: ImageView, resId: Int) {
    imageView.setImageResource(resId)
}

@BindingAdapter("android:glideSrc")
fun setImageResourceWithGlide(imageView: ImageView, resId: String?) {
    Glide.with(imageView.context).load(resId).into(imageView)
}

@BindingAdapter("android:glideHeader")
fun setImageHeaderWithGlide(imageView: ImageView, resId: String?) {
    Glide.with(imageView.context).load(resId)
        .error(R.mipmap.ic_head_default)
        .into(imageView)
}

/**
 * 自定义BindingAdapter，用于处理LiveData<Integer>到ImageView的src属性的绑定
 */
@BindingAdapter("android:src")
fun setImageResourceFromLiveData(imageView: ImageView, liveData: LiveData<Int>?) {
    liveData?.value?.let {
        imageView.setImageResource(it)
    }
} 