package com.yuri.phoneassistant;

import com.yuri.phoneassistant.traffic.TrafficService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PAReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			//
			Intent intent2 = new Intent();
			intent2.setClass(context, TrafficService.class);
			context.startService(intent2);
		}
	}

}
