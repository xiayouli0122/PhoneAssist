package com.yuri.phoneassistant.camera;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.yuri.phoneassistant.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.DisplayMetrics;
import com.zhaoyan.common.utils.Log;

import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class QuickCapture extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = QuickCapture.class.getSimpleName();

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	private Camera mCamera;
	private Parameters mParameters;
	private int mCameraBackId = -1;
	private int mCameraFrontId = -1;
	private int mCameraId;
	private int mCameraOrientation;
	private String mAutoFocusMode;

	private AudioManager mAudioManager;
	private int mVolumn;
	private int mTakePictureNumber;

	private PhotoSaver mPhotoSaver;
	private HapticFeedback mHapticFeedback;

	private WakeLock mWakeLock;

	private static final int MSG_AUTO_FOCUS = 1;

	private boolean mIsCaptureSuccess = false;
	
	private boolean mIsShowPreview = false;
	
	private SwipeRefreshLayout mRefreshLayout;
	
	private ListView mListView;
	private TextView mEmptyView;
	
	private int mCount = 0;
	
	private boolean mIsFocus = false;
	private boolean mIsReady = false;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUTO_FOCUS:
				if (mCamera != null && !mIsCaptureSuccess) {
					Log.d(TAG, "MSG_AUTO_FOCUS");
					try {
						mCamera.autoFocus(mAutoFocusCallback);
					} catch (Exception e) {
						Log.e(TAG, "MSG_AUTO_FOCUS " + e);
					}
					mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 2000);
				} else {
					mHandler.removeMessages(MSG_AUTO_FOCUS);
				}
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		if (mIsShowPreview) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		setContentView(R.layout.quick_capture);
		
		View contentView = findViewById(R.id.quick_capture_main);
		contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
    			| View.SYSTEM_UI_FLAG_FULLSCREEN
    			| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		
		initView();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mPhotoSaver = new PhotoSaver(this);
		mHapticFeedback = new HapticFeedback();
		mHapticFeedback.init(this, true);

		int orientationBack = 0;
		int orientationFront = 0;
		int cameraNumber = Camera.getNumberOfCameras();
		for (int i = 0; i < cameraNumber; i++) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				orientationBack = cameraInfo.orientation;
				mCameraBackId = i;
			} else {
				orientationFront = cameraInfo.orientation;
				mCameraFrontId = i;
			}
		}

		if (mCameraBackId != -1) {
			mCameraId = mCameraBackId;
			mCameraOrientation = orientationBack;
		} else {
			mCameraId = mCameraFrontId;
			mCameraOrientation = orientationFront;
		}

		if (mIsShowPreview) {
			WakeupUtil.wakeup(this);
		}
		
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"zhaoyan_camera");
		mWakeLock.acquire();
	}

	private void initView() {
		mEmptyView = (TextView) findViewById(R.id.empty_view);
		mEmptyView.setVisibility(View.GONE);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
		mSurfaceView.getHolder().addCallback(this);
		
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
		mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Handler().post(new Runnable() {
					
					@Override
					public void run() {
						while (mIsFocus && !mIsReady) {
							System.out.println("takePicture");
							takePicture();
							mIsFocus = false;
						}
						
						while (mIsReady) {
							System.out.println("rrrrtakePicture");
							mIsReady = false;
							mCamera.startPreview();
							mTakePictureNumber = 3;
							mCamera.takePicture(null, mRawPictureCallback,
									mJpegPictureCallback);
						}
						
					}
				});
			}
		});
		
		mListView = (ListView) findViewById(R.id.listview);
	}

	private void openCamera() {
		Log.d(TAG, "openCamera");
		mCamera = Camera.open(mCameraId);

		mParameters = mCamera.getParameters();
		// Turn off flash.
		mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		// Enable auto focus.
		List<String> focusModes = mParameters.getSupportedFocusModes();
		if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
			mAutoFocusMode = Parameters.FOCUS_MODE_AUTO;
			mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}
		Log.d(TAG, "mAutoFocusMode = " + mAutoFocusMode);
		// rotation
		mParameters.setRotation(mCameraOrientation);
		// preview and picture size
		setPreviewAndPictureSize();

		mCamera.setParameters(mParameters);

		setCameraOrientation(mSurfaceView, mCamera);

		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
			mCamera.autoFocus(mAutoFocusCallback);
		} catch (IOException e) {
			Log.e(TAG, "open camera error. " + e);
		} catch (Exception e) {
			Log.e(TAG, "open camera error. " + e);
		}
	}

	private void setPreviewAndPictureSize() {
		List<Size> pictureSizes = mParameters.getSupportedPictureSizes();
		List<Size> previewSizes = mParameters.getSupportedPreviewSizes();

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Size pictureSize = findBestFullScreenSize(pictureSizes, metrics);
		Size previewSize = findBestFullScreenSize(previewSizes, metrics);

		Log.d(TAG, "pictureSize " + pictureSize.width + " x "
				+ pictureSize.height);
		Log.d(TAG, "previewSize = " + previewSize.width + " x "
				+ previewSize.height);

		mParameters.setPictureSize(pictureSize.width, pictureSize.height);
		mParameters.setPreviewSize(previewSize.width, previewSize.height);
	}

	private void showSize(String name, List<Size> sizes) {
		for (Size size : sizes) {
			Log.d(TAG, "name " + size.width + " x " + size.height);
		}
	}

	private Size findBestFullScreenSize(List<Size> sizes, DisplayMetrics metrics) {
		double fullscreen;
		if (metrics.widthPixels > metrics.heightPixels) {
			fullscreen = (double) metrics.widthPixels / metrics.heightPixels;
		} else {
			fullscreen = (double) metrics.heightPixels / metrics.widthPixels;
		}

		Collections.sort(sizes, mSizeComparator);

		for (int i = sizes.size() - 1; i >= 0; i--) {
			if (toleranceRatio(fullscreen,
					(double) sizes.get(i).width / sizes.get(i).height)) {
				return sizes.get(i);
			}
		}
		// If no matched size, find the 4/3.
		for (int i = sizes.size() - 1; i >= 0; i--) {
			if (toleranceRatio((double) 4 / 3, (double) sizes.get(i).width
					/ sizes.get(i).height)) {
				return sizes.get(i);
			}
		}
		return sizes.get(sizes.size() - 1);
	}

	private boolean toleranceRatio(double d1, double d2) {
		boolean result = false;
		if (d2 > 0) {
			result = Math.abs(d1 - d2) <= 0.01;
		}
		return result;
	}

	private void setCameraOrientation(View display, Camera camera) {
		if (display.getRotation() == Surface.ROTATION_0
				|| display.getRotation() == Surface.ROTATION_180) {
			// portait
			int orientation = display.getRotation() == Surface.ROTATION_0 ? 90
					: 270;
			mCamera.setDisplayOrientation(orientation);
		} else if (display.getRotation() == Surface.ROTATION_90
				|| display.getRotation() == Surface.ROTATION_270) {
			// landscape
			int orientation = display.getRotation() == Surface.ROTATION_90 ? 0
					: 180;
			mCamera.setDisplayOrientation(orientation);
		}
	}

	private void closeCamera() {
		Log.d(TAG, "closeCamera");
		if (mCamera != null) {
			mCamera.cancelAutoFocus();
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private void takePicture() {
		mTakePictureNumber = 3;
		mCamera.takePicture(null, mRawPictureCallback, mJpegPictureCallback);
		mIsCaptureSuccess = true;
	}

	private ShutterCallback mShutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			Log.d(TAG, "onShutter");
//			mHapticFeedback.vibrate();
		}
	};

	private PictureCallback mRawPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			Log.d(TAG, "RawPictureCallback");
		}
	};

	private PictureCallback mJpegPictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera arg1) {
			Log.d(TAG, "JpegPictureCallback");
			mPhotoSaver.savePhoto(data, new Date(),
					mParameters.getPictureSize().width,
					mParameters.getPictureSize().height);
//			mHapticFeedback.vibrate();

			mTakePictureNumber--;
			if (mTakePictureNumber > 0) {
				mCamera.startPreview();
				mCamera.takePicture(null, mRawPictureCallback,
						mJpegPictureCallback);
			} else {
				mCount ++;
				mEmptyView.setText(mCount + "");
				// finish.
//				finish();
				mIsReady = true;
				mRefreshLayout.setRefreshing(false);
				
				Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
				vib.vibrate(40);
			}
		}
	};

	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean focus, Camera camera) {
			Log.d(TAG, "onAutoFocus " + (focus ? "success. " : "fail."));
			if (focus) {
				mCamera.cancelAutoFocus();
				Log.d(TAG, "onAutoFocus mAutoFocusMode " + mAutoFocusMode);
				mIsFocus = true;
//				takePicture();
			} else {
				mHandler.sendEmptyMessage(MSG_AUTO_FOCUS);
			}
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;
		openCamera();
		silenceVolume();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;
		if (mCamera != null) {
			closeCamera();
		}
		restoreVolume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCamera != null) {
			closeCamera();
		}
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	private void silenceVolume() {
		// mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		// mVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		// if (mVolumn != 0) {
		// mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0,
		// AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
		// AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		// }
	}

	private void restoreVolume() {
		// mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mVolumn,
		// AudioManager.FLAG_ALLOW_RINGER_MODES);
	}

	private Comparator<Size> mSizeComparator = new Comparator<Size>() {

		@Override
		public int compare(Size size1, Size size2) {
			if (size1.width > size2.width) {
				return 1;
			} else if (size1.width == size2.width) {
				return 0;
			} else {
				return -1;
			}
		}

	};
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyAction = event.getAction();
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			Log.d(TAG, "DDDDDKEYCODE_VOLUME_DOWN:" + keyAction);
			if (KeyEvent.ACTION_UP == keyAction) {
				while (mIsFocus && !mIsReady) {
					Log.d(TAG, "ACTION_UP:first takePicture");
					takePicture();
					mIsFocus = false;
				}
				
				while (mIsReady) {
					Log.d(TAG, "re takepicture");
					mIsReady = false;
					mCamera.startPreview();
					mTakePictureNumber = 3;
					mCamera.takePicture(null, mRawPictureCallback,
							mJpegPictureCallback);
				}
			} 
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

}
