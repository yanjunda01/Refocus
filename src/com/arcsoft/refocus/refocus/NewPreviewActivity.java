package com.arcsoft.refocus.refocus;

import com.arcsoft.refocus.R;
import com.arcsoft.refocus.UIGlobalDef;
import com.arcsoft.refocus.utils.FileUtil;
import com.arcsoft.refocus.utils.YuvUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewPreviewActivity extends Activity {
	public final static int REQUEST_CODE_OPENDIALOG_CALIBRATION = 5;	// 标定文件
	public final static int REQUEST_CODE_OPENDIALOG_MAIN = 6;	// 主摄
	public final static int REQUEST_CODE_OPENDIALOG_AUX = 7;	// 副摄
	private final static String TAG = "NewPreviewActivity";
	private String strDefaultWorkDir;	//缺省的打开文件对话框目录
	private TextView mCaliFileTV;	//标定文件名显示控件
	private TextView mMainFileTV;	//主摄文件名显示控件
	private TextView mAuxFileTV;	//副摄文件名显示控件
	private TextView mFocusXTV;		//对焦坐标显示控件
	private TextView mFocusYTV;		//对焦坐标显示控件
	byte[] bCalibrationData;	//标定文件数据
	byte[] bMainFileData;	//主摄文件数据
	byte[] bAuxFileData;	//副摄文件数据
	private int nFocusX;	//refocus x坐标
	private int nFocusY;	//refocus y坐标
	private int nMainCropWidth;		//crop size，如果是拍照，一般等于拍照尺寸，可能等于sensor全尺寸，也可能小于sensor全尺寸。
	private int nMainCropHeight;	//如果是预览，一般预览尺寸要在此crop基础上做resize，比如到720P
	private int nAuxCropWidth;
	private int nAuxCropHeight;
	private int nMainWidth;	//拍照尺寸，主摄
	private int nMainHeight;
	private int nAuxWidth;	//副摄尺寸
	private int nAuxHeight;
	private VideoRefocus mVideoRefocus;	//算法引擎
	private Bitmap mBmp;
	private EditText mSize[];	//四个尺寸的控件，用户需要填入的crop size
	private int nMainFileFormat;	//文件格式，yuyv=1或者nv21=0
	private int nAuxFileFormat;	//文件格式，yuyv=1或者nv21=0
	private String strMainFile;	//主摄文件名
	private String strAuxFile;	//副摄文件名

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate in");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_preview);
		
		mCaliFileTV = (TextView) this.findViewById(R.id.TextViewCaliFile);
		mMainFileTV = (TextView) this.findViewById(R.id.TextViewMainFile);
		mAuxFileTV = (TextView) this.findViewById(R.id.TextViewAuxFile);
		mFocusXTV = (TextView) this.findViewById(R.id.editTextRefocusX);
		mFocusYTV = (TextView) this.findViewById(R.id.editTextRefocusY);
		
		if(mSize == null){
			mSize = new EditText[4];
		}
		mSize[0] = (EditText) this.findViewById(R.id.editTextMainWidth);
		mSize[1] = (EditText) this.findViewById(R.id.editTextMainHeight);
		mSize[2] = (EditText) this.findViewById(R.id.editTextAuxWidth);
		mSize[3] = (EditText) this.findViewById(R.id.editTextAuxHeight);
		Log.i(TAG, "onCreate out");
	}
	
	public void onClickSelectCalibration(View srcView){
		Log.i(TAG, "onClickSelectCalibration in");
		Intent intent = new Intent(this, OpenFileDialogActivity.class);  
		intent.putExtra("DefaultFilePath", strDefaultWorkDir == null?Environment.getExternalStorageDirectory().getPath():strDefaultWorkDir);  
		intent.putExtra("DefaultFileName", "default.lag");
		startActivityForResult(intent, NewPreviewActivity.REQUEST_CODE_OPENDIALOG_CALIBRATION);
		Log.i(TAG, "onClickSelectCalibration out");
	}
	
	public void onClickSelectMainFile(View srcView){
		Log.i(TAG, "onClickSelectMainFile in");
		Intent intent = new Intent(this, OpenFileDialogActivity.class);  
		intent.putExtra("DefaultFilePath", strDefaultWorkDir == null?Environment.getExternalStorageDirectory().getPath():strDefaultWorkDir);  
		intent.putExtra("DefaultFileName", "default.lag");
		startActivityForResult(intent, NewPreviewActivity.REQUEST_CODE_OPENDIALOG_MAIN);
		Log.i(TAG, "onClickSelectMainFile out");
	}
	
	public void onClickSelectAuxFile(View srcView){
		Log.i(TAG, "onClickSelectAuxFile in");
		Intent intent = new Intent(this, OpenFileDialogActivity.class);  
		intent.putExtra("DefaultFilePath", strDefaultWorkDir == null?Environment.getExternalStorageDirectory().getPath():strDefaultWorkDir);  
		intent.putExtra("DefaultFileName", "default.lag");
		startActivityForResult(intent, NewPreviewActivity.REQUEST_CODE_OPENDIALOG_AUX);
		Log.i(TAG, "onClickSelectAuxFile out");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult in, requestCode=" + requestCode + " resultCode="+resultCode);
		switch (requestCode) {
		   //标定文件 
		   case NewPreviewActivity.REQUEST_CODE_OPENDIALOG_CALIBRATION:
			   if (resultCode == RESULT_OK){
				   Bundle b = data.getExtras();
				   String[] fileList = b.getStringArray("FilePathName");
				   if(fileList.length == 1){
					   bCalibrationData = FileUtil.LoadFromFile(fileList[0]);
					   mCaliFileTV.setText(getFileName(fileList[0]));
				   }
				   //保存访问路径
				   if(fileList.length>0){
						int lastIndex = fileList[0].lastIndexOf("/");
						strDefaultWorkDir = fileList[0].substring(0, lastIndex);
				   }
		        }  
		    break;
			   //主摄文件
			   case NewPreviewActivity.REQUEST_CODE_OPENDIALOG_MAIN:
				   if (resultCode == RESULT_OK){
					   Bundle b = data.getExtras();
					   String[] fileList = b.getStringArray("FilePathName");
					   if(fileList.length == 1){
						   strMainFile = fileList[0];
						   bMainFileData = FileUtil.LoadFromFile(fileList[0]);
						   mMainFileTV.setText(getFileName(fileList[0]));
							//根据文件名猜测宽和高
							int[] wh = new int[2];
							guessFileWH(fileList[0], wh);
							if(wh[0] == 0 || wh[1] == 0){
								Toast.makeText(NewPreviewActivity.this, "file name error", Toast.LENGTH_SHORT).show();
								Log.i(TAG, "onActivityResult, file name error="+fileList[0]);
							}
							nMainWidth = wh[0];
							nMainHeight = wh[1];

							nMainCropWidth = wh[0];
							nMainCropHeight = wh[1];
							nFocusX = wh[0]/2;
							nFocusY = wh[1]/2;
							//填充到控件
							mSize[0].setText(String.valueOf(nMainCropWidth));
							mSize[1].setText(String.valueOf(nMainCropHeight));
							mFocusXTV.setText(String.valueOf(nFocusX));
							mFocusYTV.setText(String.valueOf(nFocusY));
							
							if(fileList[0].indexOf("nv21")!=-1 || fileList[0].indexOf("NV21")!=-1){
								nMainFileFormat = 0;
							}else if(fileList[0].indexOf("yuyv")!=-1 || fileList[0].indexOf("YUYV")!=-1){
								nMainFileFormat = 1;								
							}else{								
								Toast.makeText(NewPreviewActivity.this, "file surfix error", Toast.LENGTH_SHORT).show();
							}
					   }
					   
					   //保存访问路径
					   if(fileList.length>0){
							int lastIndex = fileList[0].lastIndexOf("/");
							strDefaultWorkDir = fileList[0].substring(0, lastIndex);
					   }
			        }  
			   break;
			   //副摄文件 
			   case NewPreviewActivity.REQUEST_CODE_OPENDIALOG_AUX:
				   if (resultCode == RESULT_OK){
					   Bundle b = data.getExtras();
					   String[] fileList = b.getStringArray("FilePathName");
					   if(fileList.length == 1){
						   strAuxFile = fileList[0];
						   bAuxFileData = FileUtil.LoadFromFile(fileList[0]);
						   mAuxFileTV.setText(getFileName(fileList[0]));
						   
							//根据文件名猜测宽和高
							int[] wh = new int[2];
							guessFileWH(fileList[0], wh);
							if(wh[0] == 0 || wh[1] == 0){
								Toast.makeText(NewPreviewActivity.this, "file name error", Toast.LENGTH_SHORT).show();
								Log.i(TAG, "onActivityResult, file name error="+fileList[0]);
							}
							nAuxWidth = wh[0];
							nAuxHeight = wh[1];
							nAuxCropWidth = wh[0];
							nAuxCropHeight = wh[1];
							//填充到控件
							mSize[2].setText(String.valueOf(nAuxCropWidth));
							mSize[3].setText(String.valueOf(nAuxCropHeight));
							//猜测后缀名
							if(fileList[0].indexOf("nv21")!=-1 || fileList[0].indexOf("NV21")!=-1){
								nAuxFileFormat = 0;
							}else if(fileList[0].indexOf("yuyv")!=-1 || fileList[0].indexOf("YUYV")!=-1){
								nAuxFileFormat = 1;								
							}else{								
								Toast.makeText(NewPreviewActivity.this, "file surfix error", Toast.LENGTH_SHORT).show();
							}
					   }
					   
					   //保存访问路径
					   if(fileList.length>0){
							int lastIndex = fileList[0].lastIndexOf("/");
							strDefaultWorkDir = fileList[0].substring(0, lastIndex);
					   }
			        }  
			    break;
		    default:
		    	break;
		}
		Log.i(TAG, "onActivityResult out, requestCode=" + requestCode + " resultCode="+resultCode);
	}

	public void onClickOK(View srcView){
		//参数判断
		Log.i(TAG, "onClickOK in");
		if(bCalibrationData == null){
			   Toast.makeText(NewPreviewActivity.this, "calibration error", Toast.LENGTH_SHORT).show();
			   Log.i(TAG, "onClickOK out, cali error");
			   return;
		}
		//读取尺寸
    	try{
    		nMainCropWidth = Integer.parseInt(mSize[0].getText().toString());
    	}catch(NumberFormatException e){
    		nMainCropWidth = 0;
    	}
    	try{
    		nMainCropHeight = Integer.parseInt(mSize[1].getText().toString());
    	}catch(NumberFormatException e){
    		nMainCropHeight = 0;
    	}
    	try{
    		nAuxCropWidth = Integer.parseInt(mSize[2].getText().toString());
    	}catch(NumberFormatException e){
    		nAuxCropWidth = 0;
    	}
    	try{
    		nAuxCropHeight = Integer.parseInt(mSize[3].getText().toString());
    	}catch(NumberFormatException e){
    		nAuxCropHeight = 0;
    	}
    	//判断尺寸
		if(nMainCropWidth == 0 ||nMainCropHeight == 0 ||nAuxCropWidth == 0 ||nAuxCropHeight == 0){
			Toast.makeText(NewPreviewActivity.this, "crop size error", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "onClickOK out, size error");
			return;
		}
		if(bMainFileData == null || bAuxFileData == null){	
			Toast.makeText(NewPreviewActivity.this, "input image error", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "onClickOK out, input image error");
			return;
		}
		//focus坐标
		try{
			nFocusX = Integer.parseInt(mFocusXTV.getText().toString());
    	}catch(NumberFormatException e){
    		nFocusX = nMainWidth/2;
    	}
		try{
			nFocusY = Integer.parseInt(mFocusYTV.getText().toString());
    	}catch(NumberFormatException e){
    		nFocusY = nMainHeight/2;
    	}
		
		// begin
		mVideoRefocus = new VideoRefocus();
		mVideoRefocus.Init();
		mVideoRefocus.SetCalibrationData(bCalibrationData, bCalibrationData.length);
		mVideoRefocus.SetCameraImageInfo(nMainCropWidth, nMainCropHeight, nAuxCropWidth, nAuxCropHeight);
		mVideoRefocus.SetImageDegree(0);
		for(int i=0; i<10; i++){
			mVideoRefocus.SetParam(nFocusX, nFocusY, 50, 1);
			byte[] resultData = mVideoRefocus.Process(bMainFileData, bMainFileData.length, bAuxFileData, bAuxFileData.length, nMainWidth, nMainHeight, nAuxWidth, nAuxHeight);
			if(resultData != null) {
				int pointIndex = strMainFile.indexOf('.');
				//main_4000x3000.nv21 -> main_4000x3000.bin
				String fileSufix = strMainFile.substring(pointIndex); //后缀名包含. .nv21
				String out_file = strMainFile.substring(0, pointIndex) + "_bokeh" + i + fileSufix;
				FileUtil.saveImage(out_file, resultData);
			}
		}
		//destroy engine
	    if(mVideoRefocus != null) {
	    	mVideoRefocus.UnInit();
	    	mVideoRefocus = null;
	    }
		Toast.makeText(NewPreviewActivity.this, "Succeed", Toast.LENGTH_SHORT).show();		
		Log.i(TAG, "onClickOK out");
	}
	
	private void guessFileWH(String strFile, int[] wh){
		Log.i(TAG, "guessFileWH in = "+strFile);
		// 4_arcsoft6144x3456_output_317_ISO602.yuyv
		// ax8_IMG_20150101_192924_0_2976x3968.yuyv
		wh[0] = -1;	//width
		wh[1] = -1;	//height
		if(strFile == null){
			return;
		}
		int index_X = strFile.lastIndexOf('x');
		if(index_X == -1){		
			index_X = strFile.lastIndexOf('X');
		}
		if(index_X!=-1){
			int iStart = 0;
			for(iStart=index_X-1; iStart>=0; iStart--){
				if(!isDigital(strFile.charAt(iStart))){
					break;
				}
			}
			if(iStart>=0){
				String sWidth = strFile.substring(iStart+1, index_X);
				try{
					wh[0] = Integer.parseInt(sWidth);
				}
				catch(NumberFormatException e){
					wh[0] = 0;
				}
			}
			int iEnd = 0;
			for(iEnd=index_X+1; iEnd<strFile.length(); iEnd++){
				if(!isDigital(strFile.charAt(iEnd))){
					break;
				}
			}
			if(iEnd<strFile.length()){
				String sHeight = strFile.substring(index_X+1, iEnd);
				try{
				wh[1] = Integer.parseInt(sHeight);
				}
				catch(NumberFormatException e){
					wh[1] = 0;
				}
			}		
		}
		Log.i(TAG, "guessFileWH out "+wh[0]+"x"+wh[1]);
	}
	
	private boolean isDigital(char c){
		if(c>='0' && c<='9'){
			return true;
		}
		else{
			return false;
		}
	}

	private String getFileName(String fullPath){
		int lastIndex = fullPath.lastIndexOf("/");
		return fullPath.substring(lastIndex+1);
	}
}
