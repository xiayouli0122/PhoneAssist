package com.yuri.phoneassistant.traffic;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.yuri.phoneassistant.CommonService;
import com.yuri.phoneassistant.DateUtils;
import com.yuri.phoneassistant.NetWorkUtil;
import com.yuri.phoneassistant.R;
import com.yuri.phoneassistant.WeakRefHandler;
import com.yuri.phoneassistant.db.MetaData.TrafficsDay;
import com.yuri.phoneassistant.db.MetaData.TrafficsMonth;
import com.zhaoyan.common.utils.Log;

public class TrafficService extends Service {
	private static final String TAG = TrafficService.class.getSimpleName();
	private final IBinder mBinder = new LocalBinder();

	private Timer mTimer;
	
	private static final int TRAFFIC_NOTIFICATION = 0x24;
	
	private static final int REFRESH_TIME_MILLS = 60 * 1000;
	
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	
	private static final String EXTRA_TODAY_BYTES = "todayBytes";
	private static final String EXTRA_MONTH_BYTES = "monthBytes";
	private static final String EXTRA_DAY_END = "day_end";
	private static final String EXTRA_MONTH_END = "month_end";
	private static final String EXTRA_TEMP_BYTES  = "tempBytes";
	
	private SharedPreferences mSharedPreferences = null;
	private long mTodayEnd = 0;
	private long mMonthEnd = 0;
	
	private long mPreDayBytes = 0;
	private long mPreMonthBytes = 0;
	private long mTodayBytes = 0;
	private long mMonthBytes = 0;
	
	private long mTempBytes = 0;
	
	SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
	
	public static final String UPDATE_ACTION = "com.yuri.pa.update_action";

	private WeakRefHandler mHandler = new WeakRefHandler(this) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 12:
				Bundle bundle = msg.getData();
				
				String today = bundle.getString(EXTRA_TODAY_BYTES);
				String month = bundle.getString(EXTRA_MONTH_BYTES);
				
				String string = "Day:" + today + ",Month:" + month;
				Log.d(TAG, "handler:"  + string);
				
				updateNotificationText(R.id.tv_network_traffic, string);
				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}
	};
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				checkNetwork();
			} else if (UPDATE_ACTION.equals(action)) {
				Log.d(TAG, "update notify icon");
				int viewId = intent.getIntExtra("viewid", 0);
				int resId = intent.getIntExtra("resid", 0);
				updateNotificationIcon(viewId, resId);
			}
			
		}
	};

	public TrafficService() {
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate>>>>");
		
		String logDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "PA";
		Log.startSaveToFile(logDir);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(UPDATE_ACTION);
		registerReceiver(mReceiver, filter);
		
		mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(
				Context.NOTIFICATION_SERVICE);
		
		mSharedPreferences = getSharedPreferences("traffics", MODE_PRIVATE);
		
		mTodayEnd = mSharedPreferences.getLong(EXTRA_DAY_END, 0);
		mMonthEnd = mSharedPreferences.getLong(EXTRA_MONTH_END, 0);
		Log.d(TAG, "11>>mTodayEnd:" + mTodayEnd + ",mMonthEnd:" + mMonthEnd);
		Log.d(TAG, "11>>mTodayEndF:" + format.format(new Date(mTodayEnd)));
		Log.d(TAG, "11>>mMonthEndF:" + format.format(new Date(mMonthEnd)));
		
		mPreDayBytes = mSharedPreferences.getLong(EXTRA_TODAY_BYTES, 0);
		mPreMonthBytes = mSharedPreferences.getLong(EXTRA_MONTH_BYTES, 0);
		Log.d(TAG, "11>>mPreDayBytes:" + mPreDayBytes + ",mPreMonthBytes:" + mPreMonthBytes);
		
		if (mTodayEnd == 0 || mMonthEnd == 0) {
			Log.d(TAG, "need init day & month time");
			mTodayEnd = DateUtils.getDayEndTime();
			mMonthEnd = DateUtils.getMonthEndTime();
			Editor editor = mSharedPreferences.edit();
			editor.putLong(EXTRA_DAY_END, mTodayEnd);
			editor.putLong(EXTRA_MONTH_END, mMonthEnd);
			editor.commit();
		} else {
			Log.d(TAG, "I am not the fresh man");
			long dayEnd = DateUtils.getDayEndTime();
			Log.d(TAG, "22>>dayEnd:" + dayEnd);
			Log.d(TAG, "22>>dayEndF:" + format.format(new Date(dayEnd)));
			if (mTodayEnd == dayEnd) {
				//the same day
				Log.d(TAG, "the same day");
				mPreMonthBytes = mPreMonthBytes - mPreDayBytes;
			} else {
				Log.d(TAG, "the other day");
				new Thread(new Runnable() {
					@Override
					public void run() {
						saveYesterdayData(mPreDayBytes, mPreMonthBytes, mTodayEnd);
					}
				}).start();
				mPreDayBytes = 0;
				mTodayEnd = dayEnd;
				Editor editor = mSharedPreferences.edit();
				editor.putLong(EXTRA_DAY_END, dayEnd);
				editor.putLong(EXTRA_TODAY_BYTES, 0);
				
				long monthEnd = DateUtils.getMonthEndTime();
				Log.d(TAG, "22>>monthEnd:" + monthEnd);
				Log.d(TAG, "22>>monthEndF:" + format.format(new Date(monthEnd)));
				if (mMonthEnd == monthEnd) {
					//the same month
					Log.d(TAG, "the same month");
				} else {
					//the other month
					Log.d(TAG, "the other month");
					new Thread(new Runnable() {
						@Override
						public void run() {
							saveLastMonthData(mPreMonthBytes, mMonthEnd);
						}
					}).start();
					mPreMonthBytes = 0;
					mMonthEnd = monthEnd;
					editor.putLong(EXTRA_MONTH_END, monthEnd);
					editor.putLong(EXTRA_MONTH_BYTES, 0);
				}
				editor.commit();
			}
		}
		
		Log.d(TAG, "================");
		Log.d(TAG, "mTodayEnd:" + mTodayEnd);
		Log.d(TAG, "mMonthEnd:" + mMonthEnd);
		Log.d(TAG, "mPreDayBytes:" + mPreDayBytes);
		Log.d(TAG, "mPreMonthBytes:" + mPreMonthBytes);
		Log.d(TAG, "================");
		
		//init
		showNotification(getApplicationContext());
		initNotificationTraffic();
		
		Log.d(TAG, "onCreate<<<<");
	}

	public class LocalBinder extends Binder {
		public TrafficService getService() {
			return TrafficService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand.intent:" + intent);
		
		checkNetwork();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void checkNetwork(){
		int type = NetWorkUtil.getAPNType(getApplicationContext());
		switch (type) {
		case -1:
			//no network
			Log.d(TAG, "checkNetwork.no network");
			updateNotificationText(R.id.tv_network_type, "No Network");
			
			stopGetTraffics();
			break;
		case 1:
			//wifi
			Log.d(TAG, "checkNetwork.wifi");
			updateNotificationText(R.id.tv_network_type, "WiFi Connected");
			
			stopGetTraffics();
			break;
		case 2:
			//wap
		case 3:
			//net
			Log.d(TAG, "checkNetwork.mobile network");
			updateNotificationText(R.id.tv_network_type, "Moible Connected");
			startGetTraffics();
			break;
		}
	}
	
	public void initNotificationTraffic(){
		String today = Formatter.formatFileSize(getApplicationContext(), mTodayBytes);
		String month = Formatter.formatFileSize(getApplicationContext(), mMonthBytes);
		updateNotificationText(R.id.tv_network_traffic, "Day:" + today + ",Month:" + month);
		
		int flag = Settings.System.getInt(getContentResolver(),
	            Settings.System.ACCELEROMETER_ROTATION, 0);
		if (flag == 0) {
			updateNotificationIcon(R.id.iv_statis_bar_rotate, R.drawable.ic_status_rotate_disable);
		} else {
			updateNotificationIcon(R.id.iv_statis_bar_rotate, R.drawable.ic_status_rotate_enable);
		}
		
		boolean isMobileNetEnable = NetWorkUtil.isMobileNetEnable(getApplicationContext());
		Log.d(TAG, "initNotificationTraffic.isMobileNetEnable:" + isMobileNetEnable);
		if (isMobileNetEnable) {
			updateNotificationIcon(R.id.iv_statis_bar_data, R.drawable.ic_status_data_enable);
		} else {
			updateNotificationIcon(R.id.iv_statis_bar_data, R.drawable.ic_status_data_disable);
		}
	}
	
	public void stopGetTraffics(){
		Log.d(TAG, "stopGetTraffics()");
		//no network & wifi no need to get traffics
		if (mTimer != null) {
			mTimer.cancel();
		}
	}
	
	public void startGetTraffics(){
		if (mTimer != null) {
			mTimer.cancel();
		}

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			public void run() {
				getTraffics();
			}
		}, 0, REFRESH_TIME_MILLS);
	}
	
	public void getTraffics(){
		Log.d(TAG, "getTraffics()");
		long rxBytes = TrafficStats.getMobileRxBytes();
		long txBytes = TrafficStats.getMobileTxBytes();
		
		long nowTime = System.currentTimeMillis();
		Log.d(TAG, "getTraffics.nowTime:" + nowTime);
		Log.d(TAG, "1111getTraffics.mTodayEnd:" + mTodayEnd);
		Log.d(TAG, "1111getTraffics.mTodayEndF:" + format.format(new Date(mTodayEnd)));
		if (nowTime > mTodayEnd) {
			final long yesterdayBytes = mTodayBytes;
			final long yesterdayEnd = mTodayEnd;
			Log.d(TAG, "another day");
			Log.d(TAG, "so yesterday.mMonthBytes:" + mMonthBytes);
			Log.d(TAG, "so yesterday.mTodayBytes:" + yesterdayBytes);
			mPreMonthBytes = mSharedPreferences.getLong(EXTRA_MONTH_BYTES, mMonthBytes);
			Log.d(TAG, "so month pre:" + mPreMonthBytes);
			
			Editor editor = mSharedPreferences.edit();
			
			mTodayEnd = DateUtils.getDayEndTime();
			Log.d(TAG, "222getTraffics.mTodayEnd:" + mTodayEnd);
			//today is a new day,so reset bytes 0
			mTodayBytes = 0;
			mPreDayBytes = 0;
			mTempBytes = rxBytes + txBytes;
			
			editor.putLong(EXTRA_DAY_END, mTodayEnd);
			editor.commit();
			
			//save yesterday data to db
			new Thread(new Runnable() {
				@Override
				public void run() {
					saveYesterdayData(yesterdayBytes, mMonthBytes,  yesterdayEnd);
					saveLastMonthData(mMonthBytes, mMonthEnd);
				}
			}).start();
		} else {
			//today
			if (mTempBytes == 0) {
				mTempBytes = rxBytes + txBytes;
				Editor editor = mSharedPreferences.edit();
				editor.putLong(EXTRA_TEMP_BYTES, mTempBytes);
				editor.commit();
			}
			mTodayBytes = (rxBytes + txBytes) - mTempBytes + mPreDayBytes;
			Log.d(TAG, "today.pre:" + mPreDayBytes + ",now:" + mTodayBytes);
		}
		
		
		if (nowTime > mMonthEnd) {
			Log.d(TAG, "another month");
			final long lastMonthBytes = mMonthBytes;
			final long lastMonthEnd = mMonthEnd;
			Log.d(TAG, "so lastMonth.mMonthBytes:" + lastMonthBytes);
			Log.d(TAG, "so lastMonth.lastMonthEnd:" + lastMonthEnd);
			Editor editor = mSharedPreferences.edit();
			
			mMonthEnd = DateUtils.getMonthEndTime();
			//it is a new month,so reset month bytes 0
			mMonthBytes = 0;
			mPreMonthBytes = 0;
			
			editor.putLong(EXTRA_MONTH_END, mMonthEnd);
			editor.putLong(EXTRA_MONTH_BYTES, mMonthBytes);
			editor.commit();
			
			//save last month data to db
			new Thread(new Runnable() {
				@Override
				public void run() {
					saveLastMonthData(lastMonthBytes, lastMonthEnd);
				}
			}).start();
		} else {
			//this month
			mMonthBytes = mPreMonthBytes + mTodayBytes;
			Log.d(TAG, "this month.pre:" + mPreMonthBytes + ",now:" + mMonthBytes);
		}
		
		String totalString = Formatter.formatFileSize(getApplicationContext(), mTodayBytes);
		String monthString = Formatter.formatFileSize(getApplicationContext(), mMonthBytes);
		Message message = new Message();
		message.what = 12;
		message.getData().putString(EXTRA_TODAY_BYTES, totalString);
		message.getData().putString(EXTRA_MONTH_BYTES, monthString);
		
		mHandler.sendMessage(message);
		
		//save
		Log.d(TAG, "save todaybytes:" + mTodayBytes + ",monthBytes:" + mMonthBytes);
		Editor editor = mSharedPreferences.edit();
		editor.putLong(EXTRA_TODAY_BYTES, mTodayBytes);
		editor.putLong(EXTRA_MONTH_BYTES, mMonthBytes);
		editor.commit();
	}
	
	//big views
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void showNotification(Context context) {
		Log.d(TAG, "showNotificaiton>>>>");
		RemoteViews views = new RemoteViews(getPackageName(),R.layout.traffic_notification);
		RemoteViews bigViews = new RemoteViews(getPackageName(), R.layout.traffic_notification_expand);
		
		views.setTextViewText(R.id.tv_network_type, "网络状态: N/A");
		views.setTextViewText(R.id.tv_network_traffic, "已使用流量: N/A");
		bigViews.setTextViewText(R.id.tv_network_type, "网络状态: N/A");
		bigViews.setTextViewText(R.id.tv_network_traffic, "已使用流量: N/A");
		
		bigViews.setImageViewResource(R.id.iv_statis_bar_flashlight, R.drawable.ic_status_fl_disable);
		
		Intent intent;
		PendingIntent pIntent;
		
		intent = new Intent("com.yuri.pa.main");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.ll_main_notifi, pIntent);
		bigViews.setOnClickPendingIntent(R.id.ll_main_notifi, pIntent);
		
		intent = new Intent(CommonService.ACTION_DATA);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_1, pIntent);

		intent = new Intent(CommonService.ACTION_ROTATE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_2, pIntent);
		
		intent = new Intent(CommonService.ACTION_MUSIC);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_3, pIntent);
		
		intent = new Intent(CommonService.ACTION_FLASHLIGHT);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_4, pIntent);
		
		intent = new Intent(CommonService.ACTION_CAPTURE);
		intent.setClass(context, CommonService.class);
		pIntent = PendingIntent.getService(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_5, pIntent);
		
		intent = new Intent("com.yuri.phoneassistant.camera.quickcapture");
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		bigViews.setOnClickPendingIntent(R.id.status_bar_6, pIntent);

		mNotification = new Notification.Builder(this).build();
		mNotification.contentView = views;
		mNotification.bigContentView = bigViews;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.icon = R.drawable.icon;
        mNotification.priority = Notification.PRIORITY_MIN;
        mNotification.contentIntent = PendingIntent.getService(context, 0, intent, 0);
        startForeground(TRAFFIC_NOTIFICATION, mNotification);
		Log.d(TAG, "showNotificaiton<<<<");
	}
	
	public void updateNotificationText(int resId, String msg){
		mNotification.contentView.setTextViewText(resId, msg);
		mNotification.bigContentView.setTextViewText(resId, msg);
		mNotificationManager.notify(TRAFFIC_NOTIFICATION, mNotification);
	}
	
	public void updateNotificationIcon(int viewId, int resId){
		Log.d(TAG, "updateNotificationIcon.viewId:" + viewId + ",resId:" + resId);
		mNotification.bigContentView.setImageViewResource(viewId, resId);
		mNotificationManager.notify(TRAFFIC_NOTIFICATION, mNotification);
	}
	
	public String getTypeString(int type){
		if (type == -1) {
			return "No Network";
		} else if (type == 1) {
			return "Connecting WiFi";
		} else {
			return "Connecting Mobile Network";
		}
	}
    
    private void saveYesterdayData(long dayBytes, Long monthBytes, long time){
    	Log.d(TAG, "saveYesterdayData.daybytes:" + dayBytes + ",monthBytes:" + monthBytes);
    	Log.d(TAG, "saveYesterdayData.Date:" + format.format(new Date(time)));
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(time);
    	int month = calendar.get(Calendar.MONTH);
    	int day = calendar.get(Calendar.DAY_OF_MONTH);
    	Log.d(TAG, "month:" + month);
    	Log.d(TAG, "day:" + day);
    	
    	ContentValues values = new ContentValues();
		values.put(TrafficsDay.DAYBYTES, dayBytes);
		values.put(TrafficsDay.DAYENDTIME, time);
		values.put(TrafficsDay.MONTH, month);
		values.put(TrafficsDay.DAY, day);
		values.put(TrafficsDay.MONTHBYTES, monthBytes);
		getContentResolver().insert(TrafficsDay.CONTENT_URI, values);
		
    }
    
    private void saveLastMonthData(long bytes, long time){
    	Log.d(TAG, "saveLastMonthData.bytes:" + bytes);
    	Log.d(TAG, "saveLastMonthData.Date:" + format.format(new Date(time)));
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(time);
    	int month = calendar.get(Calendar.MONTH);
    	Log.d(TAG, "month:" + month);
    	
    	ContentValues values = new ContentValues();
		values.put(TrafficsMonth.MONTHBYTES, bytes);
		values.put(TrafficsMonth.MONTHENDTIME, time);
		values.put(TrafficsMonth.MONTH, month);
		getContentResolver().insert(TrafficsMonth.CONTENT_URI, values);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimer.cancel();
		mTimer = null;
		
		unregisterReceiver(mReceiver);
		
		Editor editor = mSharedPreferences.edit();
		editor.putLong(EXTRA_DAY_END, mTodayEnd);
		editor.putLong(EXTRA_MONTH_END, mMonthEnd);
		editor.putLong(EXTRA_TODAY_BYTES, mTodayBytes);
		editor.putLong(EXTRA_MONTH_BYTES, mMonthBytes);
		editor.commit();
	}
}
