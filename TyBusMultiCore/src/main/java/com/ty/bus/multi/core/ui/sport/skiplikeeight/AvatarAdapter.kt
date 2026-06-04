package com.ty.bus.multi.core.ui.sport.skiplikeeight

import android.annotation.SuppressLint
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.databinding.ItemAvatarBinding
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.VBViewHolder

class AvatarAdapter(data: MutableList<AvatarItem>? = null) :
    BaseBindingAdapter<ItemAvatarBinding, AvatarItem>(data) {

    @SuppressLint("SetTextI18n")
    override fun convert(holder: VBViewHolder<ItemAvatarBinding>, item: AvatarItem) {
        if (item.showMore) {
            holder.vb.ivAvatar.visibility = android.view.View.GONE
            holder.vb.tvMore.visibility = android.view.View.VISIBLE
            holder.vb.tvMore.text = "+${item.moreCount}"
        } else {
            holder.vb.ivAvatar.visibility = android.view.View.VISIBLE
            holder.vb.tvMore.visibility = android.view.View.GONE
            Glide.with(holder.vb.root)
                .load(item.url)
                .placeholder(R.mipmap.ic_head_default)
                .error(R.mipmap.ic_head_default)
                .circleCrop()
                .into(holder.vb.ivAvatar)
        }
    }
}
