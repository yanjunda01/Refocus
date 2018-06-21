/* DO NOT EDIT THIS FILE - it is machine generated */

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>	//malloc, free, memset

#include "ArcImageRefocus.h"
#include "ArcVideoRefocus.h"

#define LOG_TAG "JNI"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__) 


void* resultData = 0;	//preview和capture结果图，需要传到上层

int BuildOffScreen(LPASVLOFFSCREEN offscreen, int width, int height, void* plane0, int pitch0, void* plane1, int pitch1, void* plane2, int pitch2, int format)
{
	//format: ASVL_PAF_NV21, ASVL_PAF_YUYV, ASVL_PAF_YV12
	LOGD("BuildOffScreen in");
	int res = 0;
	memset(offscreen, 0, sizeof(ASVLOFFSCREEN));
	offscreen->i32Width = width;
	offscreen->i32Height = height;
	offscreen->u32PixelArrayFormat = format;
	offscreen->ppu8Plane[0] = (MUInt8*)plane0;
	offscreen->pi32Pitch[0] = pitch0;
	offscreen->ppu8Plane[1] = (MUInt8*)plane1;
	offscreen->pi32Pitch[1] = pitch1;
	offscreen->ppu8Plane[2] = (MUInt8*)plane2;
	offscreen->pi32Pitch[2] = pitch2;
	LOGD("BuildOffScreen out");
	return res;
}

/*----------------------------Image Refocus-------------------------------------*/
JNIEXPORT jint JNICALL ImageRefocus_Init(JNIEnv *env, jobject jobj, jint intMode)
{
	//intMode
	//#define ARC_DCIR_NORMAL_MODE				0x01
	//#define ARC_DCIR_POST_REFOCUS_MODE		0x02
	int ret = 0;
	CArcImageRefocus* imagerefocus = new CArcImageRefocus();
	ret = imagerefocus->Init(intMode);
	return (jint)imagerefocus;
}

JNIEXPORT void JNICALL ImageRefocus_Uninit(JNIEnv *env, jobject jobj, jint intRefocus)
{
	if (intRefocus)
	{
		CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;
		if (imagerefocus != MNull)
		{
			imagerefocus->UnInit();
			delete imagerefocus;
			imagerefocus = MNull;
		}
		if(resultData != 0)
		{
			free(resultData);
			resultData = 0;
		}
	}
}

JNIEXPORT jint JNICALL ImageRefocus_SetDCIRParam(JNIEnv *env, jobject jobj, jint intRefocus, jint imgDegree, jint fMaxFOV)
{
	int ret = -1;
	if (intRefocus)
	{		
		CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;

		if (imagerefocus != MNull)
		{
			ret = imagerefocus->SetDCIRParam(imgDegree, fMaxFOV);
		}
	}
	return ret;
}

JNIEXPORT jint JNICALL ImageRefocus_SetCameraImageInfo(JNIEnv *env, jobject jobj, jint intRefocus, jint main_width, jint main_height, jint aux_width, jint aux_height)
{
	int ret = -1;	

	if (intRefocus)
	{		
		CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;

		if (imagerefocus != MNull)
		{
			ret = imagerefocus->SetCameraImageInfo(main_width, main_height, aux_width, aux_height);		
		}
	}
	return ret;
}

JNIEXPORT jint JNICALL ImageRefocus_SetParam(JNIEnv *env, jobject jobj, jint intRefocus, int ptX, int ptY, int blurIntensity)
{
	int ret = -1;	

	if (intRefocus)
	{		
		CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;

		if (imagerefocus != MNull)
		{
			ret = imagerefocus->SetParam(ptX, ptY, blurIntensity);		
		}
	}
	return ret;
}

JNIEXPORT jint JNICALL ImageRefocus_SetCalibrationData(JNIEnv *env, jobject jobj, jint intRefocus, jbyteArray jCaliData, jint dataLength)
{
	int ret = -1;	

	if (intRefocus)
	{		
		CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;

		if (imagerefocus != MNull)
		{
			jbyte* data = env->GetByteArrayElements(jCaliData, 0);
			ret = imagerefocus->SetCalibrationData((MByte*)data, dataLength);
			env->ReleaseByteArrayElements(jCaliData, (jbyte *)data, 0);		
		}
	}

	return ret;
}

JNIEXPORT jbyteArray JNICALL ImageRefocus_GetImageResult(JNIEnv *env, jobject jobj, jint intRefocus, jbyteArray jleftData, jint leftlength, jbyteArray jrightData, jint rightlength, int width, int height, int auxWidth, int auxHeight)
{
	int res = -1;
	jbyteArray dataArray = 0;
	CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;
	jbyte* leftdata = 0;
	jbyte* rightdata = 0;
	int dataLength = width*height*3/2;
	leftdata = env->GetByteArrayElements(jleftData, 0);
	rightdata = env->GetByteArrayElements(jrightData, 0);
	
	if(resultData == 0)
	{
		resultData = malloc(dataLength);
	}
	
	ASVLOFFSCREEN imgLeft;
	ASVLOFFSCREEN imgRight;
	ASVLOFFSCREEN imgResult;
	BuildOffScreen(&imgLeft, width, height, leftdata, width, leftdata+width*height, width, 0, 0, ASVL_PAF_NV21);
	BuildOffScreen(&imgRight, auxWidth, auxHeight, rightdata, auxWidth, rightdata+auxWidth*auxHeight, auxWidth, 0, 0, ASVL_PAF_NV21);
	BuildOffScreen(&imgResult, width, height, resultData, width, (char*)resultData+width*height, width, 0, 0, ASVL_PAF_NV21);
	
	res = imagerefocus->GetImageResult(&imgLeft, &imgRight, &imgResult);
	
	if(res == 0)
	{
		dataArray = env->NewByteArray(dataLength);
		if (dataArray != MNull)
		{
			env->SetByteArrayRegion(dataArray, 0, dataLength, (jbyte*)resultData);
		}
	}
	env->ReleaseByteArrayElements(jleftData, leftdata, 0);
	env->ReleaseByteArrayElements(jrightData, rightdata, 0);
	//dataArray需要释放吗？
	//resultData在ImageRefocus_Uninit中释放。
	return dataArray;
}

JNIEXPORT jbyteArray JNICALL ImageRefocus_GetImageResultWithDepth(JNIEnv *env, jobject jobj, jint intRefocus, jbyteArray jleftData, jint leftlength, int width, int height, jbyteArray jDisprityMap, jint disprityMapLength)
{
	int res = -1;
	jbyteArray dataArray = 0;
	CArcImageRefocus* imagerefocus = (CArcImageRefocus*)intRefocus;
	int dataLength = width*height*3/2;
	jbyte* leftdata = env->GetByteArrayElements(jleftData, 0);
	
	if(resultData == 0)
	{
		resultData = malloc(dataLength);
	}
	
	ASVLOFFSCREEN imgLeft;
	ASVLOFFSCREEN imgResult;
	BuildOffScreen(&imgLeft, width, height, leftdata, width, leftdata+width*height, width, 0, 0, ASVL_PAF_NV21);
	BuildOffScreen(&imgResult, width, height, resultData, width, (char*)resultData+width*height, width, 0, 0, ASVL_PAF_NV21);
	
	jbyte* disprityMapData = env->GetByteArrayElements(jDisprityMap, 0);

	res = imagerefocus->GetImageResultWithDepth(&imgLeft, disprityMapData, disprityMapLength, &imgResult);
	
	if(res == 0)
	{
		dataArray = env->NewByteArray(dataLength);
		if (dataArray != MNull)
		{
			env->SetByteArrayRegion(dataArray, 0, dataLength, (jbyte*)resultData);
		}
	}
	env->ReleaseByteArrayElements(jleftData, leftdata, 0);
	env->ReleaseByteArrayElements(jDisprityMap, disprityMapData, 0);
	//dataArray需要释放吗？
	//resultData在ImageRefocus_Uninit中释放。
	return dataArray;
}

/*----------------------------Video Refocus-------------------------------------*/

JNIEXPORT jint JNICALL VideoRefocus_Init(JNIEnv *env, jobject jobj)
{
	int ret = 0;

	CArcVideoRefocus* videorefocus = new CArcVideoRefocus();

	ret = videorefocus->Init();

	return (jint)videorefocus;
}

JNIEXPORT void JNICALL VideoRefocus_Uninit(JNIEnv *env, jobject jobj, jint intRefocus)
{
	if (intRefocus)
	{		
		CArcVideoRefocus* videorefocus = (CArcVideoRefocus*)intRefocus;

		if (videorefocus != MNull)
		{
			videorefocus->UnInit();
			delete videorefocus;
			videorefocus = MNull;
		}
		if(resultData != 0)
		{
			free(resultData);
			resultData = 0;
		}
	}
}

JNIEXPORT jint JNICALL VideoRefocus_SetCameraImageInfo(JNIEnv *env, jobject jobj, jint intRefocus, jint leftW, jint leftH, jint rightW, jint rightH)
{
	int ret = -1;	

	if (intRefocus)
	{		
		CArcVideoRefocus* videorefocus = (CArcVideoRefocus*)intRefocus;

		if (videorefocus != MNull)
		{
			ret = videorefocus->SetCameraImageInfo(leftW, leftH, rightW, rightH);
		}
	}

	return ret;
}

JNIEXPORT jint JNICALL VideoRefocus_SetImageDegree(JNIEnv *env, jobject jobj, jint intRefocus, jint degree)
{
	// 0为nv21，1为yuyv，目前yuyv不支持。
	int ret = -1;	

	if (intRefocus)
	{		
		CArcVideoRefocus* videorefocus = (CArcVideoRefocus*)intRefocus;

		if (videorefocus != MNull)
		{
			ret = videorefocus->SetImageDegree(degree);		
		}
	}

	return ret;
}

JNIEXPORT jint JNICALL VideoRefocus_SetCalibrationData(JNIEnv *env, jobject jobj, jint intRefocus, jbyteArray jCaliData, jint dataLength)
{
	LOGD("VideoRefocus_SetCalibrationData in");

	int ret = -1;	

	if (intRefocus)
	{		
		CArcVideoRefocus* videorefocus = (CArcVideoRefocus*)intRefocus;
		if (videorefocus != MNull)
		{
			jbyte* data = env->GetByteArrayElements(jCaliData, 0);
			ret = videorefocus->SetCalibrationData((MByte*)data,  dataLength);
			env->ReleaseByteArrayElements(jCaliData, (jbyte *)data, 0);		
		}
	}

	LOGD("VideoRefocus_SetCalibrationData out");
	return ret;
}

JNIEXPORT jint JNICALL VideoRefocus_SetParam(JNIEnv *env, jobject jobj, jint intRefocus, jint ptX, jint ptY, jint blurIntensity, jint bRefocusOn)
{
	int ret = -1;	

	if (intRefocus)
	{		
		CArcVideoRefocus* videorefocus = (CArcVideoRefocus*)intRefocus;

		if (videorefocus != MNull)
		{
			ret = videorefocus->SetParam(ptX, ptY, blurIntensity, bRefocusOn);
		}
	}

	return ret;
}

JNIEXPORT jbyteArray JNICALL VideoRefocus_Process(JNIEnv *env, jobject jobj, jint intRefocus, jbyteArray jleftData, jint leftlength, jbyteArray jrightData, jint rightlength, int width, int height, int auxWidth, int auxHeight)
{
	LOGD("VideoRefocus_Process in, leftlength=%d, rightlength=%d, width=%d, height=%d, auxWidth=%d, auxHeight=%d", leftlength, rightlength, width, height, auxWidth, auxHeight);
	//support nv21
	int res = 0;
	MLong dataLength = width*height*3/2;
	jbyteArray dataArray = MNull;
	CArcVideoRefocus* videorefocus  = MNull;
	jbyte* leftdata = MNull;
	jbyte* rightdata = MNull;

	videorefocus = (CArcVideoRefocus*)intRefocus;
	if (videorefocus == MNull)
	{
		return 0;
	}
	
	leftdata = env->GetByteArrayElements(jleftData, 0);
	rightdata = env->GetByteArrayElements(jrightData, 0);
	
	if(resultData == 0)
	{
		resultData = malloc(dataLength);
	}
	
	ASVLOFFSCREEN imgLeft;
	ASVLOFFSCREEN imgRight;
	ASVLOFFSCREEN imgResult;
	BuildOffScreen(&imgLeft, width, height, leftdata, width, leftdata+width*height, width, 0, 0, ASVL_PAF_NV21);
	BuildOffScreen(&imgRight, auxWidth, auxHeight, rightdata, auxWidth, rightdata+auxWidth*auxHeight, auxWidth, 0, 0, ASVL_PAF_NV21);
	BuildOffScreen(&imgResult, width, height, resultData, width, (char*)resultData+width*height, width, 0, 0, ASVL_PAF_NV21);
	
	res = videorefocus->Process(&imgLeft, &imgRight, &imgResult);
	
	if(res == 0)
	{
		dataArray = env->NewByteArray(dataLength);
		if (dataArray != MNull)
		{
			env->SetByteArrayRegion(dataArray, 0, dataLength, (jbyte*)resultData);
		}
	}
	env->ReleaseByteArrayElements(jleftData, leftdata, 0);
	env->ReleaseByteArrayElements(jrightData, rightdata, 0);
	//dataArray需要释放吗？
	LOGD("VideoRefocus_Process out, res=%d", res);

	return dataArray;
}

static JNINativeMethod gImageMethods[] = 
{
	//image
	{ "ImageRefocus_Init",				 		"(I)I",										(void*) ImageRefocus_Init },
	{ "ImageRefocus_Uninit",			 		"(I)V",										(void*) ImageRefocus_Uninit },
	{ "ImageRefocus_SetDCIRParam",       		"(III)I",							    	(void*) ImageRefocus_SetDCIRParam },
	{ "ImageRefocus_SetParam",       			"(IIII)I",							    	(void*) ImageRefocus_SetParam },
	{ "ImageRefocus_SetCameraImageInfo", 		"(IIIII)I",									(void*) ImageRefocus_SetCameraImageInfo },
	{ "ImageRefocus_SetCalibrationData", 		"(I[BI)I",									(void*) ImageRefocus_SetCalibrationData },
    { "ImageRefocus_GetImageResult",     		"(I[BI[BIIIII)[B",							(void*) ImageRefocus_GetImageResult },
    { "ImageRefocus_GetImageResultWithDepth",   "(I[BIII[BI)[B",							(void*) ImageRefocus_GetImageResultWithDepth }
};

static JNINativeMethod gVideoMethods[] = 
{
	//video
	{ "VideoRefocus_Init",				 "()I",								    		(void*) VideoRefocus_Init },
	{ "VideoRefocus_Uninit",			 "(I)V",										(void*) VideoRefocus_Uninit },
	{ "VideoRefocus_SetCameraImageInfo", "(IIIII)I",									(void*) VideoRefocus_SetCameraImageInfo },
	{ "VideoRefocus_SetImageDegree",     "(II)I",						    			(void*) VideoRefocus_SetImageDegree },
	{ "VideoRefocus_SetCalibrationData", "(I[BI)I",										(void*) VideoRefocus_SetCalibrationData },
	{ "VideoRefocus_SetParam",           "(IIIII)I",									(void*) VideoRefocus_SetParam },
    { "VideoRefocus_Process",            "(I[BI[BIIIII)[B",								(void*) VideoRefocus_Process }
};

int register_arcsoft_android_imagerefocus(JNIEnv* env)
{
	jclass clazz = env->FindClass("com/arcsoft/refocus/refocus/ImageRefocus");

	if (clazz == MNull) 
	{
		return -1;
	}
	if (env->RegisterNatives(clazz, gImageMethods, sizeof(gImageMethods) / sizeof(JNINativeMethod)) < 0) 
	{
		return -1;
	}
	return 0;
}

int register_arcsoft_android_videorefocus(JNIEnv* env)
{
	jclass clazz = env->FindClass("com/arcsoft/refocus/refocus/VideoRefocus");

	if (clazz == MNull) 
	{
		return -1;
	}
	if (env->RegisterNatives(clazz, gVideoMethods, sizeof(gVideoMethods) / sizeof(JNINativeMethod)) < 0) 
	{
		return -1;
	}
	return 0;
}

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = 0;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return result;
	}

	register_arcsoft_android_imagerefocus(env);
	register_arcsoft_android_videorefocus(env);

	return JNI_VERSION_1_4;
}