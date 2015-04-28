package com.yuri.phoneassistant.camera;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.zhaoyan.common.utils.Log;

public class WakeupUtil {
	private static final String TAG = WakeupUtil.class.getSimpleName();

	public static void wakeup(Context context) {
		Log.d(TAG, "wakeupAndUnlock 1000");
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(
				PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"zhaoyan_wakeup");
		
		wakeLock.acquire(1000);
	}
}
