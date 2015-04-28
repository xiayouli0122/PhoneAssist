/**
 * @author zhoushengtao
 * @since 2014年5月21日 下午6:17:00
 */

package com.yuri.phoneassistant;

import android.app.Application;
import android.view.WindowManager;

public class MyApplication extends Application {
	private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getWindowParams() {
		return windowParams;
	}
}
