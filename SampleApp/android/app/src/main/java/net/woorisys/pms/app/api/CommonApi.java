package net.woorisys.pms.app.api;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import net.woorisys.pms.app.dataManager.UserDataSingleton;

import java.util.UUID;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommonApi {
    private static IMobileService mMobileService = null;

    private static Retrofit makeRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static IMobileService getMobileServiceInstance() {
        if (mMobileService == null) {
            // Get Base URL
            UserDataSingleton userDataSingleton = UserDataSingleton.getInstance();
            String baseUrl = userDataSingleton.getBaseUrl();
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

            Retrofit retrofit = makeRetrofit(baseUrl);
            mMobileService = retrofit.create(IMobileService.class);
        }
        return mMobileService;
    }

    public static JsonObject makeRequestJson(MobileMessageType messageType, JsonObject body) {
        JsonObject request = new JsonObject();
        request.addProperty("id", UUID.randomUUID().toString());
        request.addProperty("type", messageType.toString());
        request.addProperty("version", 1);
        request.addProperty("phoneUid", UserDataSingleton.getInstance().getPhoneUid());
        request.add("body", body);

        return request;
    }

    public static JsonObject onRetrofitResponse(String tag, String function, Response<JsonObject> response) {
        if (response.isSuccessful()) {
            if (response.body() != null) {
                Log.d(tag, function + " onResponse: " + response.body().toString());

                if (!response.body().has("error")) {
                    JsonObject body = response.body().getAsJsonObject("body");
                    return body == null ? new JsonObject() : body;
                }
                JsonObject error = response.body().getAsJsonObject("error");
                if (error != null) {
                    Log.e(tag, function + " onResponse Error: " + error.get("message").toString());
                    throw new PlatformException(error.get("code").toString(), error.get("message").toString(), error.get("detail").toString());
                }
            }
        } else {
            Log.e(tag, function + " onResponse received fail: " + response.toString());
        }
        throw new PlatformException("99", "", "알수 없는 오류가 발생하였습니다.");
    }

    public static void onRetrofitFailure(String tag, String function, Throwable t) {
        t.printStackTrace();
        Log.e(tag, function + " onFailure receive: " + t.getMessage());
        throw new PlatformException("24", "", "해당 단지의 관리서버에 연결할 수 없습니다.");
    }

    /**
     * isRunningService
     * @param context Context
     * @param serviceClass Class<?>
     * @return boolean
     */
    public static boolean isRunningService(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
