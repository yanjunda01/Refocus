#include "ArcVideoRefocus.h"
#include "arcsoft_dualcam_video_refocus.h"

#ifdef LOG
#include <android/log.h>
#define LOG_TAG "CArcVideoRefocus"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...)
#endif

#ifdef DUMPVIDEO
#include "stdio.h"	// file, NULL
#endif

//#define AGET_ALIGN_SIZE(size) (((size) + 63) & ~(63))

CArcVideoRefocus::CArcVideoRefocus()
{
	LOGD("CArcVideoRefocus in");
	m_hVideoRefocus = MNull;
	LOGD("CArcVideoRefocus out");
}

CArcVideoRefocus::~CArcVideoRefocus()
{
	LOGD("~CArcVideoRefocus in");
	LOGD("~CArcVideoRefocus out");
}

int CArcVideoRefocus::Init()
{
	LOGD("Init in");
	int res = ARC_DCVR_Init(&m_hVideoRefocus);
	LOGD("Init out = %d", res);
	return res;
}

void CArcVideoRefocus::UnInit()
{
	LOGD("UnInit in");
	if (m_hVideoRefocus)
	{
        ARC_DCVR_Uninit(&m_hVideoRefocus);
		m_hVideoRefocus = MNull;
	}
	LOGD("UnInit out");
}

int CArcVideoRefocus::SetCameraImageInfo(int leftW, int leftH, int rightW, int rightH)
{
	//crop size，就是从sensor出来后做的crop
	LOGD("SetCameraImageInfo in, %dx%d, %dx%d", leftW, leftH, rightW, rightH);
	int res = MOK;
	ARC_REFOCUSCAMERAIMAGE_PARAM caminfo = { leftW, leftH, rightW, rightH };
	res = ARC_DCVR_SetCameraImageInfo(m_hVideoRefocus, &caminfo);
	LOGD("SetCameraImageInfo out =%d", res);
	return res;
}

int CArcVideoRefocus::SetCalibrationData(void* caliData, int dataLength)
{
	int res = -1;
	LOGD("SetCalibrationData in");
	if(caliData == MNull || dataLength == 0)
	{	
	}
	else
	{
		ARC_DC_CALDATA calibrationData = {caliData,dataLength};
		res = ARC_DCVR_SetCaliData(m_hVideoRefocus, &calibrationData);
	}
	LOGD("SetCalibrationData out = %d", res);
	return res;
}

int CArcVideoRefocus::SetParam(int ptX, int ptY, int blurIntensity, int bRefocusOn)
{
	int res = MOK;
	LOGD("SetParam in, ptX=%d, ptY=%d, blurIntensity=%d, bRefocusOn=%d", ptX, ptY, blurIntensity, bRefocusOn);
	m_focusX = ptX;
	m_focusY = ptY;
	m_blurlevel = blurIntensity;
	m_bRefocusOn = bRefocusOn;
	LOGD("SetParam out");
	return res;
}

int CArcVideoRefocus::SetImageDegree(int degree)
{
	int res = MOK;
	LOGD("SetImageDegree in %d", degree);
	res = ARC_DCVR_SetImageDegree(m_hVideoRefocus, degree);
	LOGD("SetImageDegree out = %d", res);
	return res;
}

int CArcVideoRefocus::Process(ASVLOFFSCREEN* imgLeft, ASVLOFFSCREEN* imgRight, ASVLOFFSCREEN* imgResult)
{	
	int res = MOK;
	LOGD("Process in");
	
	ARC_DCVR_PARAM param;
	ARC_DCVR_GetDefaultParam(&param);
	param.ptFocus.x = m_focusX;
	param.ptFocus.y = m_focusY;
	param.i32BlurLevel = m_blurlevel;	//[1, 100]
	param.bRefocusOn = m_bRefocusOn;
#ifdef DUMPVIDEO
	char str[64];
	static int ii = 0;
	sprintf(str, "/sdcard/dcim/bokehpreview_main_in_%dx%d_%d.nv21", imgLeft->i32Width, imgLeft->i32Height, ii++);
	DumpImg(*imgLeft, str);
	sprintf(str, "/sdcard/dcim/bokehpreview_aux_in_%dx%d_%d.nv21", imgRight->i32Width, imgRight->i32Height, ii++);
	DumpImg(*imgRight, str);
#endif
	res = ARC_DCVR_Process(m_hVideoRefocus, imgLeft, imgRight, imgResult, &param);
#ifdef DUMPVIDEO
	sprintf(str, "/sdcard/dcim/bokehpreview_out_%dx%d_%d.nv21", imgResult->i32Width, imgResult->i32Height, ii++);
	DumpImg(*imgResult, str);
#endif
	LOGD("Process out = %d", res);
	return res;
}

int CArcVideoRefocus::DumpImg(ASVLOFFSCREEN img, const char* fileName)
{
	int size = 0;
#ifdef DUMPVIDEO
    FILE* fp = NULL;
	LOGD("DumpImg in, %dx%d, format=%d, pitch0=%d, pitch1=%d", img.i32Width, img.i32Height, img.u32PixelArrayFormat, img.pi32Pitch[0], img.pi32Pitch[1]);
    fp = fopen(fileName, "w");
    if (fp == NULL)
    {
		LOGD("DumpImg out, create file error");
        return 0;
    }
	if(img.u32PixelArrayFormat == ASVL_PAF_NV21)
	{
		for (int j = 0; j < img.i32Height; ++j)
		{
			size += fwrite(img.ppu8Plane[0]+j*img.pi32Pitch[0], 1, img.pi32Pitch[0], fp);
		}

		for (int j = 0; j < img.i32Height/2; ++j)
		{
			size += fwrite(img.ppu8Plane[1]+j*img.pi32Pitch[1], 1, img.pi32Pitch[1], fp);
		}
	}
	else if(img.u32PixelArrayFormat == ASVL_PAF_YUYV)
	{
		for (int j = 0; j < img.i32Height; ++j)
		{
			size += fwrite(img.ppu8Plane[0]+j*img.pi32Pitch[0], 1, img.i32Width*2, fp);
		}
	}

    fclose(fp);
	LOGD("DumpImg out, write %d bytes, file=%s", size, fileName);
#endif
	return size;
}

#if 0
	mVideoRefocus = new CArcVideoRefocus();
	mVideoRefocus->Init();
	mVideoRefocus->SetCalibrationData(bCalibrationData, bCalibrationData.length);
	mVideoRefocus->SetImageDegree(0);
	mVideoRefocus->SetCameraImageInfo(nMainCropWidth, nMainCropHeight, nAuxCropWidth, nAuxCropHeight);
	while(1)
	{
		mVideoRefocus->SetParam(nFocusX, nFocusY, 50, 1);
		mVideoRefocus->Process(&imgLeft, &imgRight, &imgResult);
	}
	mVideoRefocus->UnInit();
	mVideoRefocus = NULL;
#endif
