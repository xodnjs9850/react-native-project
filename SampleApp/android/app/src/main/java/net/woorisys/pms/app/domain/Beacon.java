package net.woorisys.pms.app.domain;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 비컨 클래스 - MAJOR 번호가 5번인 Beacon Data 를 수집
 *
 * Array List 에 넣기 위한 데이터를 만들 때
 * 서버로 데이터를 보낼때
 * 사용되는 클레스
 *
 * beaconId     :   Beacon ID                       :   ID
 * State        :   Beacon MAJOR 값                 :   State
 * Rssi         :   Beacon 의 Rssi                  :   Rssi
 * Delay        :   Beacon 을 받은 시간             :   Delay
 * Seq          :   Beacon 을 받은 갯수             :   Seq
 **/
public class Beacon {
    @SerializedName("ID")
    @Expose
    @Keep
    private String beaconId;

    @SerializedName("State")
    @Expose
    @Keep
    private String state;

    @SerializedName("Rssi")
    @Expose
    @Keep
    private String rssi;

    @SerializedName("Delay")
    @Expose
    @Keep
    private String delay;

    @SerializedName("Seq")
    @Expose
    @Keep
    private String seq;

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }
}
