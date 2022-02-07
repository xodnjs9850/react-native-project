package net.woorisys.pms.app.services.SensorService;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import net.woorisys.pms.app.api.CommonApi;

public class SensorServiceModule extends ReactContextBaseJavaModule {
    final static String TAG = "KTW_SensorService";

    private Context mContext;

    public SensorServiceModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "SensorService";
    }

    // Sensor Service Start
    @ReactMethod
    public void StartSensorService(Promise promise) {
        try {
            Intent Sensor = new Intent(mContext, SensorService.class);            

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                mContext.startForegroundService(Sensor);
            else
                mContext.startService(Sensor);

            WritableMap map= Arguments.createMap();
            map.putString("result", "success");
            promise.resolve(map);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    // Sensor Service Cancel
    @ReactMethod
    public void CancelSensorService(Promise promise) {
        try {
            Intent Sensor = new Intent(mContext, SensorService.class);
            mContext.stopService(Sensor);

            WritableMap map= Arguments.createMap();
            map.putString("result", "success");
            promise.resolve(map);
        } catch (Exception ex) {
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void IsRunningService(Promise promise) {
        if (CommonApi.isRunningService(mContext, SensorService.class)) {
            WritableMap map= Arguments.createMap();
            map.putString("result", "true");
            promise.resolve(map);
        } else {
            promise.reject("result", "false");
        }
    }
}
