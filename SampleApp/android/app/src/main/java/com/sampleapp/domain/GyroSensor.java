package com.sampleapp.domain;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 자이로 센서 클래스 - 조건을 만족하는 자이로 센서를 수집하는 Class
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * X        :   Gyro 값의 event.value[0]의 값       :   X
 * Y        :   Gyro 값의 event.value[1]의 값       :   Y
 * Z        :   Gyro 값의 event.value[2]의 값       :   Z
 * Delay    :   조건을 만족한 Gyro 를 받은 시간     :   Delay
 **/
public class GyroSensor {
    @SerializedName("X")
    @Expose
    @Keep
    private String X;

    @SerializedName("Y")
    @Expose
    @Keep
    private String Y;

    @SerializedName("Z")
    @Expose
    @Keep
    private String Z;

    @SerializedName("Delay")
    @Expose
    @Keep
    private String delay;

    public String getX() {
        return X;
    }

    public void setX(String x) {
        X = x;
    }

    public String getY() {
        return Y;
    }

    public void setY(String y) {
        Y = y;
    }

    public String getZ() {
        return Z;
    }

    public void setZ(String z) {
        Z = z;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }
}
