package com.yuri.phoneassistant.camera;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuri.phoneassistant.R;

public class VideoSizeAdapter extends BaseAdapter {

	private Context context;
	private SparseIntArray widthArray;
	private SparseIntArray heightArray;
	public int index = 0;

	public VideoSizeAdapter(Context context, SparseIntArray widthArray,
			SparseIntArray heightArray) {
		this.context = context;
		this.widthArray = widthArray;
		this.heightArray = heightArray;
	}

	@Override
	public int getCount() {
		return widthArray.size();
	}

	@Override
	public Object getItem(int position) {
		return widthArray.get(position) + "X" + heightArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.video_size, null);
		} 
		TextView textView = (TextView) convertView
				.findViewById(R.id.textViewSize);
		ImageView imageView = (ImageView) convertView
				.findViewById(R.id.imageViewVideoSize);
		textView.setText(widthArray.get(position) + "X"
				+ heightArray.get(position));
		if (position == index) {
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(View.GONE);
		}
		return convertView;
	}

}
