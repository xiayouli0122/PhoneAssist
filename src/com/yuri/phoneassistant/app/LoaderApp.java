/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuri.phoneassistant.app;

import java.text.Collator;
import java.util.Comparator;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

/**
 * Demonstration of the implementation of a custom Loader.
 */
public class LoaderApp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getFragmentManager();

		// Create the list fragment and add it as our sole content.
		if (fm.findFragmentById(android.R.id.content) == null) {
			AppListFragment list = new AppListFragment();
			fm.beginTransaction().add(android.R.id.content, list).commit();
		}
	}

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
		private final Collator sCollator = Collator.getInstance();

		@Override
		public int compare(AppEntry object1, AppEntry object2) {
			return sCollator.compare(object1.getLabel(), object2.getLabel());
		}
	};

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {
		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			int configChanges = mLastConfiguration.updateFrom(res
					.getConfiguration());
			boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE
							| ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppListLoader mLoader;

		public PackageIntentReceiver(AppListLoader loader) {
			mLoader = loader;
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

}
