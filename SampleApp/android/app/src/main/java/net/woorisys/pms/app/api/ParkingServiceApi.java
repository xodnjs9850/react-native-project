package net.woorisys.pms.app.api;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import android.content.res.Resources;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.woorisys.pms.app.R;
import net.woorisys.pms.app.dataManager.DataManagerSingleton;
import net.woorisys.pms.app.dataManager.TimerSingleton;
import net.woorisys.pms.app.dataManager.UserDataSingleton;
import net.woorisys.pms.app.domain.OnepassBeacon;
import net.woorisys.pms.app.domain.Total;
import net.woorisys.pms.app.services.NotificationService.NotificationService;
import net.woorisys.pms.app.api.PlatformException;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ParkingServiceApi {
    private static final String TAG = "KTW_ParkingServiceApi";
    private static boolean mRunOpenDoor = false;

    private IMobileService mMobileService = null;
    private String mBearerAccessToken = "";
    private final Gson mGson = new Gson();

    private ParkingServiceApi() {
        if (this.mMobileService == null) {
            this.mMobileService = CommonApi.getMobileServiceInstance();
            this.mBearerAccessToken = UserDataSingleton.getInstance().getAuthorization();
        }
    }

    public static ParkingServiceApi getInstance() {
        return ParkingServiceApiHolder.INSTANCE;
    }

    // Onepass 문열림
    public void OpenLobby(Context context, OnepassBeacon onepassBeacon, double rssi) {
        if (mRunOpenDoor) {
            return;
        } else {
            mRunOpenDoor = true;
        }

        try {
            // 최근에 OpenLobby API에 성공한 Beacon Signal 이 일정 시간내에 있으면 다시 보내지 않음.
            Date now = new Date();
            Date lastDetectedAt = onepassBeacon.getLastDetectedAt();
            if (lastDetectedAt != null && (now.getTime() - lastDetectedAt.getTime()) <= UserDataSingleton.getInstance().getSendInterval()) {
                mRunOpenDoor = false;
                return;
            }

            Log.d(TAG, "입장");

            Resources resources = context.getResources();
            String lobbyName = onepassBeacon.getName();

            final TimerSingleton timerSingleton = TimerSingleton.getInstance();

            JsonObject body = new JsonObject();
            body.addProperty("sid", onepassBeacon.getSid());
            body.addProperty("major", onepassBeacon.getMajor());
            body.addProperty("minor", onepassBeacon.getMinor());
            body.addProperty("rssi", rssi);
            JsonObject request = CommonApi.makeRequestJson(MobileMessageType.DetectBeaconSignal, body);

            Call<JsonObject> requestService = mMobileService.mobileService(this.mBearerAccessToken, request);
            requestService.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        CommonApi.onRetrofitResponse(TAG, "OpenLobby", response);
                        apiNotification(context, resources.getString(R.string.notification_onepass_success, lobbyName), true);
                        onepassBeacon.setLastDetectedAt(new Date());
                    } catch (Exception ex) {
                        // 응답이 실패로 왔을 경우 Timer 를 종료 시켜 바로 받을 수 있도록 처리해준다.
                        if (timerSingleton.isLobbyTimerStart()) {
                            try {
                                timerSingleton.getLobbyTimer().onFinish();
                                timerSingleton.getLobbyTimer().cancel();
                            } catch (RuntimeException e) {
                                Log.e(TAG, "LOBBY TIMER FINISH EXCEPTION : " + e.getMessage());
                            }
                        }
                        apiNotification(context, resources.getString(R.string.notification_onepass_fail, lobbyName, ex.getMessage()), false);
                    } finally {
                        mRunOpenDoor = false;
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                @EverythingIsNonNull
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // 응답이 실패로 왔을 경우 Timer 를 종료 시켜 바로 받을 수 있도록 처리해준다.
                    try {
                        if (timerSingleton.isLobbyTimerStart()) {
                            try {
                                timerSingleton.getLobbyTimer().onFinish();
                                timerSingleton.getLobbyTimer().cancel();
                            } catch (RuntimeException e) {
                                Log.e(TAG, "LOBBY TIMER FINISH EXCEPTION : " + e.getMessage());
                            }
                        }
                        try {
                            CommonApi.onRetrofitFailure(TAG, "OpenLobby", t);
                        } catch (PlatformException ex) {
                            apiNotification(context, resources.getString(R.string.notification_onepass_fail, lobbyName, ex.getMessage()), false);
                        }
                    } finally {
                        mRunOpenDoor = false;
                    }
                }
            });
        } catch (Exception ex) {
            mRunOpenDoor = false;
        }
    }

    // 주차 완료
    public void ParkingComplete(Context context,final Total total) {
//        total.getPhoneInfo();
//        total.getInputDate();
//        total.getBeaconList();
//        total.getAccelBeaconList();
//        total.getGyroList();
//        total.getParingState();
//        total.getSensorList();
        Log.v(TAG, ".................." + mGson.toJson(total));

        // Check Total Instance
        Resources resources = context.getResources();

        // 주차 시작 이후 진행 시간이 1분 이하일 경우 데이터를 전송하지 않는다.
        if (DataManagerSingleton.getInstance().getSAVE_DELAY() >= 60) {
//          Toast.makeText(context, "SEND SERVER", Toast.LENGTH_SHORT).show();
            UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();

            JsonObject body = new JsonObject();
            body.addProperty("dong", userDataSingleton.getDong());
            body.addProperty("ho", userDataSingleton.getHo());
            body.add("total", mGson.toJsonTree(total));
            JsonObject request = CommonApi.makeRequestJson(MobileMessageType.ParkingComplete, body);

            Call<JsonObject> requestService = mMobileService.mobileService(this.mBearerAccessToken, request);
            requestService.enqueue(new Callback<JsonObject>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    DataManagerSingleton.getInstance().Reset();
                    try {
                        CommonApi.onRetrofitResponse(TAG, "ParkingComplete", response);
                        apiNotification(context, resources.getString(R.string.notification_parking_success), true);
                    } catch (PlatformException ex) {
                        apiNotification(context, resources.getString(R.string.notification_parking_fail, ex.getMessage()), false);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                @EverythingIsNonNull
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    DataManagerSingleton.getInstance().setCAN_NOT_SEND_TOTAL_SAVE(total);
                    if (!TimerSingleton.getInstance().isTimeoutTimerStart()) {
                        if (DataManagerSingleton.getInstance().getTimeoutCount() < 4) {
                            TimerSingleton.getInstance().SEND_TIMEOUT(context);
                        } else {
                            DataManagerSingleton.getInstance().Reset();
                        }
                    }
                    try {
                        CommonApi.onRetrofitFailure(TAG, "ParkingComplete", t);
                    } catch (PlatformException ex) {
                        apiNotification(context, resources.getString(R.string.notification_parking_fail, ex.getMessage()), false);
                    }
                }
            });
        } else {
            DataManagerSingleton.getInstance().Reset();
        }
    }

    // 주차위치 이탈
    public void ParkingOut() {
        UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();

        JsonObject body = new JsonObject();
        body.addProperty("dong", userDataSingleton.getDong());
        body.addProperty("ho", userDataSingleton.getHo());
        JsonObject request = CommonApi.makeRequestJson(MobileMessageType.ParkingOut, body);

        Call<JsonObject> requestService = mMobileService.mobileService(this.mBearerAccessToken, request);
        requestService.enqueue(new Callback<JsonObject>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                DataManagerSingleton.getInstance().Reset();
                try {
                    CommonApi.onRetrofitResponse(TAG, "ParkingOut", response);
                } catch (PlatformException ex) {
                    //
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<JsonObject> call, Throwable t) {
                DataManagerSingleton.getInstance().Reset();
            }
        });
    }

    // GateInformation
    public void GateInformation(String major, final String minor) {

        Log.d(TAG, "진입");

        JsonObject body = new JsonObject();
        body.addProperty("major", major);
        body.addProperty("minor", minor);
        JsonObject request = CommonApi.makeRequestJson(MobileMessageType.ParkingGateInformation, body);

        Call<JsonObject> requestService = mMobileService.mobileService(this.mBearerAccessToken, request);
        requestService.enqueue(new Callback<JsonObject>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    CommonApi.onRetrofitResponse(TAG, "GateInformation", response);
                } catch (PlatformException ex) {
                    //
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //
            }
        });
    }

    // SendGyro
    public void GyroSend(String count) {
        JsonObject body = new JsonObject();
        body.addProperty("count", count);
        JsonObject request = CommonApi.makeRequestJson(MobileMessageType.ParkingGyroInformation, body);

        Call<JsonObject> requestService = mMobileService.mobileService(this.mBearerAccessToken, request);
        requestService.enqueue(new Callback<JsonObject>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                try {
                    CommonApi.onRetrofitResponse(TAG, "GyroSend", response);
                } catch (PlatformException ex) {
                    //
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void apiNotification(Context context, String content, boolean isSuccess) {
        int color = isSuccess ? context.getResources().getColor(R.color.default_app_background, null) :
                context.getResources().getColor(R.color.fail_icon_background, null);

        // Open Lobby Notification
        NotificationService.getInstance().openDoorNotification(context, content, color);
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class ParkingServiceApiHolder {
        static final ParkingServiceApi INSTANCE = new ParkingServiceApi();
    }
}
