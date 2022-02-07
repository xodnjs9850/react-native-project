package net.woorisys.pms.app.services.SensorVerify;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;

/**
 * 센서 확인 기능을 사용할 떄 설정해야 될 것들
 * 'PostDelayTime' 설정 기본적인 시간은 5초이며 변경을 원할 경우 'setPostDelayTime' 을 호출하여 설정하여야 한다.
 * - 해당 기능은 행동 요청이후 결과 값을 언제 반환 받을지 정해주는 시간이다.
 * 'RegisterSensorVerify' 함수를 필수 적으로 등록을 해주어야 onSensorChange 가 동작한다.
 * 'UnRegisterSensorVerify' 함수를 센서 확인 기능이 종료시 같이 종료 해주어야 한다,
 * 해당 SensorEventListener 의 경우 해제를 안할 경우 프로세서가 종료될 때까지 동작을 한다.
 * 'setOnSensorVerifyListener' 에 리스너를 등록을 해주어야 동작 종료시 결과를 받을 수 있다.
 **/
public class SensorVerifyModule extends ReactContextBaseJavaModule implements SensorEventListener {

    private final static String TAG = "KTW_SensorVerify";
    private static final float NS2S = 1.0f / 1000000000.0f;
    private Context context;
    private int postDelayTime = 5000;
    // 센서 동작 관련 변수
    private SensorManager sensorManager;
    private boolean _AccelRunning = false;
    private boolean _GyroRunning = false;
    // Sensor variables
    private float[] _GyroValue = new float[3];
    private float[] _AccValue = new float[3];
    private double pitch = 0, roll = 0, yaw = 0;
    private double timestamp;

    // 동작 결과
    private boolean IsRightResult = false;
    private boolean IsStayResult = false;
    private boolean IsLeftResult = false;

    private SensorState sensorState = null;

    SensorVerifyModule(@NonNull ReactApplicationContext reactContext) {
        super(reactContext);

        context = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "SensorVerify";
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                _GyroValue = event.values;
                if (!_GyroRunning)
                    _GyroRunning = true;

                break;
            case Sensor.TYPE_ACCELEROMETER:
                _AccValue = event.values;
                if (!_AccelRunning)
                    _AccelRunning = true;
                break;
        }

        // 두 센서 새로운 값을 받으면 상보필터 적용
        if (_GyroRunning && _AccelRunning) {
            double[] CalcResult = CalcSensorTestValue(event.timestamp, _GyroValue, _AccValue);
            if (CalcResult != null) {
                CalcResultValue(CalcResult);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    /**
     * 동작 종료 시간을 설정 해주는 곳 millisecond 로 넣어주어야 한다.
     **/
    @ReactMethod
    public void setPostDelayTime(int postDelayTime) {
        this.postDelayTime = postDelayTime;
    }

    // 센서 정상 작동 확인 시작
    @ReactMethod
    public void StartSensorVerify(Promise promise) {
        try {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

            WritableMap map = Arguments.createMap();
            map.putBoolean("result", true);
            promise.resolve(map);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    // 센서 정상 작동 확인 종료
    @ReactMethod
    public void EndSensorVerify(Promise promise) {
        try {
            sensorManager.unregisterListener(this);
            WritableMap map = Arguments.createMap();
            map.putBoolean("result", true);
            promise.resolve(map);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    // 센서 정상 작동 확인 - 오른쪽으로 기울이기
    @ReactMethod
    public void RightSensorVerify(Promise promise) {
        try {
            // Sensor 상태가 Right 가 아닐 경우 Right 로 변경
            if (sensorState != SensorState.Right) sensorState = SensorState.Right;

            Runnable runnable = () -> {
                WritableMap map = Arguments.createMap();
                map.putString("state", sensorState.name());
                map.putBoolean("result", IsRightResult);
                promise.resolve(map);

                Log.d(TAG, "RESULT : " + sensorState + " / " + IsRightResult);

                sensorState = null;
                IsRightResult = false;
            };

            Handler handler = new Handler();
            handler.postDelayed(runnable, postDelayTime);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    // 센서 정상 작동 확인 - 왼쪽으로 기울이기
    @ReactMethod
    public void LeftSensorVerify(Promise promise) {
        try {
            // Sensor 상태가 Left 가 아닐 경우 Right 로 변경
            if (sensorState != SensorState.Left) sensorState = SensorState.Left;

            Runnable runnable = () -> {
                WritableMap map = Arguments.createMap();
                map.putString("state", sensorState.name());
                map.putBoolean("result", IsLeftResult);

                promise.resolve(map);
                Log.d(TAG, "RESULT : " + sensorState + " / " + IsLeftResult);

                sensorState = null;
                IsLeftResult = false;
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, postDelayTime);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    // 센서 정상 작동 확인 - 세로로 세우기
    @ReactMethod
    public void StaySensorVerify(Promise promise) {
        try {
            // Sensor 상태가 Stay 가 아닐 경우 Stay 로 변경
            if (sensorState != SensorState.Stay) sensorState = SensorState.Stay;

            Runnable runnable = () -> {
                WritableMap map = Arguments.createMap();
                map.putString("state", sensorState.name());
                map.putBoolean("result", IsStayResult);

                promise.resolve(map);

                Log.d(TAG, "RESULT : " + sensorState + " / " + IsStayResult);
                sensorState = null;
                IsStayResult = false;
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, postDelayTime);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    private double[] CalcSensorTestValue(double TS, float[] _GyroValue, float[] _AccelValue) {
        double[] CalcValue;

        if (_AccelRunning && _GyroRunning) {
            _GyroRunning = false;
            _AccelRunning = false;

            // 센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break
            if (timestamp == 0) {
                timestamp = TS;
                return null;
            }

            double dt1 = (TS - timestamp) * NS2S; // ns->s 변환
            timestamp = TS;

            /* degree measure for accelerometer */
            double _AccPitch = -Math.atan2(_AccelValue[0], _AccelValue[2]) * 180.0 / Math.PI; // Y 축 기준
            double _AccRoll = Math.atan2(_AccelValue[1], _AccelValue[2]) * 180.0 / Math.PI; // X 축 기준
            double _AccYaw = Math.atan2(_AccelValue[2], _AccelValue[2]) * 180.0 / Math.PI;     // Z 축 기준

            // for using complementary filter
            float a = 0.2f;
            double temp = (1 / a) * (_AccPitch - pitch) + _GyroValue[1];
            pitch = pitch + (temp * dt1);

            temp = (1 / a) * (_AccRoll - roll) + _GyroValue[0];
            roll = roll + (temp * dt1);

            temp = (1 / a) * (_AccYaw - yaw) + _GyroValue[2];
            yaw = yaw + (temp * dt1);

//          float updateFreq = 30; // match this to your update speed
//          float cutOffFreq = 0.9f;
//          float RC = 1.0f / cutOffFreq;
//          float dt = 1.0f / updateFreq;
//          float alpha = RC / (dt + RC);

            double[] lastAccel = new double[3];
//          double[] Filter = new double[3];
//
//          Filter[0] = (alpha * (Filter[0] + roll - lastAccel[0]));
//          Filter[1] = (alpha * (Filter[1] + pitch - lastAccel[1]));
//          Filter[2] = (alpha * (Filter[2] + yaw - lastAccel[2]));

            lastAccel[0] = roll;
            lastAccel[1] = pitch;
            lastAccel[2] = yaw;

            CalcValue = lastAccel;
            return CalcValue;
        } else
            return null;
    }

    // 계산된 결과로 어떤값을 변경할지 계산
    private void CalcResultValue(double[] Result) {
        double ROLL = Result[0];
        double PITCH = Result[1];
//      double YAW = Result[2];

        if (sensorState != null) {
            switch (sensorState) {
                // Right
                case Right:
                    if (ROLL < -20 && PITCH > 0) {
                        // 만족할 경우 RightMatch_W 를 True 로 변경
                        IsRightResult = true;
                    }
                    break;
                // Stay
                case Stay:
                    if (ROLL >= 68 && ROLL <= 82) {
                    //if (ROLL >= 68 && ROLL <= 88) {
                        // 만족할 경우 StayMatch_W 를 True 로 변경
                        IsStayResult = true;
                    }
                    break;
                // Left
                case Left:
                    if (PITCH < 0 && ROLL <= -20) {
                        // 만족할 경우 LeftMatch_W 를 True 로 변경
                        IsLeftResult = true;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
