package com.yuri.phoneassistant.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import com.yuri.phoneassistant.Log;

public class PhotoSaver {
	private static final String TAG = PhotoSaver.class.getSimpleName();

	private Date mLastDate;
	private int mSameSecondCount = 0;
	private ContentResolver mResolver;

	public PhotoSaver(Context context) {
		mResolver = context.getContentResolver();
	}

	public void savePhoto(byte[] data, Date date, int width, int height) {
		SaveTask task = new SaveTask(data, generateFileName(date), date, width,
				height);
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * IMG_20140825_144930_1.jpg
	 * 
	 * @param date
	 * @return
	 */
	private String generateFileName(Date date) {
		String name = "IMG_"
				+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);

		if (mLastDate != null
				&& date.getTime() / 1000 == mLastDate.getTime() / 1000) {
			mSameSecondCount++;
			name += "_" + mSameSecondCount;
		} else {
			mSameSecondCount = 0;
			mLastDate = date;
		}

		name += ".jpg";

		return name;
	}

	class SaveTask extends AsyncTask<Void, Void, Boolean> {
		private byte[] mJpeg;
		private String mFileName;
		private Date mDate;
		private int mWidth;
		private int mHeight;

		public SaveTask(byte[] jpeg, String fileName, Date date, int width,
				int height) {
			mJpeg = jpeg;
			mFileName = fileName;
			mDate = date;
			mWidth = width;
			mHeight = height;
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			saveImageToSDCard(mJpeg, getFilePath());
			saveImageToDatabase();
			return true;
		}

		private void saveImageToDatabase() {
			ContentValues values = new ContentValues();
			values.put(ImageColumns.TITLE, mFileName);
			values.put(ImageColumns.DISPLAY_NAME, mFileName);
			values.put(ImageColumns.DATE_TAKEN, mDate.getTime());
			values.put(ImageColumns.DATA, getFilePath());
			values.put(ImageColumns.ORIENTATION, Exif.getOrientation(mJpeg));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				Log.d(TAG, "WIDTH = " + mWidth + ", HEIGHT = " + mHeight);
				values.put(ImageColumns.WIDTH, mWidth);
				values.put(ImageColumns.HEIGHT, mHeight);
			}
			mResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		}

		private void saveImageToSDCard(byte[] data, String filePath) {
			Log.d(TAG, "saveImageToSDCard " + filePath);
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(filePath);
				outputStream.write(data);
			} catch (Exception e) {
				Log.e(TAG, "saveImageToSDCard failed to write" + e);
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						Log.e(TAG, "saveImageToSDCard " + e);
					}
				}
			}
		}

		private String getFilePath() {
			String path = Environment.getExternalStorageDirectory()
					+ File.separator + Environment.DIRECTORY_DCIM
					+ File.separator + "Camera" + File.separator + mFileName;
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.mkdirs();
			}
			return path;
		}

	}
}
