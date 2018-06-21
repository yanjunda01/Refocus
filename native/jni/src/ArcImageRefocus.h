
#ifndef _ARC_IMAGE_REFOCUS_H_
#define _ARC_IMAGE_REFOCUS_H_

#include "asvloffscreen.h"	//ASVLOFFSCREEN

class CArcImageRefocus
{
public:
	CArcImageRefocus();
	~CArcImageRefocus();

public:
	int	Init(int focusMode);
	void UnInit();
	int	SetDCIRParam(int imgDegree, int fMaxFOV);
	int SetParam(int ptX, int ptY, int blurIntensity);
	int SetCalibrationData(void* caliData, int dataLength);
	int SetCameraImageInfo(int main_width, int main_height, int aux_width, int aux_height);
	int GetImageResult(ASVLOFFSCREEN* imgLeft, ASVLOFFSCREEN* imgRight, ASVLOFFSCREEN* imgResult/*out*/);
	int GetImageResultWithDepth(ASVLOFFSCREEN* imgLeft/*in*/, void* pDisparityMap/*in*/, int lDMSize/*in*/, ASVLOFFSCREEN* imgResult/*out*/);

private:
	int DumpImg(ASVLOFFSCREEN img, const char* fileName);
	int DumpBin(void* buffer, int length, const char* fileName);
	
private:
	void* m_hImageRefocus;
	int m_imgDegree;	//0, 90, 180, 270
	float m_fMaxFOV;	//78.4
	int m_focusX;
	int m_focusY;
	int m_blurlevel;	//0~100
};

#endif