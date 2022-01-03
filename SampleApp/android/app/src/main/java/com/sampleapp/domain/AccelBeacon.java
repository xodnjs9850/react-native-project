package com.sampleapp.domain;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * 비컨 클래스 - 조건 상관없이 들어오는 Beacon 의 Data 를 수집하여 RSSI 가 가장 큰 데이터만 가지고 있는다
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * BeaconId     :   Beacon ID                    :   ID
 * Rssi         :   Beacon 의 Rssi (강도)         :   Rssi
 * Delay        :   Beacon 을 받아온 소요 시간     :   Delay
 * Count        :   Beacon 을 받은 갯수           :   Count
 **/
public class AccelBeacon {
    @SerializedName("ID")
    @Expose
    @Keep
    private String beaconId;

    @SerializedName("Rssi")
    @Expose
    @Keep
    private String rssi;

    @SerializedName("Delay")
    @Expose
    @Keep
    private String delay;

    @SerializedName("Count")
    @Expose
    @Keep
    private String count;

    @SerializedName("DelayList")
    @Expose
    @Keep
    private ArrayList<Integer> delayList;

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public ArrayList<Integer> getDelayList() {
        return delayList;
    }

    public void setDelayList(ArrayList<Integer> delayList) {
        this.delayList = delayList;
    }

}
