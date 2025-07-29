package com.ty.tybusbase.view.whiteboard;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import androidx.annotation.NonNull;


import com.ty.tybusbase.R;
import com.ty.tybusbase.base.BaseBindingAdapter;
import com.ty.tybusbase.base.VBViewHolder;
import com.ty.tybusbase.databinding.LayoutPenColorBinding;

import java.util.List;

/**
 * Description: this is PenColorAdapter
 */
public class PenColorAdapter extends BaseBindingAdapter<LayoutPenColorBinding, String> {

    private String selectColor;

    public PenColorAdapter(List<String> datas) {
        super(datas);
    }

    public void setSelectColor(String selectColor){
        this.selectColor = selectColor;
        notifyDataSetChanged();
    }


    @Override
    protected void convert(@NonNull VBViewHolder<LayoutPenColorBinding> holder, String s) {
        holder.getVb().imgColor.setImageDrawable(new ColorDrawable(Color.parseColor(s)));
        if(s.equals(selectColor))holder.getView(R.id.img_select).setVisibility(View.VISIBLE);
        else holder.getView(R.id.img_select).setVisibility(View.GONE);
    }
}
