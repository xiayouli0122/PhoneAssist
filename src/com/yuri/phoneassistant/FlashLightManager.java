package com.yuri.phoneassistant;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import com.yuri.phoneassistant.Log;

public class FlashLightManager {
	private static final String TAG = FlashLightManager.class.getSimpleName();
	
	private Camera mCamera;
	private Parameters mParameters;
	private boolean mIsFlashOn = false;
	
	public FlashLightManager(){
		
	}
	
	public boolean isFlashOn(){
		return mIsFlashOn;
	}
	
	public void openFlashlight() {
		try {
			mCamera = Camera.open();
			int textureId = 0;
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);

			mCamera.setPreviewTexture(new SurfaceTexture(textureId));
			mCamera.startPreview();
			mIsFlashOn = true;
		} catch (Exception e) {
			Log.e(TAG, "ERROR:" + e.toString());
		}
	}

	// close flash light
	public void closeFlashlight() {
		if (mCamera != null) {
			mParameters = mCamera.getParameters();
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParameters);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			mIsFlashOn = false;
		}
	}
	
}
