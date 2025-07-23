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
