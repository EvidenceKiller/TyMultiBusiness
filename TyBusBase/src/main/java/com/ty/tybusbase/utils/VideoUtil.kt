package com.ty.tybusbase.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit

object VideoUtil {
    fun getVideoThumbnail(filePath: String): Bitmap? {
        var bitmap: Bitmap? = null;
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(1000));
        } catch (e: Exception) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (e: RuntimeException) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}