package com.yuri.phoneassistant.traffic;

import java.util.ArrayList;
import java.util.List;

import com.yuri.phoneassistant.R;
import com.yuri.phoneassistant.db.MetaData.TrafficsDay;
import com.yuri.phoneassistant.db.MetaData.TrafficsMonth;
import com.yuri.phoneassistant.Log;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TrafficActivity extends Activity implements LoaderCallbacks<Cursor>, OnItemClickListener{
	private static final String TAG = TrafficActivity.class.getSimpleName();
	
	private ListView mListView;
	private TextView mStatiticsView;
	
	private TrafficMonthAdapter mMonthAdapter;
	private TrafficDayAdapter mDayAdapter;
	
	private List<TrafficMonthItem> mMonthItems = new ArrayList<TrafficMonthItem>();
	private List<TrafficDayItem> mDayItems = new ArrayList<TrafficDayItem>();
	
	private Loader<Cursor> mMonthLoader;
	private Loader<Cursor> mDayLoader;
	
	private long mTotalMonthBytes = 0;
	
	private static final int LOADER_MONTH_ID = 0;
	private static final int LOADER_DAY_ID = 1;
	
	private static final String[] MONTH_COLUMNS = {
		TrafficsMonth.MONTH, TrafficsMonth.MONTHBYTES,
	};
	
	private static final String[] DAY_COLUMNS = {
		TrafficsDay.MONTH, TrafficsDay.DAY,
		TrafficsDay.DAYBYTES, TrafficsDay.MONTHBYTES
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.traffic_main);
		
		mStatiticsView = (TextView) findViewById(R.id.traffic_tv_statictics);
		mListView = (ListView) findViewById(R.id.traffic_listview);
		mListView.setOnItemClickListener(this);
		
		getLoaderManager().initLoader(LOADER_MONTH_ID, null, (android.app.LoaderManager.LoaderCallbacks<Cursor>) this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreateLoader.id:" + id);
		Uri uri = null;
		String selection = null;
		String[] selectionArgs = null;
		String[] columns = null;
		switch (id) {
		case LOADER_MONTH_ID:
			uri = TrafficsMonth.CONTENT_URI;
			columns = MONTH_COLUMNS;
			
			mMonthLoader = new CursorLoader(this, uri, columns, selection, selectionArgs, null);
			return mMonthLoader;
		case LOADER_DAY_ID:
			uri = TrafficsDay.CONTENT_URI;
			columns = DAY_COLUMNS;
			int month = arg1.getInt("month");
			selection = TrafficsDay.MONTH + "=?";
			selectionArgs = new String[]{month + ""};
			
			mDayLoader = new CursorLoader(this, uri, columns, selection, selectionArgs, null);
			return mDayLoader;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onLoadFinished.count:" + cursor.getColumnCount());
		if (arg0 == mMonthLoader) {
			Log.d(TAG, "Month.onLoaderFinished");
			if (cursor != null && cursor.moveToFirst()) {
				mMonthItems.clear();
				int month;
				long monthBytes;
				TrafficMonthItem monthItem = null;
				do {
					month = cursor.getInt(cursor.getColumnIndex(TrafficsMonth.MONTH));
					monthBytes = cursor.getLong(cursor.getColumnIndex(TrafficsMonth.MONTHBYTES));
					Log.d(TAG, "month:" + month + ",bytes:" + monthBytes);
					mTotalMonthBytes += monthBytes;
					
					monthItem = new TrafficMonthItem();
					monthItem.setMonth(month);
					monthItem.setMonthBytes(monthBytes);
					
					mMonthItems.add(monthItem);
					Log.d(TAG, "mMonthItems.size=" + mMonthItems.size());
				} while (cursor.moveToNext());
				
				//
				mStatiticsView.setText("Toatal Traffic:" + Formatter.formatFileSize(getApplicationContext(), mTotalMonthBytes));
				mMonthAdapter = new TrafficMonthAdapter(this, mMonthItems);
				mListView.setAdapter(mMonthAdapter);
			}
		} else {
			Log.d(TAG, "Day.onLoaderFinished");
			if (cursor != null && cursor.moveToFirst()) {
				mDayItems.clear();
				int day;
				int month;
				long dayBytes;
				long monthBytes;
				TrafficDayItem dayItem = null;
				do {
					day = cursor.getInt(cursor.getColumnIndex(TrafficsDay.DAY));
					month = cursor.getInt(cursor.getColumnIndex(TrafficsDay.MONTH));
					dayBytes = cursor.getLong(cursor.getColumnIndex(TrafficsDay.DAYBYTES));
					monthBytes = cursor.getLong(cursor.getColumnIndex(TrafficsDay.MONTHBYTES));
					Log.d(TAG, "month:" + month + ",day:" + day + ",daybytes:" + dayBytes 
							 + ",monthBytes:" + monthBytes);
					dayItem = new TrafficDayItem();
					dayItem.setMonth(month);
					dayItem.setMonthBytes(monthBytes);
					dayItem.setDay(day);
					dayItem.setDayBytes(dayBytes);
					
					mDayItems.add(dayItem);
					Log.d(TAG, "mDayItems.size=" + mDayItems.size());
				} while (cursor.moveToNext());
				
				mStatiticsView.setText("Month " + month + " Traffic:" + Formatter.formatFileSize(getApplicationContext(), monthBytes));
				mDayAdapter = new TrafficDayAdapter(this, mDayItems);
				mListView.setAdapter(mDayAdapter);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onLoaderReset");
		if (arg0 == mMonthLoader) {
			mMonthItems.clear();
		} else {
			mDayItems.clear();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (mMonthAdapter == mListView.getAdapter()) {
			Log.d(TAG, "month listview clicked");
			TrafficMonthItem monthItem = mMonthItems.get(position);
			int month = monthItem.getMonth();
			
			Bundle bundle = new Bundle();
			bundle.putInt("month", month);
			
			getLoaderManager().restartLoader(LOADER_DAY_ID, bundle, this);
		} else {
			Log.d(TAG, "day listview clicked");
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mMonthAdapter == mListView.getAdapter()) {
			this.finish();
		} else {
			mStatiticsView.setText("Toatal Traffic:" + Formatter.formatFileSize(getApplicationContext(), mTotalMonthBytes));
			mListView.setAdapter(mMonthAdapter);
		}
	}
	
}
