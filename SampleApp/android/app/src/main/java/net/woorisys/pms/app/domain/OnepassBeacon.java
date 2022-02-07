package net.woorisys.pms.app.domain;

import java.util.Date;

public class OnepassBeacon {

    private String sid;
    private String name;
    private String uuid;
    private String major;
    private String minor;
    private Double rssi;
    private Date lastDetectedAt;

    public OnepassBeacon() {
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public Double getRssi() {
        return rssi;
    }

    public void setRssi(Double rssi) {
        this.rssi = rssi;
    }

    public Date getLastDetectedAt() {
        return lastDetectedAt;
    }

    public void setLastDetectedAt(Date lastDetectedAt) {
        this.lastDetectedAt = lastDetectedAt;
    }
}
