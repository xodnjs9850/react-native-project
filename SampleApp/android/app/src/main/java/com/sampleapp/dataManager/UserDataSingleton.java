package com.sampleapp.dataManager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.sampleapp.domain.OnepassBeacon;

import java.util.ArrayList;

public class UserDataSingleton {
    private static final String TAG = "PSJ_UserDataSingleton";
    private static final String USER_DATA_KEY = "PSJ_BeaconServiceUserData";

    private String purpose;
    private String baseUrl;
    private String phoneUid;
    private String authorization;
    private String complex;
    private String dong;
    private String ho;
    private String username;
    private boolean isDriver;
    private String lobbyBeaconUUID;
    private String parkingBeaconUUID;
    private Integer sendInterval;                       // Seconds
    private ArrayList<OnepassBeacon> onepassList;

    private UserDataSingleton() {
        this.onepassList = new ArrayList<>();
    }

    public static synchronized UserDataSingleton getInstance() {
        return UserDataSingletonHolder.instance;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPhoneUid() {
        return phoneUid;
    }

    public void setPhoneUid(String phoneUid) {
        this.phoneUid = phoneUid;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getComplex() {
        return complex;
    }

    public void setComplex(String complex) {
        this.complex = complex;
    }

    public String getDong() {
        return dong;
    }

    public void setDong(String dong) {
        this.dong = dong;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isDriver() {
        return isDriver;
    }

    public void setDriver(boolean driver) {
        isDriver = driver;
    }

    public String getLobbyBeaconUUID() {
        return lobbyBeaconUUID;
    }

    public void setLobbyBeaconUUID(String lobbyBeaconUUID) {
        this.lobbyBeaconUUID = lobbyBeaconUUID;
    }

    public String getParkingBeaconUUID() {
        return parkingBeaconUUID;
    }

    public void setParkingBeaconUUID(String parkingBeaconUUID) {
        this.parkingBeaconUUID = parkingBeaconUUID;
    }

    public Integer getSendInterval() {
        return sendInterval;
    }

    public void setSendInterval(Integer sendInterval) {
        this.sendInterval = sendInterval;
    }

    public ArrayList<OnepassBeacon> getOnepassList() {
        return onepassList;
    }

    public void setOnepassList(ArrayList<OnepassBeacon> onepassList) {
        this.onepassList = onepassList;
    }

    public void saveUserData(Context context) {
        Gson gson = new Gson();
        String userData = gson.toJson(this);

        SharedDataFile sharedDataFile = new SharedDataFile(context);
        sharedDataFile.put(USER_DATA_KEY, userData);
        Log.d(TAG, "UserDataSingleton Save To File: " + userData);
    }

    public void loadUserData(Context context) {
        SharedDataFile sharedDataFile = new SharedDataFile(context);
        String userData = sharedDataFile.get(USER_DATA_KEY, "{}");

        Log.d(TAG, "UserDataSingleton Load From File: " + userData);

        Gson gson = new Gson();
        UserDataSingleton singleton = gson.fromJson(userData, UserDataSingleton.class);
        this.purpose = singleton.purpose;
        this.baseUrl = singleton.baseUrl;
        this.phoneUid = singleton.phoneUid;
        this.authorization = singleton.authorization;
        this.complex = singleton.complex;
        this.dong = singleton.dong;
        this.ho = singleton.ho;
        this.username = singleton.username;
        this.isDriver = singleton.isDriver;
        this.lobbyBeaconUUID = singleton.lobbyBeaconUUID;
        this.parkingBeaconUUID = singleton.parkingBeaconUUID;
        this.sendInterval = singleton.sendInterval;
        this.onepassList.addAll(singleton.onepassList);
    }

    private static class UserDataSingletonHolder {
        private static final UserDataSingleton instance = new UserDataSingleton();
    }
}
