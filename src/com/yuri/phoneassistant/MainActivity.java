package com.yuri.phoneassistant;

import com.yuri.phoneassistant.app.LoaderApp;
import com.yuri.phoneassistant.camera.QuickCapture;
import com.yuri.phoneassistant.camera.QuickVideoRecoder;
import com.yuri.phoneassistant.traffic.TrafficActivity;
import com.yuri.phoneassistant.traffic.TrafficService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private ListView mListView;
	
	private String[] TITLES = {
			"Traffic statistics",
			"Apps",
			"Quick Capture",
			"Video Recoder"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = new Intent();
		intent.setClass(this, TrafficService.class);
		startService(intent);
		
		mListView = (ListView) findViewById(R.id.paListview);
		mListView.setAdapter(new MyAdapter());
		mListView.setOnItemClickListener(this);
		
		
	}
	
	private class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return TITLES.length;
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
			View view = getLayoutInflater().inflate(R.layout.main_list_item, null);
			
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(TITLES[position]);
			
			return view;
		}
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent();
		switch (position) {
		case 0:
			intent.setClass(this, TrafficActivity.class);
			break;
		case 1:
			intent.setClass(this, LoaderApp.class);
			break;
		case 2:
			intent.setClass(this, QuickCapture.class);
			break;
		case 3:
			intent.setClass(this, QuickVideoRecoder.class);
			break;
		default:
			break;
		}
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		this.finish();
	}

}
