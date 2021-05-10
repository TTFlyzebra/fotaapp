package com.flyzebra.fota.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.RemoteViews;

import com.flyzebra.fota.MainActivity;
import com.flyzebra.fota.R;

public class NotificationView {

    private final static String NOTIFICATION_BROAD_CAST = "NOTIFY_BROAD_CAST";
    private NotificationManager notimanager = null;
    private RemoteViews remoteviews = null;
    private final int NOTIFICATION_ID = 1;
    private Notification noti = null;

    private NotifyBroadCast notifyBroadCast;
    private IntentFilter intentFilter;
    private Context mContext;

    private static class NotifyBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NOTIFICATION_BROAD_CAST)) {
                if (intent.getStringExtra("ACTION").equals("CANCEL")) {
                }
            }
        }
    }

    public NotificationView(Context context) {
        mContext = context;

        notimanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBroadCast = new NotifyBroadCast();
        intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFICATION_BROAD_CAST);
        mContext.registerReceiver(notifyBroadCast, intentFilter);

        remoteviews = new RemoteViews(context.getPackageName(), R.layout.notification);
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_BROAD_CAST);
        intent.putExtra("ACTION", "CANCEL");
        PendingIntent nextPI = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteviews.setOnClickPendingIntent(R.id.noti_icon, nextPI);
        remoteviews.setTextViewText(R.id.noti_msg, "");
        remoteviews.setProgressBar(R.id.noti_pbar, 100, 0, false);
        remoteviews.setImageViewResource(R.id.noti_icon, R.drawable.ic_launcher_background);
        Intent notiintent = new Intent(context, MainActivity.class);
        notiintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent PdIntent = PendingIntent.getActivity(context, 0, notiintent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "upgrade";
            String channelName = "升级系统";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notimanager.createNotificationChannel(channel);
            noti = new Notification.Builder(context, "upgrade")
                    .setCustomContentView(remoteviews)
                    .setContentIntent(PdIntent)
                    .setOngoing(true)
                    .build();
        } else {
            noti = new Notification.Builder(context)
                    .setContent(remoteviews)
                    .setContentIntent(PdIntent)
                    .setOngoing(true)
                    .build();
        }
        noti.icon = android.R.drawable.ic_media_play;
    }

    public void show(int code, int progress, String msg) {
        remoteviews.setTextViewText(R.id.noti_msg, msg);
        remoteviews.setProgressBar(R.id.noti_pbar, 100, progress, false);
        notimanager.notify(NOTIFICATION_ID, noti);
    }

    public void hide() {
        notimanager.cancel(NOTIFICATION_ID);
    }

}
