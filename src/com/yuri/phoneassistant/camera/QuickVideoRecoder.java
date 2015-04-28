package com.yuri.phoneassistant.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.nfc.Tag;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yuri.phoneassistant.R;
import com.zhaoyan.common.utils.Log;

public class QuickVideoRecoder extends Activity implements OnClickListener,
		OnTouchListener, Callback {
	
	private static final String TAG = QuickVideoRecoder.class.getSimpleName();

	private MediaRecorder mediaRecorder;
	private Camera mCamera;
	private CameraPreview preview;
	private Camera.Parameters cameraParams;
	private int mCameraBackId = -1;
	private int mCameraFrontId = -1;
	private int mCameraId;
	private int mCameraOrientation;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private String mAutoFocusMode = "";

//	private FrameLayout layout;
	private TextView videoSize;
	private ImageView back;

	private SparseIntArray widthArray = new SparseIntArray();
	private SparseIntArray heightArray = new SparseIntArray();
	private int sizeIndex = 0;
	private String path;
	private String fileName;

	private boolean isTaking;

	private TextView timer;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;

	private VertialSeekBar mZoomBar = null;
	private Button btnVideoButton;

	private AutoFocusCallback myAutoFocusCallback = null;
	private boolean isControlEnable = true;
	private LinearLayout videoSizeLayout;
	private boolean isVideoSizeSupport = true;
	private boolean isVideoAuto = true;

	private VideoSizeAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		setContentView(R.layout.video_recoder);
		myAutoFocusCallback = new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
					camera.setOneShotPreviewCallback(null);
				} else {
				}
			}
		};
		initView();
		
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
	}

	/**
	 * @Title initView
	 * @Description 加载视图及相关属性
	 * @author wen
	 * @Date 2013-12-10 上午10:14:21
	 */
	private void initView() {
//		layout = (FrameLayout) findViewById(R.id.take_video_layout);
		btnVideoButton = (Button) findViewById(R.id.arc_hf_video_start);
		videoSizeLayout = (LinearLayout) findViewById(R.id.videoSizeLayout);
		videoSize = (TextView) findViewById(R.id.take_video_size);
		timer = (TextView) findViewById(R.id.arc_hf_video_timer);
		mZoomBar = (VertialSeekBar) findViewById(R.id.seekBar);
		mZoomBar.setVisibility(View.GONE);
		back = (ImageView) findViewById(R.id.take_video_back);

		timer.setVisibility(View.GONE);
		videoSizeLayout.setVisibility(View.VISIBLE);

		back.setOnClickListener(this);
		btnVideoButton.setOnClickListener(this);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.sv_camera);
		mSurfaceView.getHolder().addCallback(this);
		mSurfaceView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isControlEnable) {
					isControlEnable = false;
					startOrStopVideo();
					handler.sendEmptyMessageDelayed(0, 2000);
				} else {
					Toast.makeText(QuickVideoRecoder.this, "2S的延迟操作", Toast.LENGTH_SHORT).show();
				}
			}
		});
//		initCamera();
		path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/";
		fileName = "video1216.mp4";
	}

	/**
	 * @Title initCamera
	 * @Description 加载相机相关组件
	 * @author wen
	 * @Date 2013-12-10 上午10:14:21
	 */
	private void initCamera() {
		
		mCamera = Camera.open(mCameraId);
//		preview = new CameraPreview(this, mCamera);
//		preview.setFocusable(false);
//		preview.setEnabled(false);

		cameraParams = mCamera.getParameters();
		
		// Enable auto focus.
//		List<String> focusModes = cameraParams.getSupportedFocusModes();
//		if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
//			mAutoFocusMode = Parameters.FOCUS_MODE_AUTO;
//			cameraParams.setFocusMode(Parameters.FOCUS_MODE_AUTO);
//		}
//				
//		if (VERSION.SDK_INT >= 14) {
//			cameraParams.setFocusMode("auto");
//			mCamera.autoFocus(myAutoFocusCallback);
//		} else {
//			cameraParams
//					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//			isVideoAuto = false;
//		}

		List<Size> tempList = cameraParams.getSupportedPreviewSizes();
		if (tempList != null && tempList.size() > 0) {
			for (int i = 0; i < tempList.size(); i++) {
				widthArray.put(i, tempList.get(i).width);
				heightArray.put(i, tempList.get(i).height);
			}
			setVideoSize();
		} else {
			isVideoSizeSupport = false;
			videoSizeLayout.setVisibility(View.GONE);
		}
		Log.d(TAG, "initCamera.orientiaon:" + mCameraOrientation);
		cameraParams.setRotation(mCameraOrientation);
		mCamera.setParameters(cameraParams);
		
		setCameraOrientation(mSurfaceView, mCamera);
		
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
			mCamera.startPreview();
			mCamera.autoFocus(myAutoFocusCallback);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * @Title startCamera
	 * @Description 开始录像
	 * @author wen
	 * @Date 2013-12-10 上午10:14:21
	 */
	private void startCamera() {
		second = 0;
		minute = 0;
		hour = 0;
		bool = true;
//		mCamera.startPreview();
		mCamera.unlock();// 解锁后才能调用，否则报错
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setCamera(mCamera);// 将camera添加到视频录制端口
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 音麦
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);// 输出格式
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// 声音源码
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// 视频源码
//		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		if (isVideoSizeSupport) {
			int width = widthArray.get(sizeIndex);
			int height = heightArray.get(sizeIndex);
			Log.d(TAG, "Recoder.width:" + width + ",height:" + height);
			mediaRecorder.setVideoSize(widthArray.get(sizeIndex),
					heightArray.get(sizeIndex));
		} else {
			mediaRecorder.setVideoSize(320, 240);
		}
		mediaRecorder.setVideoFrameRate(8);

		mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mediaRecorder.setOutputFile(path + fileName);
		try {
			mediaRecorder.prepare();
			timer.setVisibility(View.VISIBLE);
			handler.postDelayed(task, 1000);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mediaRecorder.start();
			Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
		} catch (IllegalStateException e) {
			this.finish();
			Toast.makeText(this, "不能录制视频!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mediaRecorder != null) {
			mediaRecorder.release();
		}
		if (mCamera != null) {
			mCamera.lock();
			mCamera.release();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.arc_hf_video_start:
			if (isControlEnable) {
				isControlEnable = false;
				startOrStopVideo();
				handler.sendEmptyMessageDelayed(0, 2000);
			} else {
				Toast.makeText(this, "2S的延迟操作", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.take_video_back:
			if (isTaking) {
				showDialog();
			} else {
				finish();
			}
			break;
		default:
			break;
		}
	}

	public void onRealution(View view) {
		if (isTaking) {
			Toast.makeText(this, "录制视频不允许切换视频尺寸", Toast.LENGTH_SHORT).show();
		} else {
			final Dialog dialog = new Dialog(this);
			ListView listView = new ListView(this);
			listView.setCacheColorHint(Color.TRANSPARENT);
			listView.setBackgroundColor(Color.WHITE);
			if (adapter == null)
				adapter = new VideoSizeAdapter(this, widthArray, heightArray);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					adapter.index = arg2;
					sizeIndex = arg2;
					setVideoSize();
					dialog.dismiss();
				}
			});
			dialog.setTitle("分辨率设置");
			dialog.setContentView(listView);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
		}
	}

	private void startOrStopVideo() {
		if (isTaking) {
			mediaRecorder.stop();
			Toast.makeText(this, "录制完成，已保存", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			videoSizeLayout.setVisibility(View.GONE);
			startCamera();
			isTaking = true;
		}
	}

	/*
	 * 定时器设置，实现计时
	 */
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			isControlEnable = true;
			return false;
		}
	});
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(format(hour) + ":" + format(minute) + ":"
						+ format(second));
			}
		}
	};

	/*
	 * 格式化时间
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mCamera != null && isVideoAuto) {
				float x = event.getX();
				float y = event.getY();
				float touchMajor = event.getTouchMajor();
				float touchMinor = event.getTouchMinor();

				Rect touchRect = new Rect((int) (x - touchMajor / 2),
						(int) (y - touchMinor / 2), (int) (x + touchMajor / 2),
						(int) (y + touchMinor / 2));

				this.submitFocusAreaRect(touchRect);
			}
		}
		return false;
	}

	private void submitFocusAreaRect(final Rect touchRect) {
		Camera.Parameters cameraParameters = mCamera.getParameters();
		if (cameraParameters == null
				|| cameraParameters.getMaxNumFocusAreas() == 0) {
			return;
		}

		// Convert from View's width and height to +/- 1000
		Rect focusArea = new Rect();
		focusArea.set(touchRect.left * 2000 / preview.getWidth() - 1000,
				touchRect.top * 2000 / preview.getHeight() - 1000,
				touchRect.right * 2000 / preview.getWidth() - 1000,
				touchRect.bottom * 2000 / preview.getHeight() - 1000);
		// Submit focus area to camera
		ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
		focusAreas.add(new Camera.Area(focusArea, 1000));
		cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		cameraParameters.setFocusAreas(focusAreas);
		try {
			mCamera.setParameters(cameraParameters);
			mCamera.autoFocus(myAutoFocusCallback);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isTaking) {
				showDialog();
			} else {
				finish();
			}
			// 这里不需要执行父类的点击事件，所以直接return
			return true;
		}
		// 继续执行父类的其他点击事件
		return super.onKeyDown(keyCode, event);
	}

	private void showDialog() {
		// 弹出确定退出对话框
		new AlertDialog.Builder(this).setTitle("退出").setMessage("确定退出视频录制吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						QuickVideoRecoder.this.finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}

	private void setVideoSize() {
		videoSize.setText(widthArray.get(sizeIndex) + "X"
				+ heightArray.get(sizeIndex));
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
		initCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = null;
		if (mCamera != null) {
//			closeCamera();
		}
	}

}
