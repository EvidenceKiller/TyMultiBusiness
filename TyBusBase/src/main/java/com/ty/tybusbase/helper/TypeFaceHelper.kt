package com.ty.tybusbase.helper

import android.content.Context
import android.graphics.Typeface

/**
 * @description: 字体设置
 * @author: chenwei
 * @date :   2022/3/9 9:56 下午
 */
class TypeFaceHelper {
    fun getYouSheBiaoTiHei(context: Context): Typeface {
        val typeface = Typeface.createFromAsset(context.resources.assets, "font/title_black.ttf")
        return typeface
    }

    fun getYouSheFont(context: Context): Typeface {
        return Typeface.createFromAsset(context.resources.assets, "font/you_she_biao_ti_hei.ttf")
    }

    companion object {
        @Volatile
        private var INSTANCE: TypeFaceHelper? = null

        val instance: TypeFaceHelper?
            get() {
                if (INSTANCE == null) {
                    synchronized(TypeFaceHelper::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = TypeFaceHelper()
                        }
                    }
                }
                return INSTANCE
            }
    }
}
