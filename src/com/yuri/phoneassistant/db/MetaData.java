package com.yuri.phoneassistant.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class MetaData{
	public static final String DATABASE_NAME = "traffics.db";
	public static final int DATABASE_VERSION = 1;

	public static final String AUTHORITY = "com.yuri.phoneassistant.db.dbprovider";
	
	/**profiles table*/
	public static final class TrafficsDay implements BaseColumns{
		public static final String TABLE_NAME = "day";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/day");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/day_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/day";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/day";
		
		//items
		public static final String DAYBYTES = "dayBytes";
		/**txBytes. Type:long*/
		public static final String DAYTXBYTES = "dayTxBytes";
		/**RxBytes. Type:long*/
		public static final String DAYRXBYTES = "dayRxBytes";
		public static final String MONTHBYTES = "monthBytes";
		
		/**Date. Type:Long*/
		public static final String DATE = "date";
		/**Date. Type:Long*/
		public static final String DAYENDTIME = "dayEndTime";
		public static final String YEAR = "year";
		public static final String MONTH = "month";
		public static final String DAY = "day";								
		
		/**order by _id DESC*/
		public static final String SORT_ORDER_DEFAULT = _ID + " DESC"; 
		/**order by time DESC*/
		public static final String SORT_ORDER_TIME = DATE + " DESC"; 
		/**order by group DESC*/
		
	}
	
	/**profiles table*/
	public static final class TrafficsMonth implements BaseColumns{
		public static final String TABLE_NAME = "month";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/month");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/month_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/month";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/month";
		
		//items
		public static final String MONTHBYTES = "monthBytes";
		/**txBytes. Type:long*/
		public static final String MONTHTXBYTES = "dayTxBytes";
		/**RxBytes. Type:long*/
		public static final String MONTHRXBYTES = "dayRxBytes";
		
		/**Date. Type:Long*/
		public static final String DATE = "date";
		/**Date. Type:Long*/
		public static final String MONTHENDTIME = "monthEndTime";
		public static final String YEAR = "year";
		public static final String MONTH = "month";
		
		/**order by _id DESC*/
		public static final String SORT_ORDER_DEFAULT = _ID + " DESC"; 
		/**order by time DESC*/
		public static final String SORT_ORDER_TIME = DATE + " DESC"; 
		/**order by group DESC*/
		
	}
}
