package com.yuri.phoneassistant.traffic;

import java.util.ArrayList;
import java.util.List;

import com.yuri.phoneassistant.Log;

public class TrafficMonthItem {
	private static final String TAG = TrafficMonthItem.class.getSimpleName();

	private int month;
	private int day;

	private long monthBytes;

	private long monthTxBytes;
	private long monthRxBytes;

	private long monthEndTime;
	
	private List<TrafficDayItem> list;
	
	public void addDay(TrafficDayItem dayItem){
		if (list == null) {
			list = new ArrayList<TrafficDayItem>();
		} else {
			list.add(dayItem);
		}
	}
	
	public List<TrafficDayItem> getDayList(){
		return list;
	}

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


	public void setMonthBytes(long monthBytes) {
		this.monthBytes = monthBytes;
	}

	public long getMonthBytes() {
		return monthBytes;
	}

	////////////////////////////////
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

	public long getMonthEndTime() {
		return monthEndTime;
	}

	public void setMonthEndTime(long monthEndTime) {
		this.monthEndTime = monthEndTime;
	}

	public String toString() {
		String string = "MonthBytes:" + this.getMonthBytes();
		Log.d(TAG, "string");
		return string;
	}

}
