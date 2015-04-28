package com.yuri.phoneassistant;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.zhaoyan.common.utils.Log;

public class NetWorkUtil {
	
	/**
	 * @author  获取当前的网络状态 -1：没有网络
	 *         1：WIFI网络2：wap网络3：net网络
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			Log.e("networkInfo.getExtraInfo()",
					"networkInfo.getExtraInfo() is "
							+ networkInfo.getExtraInfo());
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = 3;
			} else {
				netType = 2;
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = 1;
		}
		return netType;
	}

	/**
	 * 判断WiFi网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断数据流量是否可用
	 * 
	 * @param context
	 * @return
	 */
	public  static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				boolean isConnected = mMobileNetworkInfo
						.isConnectedOrConnecting();
				return isConnected;
			}
		}
		return false;
	}

	/**
	 * 判断是否有网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	/**
	 * 判断移动网络是否开启
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileNetEnable(Context context) {
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod(context, "getMobileDataEnabled",
					arg);
			
			return isMobileDataEnable;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setMobileNetEnable(Context context) {
		System.out.println("setMobileNetEnable()");
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod(context, "getMobileDataEnabled",
					arg);
			if (!isMobileDataEnable) {
				invokeBooleanArgMethod(context, "setMobileDataEnabled", true);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void setMobileNetUnable(Context context) {
		System.out.println("setMobileNetUnable()");
		Object[] arg = null;
		try {
			boolean isMobileDataEnable = invokeMethod(context, "getMobileDataEnabled",
					arg);
			if (isMobileDataEnable) {
				invokeBooleanArgMethod(context, "setMobileDataEnabled", false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean invokeMethod(Context context, String methodName, Object[] arg)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = null;
		if (arg != null) {
			argsClass = new Class[1];
			argsClass[0] = arg.getClass();
		}

		Method method = ownerClass.getMethod(methodName, argsClass);

		Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

		return isOpen;
	}

	public static Object invokeBooleanArgMethod(Context context, String methodName, boolean value)
			throws Exception {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		Class ownerClass = mConnectivityManager.getClass();

		Class[] argsClass = new Class[1];
		argsClass[0] = boolean.class;

		Method method = ownerClass.getMethod(methodName, argsClass);

		return method.invoke(mConnectivityManager, value);
	}
	
	
}
