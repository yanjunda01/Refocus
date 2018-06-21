package com.arcsoft.refocus.refocus;

import com.arcsoft.refocus.R;
import com.arcsoft.refocus.UIGlobalDef;
import com.arcsoft.refocus.utils.FileUtil;
import com.arcsoft.refocus.utils.YuvUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class NewPostRefocusActivity extends Activity implements Handler.Callback{
	public final static int REQUEST_CODE_OPENDIALOG_DEPTH = 5;	// 深度文件
	public final static int REQUEST_CODE_OPENDIALOG_MAIN = 6;	// 主摄
	private final static String TAG = "NewPostRefocusActivity";
	private String strDefaultWorkDir;	//缺省的打开文件对话框目录
	private String strMainFile;	//主摄文件名
	private TextView mDepthFileTV;	//深度文件名显示控件
	private TextView mMainFileTV;	//主摄文件名显示控件
	private byte[] bDepthData;	//深度图数据
	private byte[] bMainFileData;	//主摄图数据
	private int nFocusX;	//refocus x坐标
	private int nFocusY;	//refocus y坐标
	private ImageRefocus mImageRefocus;	//引擎
	private Bitmap mBmp;
	private final static int POST_REFOCUS_MODE = 0x02;	//图库处理
	private LinearLayout mMainUI;
	private RelativeLayout mRoot;
	private final static int MSG_UPDATE_UI = 0x0010;
	private ImageView mResultView;	//控件
	private Handler mHandler = new Handler(this);
	private int mMainWidth;	//主摄宽度
	private int mMainHeight;
	private Size mResultViewSize;	//image控件填充图片后的实际宽高
	private Point mFocusPt;	//重新的refocus坐标
	private boolean bProcess;
	private ImageProcessThread mImageProcessThread;	//操作线程
	private int index;	//保存点击生成的结果图index
	



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_post_refocus);
		
		mMainFileTV = (TextView)findViewById(R.id.TextViewMainFile);
		mDepthFileTV = (TextView)findViewById(R.id.TextViewDepthFile);
		mMainUI = (LinearLayout)findViewById(R.id.LinearLayoutUI);
		mRoot = (RelativeLayout)findViewById(R.id.root);
	}

	public void onClickSelectMainFile(View srcView){
		Log.i(TAG, "onClickSelectMainFile in");
		Intent intent = new Intent(this, OpenFileDialogActivity.class);  
		intent.putExtra("DefaultFilePath", strDefaultWorkDir == null?Environment.getExternalStorageDirectory().getPath():strDefaultWorkDir);  
		intent.putExtra("DefaultFileName", "default.lag");
		startActivityForResult(intent, NewPostRefocusActivity.REQUEST_CODE_OPENDIALOG_MAIN);
		Log.i(TAG, "onClickSelectMainFile out");
	}
	
	public void onClickSelectDepthFile(View srcView){
		Log.i(TAG, "onClickSelectDepthFile in");
		Intent intent = new Intent(this, OpenFileDialogActivity.class);  
		intent.putExtra("DefaultFilePath", strDefaultWorkDir == null?Environment.getExternalStorageDirectory().getPath():strDefaultWorkDir);  
		intent.putExtra("DefaultFileName", "default.lag");
		startActivityForResult(intent, NewPostRefocusActivity.REQUEST_CODE_OPENDIALOG_DEPTH);
		Log.i(TAG, "onClickSelectDepthFile out");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult in, requestCode=" + requestCode + " resultCode="+resultCode);
		switch (requestCode) {
		   //主摄文件
		   case NewPostRefocusActivity.REQUEST_CODE_OPENDIALOG_MAIN:
			   if (resultCode == RESULT_OK){
				   Bundle b = data.getExtras();
				   String[] fileList = b.getStringArray("FilePathName");
				   if(fileList.length == 1){
					   strMainFile = fileList[0];
					   mMainFileTV.setText(fileList[0]);
					   bMainFileData = FileUtil.LoadFromFile(fileList[0]);
				   }
				   //保存访问路径
				   if(fileList.length>0){
						int lastIndex = fileList[0].lastIndexOf("/");
						strDefaultWorkDir = fileList[0].substring(0, lastIndex);
				   }
		        }  
		    break;
			   //深度信息
			   case NewPostRefocusActivity.REQUEST_CODE_OPENDIALOG_DEPTH:
				   if (resultCode == RESULT_OK){
					   Bundle b = data.getExtras();
					   String[] fileList = b.getStringArray("FilePathName");
					   if(fileList.length == 1){
						   bDepthData = FileUtil.LoadFromFile(fileList[0]);
						   mDepthFileTV.setText(fileList[0]);
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
		if(bDepthData == null){
			   Toast.makeText(NewPostRefocusActivity.this, "bDepthData error", Toast.LENGTH_SHORT).show();
			   Log.i(TAG, "onClickOK out, depth error");
			   return;
		}
		
		if(strMainFile == null){	
			Toast.makeText(NewPostRefocusActivity.this, "input image error", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "onClickOK out, input image error: "+strMainFile);
			return;
		}
		//根据文件名猜测宽和高
		int[] wh = new int[2];
		guessFileWH(strMainFile, wh);
		mMainWidth = wh[0];
		mMainHeight = wh[1];
		
		//哈哈哈
		if(nFocusX == 0||nFocusX>=wh[0]){			
			nFocusX = wh[0]/3;
		}
		if(nFocusY == 0||nFocusY>=wh[1]){				
			nFocusY = wh[1]/3;
		}
		
		//猜测文件格式
		int nMainFileFormat = -1;
		if(strMainFile.indexOf("nv21")!=-1 || strMainFile.indexOf("NV21")!=-1){
			nMainFileFormat = 0;
		}else if(strMainFile.indexOf("yuyv")!=-1 || strMainFile.indexOf("YUYV")!=-1){
			nMainFileFormat = 1;								
		}else{								
			Toast.makeText(NewPostRefocusActivity.this, "file surfix error", Toast.LENGTH_SHORT).show();
		}
		
		// begin
		mImageRefocus = new ImageRefocus();
		mImageRefocus.Init(POST_REFOCUS_MODE);
		mImageRefocus.SetParam(nFocusX, nFocusY, 50);
		byte[] resultData = mImageRefocus.GetImageResultWithDepth(bMainFileData, bMainFileData.length, wh[0], wh[1], bDepthData, bDepthData.length);
		if(resultData != null) {
			mBmp = YuvUtil.nv21ToBitmap(resultData, wh[0], wh[1]);

			//int pointIndex = strMainFile.indexOf('.');
			//String fileSufix = strMainFile.substring(pointIndex); //后缀名包含. .nv21
			//String out_file = strMainFile.substring(0, pointIndex) + "_post_bokeh" + fileSufix;
			//FileUtil.saveImage(out_file, resultData);
			
			mMainUI.setVisibility(View.INVISIBLE);
			
			// add imageview
		    mResultView  = new ImageView(this);        
		    LayoutParams param = new LayoutParams(mMainWidth, mMainHeight);		
			param.addRule(RelativeLayout.CENTER_IN_PARENT);   
			mRoot.addView(mResultView, param);
			mHandler.sendEmptyMessage(MSG_UPDATE_UI);
			
			//计算image控件的实际宽高
			int new_width = UIGlobalDef.APP_SCREEN_WIDTH;
			int new_height = UIGlobalDef.APP_SCREEN_WIDTH * mMainHeight / mMainWidth;
			mResultViewSize = new Size(new_width, new_height);
		} else {
			Toast.makeText(NewPostRefocusActivity.this, "Failed", Toast.LENGTH_SHORT).show();
		}
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
	
	public boolean handleMessage(Message msg) {
		Log.i(TAG, "handleMessage in");
		boolean res = true;
		switch (msg.what) {
		case MSG_UPDATE_UI: 
			if(null != mResultView){
				Log.i(TAG, "handleMessage setImageBitmap");
				mResultView.setImageBitmap(mBmp);
			}
			break;
			
		default:
			res = false;
			break;
		}

		Log.i(TAG, "handleMessage out");
		return res;
	}
	
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
		final int action = event.getAction();
		int pointCount = event.getPointerCount();
		if (pointCount < 2) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			if (0 == action) {
				return true;
			} else if (1 == action) {
				onSingleTouchUp(x, y);
				return true;
			} else if (2 == action) {
				return true;
			}
		}
		return false;
    }
    
	private void onSingleTouchUp(int x, int y) {		
		int topMarge = (UIGlobalDef.APP_SCREEN_HEIGHT - mResultViewSize.getHeight()) / 2;
		if(bProcess) {
			return;
		}
		mBmp = null;
		mFocusPt = getFocusPoint(x, y-topMarge, mMainWidth, mMainHeight);
		if(mFocusPt != null){
			mImageProcessThread = new ImageProcessThread();
			mImageProcessThread.start();
		}
	}
	
	private class ImageProcessThread extends Thread {
		public void run(){
			bProcess = true;
			if(mImageRefocus != null) {
				mImageRefocus.SetParam(mFocusPt.x, mFocusPt.y, 50);
				byte[] resultData = mImageRefocus.GetImageResultWithDepth(bMainFileData, bMainFileData.length, mMainWidth, mMainHeight, bDepthData, bDepthData.length);
				if(resultData != null) {
//					int pointIndex = strMainFile.indexOf('.');
//					String fileSufix = strMainFile.substring(pointIndex); //后缀名包含. .nv21
//					String out_file = strMainFile.substring(0, pointIndex) + "_post_bokeh_" + (index++) + fileSufix;
//					FileUtil.saveImage(out_file, resultData);

					mBmp = YuvUtil.nv21ToBitmap(resultData, mMainWidth, mMainHeight);
				}
				mHandler.sendEmptyMessage(MSG_UPDATE_UI);
			}		
			bProcess = false;
		}
	}
	
	private Point getFocusPoint(int x, int y, int fullDataW, int fullDataH) {
		//x y在image控件中的坐标
		//fullDataW, fullDataH全图的大小
		Log.i(TAG, "getFocusPoint in "+x+","+y+";"+fullDataW+"x"+fullDataH);
		if (x < 0 || y < 0) {
			 return null;
		}
		
		int PictureX = x * fullDataW / mResultViewSize.getWidth();
		int PictureY = y * fullDataH / mResultViewSize.getHeight();
		Log.i(TAG, "getFocusPoint out, "+PictureX+","+PictureY);
		return new Point(PictureX, PictureY);
	}
	
	protected void onDestroy() {
		super.onDestroy();
	    Log.i(TAG, "onDestroy in");
	    
	    if(mImageRefocus != null) {
	    	mImageRefocus.UnInit();
	    	mImageRefocus = null;
	    }
	    Log.i(TAG, "onDestroy out");
	}

}
