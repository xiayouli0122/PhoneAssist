package com.yuri.phoneassistant.app;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.yuri.phoneassistant.NetWorkUtil;
import com.yuri.phoneassistant.WeakRefHandler;
import com.yuri.phoneassistant.Log;

import android.R.integer;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class AppListFragment extends ListFragment implements
		OnQueryTextListener, LoaderManager.LoaderCallbacks<List<AppEntry>>,
		OnItemLongClickListener {

	protected static final int REFRESH_TIME_MILLS = 5000;

	// This is the Adapter being used to display the list's data.
	AppListAdapter mAdapter;

	// If non-null, this is the current filter the user has provided.
	String mCurFilter;

	private Timer mTimer;
	
	private List<AppEntry> mAppEntries;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = NetWorkUtil.getAPNType(getActivity()
					.getApplicationContext());
			if (type == -1) {
				getActivity().setTitle("PA(无网络连接)");
				if (mTimer != null) {
					mTimer.cancel();
				}
			} else if (type == 1) {
				getActivity().setTitle("PA(Wifi连接)");
			} else {
				getActivity().setTitle("PA(数据连接)");

				if (mTimer != null) {
					mTimer.cancel();
				}

				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {
					public void run() {
						getTraffic();
					}
				}, 0, REFRESH_TIME_MILLS);
			}
		}
	};
	
	private WeakRefHandler mHandler = new WeakRefHandler(getActivity()) {
		@Override
		public void handleMessage(Message msg) {
			mAdapter.notifyDataSetChanged();
			super.handleMessage(msg);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		setEmptyText("No applications");

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mReceiver, filter);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new AppListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false);
		getListView().setOnItemLongClickListener(this);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Place an action bar item for searching.
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		SearchView sv = new SearchView(getActivity());
		sv.setOnQueryTextListener(this);
		item.setActionView(sv);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Since this
		// is a simple array adapter, we can just have it do the filtering.
		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
		mAdapter.getFilter().filter(mCurFilter);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Insert desired behavior here.
		Log.i("LoaderCustom", "Item clicked: " + id);
	}

	@Override
	public Loader<List<AppEntry>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader with no arguments, so it is simple.
		return new AppListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<AppEntry>> loader,
			List<AppEntry> data) {
		// Set the new data in the adapter.
		
		mAppEntries = data;
		
		mAdapter.setData(mAppEntries);
		

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

		int type = NetWorkUtil
				.getAPNType(getActivity().getApplicationContext());
		if (type == -1) {
			System.out.println("无网络连接");
		} else if (type == 1) {
			System.out.println("wif连接");
		} else {
			System.out.println("数据连接");
			if (mTimer != null) {
				mTimer.cancel();
			}

			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				public void run() {
					getTraffic();
				}
			}, 0, REFRESH_TIME_MILLS);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<AppEntry>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}
	
	public void getTraffic(){
		int uId;
		long rx;
		long tx;
		
		if (mAppEntries == null) {
			return;
		}
		
		for (AppEntry appEntry : mAppEntries) {
			// 获取每个应用程序在操作系统内的进程id
			 uId = appEntry.mInfo.uid;
			 rx = TrafficStats.getUidRxBytes(uId);
			 
			 tx = TrafficStats.getUidTxBytes(uId);
			 appEntry.setTxBytes(tx);
			 appEntry.setRxBytes(rx);
			 
			 mHandler.sendMessage(mHandler.obtainMessage());
		}
		
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final AppEntry appEntry = mAdapter.getItem(position);
		CharSequence[] items = { "Open" };
		new AlertDialog.Builder(getActivity()).setTitle("Menu")
				.setItems(items, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							// open
							Intent intent = getActivity().getPackageManager()
									.getLaunchIntentForPackage(
											appEntry.mInfo.packageName);
							if (intent != null) {
								startActivity(intent);
							} else {
								Toast.makeText(getActivity(), "Cannot open",
										Toast.LENGTH_SHORT).show();
							}
							break;

						default:
							System.out.println("default:" + which);
							break;
						}
					}
				}).create().show();
		return true;
	}

	public String getAppVersion(String packageName, PackageManager pm) {
		String version = "";
		try {
			version = pm.getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(mReceiver);
	}
}
