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

public class TrafficDayAdapter extends BaseAdapter {
	private static final String TAG = TrafficDayAdapter.class.getSimpleName();
	
	private LayoutInflater mInflater;
	private List<TrafficDayItem> mDataItems;
	private Context mContext;
	
	public TrafficDayAdapter(Context context, List<TrafficDayItem> list){
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
		
		TrafficDayItem item = mDataItems.get(position);
		titleView.setText(item.getDay() + "日");
		
		Log.d(TAG, "getView.getDay:" + (item.getDay()));
        Log.d(TAG, "getView.getDayBytes:" + (item.getDayBytes()));
        
		numView.setText(Formatter.formatFileSize(mContext, item.getDayBytes()));
		
		return view;
	}
	

}
