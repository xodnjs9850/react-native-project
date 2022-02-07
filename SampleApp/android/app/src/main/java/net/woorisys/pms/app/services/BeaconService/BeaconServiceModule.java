package net.woorisys.pms.app.services.BeaconService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import net.woorisys.pms.app.MainActivity;
import net.woorisys.pms.app.api.CommonApi;
import net.woorisys.pms.app.dataManager.UserDataSingleton;
import net.woorisys.pms.app.domain.OnepassBeacon;

import java.util.ArrayList;

public class BeaconServiceModule extends ReactContextBaseJavaModule {
    private final String TAG = "KTW_BeaconServiceModule";
    private final Integer DEFAULT_SEND_INTERVAL = 5000;   // MilliSeconds
    private Context mContext;

    public BeaconServiceModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);

        mContext = reactContext;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LOBBY");
        mContext.registerReceiver(receiver, intentFilter);
    }

    @NonNull
    @Override
    public String getName() {
        return "BeaconService";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        mContext.unregisterReceiver(receiver);
    }

    // Beacon Service Start
    @ReactMethod
    public void StartBeaconService(ReadableMap data, Promise promise) {
        try {
            // Setup User Data for beacon service
            setupUserData(data);

            Intent beaconServiceIntent = new Intent(mContext, BeaconService.class);
            // purpose - Beacon Service 사용처
            // 'onepass': Lobby Open 만 사용
            // 'parking': 주차위치 인식 만 사용
            // 'both': 둘 다 사용
            beaconServiceIntent.putExtra("purpose", UserDataSingleton.getInstance().getPurpose());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(beaconServiceIntent);
            } else {
                mContext.startService(beaconServiceIntent);
            }

            WritableMap map= Arguments.createMap();
            map.putString("result", "success");
            promise.resolve(map);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    // Beacon Service Cancel
    @ReactMethod
    public void CancelBeaconService(Promise promise) {
        try {
            Intent Beacon = new Intent(mContext, BeaconService.class);
            mContext.stopService(Beacon);

            WritableMap map= Arguments.createMap();
            map.putString("result", "success");
            promise.resolve(map);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void SendAppToBackground(Promise promise) {
        // Go to background
        MainActivity.getInstance().moveTaskToBack(true);

        WritableMap map= Arguments.createMap();
        map.putString("result", "success");
        promise.resolve(map);
    }

    @ReactMethod
    public void IsRunningService(Promise promise) {
        if (CommonApi.isRunningService(mContext, BeaconService.class)) {
            WritableMap map = Arguments.createMap();
            map.putString("result", "true");
            promise.resolve(map);
        } else {
            promise.reject("result", "false");
        }
    }

    private void setupUserData(ReadableMap data) {
        UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();
        userDataSingleton.setPurpose(data.getString("purpose"));
        userDataSingleton.setDriver(data.getBoolean("isDriver"));
        userDataSingleton.setDong(data.getString("dong"));
        userDataSingleton.setHo(data.getString("ho"));
        userDataSingleton.setUsername(data.getString("name"));
        //userDataSingleton.setBaseUrl(data.getString("baseUrl"));
        //userDataSingleton.setPhoneUid(data.getString("phoneUid"));
        //userDataSingleton.setAuthorization(data.getString("authorization"));
        //userDataSingleton.setComplex(data.getString("complex"));
        
        if (data.hasKey("sendInterval")) {
            userDataSingleton.setSendInterval(data.getInt("sendInterval") * 1000);
        } else {
            userDataSingleton.setSendInterval(DEFAULT_SEND_INTERVAL);
        }
        // Set Parking Beacon UUID
        String uuid = data.getString("parkingBeaconUUID");
        if (uuid != null) {
            uuid = uuid.replaceAll("-", "");
            userDataSingleton.setParkingBeaconUUID(uuid);
            Log.d(TAG, "parkingBeaconUUID = " + uuid);
        }

        ArrayList<OnepassBeacon> onepassBeaconArrayList = new ArrayList<>();
        ReadableArray onepassList = data.getArray("onepass");

        Log.d(TAG, "onepassList " + onepassList);

        if (onepassList != null) {
            for (int i = 0; i < onepassList.size(); i++) {
                OnepassBeacon onepassBeacon = new OnepassBeacon();
                ReadableMap beacon = onepassList.getMap(i);

                if (beacon == null) continue;

                String minor = beacon.getString("minor");
                String rssi = beacon.getString("rssi");
                uuid = "201510058864-5654-4111710200-2108-01";
                String major = "1";
                //onepassBeacon.setSid(beacon.getString("sid"));
                //onepassBeacon.setName(beacon.getString("name"));
                //uuid = beacon.getString("uuid");
                //Log.d(TAG, "uuid : " + uuid);
                //Log.d(TAG, "minor : " + minor);

                if (uuid != null) {
                    uuid = uuid.replaceAll("-", "");
                    onepassBeacon.setUuid(uuid);
                    userDataSingleton.setLobbyBeaconUUID(uuid);
                    Log.d(TAG, "lobbyBeaconUUID = " + uuid);
                }

                onepassBeacon.setMinor("0005");
                onepassBeacon.setMajor(major);
                //onepassBeacon.setMinor(minor);
                onepassBeacon.setRssi(Double.parseDouble(rssi));
                //onepassBeacon.setMajor(beacon.getString("major"));
                //onepassBeacon.setRssi(beacon.getDouble("rssi"));

                onepassBeaconArrayList.add(onepassBeacon);
            }

            userDataSingleton.setOnepassList(onepassBeaconArrayList);
        }
        Log.d(TAG, "Beacon SIZE : " + userDataSingleton.getOnepassList().size());

        // Save UserData to File
        userDataSingleton.saveUserData(mContext.getApplicationContext());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };
}
