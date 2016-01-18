package com.yuri.phoneassistant;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import com.yuri.phoneassistant.traffic.TrafficService;
import com.yuri.phoneassistant.Log;
import com.zhaoyan.common.utils.Utils;

public class CommonService extends Service {
	private static final String TAG = CommonService.class.getSimpleName();
	private final IBinder mBinder = new LocalBinder();

	public static final String ACTION_FLASHLIGHT = "com.yuri.phoneassistant.commonservice.flashlight";
	public static final String ACTION_CAPTURE = "com.yuri.phoneassistant.commonservice.capture";
	public static final String ACTION_QUICK_CAPTURE = "com.zhaoyao.juyou.commonservice.quickcapture";
	public static final String ACTION_DATA = "com.yuri.phoneassistant.commonservice.data";
	public static final String ACTION_MUSIC = "com.yuri.phoneassistant.commonservice.music";
	public static final String ACTION_ROTATE = "com.yuri.phoneassistant.commonservice.rotate";
	
	private FlashLightManager mFlashLightManager;
	
	private void sendBroadcast(int viewId, int resId){
		Intent intent = new Intent();
		intent.setAction(TrafficService.UPDATE_ACTION);
		intent.putExtra("viewid", viewId);
		intent.putExtra("resid", resId);
		sendBroadcast(intent);
	}
	
	public CommonService(){
	}

	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate>>>>");
		mFlashLightManager = new FlashLightManager();
		Log.d(TAG, "onCreate<<<<");
	}

	public class LocalBinder extends Binder {
		public CommonService getService() {
			return CommonService.this;
		}
	}

	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand.intent:" + intent);
		boolean stopSelf = true;
		if (intent != null) {
			String action = intent.getAction();
			Log.d(TAG, "onStartCommand " + action);
			if (ACTION_FLASHLIGHT.equals(action)) {
				if (mFlashLightManager.isFlashOn()) {
					stopSelf = true;
					mFlashLightManager.closeFlashlight();
					sendBroadcast(R.id.iv_statis_bar_flashlight, R.drawable.ic_status_fl_disable);
				} else {
					stopSelf = false;
					mFlashLightManager.openFlashlight();
					sendBroadcast(R.id.iv_statis_bar_flashlight, R.drawable.ic_status_fl_enable);
				}
			} else if (ACTION_CAPTURE.equals(action)) {
				Intent captureIntent = new Intent(Intent.ACTION_CAMERA_BUTTON);
				sendBroadcast(captureIntent);
			} else if (ACTION_DATA.equals(action)) {
				boolean isMobileNetEnable = NetWorkUtil.isMobileNetEnable(getApplicationContext());
				System.out.println("isMobileNetEnable:" + isMobileNetEnable);
				if (isMobileNetEnable) {
					NetWorkUtil.setMobileNetUnable(getApplicationContext());
					sendBroadcast(R.id.iv_statis_bar_data, R.drawable.ic_status_data_disable);
				} else {
					NetWorkUtil.setMobileNetEnable(getApplicationContext());
					sendBroadcast(R.id.iv_statis_bar_data, R.drawable.ic_status_data_enable);
				}
			} else if (ACTION_MUSIC.equals(action)) {
				try {
					Intent intent1 = new Intent();
					intent1.setAction("android.intent.action.MUSIC_PLAYER");
					intent1.addCategory(Intent.CATEGORY_APP_MUSIC);
					intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent1);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				Utils.collapseStatusBar(getApplicationContext());
			} else if (ACTION_ROTATE.equals(action)) {
				
				//得到是否开启
				int flag = Settings.System.getInt(getContentResolver(),
				            Settings.System.ACCELEROMETER_ROTATION, 0);
				
				flag = flag==0 ? 1 : 0;
				
				//0为关闭 1为开启
				Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, flag);
				
				int flag2 = Settings.System.getInt(getContentResolver(),
			            Settings.System.ACCELEROMETER_ROTATION, 0);
				if (flag2 == 0) {
					sendBroadcast(R.id.iv_statis_bar_rotate, R.drawable.ic_status_rotate_disable);
				} else {
					sendBroadcast(R.id.iv_statis_bar_rotate, R.drawable.ic_status_rotate_enable);
				}
			}
		}
		if (stopSelf) {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
