package com.ty.identifty.base;

import android.graphics.Bitmap;
import android.graphics.RectF;


public class TyFaceInfo<T> extends TyFaceFeature<T> {

    //imageID
    private String key;
    //人脸的中心点
    private float centerX;
    private float centerY;
    //
    private float width;
    private float height;
    private float angle;
    //人脸质量
    private float score;
    //特征点
    private float[] landmarks;
    //jpg/yuv
    private String faceBitmapType;
    private byte[] faceIconData;
    private Bitmap faceIcon;
    //人脸宽高
    private int faceWidth;
    private int faceHeight;
    //人脸的位置
    private RectF faceRect;

    public RectF getFaceRect() {
        return faceRect;
    }

    public void setFaceRect(RectF faceRect) {
        this.faceRect = faceRect;
    }

    private Object extendObject;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public float[] getLandmarks() {
        return landmarks;
    }

    @Override
    public void setLandmarks(float[] landmarks) {
        this.landmarks = landmarks;
    }

    public Object getExtendObject() {
        return extendObject;
    }

    public void setExtendObject(Object extendObject) {
        this.extendObject = extendObject;
    }

    public byte[] getFaceIconData() {
        return faceIconData;
    }

    public void setFaceIconData(byte[] faceIconData) {
        this.faceIconData = faceIconData;
    }

    public Bitmap getFaceIcon() {
        return faceIcon;
    }

    public void setFaceIcon(Bitmap faceIcon) {
        this.faceIcon = faceIcon;
    }

    public String getFaceBitmapType() {
        return faceBitmapType;
    }

    public void setFaceBitmapType(String faceBitmapType) {
        this.faceBitmapType = faceBitmapType;
    }

    public int getFaceWidth() {
        return faceWidth;
    }

    public void setFaceWidth(int faceWidth) {
        this.faceWidth = faceWidth;
    }

    public int getFaceHeight() {
        return faceHeight;
    }

    public void setFaceHeight(int faceHeight) {
        this.faceHeight = faceHeight;
    }
}
