#include "ArcImageRefocus.h"
#include "ammem.h"	//MPBASE_Version in arcsoft_dualcam_image_refocus.h
#include "arcsoft_dualcam_image_refocus.h"

#include <stdlib.h>	//malloc, free

#ifdef LOG
#include <android/log.h>
#define LOG_TAG "CArcImageRefocus"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define LOGD(...)
#endif

#ifdef DUMPIMAGE
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
	int res = ARC_DCIR_Init(&m_hImageRefocus, focusMode);
	LOGD("Init out = %d", res);
	return res;
}

void CArcImageRefocus::UnInit()
{
	LOGD("UnInit in");
	if (m_hImageRefocus)
	{
        ARC_DCIR_Uninit(&m_hImageRefocus);
		m_hImageRefocus = MNull;
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
	ARC_DC_CALDATA calibrationData = {caliData,dataLength};
	res = ARC_DCIR_SetCaliData(m_hImageRefocus, &calibrationData);
	LOGD("SetCalibrationData out = %d", res);
	return res;
}

int CArcImageRefocus::SetCameraImageInfo(int main_width, int main_height, int aux_width, int aux_height)
{
	LOGD("SetCameraImageInfo in");
	int res = MOK;
	ARC_REFOCUSCAMERAIMAGE_PARAM caminfo = { main_width, main_height, aux_width, aux_height };
	res = ARC_DCIR_SetCameraImageInfo(m_hImageRefocus, &caminfo);
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
	int res = MOK;
	
	//计算深度图
	ARC_DCIR_PARAM dcirParam;
	ARC_DCIR_GetDefaultParam (&dcirParam);
	dcirParam.fMaxFOV = m_fMaxFOV;
	dcirParam.i32ImgDegree = m_imgDegree;
	res = ARC_DCIR_CalcDisparityData(m_hImageRefocus, imgLeft, imgRight, &dcirParam);
	if(MOK != res) 
	{
		LOGD("GetImageResult out, ARC_DCIR_CalcDisparityData error=%d", res);
        return res;
	}
	
	int lDMSize = 0;
	// get the size of disparity map
    res = ARC_DCIR_GetDisparityDataSize(m_hImageRefocus, &lDMSize);
	if(MOK != res) 
	{
		LOGD("GetImageResult out, ARC_DCIR_GetDisparityDataSize error=%d", res);
        return res;
	}

    // allocate the memory for disparity map
	void* pDisparityMap = malloc(lDMSize);
    if(!pDisparityMap)
    {
		LOGD("GetImageResult out, MERR_NO_MEMORY %d", lDMSize);
        res = MERR_NO_MEMORY;
        return res;
    }

    // get the data of disparity map
    res = ARC_DCIR_GetDisparityData(m_hImageRefocus, pDisparityMap);
	if(MOK != res) 
	{
		free(pDisparityMap);
		pDisparityMap = 0;
		LOGD("GetImageResult out, ARC_DCIR_GetDisparityData error=%d", res);
        return res;
	}
#ifdef DUMPIMAGE
	//dump depth file.
	char str[64];
	static int ii = 0;
	sprintf(str, "/sdcard/dcim/bokeh_depth_%d.bin", ii++);
	DumpBin(pDisparityMap, lDMSize, str);
	sprintf(str, "/sdcard/dcim/bokehcapture_main_in_%dx%d_%d.nv21", imgLeft->i32Width, imgLeft->i32Height, ii++);
	DumpImg(*imgLeft, str);
	sprintf(str, "/sdcard/dcim/bokehcapture_aux_in_%dx%d_%d.nv21", imgRight->i32Width, imgRight->i32Height, ii++);
	DumpImg(*imgRight, str);
#endif
	
	ARC_DCIR_REFOCUS_PARAM rfParam;
	rfParam.ptFocus.x = m_focusX;
	rfParam.ptFocus.y = m_focusY;
    rfParam.i32BlurIntensity = m_blurlevel;
	res = ARC_DCIR_Process(m_hImageRefocus, pDisparityMap, lDMSize, imgLeft, &rfParam, imgResult);
#ifdef DUMPIMAGE
	if(res == 0)
	{
		sprintf(str, "/sdcard/dcim/bokehcapture_out_%dx%d_x%d_y%d_b%d_%d.nv21", imgResult->i32Width, imgResult->i32Height, m_focusX, m_focusY, m_blurlevel, ii++);
		DumpImg(*imgResult, str);
	}
#endif
	free(pDisparityMap);
	pDisparityMap = 0;
	LOGD("GetImageResult out = %d", res);
	return res;
}

int CArcImageRefocus::GetImageResultWithDepth(ASVLOFFSCREEN* imgLeft/*in*/, void* pDisparityMap/*in*/, int lDMSize/*in*/, ASVLOFFSCREEN* imgResult/*out*/)
{
	LOGD("GetImageResultWithDepth in");
	int res = MOK;

	ARC_DCIR_REFOCUS_PARAM rfParam;
	rfParam.ptFocus.x = m_focusX;
	rfParam.ptFocus.y = m_focusY;
    rfParam.i32BlurIntensity = m_blurlevel;
	res = ARC_DCIR_Process(m_hImageRefocus, pDisparityMap, lDMSize, imgLeft, &rfParam, imgResult);
	
#ifdef DUMPIMAGE
	if(res == 0)
	{
		char str[64];
		static int ii = 0;
		sprintf(str, "/sdcard/dcim/bokehpost_out_%dx%d_%d.nv21", imgResult->i32Width, imgResult->i32Height, ii++);
		DumpImg(*imgResult, str);
	}
#endif

	LOGD("GetImageResultWithDepth out = %d", res);
	return res;
}

int CArcImageRefocus::DumpBin(void* buffer, int length, const char* fileName)
{
	LOGD("DumpBin in");
	int size = 0;
#ifdef DUMPIMAGE
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
#ifdef DUMPIMAGE
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
	//capture
	CArcImageRefocus* mImageRefocus = new CArcImageRefocus();
	mImageRefocus->Init(1);
	mImageRefocus->SetCalibrationData(bCalibrationData, bCalibrationData.length);
	mImageRefocus->SetDCIRParam(0, 70);	//image degree, max fov
	mImageRefocus->SetParam(nFocusX, nFocusY, 50);	//blur [0,100]
	mImageRefocus->SetCameraImageInfo(nMainCropWidth, nMainCropHeight, nAuxCropWidth, nAuxCropHeight);
	mImageRefocus->GetImageResult(&imgLeft, &imgRight, &imgResult);
	mImageRefocus->UnInit();
	mImageRefocus = 0;
#endif

#if 0
	//gallery
	mImageRefocus = new CArcImageRefocus();
	mImageRefocus->Init(2);
	mImageRefocus->SetParam(nFocusX, nFocusY, 50);
	mImageRefocus->GetImageResultWithDepth(&imgLeft, disprityMapData, disprityMapLength, &imgResult);
	mImageRefocus->UnInit();
	mImageRefocus = 0;
#endif