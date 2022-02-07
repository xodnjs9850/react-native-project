package net.woorisys.pms.app.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.LocaleData;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import net.woorisys.pms.app.api.ParkingServiceApi;
import net.woorisys.pms.app.dataManager.DataManagerSingleton;
import net.woorisys.pms.app.dataManager.TimerSingleton;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "KTW_NetworkStateReceiver";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Toast.makeText(context, "인터넷이 다시 연결되었습니다.", Toast.LENGTH_SHORT)
                            .show();

                    if (!TimerSingleton.getInstance().isWholeTimerStart()) {
                        DataManagerSingleton dataManagerSingleton = DataManagerSingleton.getInstance();
                        // Send Parking Complete message to server
                        ParkingServiceApi.getInstance()
                                .ParkingComplete(context, dataManagerSingleton.getTotalArrayList().get(dataManagerSingleton.getTotalArrayList().size() - 1));
                    }
                    context.unregisterReceiver(this);
                } else {
                    Toast.makeText(context, "인터넷 연결이 끊어졌습니다.\n다시 연결해 주십시오!", Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (Exception e) {
                Log.e(TAG, "onReceive Error: " + e.getMessage());
            }
        }
    }
}
