package com.yuri.phoneassistant.app;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.yuri.phoneassistant.R;

public class AsyncAppIconLoader {
	private static final String TAG = "AsyncImageLoader";
	public static  HashMap<String, SoftReference<Bitmap>> bitmapCache;
	private Context context;

	public AsyncAppIconLoader(Context context) {
		this.context = context;
		bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	}
	
	public Bitmap loadImage(final String path, final ImageView imageView, final ILoadImageCallback callback){
		//we use file path as key
		if (bitmapCache.containsKey(path)) {
			SoftReference<Bitmap> softReference = bitmapCache.get(path);
			Bitmap bitmap = softReference.get();
			if (null != bitmap) {
				return bitmap;
			}
		}
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				callback.onObtainBitmap((Bitmap) message.obj, imageView);
			}
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bitmap bitmap = null;
				Drawable drawable = getApkIcon(path);
				BitmapDrawable bd = (BitmapDrawable) drawable;
				if (null != bd) {
					bitmap = bd.getBitmap();
				}
				
				bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
				Message msg = handler.obtainMessage(0, bitmap);
				handler.sendMessage(msg);
			}
		}).start();
		
		return null;
	}

	public interface ILoadImageCallback {
		public void onObtainBitmap(Bitmap bitmap, ImageView imageView);
	}
	
	/**
	 * δ��װ�ĳ���ͨ��apk�ļ���ȡicon
	 * 
	 * @param path
	 *            apk�ļ�·��
	 * @return apk��icon
	 */
	public Drawable getApkIcon(String path) {
		String apkPath = path; // apk �ļ����ڵ�·��
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();

			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };

			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");

			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);

			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			typeArgs = new Class[1];
			typeArgs[0] = String.class;

			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;

			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			Resources res = context.getResources();

			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();

			Constructor<Resources> resCt = Resources.class
					.getConstructor(typeArgs);

			valueArgs = new Object[3];

			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);

			if (info != null) {
				if (info.icon != 0) {
					Drawable icon = res.getDrawable(info.icon);
					return icon;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return context.getResources().getDrawable(R.drawable.icon);
	}
}
