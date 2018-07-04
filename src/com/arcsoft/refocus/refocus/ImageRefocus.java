package com.arcsoft.refocus.refocus;

import android.util.Log;

public class ImageRefocus {
	
	private static final String TAG = "ImageRefocus";

	static {
		System.loadLibrary("arcsoft_dualcam_refocus_wrap");
	}

	private long mEngineHandler = 0;

	private boolean IsInited() {
		return (mEngineHandler != 0);
	}

	public void Init(int mode) {
		//#define ARC_DCIR_NORMAL_MODE				0x01
		//#define ARC_DCIR_POST_REFOCUS_MODE		0x02
		Log.i(TAG, "Init in = " + mode);
		if (!IsInited()) {
			mEngineHandler = ImageRefocus_Init(mode);
		}
		Log.i(TAG, "Init out = " + mode);
	}

	public void UnInit() {
		Log.i(TAG, "UnInit in");
		if (IsInited()) {
			ImageRefocus_Uninit(mEngineHandler);
			mEngineHandler = 0;
		}
		Log.i(TAG, "UnInit out");
	}
	
	public int SetDCIRParam(int imgDegree, int fMaxFOV) {
		Log.i(TAG, "SetDCIRParam in, imgDegree="+imgDegree+", fMaxFOV="+fMaxFOV);
		int res = -1;
		if (IsInited()) {
			res = ImageRefocus_SetDCIRParam(mEngineHandler, imgDegree, fMaxFOV);
		}
		Log.i(TAG, "SetDCIRParam out = " + res);
		return res;
	}
	
	public int SetParam(int ptX, int ptY, int blurIntensity) {
		Log.i(TAG, "SetParam in");
		int res = -1;
		if (IsInited()) {
			res = ImageRefocus_SetParam(mEngineHandler, ptX, ptY, blurIntensity);
		}
		Log.i(TAG, "SetParam out = " + res);
		return res;
	}

	public int SetCameraImageInfo(int main_width, int main_height, int aux_width, int aux_height) {
		Log.i(TAG, "SetCameraImageInfo in, " + main_width + "x" + main_height + ", " + aux_width + "x" + aux_height);
		int res = -1;
		if (IsInited()) {
			res = ImageRefocus_SetCameraImageInfo(mEngineHandler, main_width, main_height, aux_width, aux_height);
		}
		Log.i(TAG, "SetCameraImageInfo out = " + res);
		return res;
	}
	
	public synchronized int SetCalibrationData(byte[] caliData, int length) {
		Log.i(TAG, "SetCalibrationData in, length = " + length);
		int res = -1;
		if (IsInited()) {
			res = ImageRefocus_SetCalibrationData(mEngineHandler, caliData, length);
		}
		Log.i(TAG, "SetCalibrationData out, res = " + res);
		return res;
	}

	public synchronized byte[] GetImageResult(byte[] jleftData, int leftlength, byte[] jrightData, int rightlength, int width, int height, int auxWidth, int auxHeight) {
		Log.i(TAG, "GetImageResult in");
		if (IsInited()) {
			byte[] b = ImageRefocus_GetImageResult(mEngineHandler, jleftData, leftlength, jrightData, rightlength, width, height, auxWidth, auxHeight);
			Log.i(TAG, "GetImageResult out");
			return b;
		}
		Log.i(TAG, "GetImageResult out, not inited");
		return new byte[0];
	}
	
	public synchronized byte[] GetImageResultWithDepth(byte[] jleftData, int leftlength, int width, int height, byte[] jDisprityMap, int disprityMapLength) {
		Log.i(TAG, "GetImageResultWithDepth in");
		if (IsInited()) {
			byte[] b = ImageRefocus_GetImageResultWithDepth(mEngineHandler, jleftData, leftlength, width, height, jDisprityMap, disprityMapLength);
			Log.i(TAG, "GetImageResultWithDepth out");
			return b;
		}
		Log.i(TAG, "GetImageResultWithDepth out, not inited");
		return new byte[0];
	}

	/***********************************************************************/
	/********************** Refocus Native JNI Define **********************/
	/***********************************************************************/
	private native long ImageRefocus_Init(int mode);

	private native void ImageRefocus_Uninit(long hEngine);

	private native int ImageRefocus_SetDCIRParam(long hEngine, int imgDegree, int fMaxFOV);

	private native int ImageRefocus_SetParam(long hEngine, int ptX, int ptY, int blurIntensity);

	private native int ImageRefocus_SetCameraImageInfo(long hEngine, int main_width, int main_height, int aux_width, int aux_height);
		
	private native int ImageRefocus_SetCalibrationData(long hEngine, byte[] caliData, int length);
		
	private native byte[] ImageRefocus_GetImageResult(long hEngine, byte[] jleftData, int leftlength, byte[] jrightData, int rightlength, int width, int height, int auxWidth, int auxHeight);

	private native byte[] ImageRefocus_GetImageResultWithDepth(long hEngine, byte[] jleftData, int leftlength, int width, int height, byte[] jDisprityMap, int disprityMapLength);

}