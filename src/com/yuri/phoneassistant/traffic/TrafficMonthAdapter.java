package com.yuri.phoneassistant.traffic;

import java.util.List;

import com.yuri.phoneassistant.R;
import com.yuri.phoneassistant.Log;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TrafficMonthAdapter extends BaseAdapter {
	private static final String TAG = TrafficMonthAdapter.class.getSimpleName();
	
	private LayoutInflater mInflater;
	private List<TrafficMonthItem> mDataItems;
	private Context mContext;
	
	public TrafficMonthAdapter(Context context, List<TrafficMonthItem> list){
		mInflater = LayoutInflater.from(context);
		mDataItems = list;
		
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = mInflater.inflate(R.layout.traffic_list_item, null);
		TextView titleView = (TextView) view.findViewById(R.id.item_title);
		TextView numView = (TextView) view.findViewById(R.id.item_num);
		
		TrafficMonthItem item = mDataItems.get(position);
		titleView.setText((item.getMonth() + 1) + "月");
		
		Log.d(TAG, "getView.month:" + (item.getMonth() + 1));
		Log.d(TAG, "getView.getMonthBytes:" + (item.getMonthBytes()));
		numView.setText(Formatter.formatFileSize(mContext, item.getMonthBytes()));
		
		return view;
	}
	

}
