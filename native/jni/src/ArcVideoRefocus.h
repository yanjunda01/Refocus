
#ifndef _ARC_VIDEO_REFOCUS_H_
#define _ARC_VIDEO_REFOCUS_H_

#include "asvloffscreen.h"	//ASVLOFFSCREEN

class CArcVideoRefocus
{

public:
	CArcVideoRefocus();
	~CArcVideoRefocus();

public:
	int	Init();
	void UnInit();
	int	SetCameraImageInfo(int leftW, int leftH, int rightW, int rightH);
	int SetCalibrationData(void* caliData, int dataLength);
	int SetParam(int ptX, int ptY, int blurIntensity, int bRefocusOn);
	int SetImageDegree(int degree);
	int Process(ASVLOFFSCREEN* imgLeft, ASVLOFFSCREEN* imgRight, ASVLOFFSCREEN* imgResult/*out*/);

private:
	int DumpImg(ASVLOFFSCREEN img, const char* fileName);

private:
	void* m_hVideoRefocus;
	int m_focusX;
	int m_focusY;
	int m_blurlevel;
	int m_bRefocusOn;
};

#endif