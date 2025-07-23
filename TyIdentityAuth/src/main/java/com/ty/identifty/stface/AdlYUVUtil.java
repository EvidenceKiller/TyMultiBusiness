package com.ty.identifty.stface;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * ProjectName: SportCamera
 * Author     : 南山
 * Date       : 2023/2/26
 * Describe   : Yuv 工具
 */
public final class AdlYUVUtil {

    // nv21 镜像处理
    public static void nv21Mirror(byte[] nv21, int width, int height) {

        int i;
        int index;
        byte temp;
        int a, b;

        //mirror y
        for (i = 0; i < height; i++) {
            a = i * width;
            b = (i + 1) * width - 1;
            while (a < b) {
                temp = nv21[a];
                nv21[a] = nv21[b];
                nv21[b] = temp;
                a++;
                b--;
            }
        }

        // mirror u and v
        index = width * height;
        for (i = 0; i < height / 2; i++) {
            a = i * width;
            b = (i + 1) * width - 2;
            while (a < b) {
                temp = nv21[a + index];
                nv21[a + index] = nv21[b + index];
                nv21[b + index] = temp;

                temp = nv21[a + index + 1];
                nv21[a + index + 1] = nv21[b + index + 1];
                nv21[b + index + 1] = temp;
                a += 2;
                b -= 2;
            }
        }
    }

    public static void nv21ToRGB(int[] rgba, byte[] nv21, int width, int height) {

        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) nv21[yp])) - 16;

                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & nv21[uvp++]) - 128;
                    u = (0xff & nv21[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                //int b = (y1192 + 1634 * v);
                //int g = (y1192 - 833 * v - 400 * u);
                //int r = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;

                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;

                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // A|B|G|R
                rgba[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static void nv12ToRGB(int[] rgba, byte[] nv21, int width, int height) {

        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) nv21[yp])) - 16;

                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & nv21[uvp++]) - 128;
                    u = (0xff & nv21[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                //int r = (y1192 + 1634 * v);
                //int g = (y1192 - 833 * v - 400 * u);
                //int b = (y1192 + 2066 * u);
                int b = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int r = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;

                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;

                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // A|B|G|R
                rgba[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static void YV12toNV21(byte[] input, byte[] output, int width, int height) {

        int frameSize = width * height;
        int qFrameSize = frameSize / 4;
        int tempFrameSize = frameSize * 5 / 4;
        // Y
        System.arraycopy(input, 0, output, 0, frameSize);
        for (int i = 0; i < qFrameSize; i++) {
            // Cb (U)
            output[frameSize + i * 2] = input[frameSize + i];
            // Cr (V)
            output[frameSize + i * 2 + 1] = input[tempFrameSize + i];
        }
    }

    // YCbCr(海康输出YV12) 裁剪
    public static byte[] cropYCbCr(byte[] yv12, int width, int height,
                                   int startX, int startY, int cropWidth, int cropHeight) {

        int yLength = width * height;
        int crCbLength = yLength / 4;
        byte[] yData = new byte[yLength];
        byte[] crData = new byte[crCbLength];
        byte[] cbData = new byte[crCbLength];

        // 分离 Y, Cr, Cb 通道数据
        System.arraycopy(yv12, 0, yData, 0, yLength);
        System.arraycopy(yv12, yLength, crData, 0, crCbLength);
        System.arraycopy(yv12, yLength + crCbLength, cbData, 0, crCbLength);

        // 裁剪 Y, Cr, Cb 通道
        // 裁剪Y
        byte[] croppedY = new byte[cropWidth * cropHeight];
        int startIndex = startY * width + startX;
        for (int y = 0; y < cropHeight; y++) {
            System.arraycopy(yData, startIndex + y * width, croppedY, y * cropWidth, cropWidth);
        }

        // 裁剪Cr
        int crWidth = width / 2;
        int crHeight = height / 2;
        int xCrStart = startX / 2;
        int yCrStart = startY / 2;
        if (xCrStart < 0 || yCrStart < 0 || xCrStart + cropWidth / 2 > crWidth || yCrStart + cropHeight / 2 > crHeight) {
            return null;
        }
        byte[] croppedCr = new byte[(cropWidth / 2) * (cropHeight / 2)];
        int startCrIndex = yCrStart * crWidth + xCrStart;
        for (int y = 0; y < cropHeight / 2; y++) {
            System.arraycopy(crData, startCrIndex + y * crWidth, croppedCr, y * (cropWidth / 2), cropWidth / 2);
        }

        // 裁剪Cn
        int cbWidth = width / 2;
        int cbHeight = height / 2;
        int xCbStart = startX / 2;
        int yCbStart = startY / 2;
        if (xCbStart < 0 || yCbStart < 0 || xCbStart + cropWidth / 2 > cbWidth || yCbStart + cropHeight / 2 > cbHeight) {
            return null;
        }
        byte[] croppedCb = new byte[(cropWidth / 2) * (cropHeight / 2)];
        int startCbIndex = yCbStart * cbWidth + xCbStart;
        for (int y = 0; y < cropHeight / 2; y++) {
            System.arraycopy(cbData, startCbIndex + y * cbWidth, croppedCb, y * (cropWidth / 2), cropWidth / 2);
        }

        // 组合裁剪后的 YCrCb 数据
        int yLength2 = croppedY.length;
        int crCbLength2 = croppedCr.length;
        byte[] combined = new byte[yLength2 + 2 * crCbLength2];
        System.arraycopy(croppedY, 0, combined, 0, yLength2);
        System.arraycopy(croppedCr, 0, combined, yLength2, crCbLength2);
        System.arraycopy(croppedCb, 0, combined, yLength2 + crCbLength2, crCbLength2);

        return combined;
    }

    // 针对海康Yv12数据输出的转换
    public static byte[] cropYCbCrToNv21(byte[] yv12, int width, int height,
                                         int startX, int startY, int cropWidth, int cropHeight) {

        byte[] newYv12 = cropYCbCr(yv12, width, height, startX, startY, cropWidth, cropHeight);
        if (newYv12 != null) {
            byte[] nv21 = new byte[cropWidth * cropHeight * 3 / 2];
            YV12toNV21(newYv12, nv21, cropWidth, cropHeight);
            return nv21;
        }

        return null;
    }

    private static void nv21ToNv12(byte[] nv21, byte[] nv12, int width, int height) {

        int frameSize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, frameSize);

        for (i = 0; i < frameSize; i++) {
            nv12[i] = nv21[i];
        }

        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j - 1] = nv21[j + frameSize];
        }

        for (j = 0; j < frameSize / 2; j += 2) {
            nv12[frameSize + j] = nv21[j + frameSize - 1];
        }
    }

    // rgb to yuv
    public static void rgb2YCbCr420(int[] pixels, int width, int height, byte[] yuv) {

        int l = yuv.length;
        int len = width * height;
        // yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
        //byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                // 像素的颜色顺序为bgr，移位运算。
                int r = (rgb >> 0) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                // 套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                // 调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                // 赋值
                int indexY = i * width + j;
                int indexU = len + (i >> 1) * width + (j & ~1) + 0;
                int indexV = len + +(i >> 1) * width + (j & ~1) + 1;

                if (indexY < l) yuv[indexY] = (byte) y;
                if (indexU < l) yuv[indexU] = (byte) u;
                if (indexV < l) yuv[indexV] = (byte) v;
            }
        }
    }

    ////////////////////////////// rgb byte to rgb int //////////////////////////////
    public static void rgbByteToRgbInt(byte[] bytes, int width, int height, int[] rgba) {
        for (int i = 0, length = width * height; i < length; i++) {
            int offset1 = i * 3;
            byte b1 = bytes[offset1 + 0];
            byte b2 = bytes[offset1 + 1];
            byte b3 = bytes[offset1 + 2];
            int offset2 = i * 4;
            rgba[offset2 + 0] = b1;
            rgba[offset2 + 1] = b2;
            rgba[offset2 + 2] = b3;
            rgba[offset2 + 3] = (byte) 255;
        }
    }

    ////////////////////////////// yuv 区域拷贝 ////////////////////////////
    public static byte[] cropNV21(byte[] src, int srcWidth, int srcHeight,
                                  int cropX, int cropY, int cropWidth, int cropHeight) {
        return cropNV21ByJava(src, srcWidth, srcHeight,
                cropX, cropY, cropWidth, cropHeight, new byte[cropWidth * cropHeight * 3 / 2]);
    }

    public static byte[] cropNV21ByJava(byte[] src, int srcWidth, int srcHeight,
                                        int cropX, int cropY, int cropWidth, int cropHeight) {
        return cropNV21ByJava(src, srcWidth, srcHeight,
                cropX, cropY, cropWidth, cropHeight, new byte[cropWidth * cropHeight * 3 / 2]);
    }

    // yuv 区域拷贝
    public static byte[] cropNV21ByJava(byte[] src, int srcWidth, int srcHeight,
                                        int cropX, int cropY, int cropWidth, int cropHeight, byte[] out) {

        if (src.length < srcWidth * srcHeight * 3 / 2) {
            return null;
        }

        if (cropWidth + cropX > srcWidth
                || cropHeight + cropY > srcHeight) {
            return null;
        }

        if (cropWidth == srcWidth && cropHeight == srcHeight
                && cropX == 0 && cropY == 0) {
            return src;
        }

        // 确保为偶数
        cropWidth &= ~1;
        cropHeight &= ~1;
        cropX &= ~1;
        cropY &= ~1;

        int bottom = cropY + cropHeight;

        // 先复制Y数据
        for (int i = cropY; i < bottom; i++) {
            System.arraycopy(src, cropX + i * srcWidth, out,
                    (i - cropY) * cropWidth, cropWidth);
        }

        // 复制UV数据
        int startH = srcHeight + cropY / 2;
        int endH = srcHeight + bottom / 2;
        for (int i = startH; i < endH; i++) {
            System.arraycopy(src,
                    cropX + i * srcWidth,
                    out,
                    (i - startH + cropHeight) * cropWidth,
                    cropWidth);
        }

        return out;
    }

    ////////////////////////////// 保存YUV //////////////////////////////
    public static void saveYuv(byte[] yuv, File file) {
        FileOutputStream stream = null;
        try {
            file.createNewFile();
            stream = new FileOutputStream(file);
            stream.write(yuv);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(stream);
        }
    }

    // 读YUV
    public static byte[] readYuv(File dir, String fileName) {
        FileInputStream stream = null;
        try {
            File file = new File(dir, fileName);
            stream = new FileInputStream(file);
            int length = stream.available();
            byte[] yuv = new byte[length];
            stream.read(yuv);
            stream.close();
            return yuv;
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            close(stream);
        }
        return null;
    }

    // 保存图片
    public static void saveYuvToImageFile(File rootDir, byte[] yuv, int width, int height,
                                          String key) {

        FileOutputStream stream = null;
        try {
            YuvImage image = new YuvImage(yuv, ImageFormat.NV21, width, height, null);
            File file = new File(rootDir, key + ".jpg");

            stream = new FileOutputStream(file);
            if (image.compressToJpeg(new Rect(0, 0, width, height), 100, stream)) {
                stream.flush();
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(stream);
        }
    }

    // yuv to bitmap
    public static Bitmap yuvToBitmap(byte[] yuv, int width, int height) {
        if (yuv == null || width <= 0 || height <= 0) return null;
        int[] rgb = new int[width * height];
        return yuvToBitmap(yuv, rgb, width, height);
    }

    // yuv to bitmap
    public static Bitmap yuvToBitmap(byte[] yuv, int[] rgbBuffer, int width, int height) {
        if (yuv == null || width <= 0 || height <= 0) return null;
        yuv420ToRgbByJava(rgbBuffer, yuv, width, height);
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.setPixels(rgbBuffer, 0, width, 0, 0, width, height);
        return out;
    }

    // yuv420 -> rgb
    public static void yuv420ToRgbByJava(int[] rgba, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;

                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                //int r = (y1192 + 1634 * v);
                //int g = (y1192 - 833 * v - 400 * u);
                //int b = (y1192 + 2066 * u);
                int b = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int r = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;

                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;

                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // A|B|G|R
                rgba[yp] = 0xff000000 | ((b << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((r >> 10) & 0xff);
            }
        }
    }

    // yuv420 -> 旋转180度
    public static void yuvRotate180ByJava(byte[] src, byte[] des, int width, int height) {

        int n = 0;
        int uh = height >> 1;
        int wh = width * height;

        //copy y
        for (int j = height - 1; j >= 0; j--) {
            for (int i = width - 1; i >= 0; i--) {
                des[n++] = src[width * j + i];
            }
        }
        for (int j = uh - 1; j >= 0; j--) {
            for (int i = width - 1; i > 0; i -= 2) {
                des[n] = src[wh + width * j + i - 1];
                des[n + 1] = src[wh + width * j + i];
                n += 2;
            }
        }
    }

    // 顺时针旋转90
    public static void yuvRotate90ClockwiseByJava(byte[] src, byte[] dst, int srcWidth, int srcHeight) {

        int wh = srcWidth * srcHeight;
        int uvHeight = srcHeight >> 1;
        //旋转Y
        int k = 0;
        for (int i = 0; i < srcWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < srcHeight; j++) {
                dst[k] = src[nPos + i];
                k++;
                nPos += srcWidth;
            }
        }

        for (int i = 0; i < srcWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                dst[k] = src[nPos + i];
                dst[k + 1] = src[nPos + i + 1];
                k += 2;
                nPos += srcWidth;
            }
        }
    }


    // 关闭流
    static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
    }
}
