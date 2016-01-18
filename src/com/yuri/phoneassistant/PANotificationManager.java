package com.yuri.phoneassistant;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.yuri.phoneassistant.Log;
import android.widget.RemoteViews;

public class PANotificationManager {
	private static final String TAG = PANotificationManager.class.getSimpleName();
	
	public PANotificationManager(Context context) {
		// TODO Auto-generated constructor stub
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void updateNotification(Context context, int flashLightResId) {
		Log.d(TAG, "showNotificaiton>>>>");
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.main_notification);
		
		views.setImageViewResource(R.id.iv_statis_bar_flashlight, flashLightResId);

		Intent intent;
		PendingIntent pIntent;

		intent = new Intent("com.yuri.phoneassistant");
		pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.ll_main_notifi, pIntent);
		
		intent = new Intent(CommonService.ACTION_DATA);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_1, pIntent);
		
		intent = new Intent(CommonService.ACTION_ROTATE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_2, pIntent);
		
		intent = new Intent(CommonService.ACTION_MUSIC);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_3, pIntent);
		
		intent = new Intent(CommonService.ACTION_FLASHLIGHT);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_4, pIntent);
		
		intent = new Intent(CommonService.ACTION_CAPTURE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_5, pIntent);

		intent = new Intent("com.yuri.phoneassistant.camera.quickcapture");
		pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.status_bar_6, pIntent);

		Notification notification = new Notification.Builder(context).getNotification();
		notification.contentView = views;
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.icon = R.drawable.icon;
		notification.priority = Notification.PRIORITY_MIN;
		notification.contentIntent = PendingIntent.getService(context, 0, intent, 0);
        
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(11, notification);
		Log.d(TAG, "showNotificaiton<<<<");
	}
}
