package net.woorisys.pms.app.services.NotificationService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import net.woorisys.pms.app.MainActivity;
import net.woorisys.pms.app.R;

public class NotificationService {
    private static final String TAG = "KTW_Notification";

    private static final String FOREGROUND_CHANNEL_ID = "smart-parking-bluetooth";
    public static final int FOREGROUND_NOTIFICATION_ID = 821;
    private final static int ONEPASS_NOTIFICATION_ID = 822;

    private static NotificationService mNotificationServiceInstance;

    private static NotificationManager mNotificationManager = null;

    public NotificationService(@NonNull Context context) {
        mNotificationServiceInstance = this;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationService getInstance() {
        return mNotificationServiceInstance;
    }

    // ---------------------------------------------------------------------------------------------
    // createForegroundNotification
    // ---------------------------------------------------------------------------------------------
    public Notification createForegroundNotification(Context context, String purpose)
    {
        Resources resources = context.getResources();

        String name;
        String title;
        String content;
        String description;
        String action = "";
        Drawable drawable = null;
        Bitmap largeIcon = null;
        switch(purpose) {
            case "parking":
                name = resources.getString(R.string.notification_parking_name);
                title = resources.getString(R.string.notification_parking_title);
                content = resources.getString(R.string.notification_parking_text);
                description = resources.getString(R.string.notification_parking_description);
                action = resources.getString(R.string.notification_parking_action);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    drawable = resources.getDrawable(R.mipmap.ic_smart_parking, null);
                }
                break;
            case "both":
                name = resources.getString(R.string.notification_both_name);
                title = resources.getString(R.string.notification_both_title);
                content = resources.getString(R.string.notification_both_text);
                description = resources.getString(R.string.notification_both_description);
                action = resources.getString(R.string.notification_both_action);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    drawable = resources.getDrawable(R.mipmap.ic_smart_parking, null);
                }
                break;
//          case "onepass":
            default:
                name = resources.getString(R.string.notification_onepass_name);
                title = resources.getString(R.string.notification_onepass_title);
                content = resources.getString(R.string.notification_onepass_text);
                description = resources.getString(R.string.notification_onepass_description);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    drawable = resources.getDrawable(R.mipmap.ic_smart_onepass, null);
                }
                break;
        }

        // for Large Icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            largeIcon = getDefaultBitmap(context, drawable);
        }

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create Notification Channel
            NotificationChannel notificationChannel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(false);

            if (mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // Activity Pending Intent
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(false)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setLargeIcon(largeIcon)
                   .setColor(resources.getColor(R.color.default_app_background, null));
        }

        // Action Pending Intent
        if (!purpose.equals("onepass")) {
            IconCompat icon = IconCompat.createWithResource(context, R.mipmap.ic_smart_parking);

            Intent actionIntent = new Intent("com.poscoict.the_sharp_iot_app.PARKING");
            actionIntent.putExtra("result", "success");
            actionIntent.putExtra("action", "parkingLocationView");
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(icon, action, actionPendingIntent);
            builder.addAction(actionBuilder.build());
        }

        return builder.build();
    }

    // ---------------------------------------------------------------------------------------------
    // getDefaultBitmap
    // ---------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap getDefaultBitmap(Context context, Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        } else if ((Build.VERSION.SDK_INT >= 26) && (d instanceof AdaptiveIconDrawable)) {
            AdaptiveIconDrawable icon = ((AdaptiveIconDrawable)d);
            int w = icon.getIntrinsicWidth();
            int h = icon.getIntrinsicHeight();
            Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            icon.setBounds(0, 0, w, h);
            icon.draw(canvas);
            return result;
        }

        float density = context.getResources().getDisplayMetrics().density;
        int defaultWidth = (int)(48* density);
        int defaultHeight = (int)(48* density);
        return Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888);
    }

    // ---------------------------------------------------------------------------------------------
    // openDoorNotification
    // ---------------------------------------------------------------------------------------------
    public void openDoorNotification(Context context, String content, int color) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.notification)
                .setTicker(context.getResources().getString(R.string.notification_onepass_ticker))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.notification_onepass_title))
                .setContentText(content)
                .setColor(color);
        mNotificationManager.notify(ONEPASS_NOTIFICATION_ID, builder.build());
    }
}
