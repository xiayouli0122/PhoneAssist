package com.yuri.phoneassistant;

import java.util.Calendar;

public class DateUtils {
	public static Long getDayStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime().getTime();
	}

	public static Long getDayEndTime() {
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		todayEnd.set(Calendar.MILLISECOND, 999);
		return todayEnd.getTime().getTime();
	}

	public static Long getMonthStartTime() {
		Calendar monthStart = Calendar.getInstance();
		monthStart.set(Calendar.DAY_OF_MONTH, 1);
		monthStart.set(Calendar.HOUR_OF_DAY, 0);
		monthStart.set(Calendar.MINUTE, 0);
		monthStart.set(Calendar.SECOND, 0);
		monthStart.set(Calendar.MILLISECOND, 0);
		return monthStart.getTime().getTime();
	}

	public static Long getMonthEndTime() {
		Calendar monthEnd = Calendar.getInstance();
		int day = monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH);
		monthEnd.set(Calendar.DAY_OF_MONTH, day);
		monthEnd.set(Calendar.HOUR_OF_DAY, 23);
		monthEnd.set(Calendar.MINUTE, 59);
		monthEnd.set(Calendar.SECOND, 59);
		monthEnd.set(Calendar.MILLISECOND, 999);
		return monthEnd.getTime().getTime();
	}
}
