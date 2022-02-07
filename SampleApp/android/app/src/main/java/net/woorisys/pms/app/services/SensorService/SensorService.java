package net.woorisys.pms.app.services.SensorService;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import net.woorisys.pms.app.dataManager.DataManagerSingleton;
import net.woorisys.pms.app.dataManager.SaveArrayListValue;
import net.woorisys.pms.app.dataManager.TimerSingleton;
import net.woorisys.pms.app.dataManager.UserDataSingleton;
import net.woorisys.pms.app.services.BeaconService.Function.KalmanFilter;
import net.woorisys.pms.app.services.NotificationService.NotificationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.view.KeyCharacterMap.ALPHA;
import static net.woorisys.pms.app.services.NotificationService.NotificationService.FOREGROUND_NOTIFICATION_ID;

public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "KTW_SensorService";

    SensorManager SensorManager_W;
    Sensor AccelSensor_W;
    Sensor GyroSensor_W;

    int RollResultCount = 0, PitchResultCount = 0, YawResultCount = 0;

    int PreRollCount = 0;
    int PrePitchCount = 0;
    int PreYawCount = 0;

    float NextValue = 0;      //현재 CVA 값
    float PreValue = 0;       //이전 CVA 값
    int DefaultAbsValue = 4;  //기본 Default PreValue-NextValue 절대값

    boolean BMatchValue = true;
    int IMatchValue = 0;
    boolean SR = false;
    boolean SP = false;
    boolean SY = false;
    int limitValue = 100;
    private KalmanFilter kalmanX;
    private KalmanFilter kalmanY;
    private KalmanFilter kalmanZ;

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SensorManager_W = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        AccelSensor_W = SensorManager_W.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroSensor_W = SensorManager_W.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        SensorManager_W.registerListener(this, AccelSensor_W, SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        SensorManager_W.registerListener(this, GyroSensor_W, SensorManager.SENSOR_STATUS_ACCURACY_LOW);

        kalmanX = new KalmanFilter(0.0f);
        kalmanY = new KalmanFilter(0.0f);
        kalmanZ = new KalmanFilter(0.0f);

        Log.d(TAG, "onCreate: Created Sensor Service....");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Destroy Sensor Service....");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = NotificationService.getInstance().createForegroundNotification(this, UserDataSingleton.getInstance().getPurpose());
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        Log.d(TAG, "onStartCommand: Start Sensor Service....");
        return START_NOT_STICKY;
        //return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float CVA;

            // CVA 값 계산
            float[] accelData = new float[3];
            accelData = filter(event.values.clone(), accelData);
            CVA = (float) Math.sqrt(accelData[0] * accelData[0] + accelData[1] * accelData[1] + accelData[2] * accelData[2]);
            
            //Log.d(TAG, "CVA : " + CVA);

            // 이전 값이 있을 경우에는 계산을 진행한다.
            if (PreValue != 0) {

                NextValue = CVA;

                float ABSValue = Math.abs(PreValue - NextValue);

                if (ABSValue >= DefaultAbsValue) {
                    int accel_count = DataManagerSingleton.getInstance().getAccelCount() + 1;
                    DataManagerSingleton.getInstance().setAccelCount(accel_count);
                }
                PreValue = NextValue;
            } else
                PreValue = CVA;

            if (!TimerSingleton.getInstance().isAccelTimerStart()) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // only for gingerbread and newer versions
                    TimerSingleton.getInstance().AccelTimer();

                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> TimerSingleton.getInstance().AccelTimer());
                }
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            final float LIMIT_MAX = 0.5f;
            final float LIMIT_MIN = -0.5f;

            double Roll = event.values[0];
            double Pitch = event.values[1];
            double Yaw = event.values[2];

            if (!TimerSingleton.getInstance().isGyroTimerStart()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // only for gingerbread and newer versions
                    TimerSingleton.getInstance().GyroTimer();
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> TimerSingleton.getInstance().GyroTimer());
                }
            } else {
                if ((Roll > LIMIT_MAX || Roll < LIMIT_MIN) || (Pitch > LIMIT_MAX || Pitch < LIMIT_MIN) || (Yaw > LIMIT_MAX || Yaw < LIMIT_MIN)) {
                    kalmanX.Init();
                    kalmanY.Init();
                    kalmanZ.Init();
                } else {
                    double FilterX = kalmanX.Update(Roll);
                    double FilterY = kalmanY.Update(Pitch);
                    double FilterZ = kalmanZ.Update(Yaw);

                    GyroSensorResult((FilterX), (FilterY), (FilterZ));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //Sensor X,Y,Z 값 을 CVA 로 사용할 Data 변환 시켜주는 코드
    private float[] filter(float[] input, float[] output) {

        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void GyroSensorResult(double Roll, double Pitch, double Yaw) {

        final float LimitMinus = -0.025f;
        final float LimitPlus = 0.025f;

        DataManagerSingleton dataManagerSingleton = DataManagerSingleton.getInstance();

        if (dataManagerSingleton.ROLL_QUEUE.size() == 4) {
            Object[] RollA = dataManagerSingleton.ROLL_QUEUE.toArray();

            if (!(((double) RollA[0] < LimitPlus && (double) RollA[0] > LimitMinus) || ((double) RollA[1] < LimitPlus && (double) RollA[1] > LimitMinus) || ((double) RollA[2] < LimitPlus && (double) RollA[2] > LimitMinus) || ((double) RollA[3] < LimitPlus && (double) RollA[3] > LimitMinus))) {
                RollResultCount++;
            } else {
                if (RollResultCount >= limitValue) {
                    SR = true;
                    PreRollCount = RollResultCount;
                }
                RollResultCount = 0;
            }

            double FIRSTR = dataManagerSingleton.ROLL_QUEUE.poll();
            double SECONDR = dataManagerSingleton.ROLL_QUEUE.poll();
            double THIRDR = dataManagerSingleton.ROLL_QUEUE.poll();
            double FORTHR = dataManagerSingleton.ROLL_QUEUE.poll();

            dataManagerSingleton.ROLL_QUEUE.offer(SECONDR);
            dataManagerSingleton.ROLL_QUEUE.offer(THIRDR);
            dataManagerSingleton.ROLL_QUEUE.offer(FORTHR);
            dataManagerSingleton.ROLL_QUEUE.offer(Roll);
        } else {
            dataManagerSingleton.ROLL_QUEUE.offer(Roll);
        }

        if (dataManagerSingleton.PITCH_QUEUE.size() == 4) {
            Object[] PITCHA = dataManagerSingleton.PITCH_QUEUE.toArray();

            if (!(((double) PITCHA[0] < LimitPlus && (double) PITCHA[0] > LimitMinus) || ((double) PITCHA[1] < LimitPlus && (double) PITCHA[1] > LimitMinus) || ((double) PITCHA[2] < LimitPlus && (double) PITCHA[2] > LimitMinus) || ((double) PITCHA[3] < LimitPlus && (double) PITCHA[3] > LimitMinus))) {
                PitchResultCount++;

            } else {
                if (PitchResultCount >= limitValue) {
                    SP = true;
                    PrePitchCount = PitchResultCount;
                }
                PitchResultCount = 0;
            }

            double FIRSTP = dataManagerSingleton.PITCH_QUEUE.poll();
            double SECONDP = dataManagerSingleton.PITCH_QUEUE.poll();
            double THIRDP = dataManagerSingleton.PITCH_QUEUE.poll();
            double FORTHP = dataManagerSingleton.PITCH_QUEUE.poll();

            dataManagerSingleton.PITCH_QUEUE.offer(SECONDP);
            dataManagerSingleton.PITCH_QUEUE.offer(THIRDP);
            dataManagerSingleton.PITCH_QUEUE.offer(FORTHP);
            dataManagerSingleton.PITCH_QUEUE.offer(Pitch);
        } else {
            dataManagerSingleton.PITCH_QUEUE.offer(Pitch);
        }

        if (dataManagerSingleton.YAW_QUEUE.size() == 4) {
            Object[] YAWA = dataManagerSingleton.YAW_QUEUE.toArray();

            if (!(((double) YAWA[0] < LimitPlus && (double) YAWA[0] > LimitMinus) ||
                  ((double) YAWA[1] < LimitPlus && (double) YAWA[1] > LimitMinus) ||
                  ((double) YAWA[2] < LimitPlus && (double) YAWA[2] > LimitMinus) ||
                  ((double) YAWA[3] < LimitPlus && (double) YAWA[3] > LimitMinus))) {
                YawResultCount++;
            } else {
                if (YawResultCount >= limitValue) {
                    SY = true;
                    PreYawCount = YawResultCount;
                }
                YawResultCount = 0;
            }

            double FIRSTY = dataManagerSingleton.YAW_QUEUE.poll();
            double SECONDY = dataManagerSingleton.YAW_QUEUE.poll();
            double THIRDY = dataManagerSingleton.YAW_QUEUE.poll();
            double FORTH = dataManagerSingleton.YAW_QUEUE.poll();

            dataManagerSingleton.YAW_QUEUE.offer(SECONDY);
            dataManagerSingleton.YAW_QUEUE.offer(THIRDY);
            dataManagerSingleton.YAW_QUEUE.offer(FORTH);
            dataManagerSingleton.YAW_QUEUE.offer(Yaw);
        } else {
            dataManagerSingleton.YAW_QUEUE.offer(Yaw);
        }

        if ((PreRollCount >= limitValue || PrePitchCount >= limitValue || PreYawCount >= limitValue) && (RollResultCount == 0 && PreRollCount != 0) || (PitchResultCount == 0 && PrePitchCount != 0) || (YawResultCount == 0 && PreYawCount != 0)) {
            IMatchValue++;
            BMatchValue = true;

            dataManagerSingleton.setSaveCountRoll(PreRollCount);
            dataManagerSingleton.setSaveCountPitch(PrePitchCount);
            dataManagerSingleton.setSaveCountYaw(PreYawCount);

            SR = false;
            SP = false;
            SY = false;
            PreRollCount = 0;
            PrePitchCount = 0;
            PreYawCount = 0;
        } else {
            BMatchValue = false;
            if (IMatchValue != 0) {
                if (dataManagerSingleton.isRESTART_BEACON() && !TimerSingleton.getInstance().isWholeTimerStart() && DataManagerSingleton.getInstance().getPreState().equals("T") && TimerSingleton.getInstance().isStayRestartStart()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // only for gingerbread and newer versions
                        TimerSingleton.getInstance().StartWholeTimer(getApplicationContext());
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> TimerSingleton.getInstance().StartWholeTimer(getApplicationContext()));
                    }

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
                    dataManagerSingleton.setInputTime("move_" + currentDate);

                    if (TimerSingleton.getInstance().isStayRestartStart()) {
                        try {
                            TimerSingleton.getInstance().getStayRestartStartTimer().onFinish();
                            TimerSingleton.getInstance().getStayRestartStartTimer().cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - STY RESTART START ONFINISH : " + e.getMessage());
                        }
                    }
                }

                if (!TimerSingleton.getInstance().isWholeTimerStart()) {
                    if (!TimerSingleton.getInstance().isAFTER_GYRO_START_CALC()) {
                        TimerSingleton.getInstance().AFTER_GYRO_START_TIMER(getApplicationContext());
                    }
                }

                if (TimerSingleton.getInstance().isWholeTimerStart()) {
                    SaveArrayListValue saveArrayListValue = new SaveArrayListValue();
                    saveArrayListValue.SaveGyro();
                }

                BMatchValue = false;
                IMatchValue = 0;

                PitchResultCount = 0;
                RollResultCount = 0;
                YawResultCount = 0;
            }
        }
    }
}
