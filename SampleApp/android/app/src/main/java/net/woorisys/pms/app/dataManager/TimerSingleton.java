package net.woorisys.pms.app.dataManager;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.woorisys.pms.app.api.ParkingServiceApi;
import net.woorisys.pms.app.domain.AccelSensor;
import net.woorisys.pms.app.domain.Total;
import net.woorisys.pms.app.services.NetworkStateReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimerSingleton {
    private static final String TAG = "KTW_TimerSingleton";

    private DataManagerSingleton mDataManagerSingleton = DataManagerSingleton.getInstance();
    private boolean mLobbyTimerStart = false;
    private boolean mWholeTimerStart = false;
    private boolean mAccelTimerStart = false;
    private boolean mGyroTimerStart = false;
    // 시작 이후 수집하는 타이머
    private boolean COLLECT_START_BEACON_CALC = false;
    private boolean mNotRestart = false;
    private boolean mCollectAccelBeaconStart = false;
    private boolean mCollectLobbyStart = false;
    private boolean mStayRestartStart = false;
    private boolean mNotStartBeaconStart = false;
    /**
     * Time out 관련 Data
     **/
    private boolean mTimeoutTimerStart = false;
    private int mFirstTimeout = 60 * 1000;
    private int mSecondTimeout = 120 * 1000;
    private int mThirdTimeout = 180 * 1000;
    private CountDownTimer mTimeoutCountDownTimer;
    private CountDownTimer mLobbyTimer;
    private CountDownTimer mWholeTimer;
    private CountDownTimer mAccelTimer;
    private CountDownTimer mGyroTimer;
    private CountDownTimer mCollectAccelBeaconTimer;
    private CountDownTimer mCollectLobbyTimer;
    private CountDownTimer mStayRestartStartTimer;
    // 시작 이후 10초간 일반 비컨이 몇개들어오는지 셀 Count
    private CountDownTimer mAfterStartCountDownTimer;
    private boolean mAfterStart = false;
    // 자이로 발생이후 일반 비컨을 수집하는 타이머
    private boolean AFTER_GYRO_START_CALC = false;
    private CountDownTimer AFTER_GYRO_START_COUNTDOWN_TIMER;
    // 시작을 하였으나 로비비컨만 받고 엘리베이터 비컨을 못받았을때 상황을 확인하는 타이머
    private boolean AFTER_LOBBY_ELEVATOR_CHECK = false;
    private CountDownTimer AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER;
    private TimerTask mNotStartBeaconStartTimerTask;
    private Timer mNotStartBeaconStartTimer;
    private TimerTask Collect_START_CALC_TIMER_TASK;
    private Timer Collect_START_CALC_TIMER;

    private TimerSingleton() {
    }

    public static TimerSingleton getInstance() {
        return TimerSingletonHolder.instance;
    }

    public boolean isLobbyTimerStart() {
        return mLobbyTimerStart;
    }

    public void setLobbyTimerStart(boolean lobbyTimerStart) {
        mLobbyTimerStart = lobbyTimerStart;
    }

    public boolean isWholeTimerStart() {
        return mWholeTimerStart;
    }

    public void setWholeTimerStart(boolean wholeTimerStart) {
        mWholeTimerStart = wholeTimerStart;
    }

    public boolean isAccelTimerStart() {
        return mAccelTimerStart;
    }

    public void setAccelTimerStart(boolean accelTimerStart) {
        mAccelTimerStart = accelTimerStart;
    }

    public boolean isGyroTimerStart() {
        return mGyroTimerStart;
    }

    public void setGyroTimerStart(boolean gyroTimerStart) {
        mGyroTimerStart = gyroTimerStart;
    }

    public boolean isCOLLECT_START_BEACON_CALC() {
        return COLLECT_START_BEACON_CALC;
    }

    public void setCOLLECT_START_BEACON_CALC(boolean COLLECT_START_BEACON_CALC) {
        this.COLLECT_START_BEACON_CALC = COLLECT_START_BEACON_CALC;
    }

    public boolean isNotRestart() {
        return mNotRestart;
    }

    public void setNotRestart(boolean notRestart) {
        mNotRestart = notRestart;
    }

    public boolean isCollectAccelBeaconStart() {
        return mCollectAccelBeaconStart;
    }

    public void setCollectAccelBeaconStart(boolean collectAccelBeaconStart) {
        mCollectAccelBeaconStart = collectAccelBeaconStart;
    }

    public boolean isCollectLobbyStart() {
        return mCollectLobbyStart;
    }

    public void setCollectLobbyStart(boolean collectLobbyStart) {
        mCollectLobbyStart = collectLobbyStart;
    }

    public boolean isStayRestartStart() {
        return mStayRestartStart;
    }

    public void setStayRestartStart(boolean stayRestartStart) {
        mStayRestartStart = stayRestartStart;
    }

    public boolean isNotStartBeaconStart() {
        return mNotStartBeaconStart;
    }

    public void setNotStartBeaconStart(boolean notStartBeaconStart) {
        mNotStartBeaconStart = notStartBeaconStart;
    }

    public boolean isTimeoutTimerStart() {
        return mTimeoutTimerStart;
    }

    public void setTimeoutTimerStart(boolean timeoutTimerStart) {
        mTimeoutTimerStart = timeoutTimerStart;
    }

    public int getFirstTimeout() {
        return mFirstTimeout;
    }

    public void setFirstTimeout(int firstTimeout) {
        mFirstTimeout = firstTimeout;
    }

    public int getSecondTimeout() {
        return mSecondTimeout;
    }

    public void setSecondTimeout(int secondTimeout) {
        mSecondTimeout = secondTimeout;
    }

    public int getThirdTimeout() {
        return mThirdTimeout;
    }

    public void setThirdTimeout(int thirdTimeout) {
        mThirdTimeout = thirdTimeout;
    }

    public CountDownTimer getTimeoutCountDownTimer() {
        return mTimeoutCountDownTimer;
    }

    public void setTimeoutCountDownTimer(CountDownTimer timeoutCountDownTimer) {
        mTimeoutCountDownTimer = timeoutCountDownTimer;
    }

    public CountDownTimer getLobbyTimer() {
        return mLobbyTimer;
    }

    public void setLobbyTimer(CountDownTimer lobbyTimer) {
        mLobbyTimer = lobbyTimer;
    }

    public CountDownTimer getWholeTimer() {
        return mWholeTimer;
    }

    public void setWholeTimer(CountDownTimer wholeTimer) {
        mWholeTimer = wholeTimer;
    }

    public CountDownTimer getAccelTimer() {
        return mAccelTimer;
    }

    public void setAccelTimer(CountDownTimer accelTimer) {
        mAccelTimer = accelTimer;
    }

    public CountDownTimer getGyroTimer() {
        return mGyroTimer;
    }

    public void setGyroTimer(CountDownTimer gyroTimer) {
        mGyroTimer = gyroTimer;
    }

    public CountDownTimer getCollectAccelBeaconTimer() {
        return mCollectAccelBeaconTimer;
    }

    public void setCollectAccelBeaconTimer(CountDownTimer collectAccelBeaconTimer) {
        mCollectAccelBeaconTimer = collectAccelBeaconTimer;
    }

    public CountDownTimer getCollectLobbyTimer() {
        return mCollectLobbyTimer;
    }

    public void setCollectLobbyTimer(CountDownTimer collectLobbyTimer) {
        mCollectLobbyTimer = collectLobbyTimer;
    }

    public CountDownTimer getStayRestartStartTimer() {
        return mStayRestartStartTimer;
    }

    public void setStayRestartStartTimer(CountDownTimer stayRestartStartTimer) {
        mStayRestartStartTimer = stayRestartStartTimer;
    }

    public CountDownTimer getAfterStartCountDownTimer() {
        return mAfterStartCountDownTimer;
    }

    public void setAfterStartCountDownTimer(CountDownTimer afterStartCountDownTimer) {
        mAfterStartCountDownTimer = afterStartCountDownTimer;
    }

    public boolean isAfterStart() {
        return mAfterStart;
    }

    public void setAfterStart(boolean afterStart) {
        mAfterStart = afterStart;
    }

    public boolean isAFTER_GYRO_START_CALC() {
        return AFTER_GYRO_START_CALC;
    }

    public void setAFTER_GYRO_START_CALC(boolean AFTER_GYRO_START_CALC) {
        this.AFTER_GYRO_START_CALC = AFTER_GYRO_START_CALC;
    }

    public CountDownTimer getAFTER_GYRO_START_COUNTDOWN_TIMER() {
        return AFTER_GYRO_START_COUNTDOWN_TIMER;
    }

    public void setAFTER_GYRO_START_COUNTDOWN_TIMER(CountDownTimer AFTER_GYRO_START_COUNTDOWN_TIMER) {
        this.AFTER_GYRO_START_COUNTDOWN_TIMER = AFTER_GYRO_START_COUNTDOWN_TIMER;
    }

    public boolean isAFTER_LOBBY_ELEVATOR_CHECK() {
        return AFTER_LOBBY_ELEVATOR_CHECK;
    }

    public void setAFTER_LOBBY_ELEVATOR_CHECK(boolean AFTER_LOBBY_ELEVATOR_CHECK) {
        this.AFTER_LOBBY_ELEVATOR_CHECK = AFTER_LOBBY_ELEVATOR_CHECK;
    }

    public CountDownTimer getAFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER() {
        return AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER;
    }

    public void setAFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER(CountDownTimer AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER) {
        this.AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER = AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER;
    }

    public TimerTask getNotStartBeaconStartTimerTask() {
        return mNotStartBeaconStartTimerTask;
    }

    public void setNotStartBeaconStartTimerTask(TimerTask notStartBeaconStartTimerTask) {
        mNotStartBeaconStartTimerTask = notStartBeaconStartTimerTask;
    }

    public Timer getNotStartBeaconStartTimer() {
        return mNotStartBeaconStartTimer;
    }

    public void setNotStartBeaconStartTimer(Timer notStartBeaconStartTimer) {
        mNotStartBeaconStartTimer = notStartBeaconStartTimer;
    }

    public TimerTask getCollect_START_CALC_TIMER_TASK() {
        return Collect_START_CALC_TIMER_TASK;
    }

    public void setCollect_START_CALC_TIMER_TASK(TimerTask collect_START_CALC_TIMER_TASK) {
        Collect_START_CALC_TIMER_TASK = collect_START_CALC_TIMER_TASK;
    }

    public Timer getCollect_START_CALC_TIMER() {
        return Collect_START_CALC_TIMER;
    }

    public void setCollect_START_CALC_TIMER(Timer collect_START_CALC_TIMER) {
        Collect_START_CALC_TIMER = collect_START_CALC_TIMER;
    }

    /**
     * 로비 시작 Timer
     * 엘리베이터 비컨 -> 로비 비컨 을 받은 후 자이로가 들어오기 전까지 대기하도록 하는 Timer
     * 15분간 동작하며 해당 시간안에 자이로가 발생 안할 시에 종료 시켜버린다.
     **/
    public void StartLobbyTimer() {
        int Delay = 3 * 1000;

        if (mLobbyTimer != null)
            mLobbyTimer = null;

        mLobbyTimerStart = true;

        mLobbyTimer = new CountDownTimer(Delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mLobbyTimerStart = false;
            }
        }.start();
    }

    /**
     * 전체 Timer
     * 주차 시스템 측정 시작
     * 해당 타이머가 돌경우에 정보 수집을 한다.
     * 15분간 동작하며 타이머가 끝날시 서버로 데이터를 보낸다.
     **/
    public void StartWholeTimer(final Context context) {
        DataManagerSingleton.getInstance().Reset();

        Log.d(TAG, "START WHOLE TIMER");

        AfterStartTIMER();

        if (!COLLECT_START_BEACON_CALC) {
            START_CALC_TIMER();
        }

        if (mNotStartBeaconStart) {
            mNotStartBeaconStartTimerTask.cancel();
            mDataManagerSingleton.getNO_START_BEACON().clear();
        }

        int Delay = 900 * 1000;
        if (mWholeTimer != null) mWholeTimer = null;

        mWholeTimerStart = true;

        mWholeTimer = new CountDownTimer(Delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int Time = mDataManagerSingleton.getWholeTimerDelay();
                Time++;

                mDataManagerSingleton.setWholeTimerDelay(Time);
            }

            @Override
            public void onFinish() {
                if (COLLECT_START_BEACON_CALC) {
                    Collect_START_CALC_TIMER_TASK.cancel();
                    Collect_START_CALC_TIMER.cancel();
                    mDataManagerSingleton.setCollectStartCalcBeacon(0);
                    TimerSingleton.getInstance().setCOLLECT_START_BEACON_CALC(false);
                }

                mWholeTimerStart = false;
                COLLECT_START_BEACON_CALC = false;

                if (mAccelTimerStart) {
                    if (mAccelTimer != null) {
                        try {
                            mAccelTimer.onFinish();
                            mAccelTimer.cancel();
                        } catch (RuntimeException e) {
                            Log.d(TAG, "RunTimeException - ACCEL TIMER FINISH : " + e.getMessage());
                        }
                    }
                }

                if (mGyroTimerStart) {
                    if (mGyroTimer != null) {
                        try {
                            mGyroTimer.onFinish();
                            mGyroTimer.cancel();
                        } catch (RuntimeException e) {
                            Log.d(TAG, "RunTimeException - GYRO TIMER FINISH : " + e.getMessage());
                        }
                    }
                }

                if (mDataManagerSingleton.isOutParking() && !mDataManagerSingleton.isABNORMAL_END()) {
//                    ServerData serverData=new ServerData(context);
//                    serverData.OutParking();
                } else {
                    String ParingState = mDataManagerSingleton.getParingStateValue();
                    if (mDataManagerSingleton.isABNORMAL_END()) {
                        ParingState += "-end";
                    }

                    if (mDataManagerSingleton.isLobbyBeaconEnd()) {
                        ParingState += "-lobby";
                    }

                    Log.d(TAG, "Test After Start - COUNT : " + mDataManagerSingleton.getAfterStartCount() + "PARING STATE : " + ParingState);

                    SaveArrayListValue saveArrayListValue = new SaveArrayListValue();
                    saveArrayListValue.SaveAccelBeacon();

                    Total total = new Total();
                    total.setPhoneInfo(UserDataSingleton.getInstance().getPhoneUid());
                    total.setBeaconList(mDataManagerSingleton.getBeaconArrayList());
                    total.setGyroList(mDataManagerSingleton.getGyroSensorArrayList());
                    total.setSensorList(mDataManagerSingleton.getAccelSensorArrayList());
                    total.setAccelBeaconList(mDataManagerSingleton.getAccelBeaconArrayList());
                    total.setInputDate(mDataManagerSingleton.getInputTime());
                    total.setParingState(ParingState);

                    mDataManagerSingleton.getTotalArrayList().add(total);

                    String LogValue = String.format(Locale.getDefault(), "TOTAL ---- PHONE: %s, INPUT_DATE: %s, STATE %s,  BEACON : %d , GYRO : %d , ACCEL : %d , ACCELBEACON : %d",
                            total.getPhoneInfo(),
                            total.getInputDate(),
                            total.getParingState(),
                            total.getBeaconList().size(),
                            total.getGyroList().size(),
                            total.getSensorList().size(),
                            total.getAccelBeaconList().size());
                    Log.d(TAG, "TimerSingleton: " + LogValue);
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected) {
                        mDataManagerSingleton.setSAVE_DELAY(mDataManagerSingleton.getWholeTimerDelay());
                        // Send Parking Complete message to server
                        ParkingServiceApi.getInstance().ParkingComplete(context, total);

                        mDataManagerSingleton.setParingStateValue("non-paring");
                    } else {
                        mDataManagerSingleton.setSAVE_DELAY(mDataManagerSingleton.getWholeTimerDelay());
                        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

                        NetworkStateReceiver receiver = new NetworkStateReceiver();
                        context.registerReceiver(receiver, filter);
                    }

                    if (mDataManagerSingleton.isOutParking()) {
                        ParkingServiceApi.getInstance().ParkingOut();
                    }
                }
                mDataManagerSingleton.setWholeTimerDelay(0);
            }
        }.start();
    }

    public void AccelTimer() {
        int Delay = 2 * 1000;

        if (mAccelTimer != null) mAccelTimer = null;

        mAccelTimerStart = true;

        mAccelTimer = new CountDownTimer(Delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mAccelTimerStart = false;

                SaveArrayListValue saveArrayListValue = new SaveArrayListValue();

                String accelResult = saveArrayListValue.AccelSensorResult();

                String preState = mDataManagerSingleton.getPreState();
                if (preState == null) {
                    mDataManagerSingleton.setPreState(accelResult);
                } else {
                    // 이전값이 T 이고 현재 값이 S/W 일경우 타이머를 돌린다.
                    if (preState.equals("T") && (accelResult.equals("S") || accelResult.equals("W"))) {
                        if (!mCollectAccelBeaconStart) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                // only for gingerbread and newer versions
                                StartCollectAccelBeacon();
                            } else {
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(() -> StartCollectAccelBeacon());
                            }
                        }
                    }

                    mDataManagerSingleton.setPreState(accelResult);
                }

                if (TimerSingleton.getInstance().isWholeTimerStart()) {
                    AccelSensor accelSensor = new AccelSensor();
                    accelSensor.setState(accelResult);
                    accelSensor.setSeq(String.valueOf(mDataManagerSingleton.getAccelSequence()));
                    accelSensor.setDelay(String.valueOf(mDataManagerSingleton.getWholeTimerDelay()));

                    mDataManagerSingleton.getAccelSensorArrayList().add(accelSensor);
                }

                int accelSensorSequence = mDataManagerSingleton.getAccelSequence() + 1;
                mDataManagerSingleton.setAccelSequence(accelSensorSequence);

                mDataManagerSingleton.setAccelCount(0);
            }
        }.start();
    }

    public void GyroTimer() {
        int delay = 1000;   // 1 Second

        if (mGyroTimer != null) mGyroTimer = null;

        mGyroTimerStart = true;

        mGyroTimer = new CountDownTimer(delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mGyroTimerStart = false;
            }
        }.start();
    }

    public void StartCollectAccelBeacon() {
        mCollectAccelBeaconStart = true;

        mCollectAccelBeaconTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mCollectAccelBeaconStart = false;
            }
        }.start();
    }

    public void StartCollectLobby(final Context context) {
        mCollectLobbyStart = true;

        mCollectLobbyTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mCollectLobbyStart = false;

                if (!mNotRestart) {
                    if (mDataManagerSingleton.getINOUT_DATA_MAJOR().size() == 0) {
                        Log.d("TAG_EST_BEACON_VALUE", "END VALUE : " + mDataManagerSingleton.getLAST_BEACON());
                        if (mDataManagerSingleton.getLAST_BEACON() == 3) {
                            if (isWholeTimerStart()) {
                                try {
                                    mWholeTimer.onFinish();
                                    mWholeTimer.cancel();
                                } catch (RuntimeException e) {
                                    Log.e(TAG, "RuntimeException - Whole Timer Finish : " + e.getMessage());
                                }
                            }
                        } else {
                            mDataManagerSingleton.getINOUT_DATA_MAJOR().clear();
                            StartCollectLobby(context);
                        }
                    } else {
                        int Last = mDataManagerSingleton.getINOUT_DATA_MAJOR().get(mDataManagerSingleton.getINOUT_DATA_MAJOR().size() - 1);

                        mDataManagerSingleton.setLAST_BEACON(Last);
                        mDataManagerSingleton.getINOUT_DATA_MAJOR().clear();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            StartCollectLobby(context);
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> StartCollectLobby(context));
                        }
                    }
                } else {
                    mDataManagerSingleton.getINOUT_DATA_MAJOR().clear();
                }
            }
        }.start();
    }

    public void StartStayRestart() {
        mStayRestartStart = true;

        mStayRestartStartTimer = new CountDownTimer(900 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mStayRestartStart = false;

                mDataManagerSingleton.setRESTART_BEACON(false);
                mDataManagerSingleton.setEnd1Beacon(false);
                mDataManagerSingleton.setEnd2Beacon(false);
            }
        }.start();
    }

    public void NotStartBeacon() {
        mNotStartBeaconStart = true;

        mNotStartBeaconStartTimerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "TAG_TEST_NOT_START - VALUE BEACON : " + mDataManagerSingleton.getNO_START_BEACON().size());
                mDataManagerSingleton.getNO_START_BEACON().clear();
            }
        };

        mNotStartBeaconStartTimer = new Timer();
        mNotStartBeaconStartTimer.schedule(mNotStartBeaconStartTimerTask, 10000, 10000);
    }

    public void START_CALC_TIMER() {
        COLLECT_START_BEACON_CALC = true;

        Collect_START_CALC_TIMER_TASK = new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG, "TEST_TIMER_SINGLETON - 30초");
                if (mDataManagerSingleton.getCollectStartCalcBeacon() != 0) {
                    Log.d(TAG, "TEST_TIMER_SINGLETON_VAL - ACCEL_BEACON 0개 아님 종료 처리 X : " + mDataManagerSingleton.getCollectStartCalcBeacon());
                    mDataManagerSingleton.setCollectStartCalcBeacon(0);
                } else {
                    // 전체 타이머가 돌고있을 경우 전체 타이머 종료
                    if (mWholeTimerStart) {
                        if (!mCollectLobbyStart) {
                            if (!mDataManagerSingleton.isEnd1Beacon()) {
                                mDataManagerSingleton.setABNORMAL_END(true);

                                Log.d(TAG, "TEST_TIMER_SINGLETON_VAL - ACCEL_BEACON 0개 종료 처리");
                                mDataManagerSingleton.setOutParking(true);

                                if (isWholeTimerStart()) {
                                    try {
                                        mWholeTimer.onFinish();
                                        mWholeTimer.cancel();
                                    } catch (RuntimeException e) {
                                        Log.e(TAG, "RuntimeException - Whole Timer Finish : " + e.getMessage());
                                    }
                                }
                                Collect_START_CALC_TIMER_TASK.cancel();
                                Collect_START_CALC_TIMER.cancel();
                            }
                        }
                    }
                    // 초기화
                    mDataManagerSingleton.setCollectStartCalcBeacon(0);
                }
            }
        };

        Collect_START_CALC_TIMER = new Timer();
        Collect_START_CALC_TIMER.schedule(Collect_START_CALC_TIMER_TASK, 30000, 30000);
    }

    public void SEND_TIMEOUT(final Context context) {
        int count = mDataManagerSingleton.getTimeoutCount();

        int DelayTimer = mFirstTimeout;

        if (count == 1) {
            DelayTimer = mSecondTimeout;
        } else if (count == 2) {
            DelayTimer = mThirdTimeout;
        }

        if (!mTimeoutTimerStart) {
            mTimeoutTimerStart = true;
            count++;
            mDataManagerSingleton.setTimeoutCount(count);

            mTimeoutCountDownTimer = new CountDownTimer(DelayTimer, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mTimeoutTimerStart = false;

                    ParkingServiceApi.getInstance()
                            .ParkingComplete(context, mDataManagerSingleton.getCAN_NOT_SEND_TOTAL_SAVE());
                }
            }.start();
        }
    }

    public void AfterStartTIMER() {
        if (!mAfterStart) {
            mAfterStart = true;
            mAfterStartCountDownTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    mAfterStart = false;

                    String ParingState;

                    if (mDataManagerSingleton.getAfterStartCount() < 10) {
                        ParingState = "paring";
                    } else {
                        ParingState = "non-paring";
                    }

                    mDataManagerSingleton.setParingStateValue(ParingState);
                }
            }.start();
        }
    }

    public void AFTER_GYRO_START_TIMER(final Context context) {
        AFTER_GYRO_START_CALC = true;
        AFTER_GYRO_START_COUNTDOWN_TIMER = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (mDataManagerSingleton.getAfterGyroCount() != 0) {
                    if (!TimerSingleton.getInstance().isWholeTimerStart()) {
                        Log.d(TAG, "TAG_GYRO_START - IS WHOLE : " + TimerSingleton.getInstance().isWholeTimerStart());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // only for gingerbread and newer versions
                            TimerSingleton.getInstance().StartWholeTimer(context);
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> TimerSingleton.getInstance().StartWholeTimer(context));
                        }

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
                        mDataManagerSingleton.setInputTime(currentDate);

                        ParkingServiceApi.getInstance()
                                .GateInformation("GYRO_START", "GYRO_START");

                        try {
                            AFTER_GYRO_START_COUNTDOWN_TIMER.onFinish();
                            AFTER_GYRO_START_COUNTDOWN_TIMER.cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - AFTER GYRO START TIME ON FINISH : " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                AFTER_GYRO_START_CALC = false;

                ParkingServiceApi.getInstance()
                        .GyroSend(Integer.toString(mDataManagerSingleton.getAfterGyroCount()));
            }
        }.start();
    }

    public void AFTER_LOBBY_ELEVATOR_TIMER() {
        AFTER_LOBBY_ELEVATOR_CHECK = true;
        AFTER_LOBBY_ELEVATOR_CHECK_COUNTDOWN_TIMER = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (mDataManagerSingleton.getAfterLobbyEleCount() == 0) {
                    if (mWholeTimerStart && !mDataManagerSingleton.isElevatorBeaconGet()) {
                        Log.d(TAG, "TAG_BEACON_FUNCTION - END LOBBY");
                        mDataManagerSingleton.setLobbyBeaconEnd(true);
                        try {
                            mWholeTimer.onFinish();
                            mWholeTimer.cancel();
                        } catch (RuntimeException e) {
                            Log.e(TAG, "RuntimeException - Whole Timer Finish : " + e.getMessage());
                        }
                        AFTER_LOBBY_ELEVATOR_CHECK = false;
                    } else {
                        AFTER_LOBBY_ELEVATOR_CHECK = false;
                        Log.d(TAG, "TAG_BEACON_FUNCTION - END LOBBY NO MATCH");
                    }
                } else {
                    AFTER_LOBBY_ELEVATOR_CHECK = false;
                    AFTER_LOBBY_ELEVATOR_TIMER();
                    Log.d(TAG, "TAG_BEACON_FUNCTION - WHOLE_TIMER : " + mWholeTimerStart + " , " + mDataManagerSingleton.isElevatorBeaconGet() + " , COUNT : " + mDataManagerSingleton.getAfterLobbyEleCount());
                    mDataManagerSingleton.setAfterLobbyEleCount(0);
                }
            }
        }.start();
    }

    private static class TimerSingletonHolder {
        private static final TimerSingleton instance = new TimerSingleton();
    }
}
