package com.yuri.phoneassistant.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.zhaoyan.common.utils.Log;

public class DBProvider extends ContentProvider {
	private static final String TAG = DBProvider.class.getSimpleName();
	
	private SQLiteDatabase mSqLiteDatabase;
	private DatabaseHelper mDatabaseHelper;
	
	public static final int DAY_COLLECTION = 1;
	public static final int DAY_SINGLE = 2;
	public static final int DAY_FILTER = 3;
	
	public static final int MONTH_COLLECTION = 4;
	public static final int MONTH_SINGLE = 5;
	public static final int MONTH_FILTER = 6;
	
	public static final UriMatcher uriMatcher;
	
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(MetaData.AUTHORITY, "day", DAY_COLLECTION);
		uriMatcher.addURI(MetaData.AUTHORITY, "day/#", DAY_SINGLE);
		uriMatcher.addURI(MetaData.AUTHORITY, "day_filter/*", DAY_FILTER);
		
		uriMatcher.addURI(MetaData.AUTHORITY, "month", MONTH_COLLECTION);
		uriMatcher.addURI(MetaData.AUTHORITY, "month/#", MONTH_SINGLE);
		uriMatcher.addURI(MetaData.AUTHORITY, "month/*", MONTH_FILTER);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, MetaData.DATABASE_NAME, null, MetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			//notes table
			db.execSQL("create table " + MetaData.TrafficsDay.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ MetaData.TrafficsDay.DAYBYTES + " LONG, "
					+ MetaData.TrafficsDay.DAYTXBYTES + " LONG, "
					+ MetaData.TrafficsDay.DAYRXBYTES + " LONG, "
					+ MetaData.TrafficsDay.MONTHBYTES + " LONG, "
					+ MetaData.TrafficsDay.DATE + " LONG, "
					+ MetaData.TrafficsDay.YEAR + " INTEGER, "
					+ MetaData.TrafficsDay.MONTH + " INTEGER, "
					+ MetaData.TrafficsDay.DAY + " INTEGER, "
					+ MetaData.TrafficsDay.DAYENDTIME + " LONG UNIQUE);"
					);
			
			db.execSQL("create table " + MetaData.TrafficsMonth.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ MetaData.TrafficsMonth.MONTHBYTES + " LONG, "
					+ MetaData.TrafficsMonth.MONTHTXBYTES + " LONG, "
					+ MetaData.TrafficsMonth.MONTHRXBYTES + " LONG, "
					+ MetaData.TrafficsMonth.DATE + " LONG, "
					+ MetaData.TrafficsDay.YEAR + " INTEGER, "
					+ MetaData.TrafficsDay.MONTH + " INTEGER, "
					+ MetaData.TrafficsMonth.MONTHENDTIME + " LONG UNIQUE);"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//如果数据库版本发生变化，则删掉重建
			db.execSQL("DROP TABLE IF EXISTS " + MetaData.TrafficsDay.TABLE_NAME);
			onCreate(db);
		}
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case DAY_COLLECTION:
			
			count = mSqLiteDatabase.delete(MetaData.TrafficsDay.TABLE_NAME, selection, selectionArgs);
			break;
		case DAY_SINGLE:
			String segment = uri.getPathSegments().get(1);
			if (selection != null && segment.length() > 0) {
				selection = "_id=" + segment + " AND (" + selection + ")";
			}else {
				selection = "_id=" +  segment;
			}
			count = mSqLiteDatabase.delete(MetaData.TrafficsDay.TABLE_NAME, selection, selectionArgs);
			break;
			
		case MONTH_COLLECTION:
			count = mSqLiteDatabase.delete(MetaData.TrafficsMonth.TABLE_NAME, selection, selectionArgs);
			break;
		case MONTH_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			if (selection != null && segment2.length() > 0) {
				selection = "_id=" + segment2 + " AND (" + selection + ")";
			}else {
				selection = "_id=" +  segment2;
			}
			count = mSqLiteDatabase.delete(MetaData.TrafficsMonth.TABLE_NAME, selection, selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("UnKnow Uri:" + uri);
		}
		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case DAY_COLLECTION:
			return MetaData.TrafficsDay.CONTENT_TYPE;
		case DAY_SINGLE:
			return MetaData.TrafficsDay.CONTENT_TYPE_ITEM;
		case MONTH_COLLECTION:
			return MetaData.TrafficsMonth.CONTENT_TYPE;
		case MONTH_SINGLE:
			return MetaData.TrafficsMonth.CONTENT_TYPE_ITEM;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert db");
		switch (uriMatcher.match(uri)) {
		case DAY_COLLECTION:
		case DAY_SINGLE:
			mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
			long rowId = mSqLiteDatabase.insertWithOnConflict(MetaData.TrafficsDay.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(MetaData.TrafficsDay.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		case MONTH_COLLECTION:
		case MONTH_SINGLE:
			mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
			long rowId2 = mSqLiteDatabase.insertWithOnConflict(MetaData.TrafficsMonth.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId2 > 0) {
				Uri rowUri = ContentUris.withAppendedId(MetaData.TrafficsMonth.CONTENT_URI, rowId2);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return (mDatabaseHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case DAY_COLLECTION:
			qb.setTables(MetaData.TrafficsDay.TABLE_NAME);
			break;
		case DAY_SINGLE:
			qb.setTables(MetaData.TrafficsDay.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case MONTH_COLLECTION:
			qb.setTables(MetaData.TrafficsMonth.TABLE_NAME);
			break;
		case MONTH_SINGLE:
			qb.setTables(MetaData.TrafficsMonth.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case DAY_FILTER:
			break;
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
		
		mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
		Cursor ret = qb.query(mSqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		
		if (ret != null) {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count;
		long rowId = 0;
		int match = uriMatcher.match(uri);
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		switch (match) {
		case DAY_SINGLE:
			String segment = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			
			count = mSqLiteDatabase.update(MetaData.TrafficsDay.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case DAY_COLLECTION:
			count = mSqLiteDatabase.update(MetaData.TrafficsDay.TABLE_NAME, values, selection, null);
			break;
		case MONTH_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment2);
			
			count = mSqLiteDatabase.update(MetaData.TrafficsMonth.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case MONTH_COLLECTION:
			count = mSqLiteDatabase.update(MetaData.TrafficsMonth.TABLE_NAME, values, selection, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update uri:" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

}
