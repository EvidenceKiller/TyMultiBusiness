package com.ty.identifty.base;

public class TyFaceFeature<T>{

    private int id;
    private String userId;
    private String uniqueNo;
    private byte[] feature;
    private float[] featureFloat;
    private float[] landmarks;
    private T featureObj;

    public T getFeatureObj() {
        return featureObj;
    }

    public void setFeatureObj(T featureObj) {
        this.featureObj = featureObj;
    }

    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUniqueNo() {
        return uniqueNo;
    }

    public void setUniqueNo(String uniqueNo) {
        this.uniqueNo = uniqueNo;
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public float[] getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(float[] landmarks) {
        this.landmarks = landmarks;
    }

    public float[] getFeatureFloat() {
        return featureFloat;
    }

    public void setFeatureFloat(float[] featureFloat) {
        this.featureFloat = featureFloat;
    }
}
