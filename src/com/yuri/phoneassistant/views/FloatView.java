package com.yuri.phoneassistant.views;

import android.content.Context;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuri.phoneassistant.R;

public class FloatView extends RelativeLayout {

	private TextView mTypeView;
	private TextView mTodayView;
	private TextView mMonthView;

	public FloatView(Context context) {
		super(context);
		initViews();
	}

	private void initViews() {
		inflate(getContext(), R.layout.floating_layout, this);
		mTypeView = (TextView) findViewById(R.id.tv_title);
		mMonthView = (TextView) findViewById(R.id.tv_month_traffic);
		mTodayView = (TextView) findViewById(R.id.tv_today_traffic);
	}
	
	public void setType(int type){
		if (type == 0) {
			mTypeView.setText("类型:" + "wifi");
		} else if(type == 1){
			mTypeView.setText("类型:" + "移动数据");
		} else {
			mTypeView.setText("类型:" + "无网络连接");
		}
	}

	public void setTodayData(String text) {
		if (TextUtils.isEmpty(text)) {
			mTodayView.setText("");
		} else {
			mTodayView.setText("" + text);
		}
	}

	public void setMonthData(String text) {
		mMonthView.setText(text);
	}

}