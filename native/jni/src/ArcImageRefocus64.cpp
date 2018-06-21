#include <android/log.h>
#include "ArcImageRefocus.h"
#include "ammem.h"	//MPBASE_Version in arcsoft_dualcam_image_refocus.h
#include "arcsoft_dualcam_image_refocus.h"

#include <stdlib.h>	//malloc

#define LOG_TAG "CArcImageRefocus"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__) 

#define DUMP

#ifdef DUMP
#include "stdio.h"	// file, NULL
#endif

CArcImageRefocus::CArcImageRefocus()
{
	LOGD("CArcImageRefocus in");
	m_hImageRefocus = 0;
	LOGD("CArcImageRefocus out");
}

CArcImageRefocus::~CArcImageRefocus()
{
	LOGD("~CArcImageRefocus in");
	LOGD("~CArcImageRefocus out");
}

int CArcImageRefocus::Init(int focusMode)
{
	//#define ARC_DCIR_NORMAL_MODE				0x01
	//#define ARC_DCIR_POST_REFOCUS_MODE		0x02
	LOGD("Init in, focusMode=%d", focusMode);
	int res = 0;
	LOGD("Init out = %d", res);
	return res;
}

void CArcImageRefocus::UnInit()
{
	LOGD("UnInit in");
	if (m_hImageRefocus)
	{
		m_hImageRefocus = 0;
	}
	LOGD("UnInit out");
}

int CArcImageRefocus::SetDCIRParam(int imgDegree, int fMaxFOV)
{
	LOGD("SetDCIRParam in");
	int res = MOK;
	m_imgDegree = imgDegree;
	m_fMaxFOV = fMaxFOV;	
	LOGD("SetDCIRParam out, fov = %d, Degree = %d", fMaxFOV, imgDegree);
	return res;
}

int CArcImageRefocus::SetCalibrationData(void* caliData, int dataLength)
{
	LOGD("SetCalibrationData in, dataLength=%d", dataLength);
	int res = MOK;
	if(caliData == MNull || dataLength == 0)
	{	
		LOGD("SetCalibrationData out, MERR_INVALID_PARAM");
		return MERR_INVALID_PARAM;
	}
	LOGD("SetCalibrationData out = %d", res);
	return res;
}

int CArcImageRefocus::SetCameraImageInfo(int main_width, int main_height, int aux_width, int aux_height)
{
	LOGD("SetCameraImageInfo in");
	int res = MOK;
	LOGD("SetCameraImageInfo out, res = %d", res);
	return res;
}

int CArcImageRefocus::SetParam(int ptX, int ptY, int blurIntensity)
{
	int res = MOK;
	LOGD("SetParam in, ptX=%d, ptY=%d, blurIntensity=%d", ptX, ptY, blurIntensity);
	m_focusX = ptX;
	m_focusY = ptY;
	m_blurlevel = blurIntensity;
	LOGD("SetParam out");
	return res;
}

int CArcImageRefocus::GetImageResult(ASVLOFFSCREEN* imgLeft, ASVLOFFSCREEN* imgRight, ASVLOFFSCREEN* imgResult/*out*/)
{
	LOGD("GetImageResult in");
	int res = 0;
	LOGD("GetImageResult out = %d", res);
	return res;
}

int CArcImageRefocus::GetImageResultWithDepth(ASVLOFFSCREEN* imgLeft/*in*/, void* pDisparityMap/*in*/, int lDMSize/*in*/, ASVLOFFSCREEN* imgResult/*out*/)
{
	LOGD("GetImageResultWithDepth in");
	int res = MOK;
	LOGD("GetImageResultWithDepth out = %d", res);
	return res;
}

int CArcImageRefocus::DumpBin(void* buffer, int length, const char* fileName)
{
	LOGD("DumpBin in");
	int size = 0;
#ifdef DUMP
    FILE* fp = NULL;
    fp = fopen(fileName, "w");
    if (fp == NULL)
    {
		LOGD("DumpBin out, create file error");
        return 0;
    }
	size = fwrite(buffer, 1, length, fp);
	fclose(fp);
#endif
	LOGD("DumpBin out, write %d bytes", size);
	return size;
}

int CArcImageRefocus::DumpImg(ASVLOFFSCREEN img, const char* fileName)
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
