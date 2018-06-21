#include <android/log.h>

#include "ArcVideoRefocus.h"
#include "arcsoft_dualcam_video_refocus.h"

#define LOG_TAG "CArcVideoRefocus"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__) 

#define DUMP

#ifdef DUMP
#include "stdio.h"	// file, NULL
#endif

//#define AGET_ALIGN_SIZE(size) (((size) + 63) & ~(63))

CArcVideoRefocus::CArcVideoRefocus()
{
	LOGD("CArcVideoRefocus in");
	m_hVideoRefocus = 0;
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
	int res = 0;
	LOGD("Init out = %d", res);
	return res;
}

void CArcVideoRefocus::UnInit()
{
	LOGD("UnInit in");
	if (m_hVideoRefocus)
	{
		m_hVideoRefocus = MNull;
	}
	LOGD("UnInit out");
}

int CArcVideoRefocus::SetCameraImageInfo(int leftW, int leftH, int rightW, int rightH)
{
	//crop size，就是从sensor出来后做的crop
	LOGD("SetCameraImageInfo in, %dx%d, %dx%d", leftW, leftH, rightW, rightH);
	int res = MOK;
	LOGD("SetCameraImageInfo out =%d", res);
	return res;
}

int CArcVideoRefocus::SetCalibrationData(void* caliData, int dataLength)
{
	int res = -1;
	LOGD("SetCalibrationData in");
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
	LOGD("SetImageDegree out = %d", res);
	return res;
}

int CArcVideoRefocus::Process(ASVLOFFSCREEN* imgLeft, ASVLOFFSCREEN* imgRight, ASVLOFFSCREEN* imgResult)
{	
	int res = MOK;
	LOGD("Process in");
	LOGD("Process out = %d", res);
	return res;
}

int CArcVideoRefocus::DumpImg(ASVLOFFSCREEN img, const char* fileName)
{
	int size = 0;
#ifdef DUMP
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
	LOGD("DumpImg out, write %d bytes", size);
#endif
	return size;
}

