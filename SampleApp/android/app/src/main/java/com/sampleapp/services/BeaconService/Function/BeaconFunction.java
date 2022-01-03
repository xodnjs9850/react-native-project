package com.sampleapp.services.BeaconService.Function;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.sampleapp.api.ParkingServiceApi;
import com.sampleapp.dataManager.DataManagerSingleton;
import com.sampleapp.dataManager.SaveArrayListValue;
import com.sampleapp.dataManager.TimerSingleton;
import com.sampleapp.dataManager.UserDataSingleton;
import com.sampleapp.domain.Beacon;
import com.sampleapp.domain.OnepassBeacon;
import com.sampleapp.services.BeaconService.BeaconServiceUsage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class BeaconFunction {
    private static final String TAG = "PSJ_BeaconFunction";

    private TimerSingleton mTimerSingleton;
    private DataManagerSingleton mDataManagerSingleton;
    private Context mContext;

    private BeaconServiceUsage mBeaconServiceUsage = BeaconServiceUsage.PARKING;

    public BeaconFunction(Context context) {
        this.mContext = context;

        mTimerSingleton = TimerSingleton.getInstance();
        mDataManagerSingleton = DataManagerSingleton.getInstance();
    }

    public void setBeaconServiceUsage(BeaconServiceUsage beaconServiceUsage) {
        this.mBeaconServiceUsage = beaconServiceUsage;
    }

    // 입구 비컨
    public void ENTRANCE_BEACON(int major, int minor, double rssi) {
        if (mTimerSingleton.isAfterStart()) {
            int count = mDataManagerSingleton.getAfterStartCount();
            count++;
            mDataManagerSingleton.setAfterStartCount(count);
        }

        if (mTimerSingleton.isAFTER_GYRO_START_CALC()) {
            int value = mDataManagerSingleton.getAfterGyroCount();
            value++;
            mDataManagerSingleton.setAfterGyroCount(value);

        }
        /* 주차 시작 조건
         * RSSI -90 이상
         * 입차 Beacon 이 들어온적이 없어야한다.
         * 주차장 Beacon 이 들어온적이 없어야한다.
         * 전체 타이머 (데이터를 수집하는 기준) 가 동작하지 않는다.
         */
        if (rssi >= -90) {
            if (!mDataManagerSingleton.isStart2Beacon()) {
                if (!mDataManagerSingleton.isStart1Beacon()) {
                    if (!mTimerSingleton.isWholeTimerStart()) {
                        mDataManagerSingleton.setStart1Beacon(true);

                        ParkingServiceApi.getInstance()
                                .GateInformation(String.valueOf(major), String.valueOf(minor));
                    }
                }
            } else {
                if (!mDataManagerSingleton.isStart1Beacon()) {
                    mDataManagerSingleton.setStart1Beacon(true);

                    ParkingServiceApi.getInstance()
                            .GateInformation(String.valueOf(major), String.valueOf(minor));

                    mDataManagerSingleton.setOutParking(true);

                    //전체 타이머가 돌고 있을 경우
                    if (mTimerSingleton.isWholeTimerStart()) {
                        try {
                            mTimerSingleton.getWholeTimer().onFinish();
                            mTimerSingleton.getWholeTimer().cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - Whole Timer Finish : " + e.getMessage());
                        }
                    } else {
                        ParkingServiceApi.getInstance().ParkingOut();
                    }

                    if (mTimerSingleton.isCollectLobbyStart()) {
                        mTimerSingleton.setNotRestart(true);
                        try {
                            mTimerSingleton.getCollectLobbyTimer().onFinish();
                            mTimerSingleton.getCollectLobbyTimer().cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - COLLECT LOBBY TIMER ON FINISH : " + e.getMessage());
                        }
                        mTimerSingleton.setNotRestart(false);
                    }

                    if (mTimerSingleton.isStayRestartStart()) {
                        try {
                            mTimerSingleton.getStayRestartStartTimer().onFinish();
                            mTimerSingleton.getStayRestartStartTimer().cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - STY RESTART START ON FINISH : " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    // 주차장 비컨
    public void PARKING_BEACON(int major, int minor, double rssi) {
        if (mTimerSingleton.isAfterStart()) {
            int count = mDataManagerSingleton.getAfterStartCount();
            count++;
            mDataManagerSingleton.setAfterStartCount(count);
            Log.d(TAG, "TEST_AFTER_START COUNT : "+count);
        }

        if (mTimerSingleton.isAFTER_GYRO_START_CALC()) {
            int value = mDataManagerSingleton.getAfterGyroCount();
            value++;
            mDataManagerSingleton.setAfterGyroCount(value);
        }

        if (rssi >= -80) {
            if (mDataManagerSingleton.isStart1Beacon()) {
                if (!mDataManagerSingleton.isStart2Beacon()) {
                    if (!mTimerSingleton.isWholeTimerStart()) {
                        mDataManagerSingleton.setStart2Beacon(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            mTimerSingleton.StartWholeTimer(mContext);
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> mTimerSingleton.StartWholeTimer(mContext));
                        }
                        mDataManagerSingleton.setEnd1Beacon(false);
                        mDataManagerSingleton.setEnd2Beacon(false);

                        ParkingServiceApi.getInstance()
                                .GateInformation(String.valueOf(major), String.valueOf(minor));

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
                        mDataManagerSingleton.setInputTime(currentDate);

                        ParkingServiceApi.getInstance()
                                .GateInformation("NORMAL_START", "NORMAL_START");
                    }
                }
            } else {
                if (!mDataManagerSingleton.isStart2Beacon()) {
                    mDataManagerSingleton.setStart2Beacon(true);

                    ParkingServiceApi.getInstance()
                            .GateInformation(String.valueOf(major), String.valueOf(minor));
                }
            }
        }
    }

    /**
     * ** 종료 준비 **
     * -> 전재 조건 : 3번 비컨(엘리베이터)이 컨저 들어와선 안된다
     * : 1번 비컨(로비)을 받은 후 3번 비컨(엘리베이터)을 받을 수 있도록 처리하여야 한다.
     * : 1번 비컨(로비)를 초기화 시키기 전까지는 한번 받은 이후로 받을 수 없다.
     * : 들어온 이후로는 계속 받는지만 Check
     * <p>
     * -> 종료
     * : 로비 -> 엘리베이터 순으로 비컨이 들어와야한다.
     * : 로비 들어온 이후 엘리베이터를 받으면 종료 모드 시작
     * : 종료 모드 시작되면 일정 시간동안 로비.엘리베이터 비컨을 계속 받는지 Check -> 안받기 시작한 시점을 기준으로 3~5초간 안들어오면 종료
     * <p>
     * -> 시작      : 엘리베이터 -> 로비 순으로 비컨이 들어와야한다.
     * : 로비가 들어온 이후 T 상태로 Gyro 가 발생할 경우 시작
     * <p>
     * -> 1층 로비 : 3번째 자리가 1이다.
     **/
    // 로비 비컨
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void LOBBY_BEACON(String uuid, int major, int minor, final double rssi) {
        if (mTimerSingleton.isAFTER_LOBBY_ELEVATOR_CHECK()) {
            int value = mDataManagerSingleton.getAfterLobbyEleCount();
            value++;
            mDataManagerSingleton.setAfterLobbyEleCount(value);
        }

        if (mTimerSingleton.isCOLLECT_START_BEACON_CALC()) {
            int value = mDataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            mDataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();
        ArrayList<OnepassBeacon> onepassList = userDataSingleton.getOnepassList();

        if (mTimerSingleton.isCollectLobbyStart())
            mDataManagerSingleton.getINOUT_DATA_MAJOR().add(major);

        // region 로비 문열림
        for (int i = 0; i < onepassList.size(); i++) {
            OnepassBeacon onepassBeacon = onepassList.get(i);
            if (!uuid.equals(onepassBeacon.getUuid())) {
                continue;
            }
            String lobbyMinor = onepassBeacon.getMinor();
            double lobbyRssi = onepassBeacon.getRssi();
            String minorHex = String.format("%04X", minor);

            if (lobbyMinor.equals(minorHex) && rssi >= lobbyRssi) {
                if (!mTimerSingleton.isLobbyTimerStart()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // only for gingerbread and newer versions
                        mTimerSingleton.StartLobbyTimer();
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> mTimerSingleton.StartLobbyTimer());
                    }
                }
                Log.d(TAG, "LOBBY_BEACON - OPEN 요청 : " + mTimerSingleton.isLobbyTimerStart() + ", rssi : " + rssi);

                // Send Lobby Open Command to Server
                if (this.mBeaconServiceUsage != BeaconServiceUsage.PARKING) {
                    ParkingServiceApi.getInstance().OpenLobby(mContext, onepassBeacon, rssi);
                }
            }
        }
        // endregion

        if (rssi >= -80) {
            // region 종료 준비
            // 로비 , 엘리베이터 둘다 들어오지 않아야 한다.
            if (!mDataManagerSingleton.isEnd2Beacon() && !mDataManagerSingleton.isEnd1Beacon()) {
                // 타이머가 돌고있어야 한다.
                if (mTimerSingleton.isWholeTimerStart()) {
                    ParkingServiceApi.getInstance()
                            .GateInformation(String.valueOf(major), String.valueOf(minor));

                    mDataManagerSingleton.setEnd1Beacon(true);
                    mDataManagerSingleton.setINOUT_STATE("OUT");
                    if (!mTimerSingleton.isAFTER_LOBBY_ELEVATOR_CHECK()) {
                        Log.d(TAG, "TAG_BEACON_FUNCTION - TIMER LOBBY END START");
                        mTimerSingleton.AFTER_LOBBY_ELEVATOR_TIMER();
                    }
                }
            }
            //endregion

            // region 시작
            if (!mDataManagerSingleton.isEnd1Beacon() && mDataManagerSingleton.isEnd2Beacon()) {
                if (!mTimerSingleton.isWholeTimerStart()) {
                    if (mDataManagerSingleton.getINOUT_STATE().equals("IN")) {
                        mDataManagerSingleton.setEnd1Beacon(true);
                        mDataManagerSingleton.setRESTART_BEACON(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            mTimerSingleton.StartStayRestart();
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> mTimerSingleton.StartStayRestart());
                        }

                        ParkingServiceApi.getInstance()
                                .GateInformation(String.valueOf(major), String.valueOf(minor));
                    }
                }
            }
            // endregion
        }
    }

    // 엘리베이터 비컨
    public void ELEVATOR_BEACON(int major, int minor, double rssi) {
        if (mTimerSingleton.isCOLLECT_START_BEACON_CALC()) {
            if (!mDataManagerSingleton.isElevatorBeaconGet()) {
                mDataManagerSingleton.setElevatorBeaconGet(true);
            }

            int value = mDataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            mDataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        if (rssi >= -75) {
            if (mTimerSingleton.isCollectLobbyStart())
                mDataManagerSingleton.getINOUT_DATA_MAJOR().add(major);

            // region 종료
            if (mDataManagerSingleton.isEnd1Beacon() && !mDataManagerSingleton.isEnd2Beacon()) {
                if (mTimerSingleton.isWholeTimerStart()) {
                    if (mDataManagerSingleton.getINOUT_STATE().equals("OUT")) {
                        // 타이머 돌림 - 5초간 값 들어온 것을 Check -> 1,3 번 비컨 없을시 종료 , 있을시 재시작
                        mDataManagerSingleton.setEnd2Beacon(true);

                        if (!mTimerSingleton.isCollectLobbyStart()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // only for gingerbread and newer versions
                                mTimerSingleton.StartCollectLobby(mContext);
                            } else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(() -> mTimerSingleton.StartCollectLobby(mContext));
                            }

                            ParkingServiceApi.getInstance()
                                    .GateInformation(String.valueOf(major), String.valueOf(minor));
                        }
                    }
                }
            }
            //endregion

            //region 시작 준비
            if (!mDataManagerSingleton.isEnd1Beacon() && !mDataManagerSingleton.isEnd2Beacon()) {
                if (!mTimerSingleton.isWholeTimerStart()) {
                    mDataManagerSingleton.setEnd2Beacon(true);
                    mDataManagerSingleton.setINOUT_STATE("IN");

                    ParkingServiceApi.getInstance()
                            .GateInformation(String.valueOf(major), String.valueOf(minor));
                }
            }
            //endregion
        }
    }

    public void StayBeacon(int major, int minor, double rssi, SaveArrayListValue saveArrayListValue) {
        if (mTimerSingleton.isAFTER_LOBBY_ELEVATOR_CHECK()) {
            int value = mDataManagerSingleton.getAfterLobbyEleCount();
            value++;
            mDataManagerSingleton.setAfterLobbyEleCount(value);
        }

        if (mTimerSingleton.isAFTER_GYRO_START_CALC()) {
            int value = mDataManagerSingleton.getAfterGyroCount();
            value++;
            mDataManagerSingleton.setAfterGyroCount(value);

            Log.d(TAG, "TAG_TEST_TIMER - GYRO COUNT VALUE : " + mDataManagerSingleton.getAfterGyroCount());
        }

        if (mTimerSingleton.isAfterStart()) {
            int count = mDataManagerSingleton.getAfterStartCount();
            count++;
            mDataManagerSingleton.setAfterStartCount(count);
//          Log.d("TEST_AFTER_START","COUNT : "+count);
        }

        if (mTimerSingleton.isCOLLECT_START_BEACON_CALC()) {
            int value = mDataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            mDataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        if (mTimerSingleton.isNotStartBeaconStart()) {
//          Log.d("TAG_NOT_START","BEACON VALUE : "+minor);
            mDataManagerSingleton.getNO_START_BEACON().add(minor);
        }

        if (mTimerSingleton.isWholeTimerStart()) {
            String id;

            if (minor > 32768) {
                id = String.valueOf(minor - 32768);
            } else {
                id = String.valueOf(minor);
            }

            if (mTimerSingleton.isCollectAccelBeaconStart()) {
                String hexValue = String.format("%04X", Integer.valueOf(id));
                saveArrayListValue.SaveAccelBeacon(hexValue, String.valueOf(rssi), String.valueOf(mDataManagerSingleton.getWholeTimerDelay()));

                int value = mDataManagerSingleton.getPARING_COUNT();
                value++;
                mDataManagerSingleton.setPARING_COUNT(value);
                AddAccelDelay(hexValue, major);
            }
        }
    }

    public void ChangeBeacon(int major, int minor, double rssi, SaveArrayListValue saveArrayListValue) {
        if (mTimerSingleton.isAFTER_LOBBY_ELEVATOR_CHECK()) {
            int value = mDataManagerSingleton.getAfterLobbyEleCount();
            value++;
            mDataManagerSingleton.setAfterLobbyEleCount(value);
        }

        if (mTimerSingleton.isCOLLECT_START_BEACON_CALC()) {
            int value = mDataManagerSingleton.getCollectStartCalcBeacon();
            value++;
            mDataManagerSingleton.setCollectStartCalcBeacon(value);
        }

        if (mTimerSingleton.isNotStartBeaconStart()) {
//          Log.d("TAG_NOT_START","BEACON VALUE : "+minor);
            mDataManagerSingleton.getNO_START_BEACON().add(minor);
        }

        if (mTimerSingleton.isWholeTimerStart()) {
            String id;

            if (minor > 32768) {
                id = String.valueOf(minor - 32768);
                String hexValue = String.format("%04X", Integer.valueOf(id));

                Beacon beacon = new Beacon();
                beacon.setBeaconId(hexValue);
                beacon.setRssi(String.valueOf(rssi));
                beacon.setState(String.valueOf(major));
                beacon.setDelay(String.valueOf(mDataManagerSingleton.getWholeTimerDelay()));
                beacon.setSeq(String.valueOf(mDataManagerSingleton.getBeaconSequence()));

                mDataManagerSingleton.getBeaconArrayList().add(beacon);

//              SimpleDateFormat full_sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss : SSS", Locale.getDefault());
                Log.d(TAG, "TAG_BEACON_CHANGE - BEACON VALUE 5 :" + hexValue + " / " + rssi);
                int beaconSequence = mDataManagerSingleton.getBeaconSequence() + 1;
                mDataManagerSingleton.setBeaconSequence(beaconSequence);
            } else {
                id = String.valueOf(minor);
            }

            if (mTimerSingleton.isCollectAccelBeaconStart()) {
                String hexValue = String.format("%04X", Integer.valueOf(id));
                saveArrayListValue.SaveAccelBeacon(hexValue, String.valueOf(rssi), String.valueOf(mDataManagerSingleton.getWholeTimerDelay()));
                AddAccelDelay(hexValue, major);
            }
        }
    }

    public void OnlyOpenLobby(String uuid, int major, int minor, double rssi) {
        UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();
        ArrayList<OnepassBeacon> onepassList = userDataSingleton.getOnepassList();

        final TimerSingleton timerSingleton = TimerSingleton.getInstance();

        for (int i = 0; i < onepassList.size(); i++) {
            OnepassBeacon onepassBeacon = onepassList.get(i);
            if (!uuid.equals(onepassBeacon.getUuid())) continue;

            String lobbyMinor = onepassBeacon.getMinor();
            double lobbyRssi = onepassBeacon.getRssi();
            String minorHex = String.format("%04X", minor);

            if (lobbyMinor.equals(minorHex) && rssi >= lobbyRssi) {
                if (!timerSingleton.isLobbyTimerStart()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // only for gingerbread and newer versions
                        timerSingleton.StartLobbyTimer();
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(timerSingleton::StartLobbyTimer);
                    }
                }

                if (mBeaconServiceUsage != BeaconServiceUsage.PARKING) {
                    Log.v(TAG, "ONLY_LOBBY_BEACON - OPEN 요청 : " + timerSingleton.isLobbyTimerStart() + ", rssi : " + rssi);
                    ParkingServiceApi.getInstance().OpenLobby(mContext, onepassBeacon, rssi);
                }
            }
        }
        //endregion
    }

    private void AddAccelDelay(String HexValue, int major) {
        ArrayList<Integer> ArrayValue = mDataManagerSingleton.getAccelBeaconDelayMap().get(HexValue);
        if (ArrayValue == null) {
            ArrayValue = new ArrayList<>();
        }
        ArrayValue.add(mDataManagerSingleton.getWholeTimerDelay());
        mDataManagerSingleton.getAccelBeaconDelayMap().put(HexValue, ArrayValue);

        Log.d(TAG, "TAG_BEACON_FUNCTION - HASH MAP ACCEL_BEACON DELAY VALUE " + major + " : " + ArrayValue);
    }
}
