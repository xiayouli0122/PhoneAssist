package com.yuri.phoneassistant.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.yuri.phoneassistant.app.LoaderApp.InterestingConfigChanges;
import com.yuri.phoneassistant.app.LoaderApp.PackageIntentReceiver;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {
	final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	final PackageManager mPm;

	List<AppEntry> mApps;
	PackageIntentReceiver mPackageObserver;
	
	private Context mContext;

	public AppListLoader(Context context) {
		super(context);
		mContext = context;
		// Retrieve the package manager for later use; note we don't
		// use 'context' directly but instead the save global application
		// context returned by getContext().
		mPm = getContext().getPackageManager();
	}

	/**
	 * This is where the bulk of our work is done. This function is called
	 * in a background thread and should generate a new set of data to be
	 * published by the loader.
	 */
	@Override
	public List<AppEntry> loadInBackground() {
		// Retrieve all known applications.
		// List<ApplicationInfo> apps = mPm.getInstalledApplications(
		// PackageManager.GET_UNINSTALLED_PACKAGES |
		// PackageManager.GET_DISABLED_COMPONENTS);
		// if (apps == null) {
		// apps = new ArrayList<ApplicationInfo>();
		// }

		// 获取每个包内的androidmanifest.xml信息，它的权限等等
		List<PackageInfo> pinfos = mPm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
						| PackageManager.GET_PERMISSIONS);
		if (pinfos == null) {
			pinfos = new ArrayList<PackageInfo>();
		}

		final Context context = getContext();

		// Create corresponding array of entries and load their labels.
		List<AppEntry> entries = new ArrayList<AppEntry>(pinfos.size());
		for (PackageInfo info : pinfos) {
			AppEntry entry = new AppEntry(this, info.applicationInfo);
			entry.loadLabel(context);
			entry.setIntentAccess(false);

			// 请求每个程序包对应的androidManifest.xml里面的权限
			String[] premissions = info.requestedPermissions;
			if (premissions != null && premissions.length > 0) {
				// 找出需要网络服务的应用程序
				for (String premission : premissions) {
					if ("android.permission.INTERNET".equals(premission)) {
						entry.setIntentAccess(true);
						entries.add(entry);
					}
				}
			}
			
		}

		// Sort the list.
		Collections.sort(entries, LoaderApp.ALPHA_COMPARATOR);

		// Done!
		return entries;
	}

	/**
	 * Called when there is new data to deliver to the client. The super
	 * class will take care of delivering it; the implementation here just
	 * adds a little more logic.
	 */
	@Override
	public void deliverResult(List<AppEntry> apps) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
			if (apps != null) {
				onReleaseResources(apps);
			}
		}
		List<AppEntry> oldApps = apps;
		mApps = apps;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(apps);
		}

		// At this point we can release the resources associated with
		// 'oldApps' if needed; now that the new result is delivered we
		// know that it is no longer in use.
		if (oldApps != null) {
			onReleaseResources(oldApps);
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mApps != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mApps);
		}

		// Start watching for changes in the app data.
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		boolean configChange = mLastConfig.applyNewConfig(getContext()
				.getResources());

		if (takeContentChanged() || mApps == null || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(List<AppEntry> apps) {
		super.onCanceled(apps);

		// At this point we can release the resources associated with 'apps'
		// if needed.
		onReleaseResources(apps);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// At this point we can release the resources associated with 'apps'
		// if needed.
		if (mApps != null) {
			onReleaseResources(mApps);
			mApps = null;
		}

		// Stop monitoring for changes.
		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}

	/**
	 * Helper function to take care of releasing resources associated with
	 * an actively loaded data set.
	 */
	protected void onReleaseResources(List<AppEntry> apps) {
		// For a simple List<> there is nothing to do. For something
		// like a Cursor, we would close it here.
	}
}
