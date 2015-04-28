package com.yuri.phoneassistant.traffic;

import com.zhaoyan.common.utils.Log;

public class TrafficDayItem {
	private static final String TAG = TrafficDayItem.class.getSimpleName();

	private int month;
	private int day;

	private long dayBytes;
	private long monthBytes;

	private long dayTxBytes;
	private long dayRxBytes;

	private long monthTxBytes;
	private long monthRxBytes;

	private long dayEndTime;
	private long monthEndTime;

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setDayBytes(long dayBytes) {
		this.dayBytes = dayBytes;
	}

	public long getDayBytes() {
		return dayBytes;
	}

	public void setMonthBytes(long monthBytes) {
		this.monthBytes = monthBytes;
	}

	public long getMonthBytes() {
		return monthBytes;
	}

	////////////////////////////////
	public long getDayTxBytes() {
		return dayTxBytes;
	}

	public void setDayTxBytes(long dayTxBytes) {
		this.dayTxBytes = dayTxBytes;
	}

	public long getDayRxBytes() {
		return dayRxBytes;
	}

	public void setDayRxBytes(long dayRxBytes) {
		this.dayRxBytes = dayRxBytes;
	}

	public long getMonthTxBytes() {
		return monthTxBytes;
	}

	public void setMonthTxBytes(long monthTxBytes) {
		this.monthTxBytes = monthTxBytes;
	}

	public long getMonthRxBytes() {
		return monthRxBytes;
	}

	public void setMonthRxBytes(long monthRxBytes) {
		this.monthRxBytes = monthRxBytes;
	}
	////////////////////////////////

	public long getDayEndTime() {
		return dayEndTime;
	}

	public void setDayEndTime(long dayEndTime) {
		this.dayEndTime = dayEndTime;
	}

	public long getMonthEndTime() {
		return monthEndTime;
	}

	public void setMonthEndTime(long monthEndTime) {
		this.monthEndTime = monthEndTime;
	}

	public String toString() {
		String string = "DayBytes:" + this.getDayBytes() + ",MonthBytes:"
				+ this.getMonthBytes();
		Log.d(TAG, "string");
		return string;
	}

}
