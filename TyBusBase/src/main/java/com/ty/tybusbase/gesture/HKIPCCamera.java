package com.ty.tybusbase.gesture;

import android.os.Handler;
import android.os.Looper;

import com.adl.base.log.AdlLogger;
import com.hcnetsdk.control.DevManageGuider;
import com.hcnetsdk.control.SDKGuider;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO_V20;
import com.ty.tybusbase.constants.BusiBaseConstantsValue;
import com.ty.tybusbase.constants.MMKVBusiKey;
import com.ty.tybusbase.utils.MMKVUtil;

import org.MediaPlayer.PlayM4.Player;
import org.MediaPlayer.PlayM4.PlayerCallBack;

import java.text.SimpleDateFormat;

/**
 * 海康运动摄像头定义
 */
public final class HKIPCCamera {

    private static final String TAG = HKIPCCamera.class.getSimpleName();
    private String cameraIp;
    private DevManageGuider.DeviceItem mDeviceItem;

    private int mPreviewHandle = -1;
    private VideoDecoder mVideoDecoder;
    private OnCameraCallback mCameraCallback;
    private boolean isPause = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private long retryTime = 5 * 1000;

    static {
        HCNetSDKJNAInstance.getInstance();
    }

    private final OnCameraCallback innerCameraCallback = new OnCameraCallback() {
        @Override
        public void onSuccess(String ip) {
            if (mCameraCallback != null) {
                mCameraCallback.onSuccess(ip);
            }
        }

        @Override
        public void onFailed(String ip) {
            if (mCameraCallback != null) {
                mCameraCallback.onFailed(ip);
            }
            retryOpen();
        }

        @Override
        public void onFrame(String ip, byte[] yuv, int width, int height, long timestamp) {
            if (mCameraCallback != null) {
                mCameraCallback.onFrame(ip, yuv, width, height, timestamp);
            }
        }
    };

    public void setOnCameraCallback(OnCameraCallback callback) {
        mCameraCallback = callback;
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    private void retryOpen() {
        mHandler.removeCallbacksAndMessages(null);
        close();
        mHandler.postDelayed(this::open, retryTime);
    }

    public void open() {
        open(2);
    }

    // 打开摄像头流
    public void open(int streamType) {
        String ip =
                MMKVUtil.INSTANCE.decodeString(
                        MMKVBusiKey.HTTP_CAMERA_IP,
                        BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_IP
                );
        String account = MMKVUtil.INSTANCE.decodeString(
                MMKVBusiKey.HTTP_CAMERA_ACCOUNT,
                BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_ACCOUNT
        );
        String psw =
                MMKVUtil.INSTANCE.decodeString(
                        MMKVBusiKey.HTTP_CAMERA_PSW,
                        BusiBaseConstantsValue.HTTP_CAMERA_DEFAULT_PASSWORD
                );

        this.cameraIp = ip;

        if (mDeviceItem == null) {
            mDeviceItem = SDKGuider.g_sdkGuider.m_comDMGuider.new DeviceItem();
            mDeviceItem.m_szDevName = String.valueOf(System.currentTimeMillis());
            mDeviceItem.m_struNetInfo = SDKGuider.g_sdkGuider.m_comDMGuider.new DevNetInfo(
                    ip, String.valueOf(8000), account, psw);
        }

        // 打开IPC
        if (mDeviceItem.m_struDevState.m_iLogState == 0
                || mDeviceItem.m_struDevState.m_iLogState == 2) {
            new Thread(() -> {

                try {
                    if (!SDKGuider.g_sdkGuider.m_comDMGuider.login_v40_jna_with_deviceInfo(mDeviceItem)) {
                        logger("登录失败");
                        innerCameraCallback.onFailed(cameraIp);
                        return;
                    }

                    logger("登录成功: ip=" + ip + ", uid=" + mDeviceItem.m_lUserID);

                    // 开始预览
                    openPreview(streamType);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }

    public void pause() {
        isPause = true;
    }

    public void restart() {
        isPause = false;
    }

    private void openPreview(int streamType) {

        logger("打开摄像头...");

        if (mDeviceItem.m_lUserID < 0) {
            logger("无效用户id");
            innerCameraCallback.onFailed(cameraIp);
            return;
        }

        // 解码回调
        mVideoDecoder = new VideoDecoder();

        int userId = mDeviceItem.m_lUserID;
        NET_DVR_PREVIEWINFO_V20 struPlayInfo = new NET_DVR_PREVIEWINFO_V20();//页面底部有该参数的注释描述
        struPlayInfo.lChannel = 1;//通道号，目前设备模拟通道号从1开始，数字通道的起始通道号通过NET_DVR_GetDVRConfig（配置命令NET_DVR_GET_IPPARACFG_V40）获取（dwStartDChan
        struPlayInfo.dwStreamType = streamType; // 码流类型：0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        struPlayInfo.bBlocked = 1;//0- 非阻塞取流，1- 阻塞取流
        struPlayInfo.hHwnd = null;
        mPreviewHandle = SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlay_V40_jni(userId, struPlayInfo,
                null, (iHandle, dataType, buffer, bufferSize) -> {

                    if (mVideoDecoder != null) {
                        mVideoDecoder.input(iHandle, dataType, buffer, bufferSize);
                    }
                });

        if (mPreviewHandle < 0) {
            logger("预览失败");
            innerCameraCallback.onFailed(cameraIp);
            return;
        }
        // 打开成功
        innerCameraCallback.onSuccess(cameraIp);
    }

    public void close() {

        closePreview();

        // 退出登录
        if (mDeviceItem != null) {
            // logout
            if (mDeviceItem.m_struDevState.m_iLogState == 1) {
                if (SDKGuider.g_sdkGuider.m_comDMGuider.logout_jni_deviceInfo(mDeviceItem)) {
                    logger("退出登录成功");
                } else {
                    logger("退出登录失败!");
                }
            }
            mDeviceItem = null;
        }
    }

    private void closePreview() {
        logger("关闭预览");

        if (mPreviewHandle != -1) {
            SDKGuider.g_sdkGuider.m_comPreviewGuider.RealPlay_Stop_jni(mPreviewHandle);
            mPreviewHandle = -1;
        }

        if (mVideoDecoder != null) {
            mVideoDecoder.closeDecode();
            mVideoDecoder = null;
        }
    }

    private static void logger(String msg) {
        AdlLogger.d(TAG + "--" + msg);
    }

    private class VideoDecoder implements PlayerCallBack.PlayerDecodeCBEx {

        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        private boolean isHeaderOk;
        private int playPort;
        private byte[] yuvBuffer;

        public synchronized void closeDecode() {
            logger("关闭解码器");
            isHeaderOk = false;
            Player.getInstance().stop(playPort);
            Player.getInstance().closeStream(playPort);
            Player.getInstance().freePort(playPort);
        }

        public void input(int iHandle, int dataType, byte[] buffer, int bufferSize) {
            // 头数据
            if (dataType == HCNetSDK.NET_DVR_SYSHEAD) {

                playPort = Player.getInstance().getPort();
                if (!Player.getInstance().setStreamOpenMode(playPort, 1)) {
                    logger("解码打开失败");
                    return;
                }

                // 打开流
                if (!Player.getInstance().openStream(playPort, buffer, bufferSize, 2 * 1024 * 1024)) {
                    logger("打开流失败");
                    return;
                }

                // 启动解码
                Player.getInstance().setDecodeCBEx(playPort, this);

                if (!Player.getInstance().playEx(playPort, null)) {
                    logger("打开预览失败");
                    return;
                }

                isHeaderOk = true;
            }
            // 视频数据
            else if (dataType == HCNetSDK.NET_DVR_STREAMDATA) {
                // decode
                if (isHeaderOk && playPort != -1 && bufferSize > 0 && !isPause) {
                    Player.getInstance().inputData(playPort, buffer, bufferSize);
                }
            }
        }

        private void YV12toNV21(byte[] input, byte[] output, int width, int height) {

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

        // 解码回调
        @Override
        public void onDecodeEx(int nPort, byte[] data, int nDataLen,
                               int nWidth, int nHeight, int nFrameTime, int nDataType,
                               int Reserved, int nYear, int nMon, int nDay, int nHour,
                               int nMin, int nSec, int nMs) {
            if (yuvBuffer == null) {
                yuvBuffer = new byte[nWidth * nHeight * 3 / 2];
            }

            // 实际大小
            long timestamp = System.currentTimeMillis();
            String timeStr = nYear
                    + "-" + (nMon > 9 ? nMon : ("0" + nMon))
                    + "-" + (nDay > 9 ? nDay : ("0" + nDay))
                    + " " + nHour + ":" + nMin + ":" + nSec + ":"
                    + (nMs > 100 ? nMs : (nMs > 10 ? "0" + nMs : ("00" + nMs)));
            try {
                timestamp = df.parse(timeStr).getTime();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // yv12 - nv21
            YV12toNV21(data, yuvBuffer, nWidth, nHeight);

            innerCameraCallback.onFrame(cameraIp, yuvBuffer, nWidth, nHeight, timestamp);
        }
    }

    public interface OnCameraCallback {

        void onSuccess(String ip);

        void onFailed(String ip);

        void onFrame(String ip, byte[] yuv, int width, int height, long timestamp);
    }

    public static class OnCameraCallbackSample implements OnCameraCallback {

        @Override
        public void onSuccess(String ip) {

        }

        @Override
        public void onFailed(String ip) {

        }

        @Override
        public void onFrame(String ip, byte[] yuv, int width, int height, long timestamp) {

        }
    }
}
