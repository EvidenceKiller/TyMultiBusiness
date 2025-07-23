package com.ty.identifty.stface

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object SaveBitmapUtil {
    fun saveBitmap(yuv: ByteArray, width: Int, height: Int, bitmapDic: String, name: String) {
        try {
            val bm =
                AdlYUVUtil.yuvToBitmap(yuv, width, height)
            val fileDic = File(bitmapDic)
            if (!fileDic.exists()) {
                fileDic.mkdirs()
            }
            val out =
                FileOutputStream(File(fileDic, name))
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 将本地图片文件（如jpg/png）转为YUV字节数组
     * @param imagePath 图片文件路径
     * @return Triple<YUV字节数组, 宽, 高>，如失败返回null
     */
    fun imageFileToYuv(imagePath: String): Triple<ByteArray, Int, Int>? {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath) ?: return null
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val yuv = ByteArray(width * height * 3 / 2)
            AdlYUVUtil.rgb2YCbCr420(pixels, width, height, yuv)
            return Triple(yuv, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}