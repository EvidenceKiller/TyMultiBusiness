package com.ty.identifty.stface;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public final class TyFaceUtil {

    // 像素缩放
    public static void scalePixels(int[] pixels, int width, int height,
                                   int[] out, float scale) {
        int nw = (int) (width * scale);
        int nh = (int) (height * scale);

        int max = nw * nh;
        int max2 = width * height;

        for (int i = 0; i < nw; ++i) {
            for (int j = 0; j < nh; ++j) {
                int ox = (int) (i / scale);
                int oy = (int) (j / scale);

                int pos = i * nw + j;
                int pos1 = ox * width + oy;
                if (pos >= max) pos = max - 1;
                if (pos1 >= max2) pos1 = max2 - 1;

                out[pos] = pixels[pos1];
            }
        }
    }

    // 拷贝指定像素
    public static void copyPixels(int[] srcPixels, int srcWidth, int srcHeight,
                                  int[] dstPixels, int dstX, int dstY, int dstWidth, int dstHeight) {
        for (int i = 0; i < dstHeight; i++) {
            int pos = dstX + (dstY + i) * srcWidth;
            int desPost = i * dstWidth;
            System.arraycopy(srcPixels, pos, dstPixels, desPost, dstWidth);
        }
    }

    public static Bitmap bgr2Bitmap(byte[] bytes, int width, int height) {
        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {

            int idx1 = i * 3;
            byte b1 = bytes[idx1 + 2];
            byte b2 = bytes[idx1 + 1];
            byte b3 = bytes[idx1 + 0];

            // set value
            int idx2 = i * 4;
            rgba[idx2 + 0] = b1;
            rgba[idx2 + 1] = b2;
            rgba[idx2 + 2] = b3;
            rgba[idx2 + 3] = (byte) 255;
        }

        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));

        return stitchBmp;
    }

    public static void yuv420ToRGB(int[] rgba, byte[] yuv420sp, int width, int height) {

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
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

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

    public static void yuv420ToRGBA(byte[] data, int width, int height, byte[] bytes) {
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                bytes[width * i * 4 + j * 4 + 0] = (byte) b;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) r;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
    }
}
