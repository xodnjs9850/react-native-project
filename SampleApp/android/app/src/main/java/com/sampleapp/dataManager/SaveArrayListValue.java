package com.sampleapp.dataManager;

import android.util.Log;

import com.sampleapp.domain.AccelBeacon;
import com.sampleapp.domain.GyroSensor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SaveArrayListValue {
    private static final String TAG = "PSJ_SaveArrayListValue";

    public void SaveAccelBeacon(String id, String rssi, String delay) {
        DataManagerSingleton dataManagerSingleton = DataManagerSingleton.getInstance();
        AccelBeacon accelBeacon = dataManagerSingleton.getAccelBeaconMap().get(id);

        if (accelBeacon == null) {
            String LogValue = String.format("ID : %s , RSSI : %s , DELAY : %s", id, rssi, delay);
            Log.d(TAG, "TEST_LOG_190221_AB - 엑셀 비컨 저장 - 값 없음 -> 추가 // " + LogValue);

            AccelBeacon saveAccelBeacon = new AccelBeacon();
            saveAccelBeacon.setBeaconId(id);
            saveAccelBeacon.setRssi(rssi);
            saveAccelBeacon.setDelay(delay);
            saveAccelBeacon.setCount("1");

            dataManagerSingleton.getAccelBeaconMap().put(id, saveAccelBeacon);
        } else {
            if (Float.parseFloat(rssi) >= Float.parseFloat(accelBeacon.getRssi())) {
                String LogValue = String.format("ID : %s , RSSI : %s , DELAY : %s", id, rssi, delay);
                Log.d(TAG, "TEST_LOG_190221_AB - 엑셀 비컨 저장 - 값 있음 -> 변경 // " + LogValue);

                int accelCount = Integer.parseInt(accelBeacon.getCount());
                accelCount++;

                AccelBeacon saveAccelBeacon = new AccelBeacon();
                saveAccelBeacon.setBeaconId(id);
                saveAccelBeacon.setRssi(rssi);
                saveAccelBeacon.setDelay(delay);
                saveAccelBeacon.setCount(String.valueOf(accelCount));

                dataManagerSingleton.getAccelBeaconMap().put(id, saveAccelBeacon);
            } else {
                int accelCount = Integer.parseInt(accelBeacon.getCount());
                accelCount++;

                String LogValue = String.format("ID : %s , RSSI : %s , DELAY : %s", accelBeacon.getBeaconId(), accelBeacon.getRssi(), accelBeacon.getDelay());
                Log.d(TAG, "TEST_LOG_190221_AB - 엑셀 비컨 저장 - 값 있음 -> 유지 // " + LogValue);

                AccelBeacon saveAccelBeacon = new AccelBeacon();
                saveAccelBeacon.setBeaconId(accelBeacon.getBeaconId());
                saveAccelBeacon.setRssi(accelBeacon.getRssi());
                saveAccelBeacon.setDelay(accelBeacon.getDelay());
                saveAccelBeacon.setCount(String.valueOf(accelCount));

                dataManagerSingleton.getAccelBeaconMap().put(id, saveAccelBeacon);
            }
        }

        SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS", Locale.getDefault());
        Log.d(TAG, "TAG_BYTE_CALC_ACC_BEACON - ACCEL_BEACON BYTE 0 : " + dataManagerSingleton.getAccelBeaconMap().toString().length() + " , TIME : " + full_sdf.format(System.currentTimeMillis()));
    }

    String AccelSensorResult() {
        int resultCount = DataManagerSingleton.getInstance().getAccelCount();

        String result;

        if (resultCount < 3 && resultCount >= 0) {
            result = "T";
        } else if (resultCount < 12 && resultCount >= 3) {
            result = "S";
        } else {
            result = "W";
        }

        return result;
    }

    public void SaveGyro() {
        DataManagerSingleton dataManagerSingleton = DataManagerSingleton.getInstance();

        GyroSensor gyroSensor = new GyroSensor();
        gyroSensor.setDelay(String.valueOf(dataManagerSingleton.getWholeTimerDelay()));
        gyroSensor.setX(String.valueOf(dataManagerSingleton.getSaveCountRoll()));
        gyroSensor.setY(String.valueOf(dataManagerSingleton.getSaveCountPitch()));
        gyroSensor.setZ(String.valueOf(dataManagerSingleton.getSaveCountYaw()));

        dataManagerSingleton.getGyroSensorArrayList().add(gyroSensor);

        SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS", Locale.getDefault());
        Log.d(TAG, "TAG_BYTE_CALC_GYRO - GYRO BYTE 0 : " + dataManagerSingleton.getGyroSensorArrayList().toString().length() + " , TIME : " + full_sdf.format(System.currentTimeMillis()));
    }

    void SaveAccelBeacon() {
        if (DataManagerSingleton.getInstance().getAccelBeaconMap() != null) {
            DataManagerSingleton dataManagerSingleton = DataManagerSingleton.getInstance();

            for (String key : dataManagerSingleton.getAccelBeaconMap().keySet()) {
                AccelBeacon value = dataManagerSingleton.getAccelBeaconMap().get(key);
                if (value == null) continue;

                String id = value.getBeaconId();
                String rssi = value.getRssi();
                String delay = value.getDelay();
                String count = value.getCount();

                ArrayList<Integer> AccelDelay = dataManagerSingleton.getAccelBeaconDelayMap().get(id);
                Log.d(TAG, "TAG_ACCEL_VALUE_INFORM2 - ACCEL VALUE : " + AccelDelay + " , " + dataManagerSingleton.getAccelBeaconDelayMap());

                AccelBeacon accelBeacon = new AccelBeacon();
                accelBeacon.setBeaconId(id);
                accelBeacon.setRssi(rssi);
                accelBeacon.setDelay(delay);
                accelBeacon.setCount(count);
                accelBeacon.setDelayList(AccelDelay);

                dataManagerSingleton.getAccelBeaconArrayList().add(accelBeacon);

                Log.d(TAG, "TAG_ACCEL_VALUE_INFORM" + String.format("ID : %s , RSSI : %s , DELAY : %s , COUNT : %s", id, rssi, delay, count) + " , ACCEL_DELAY : " + AccelDelay);
            }
        }
    }
}
