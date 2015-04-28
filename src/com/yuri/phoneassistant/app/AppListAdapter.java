package com.yuri.phoneassistant.app;

import java.util.List;

import com.yuri.phoneassistant.R;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends ArrayAdapter<AppEntry> {
	private final LayoutInflater mInflater;
	private Context mContext;

	public AppListAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2);
		
		mContext = context;
		
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<AppEntry> data) {
		clear();
		if (data != null) {
			addAll(data);
		}
	}

	/**
	 * Populate new items in the list.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder = null;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.list_item_icon_text, parent,
					false);
			
			holder = new ViewHolder();
			holder.imageView = (ImageView) view.findViewById(R.id.icon);
			holder.labelView = (TextView) view.findViewById(R.id.text);
			holder.pkgView = (TextView) view.findViewById(R.id.text_pkg);
			holder.txView = (TextView) view.findViewById(R.id.text_tx);
			holder.rxView = (TextView) view.findViewById(R.id.text_rx);
			
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		AppEntry item = getItem(position);
		holder.imageView.setImageDrawable(item.getIcon());
		holder.labelView.setText(item.getLabel());
		holder.pkgView.setText(item.getApplicationInfo().packageName);
		
		if (!item.isAccessInternet()) {
			holder.txView.setText("No Access Internet");
		} else {
			holder.txView.setText(Formatter.formatFileSize(mContext, item.getTxBytes()));
		}
		
		holder.rxView.setText(Formatter.formatFileSize(mContext, item.getRxBytes()));

		return view;
	}
	
	private class ViewHolder{
		ImageView imageView;
		TextView labelView;
		TextView pkgView;
		TextView txView;
		TextView rxView;
	}


}
