package com.arcsoft.refocus.refocus;

public class VideoRefocus {
	private static final String TAG = "VideoRefocus";

	static {
		System.loadLibrary("arcsoft_dualcam_refocus_wrap");
	}

	private int mEngineHandler = 0;

	private boolean IsInited() {
		return (mEngineHandler != 0);
	}

	public void Init() {
		if (!IsInited()) {
			mEngineHandler = VideoRefocus_Init();
		}
	}

	public void UnInit() {
		if (IsInited()) {
			VideoRefocus_Uninit(mEngineHandler);
			mEngineHandler = 0;
		}
	}
	
	public int SetCameraImageInfo(int leftW, int leftH, int rightW, int rightH) {
		int res = -1;
		if (IsInited()) {
			res = VideoRefocus_SetCameraImageInfo(mEngineHandler, leftW, leftH, rightW, rightH);
		}
		return res;
	}
	
	public int SetImageDegree(int degree)
	{
		int res = -1;
		if (IsInited()) {
			res = VideoRefocus_SetImageDegree(mEngineHandler, degree);
		}
		return res;
	}
	
	public synchronized int SetCalibrationData(byte[] caliData, int length) {
		int res = -1;
		if (IsInited()) {
			res = VideoRefocus_SetCalibrationData(mEngineHandler, caliData, length);
		}
		return res;
	}
	
	public synchronized int SetParam(int x, int y, int blurIntensity, int isOn) {
		int res = -1;
		if (IsInited()) {
			res = VideoRefocus_SetParam(mEngineHandler, x, y, blurIntensity, isOn);
		}
		return res;
	}
	
	public synchronized byte[] Process(byte[] leftData, int leftlength, byte[] rightData, int rightlength, int mainWidth, int mainHeight, int auxWidth, int auxHeight) {
		if (IsInited()) {
			return VideoRefocus_Process(mEngineHandler, leftData, leftlength, rightData, rightlength, mainWidth, mainHeight, auxWidth, auxHeight);
		}
		return new byte[0];
	}

	/***********************************************************************/
	/********************** HDR Native JNI Define **********************/
	/***********************************************************************/
	private native int VideoRefocus_Init();

	private native void VideoRefocus_Uninit(int hEngine);
	
	private native int VideoRefocus_SetCameraImageInfo(int hEngine, int leftW, int leftH, int rightW, int rightH);
	
	private native int VideoRefocus_SetImageDegree(int hEngine, int degree);
	
	private native int VideoRefocus_SetCalibrationData(int hEngine, byte[] caliData, int length);
	
	private native int VideoRefocus_SetParam(int hEngine, int pointX, int pointY, int blurIntensity, int bOn);
	
	private native byte[] VideoRefocus_Process(int hEngine, byte[] leftData, int leftlength, byte[] rightData, int rightlength, int mainWidth, int mainHeight, int auxWidth, int auxHeight);
}