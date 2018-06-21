package com.arcsoft.refocus;

import com.arcsoft.refocus.refocus.NewRefocusActivity;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

// ----------------------------------------------------------------------
public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";
    
	public static final int ID_BTN_IMGREFOCUS = 0x0010;
	public static final int ID_BTN_IMGREFOCUS_POST = 0x0011;
	public static final int ID_BTN_VIDREFOCUS = 0x0012;
	public static final int ID_INPUT_MAIN_WIDTH = 0x0013;
	public static final int ID_INPUT_MAIN_HEIGHT = 0x0014;
	public static final int ID_INPUT_AUX_WIDTH = 0x0015;
	public static final int ID_INPUT_AUX_HEIGHT = 0x0016;
	public static final int ID_INPUT_PREV_WIDTH = 0x0017;
	public static final int ID_INPUT_PREV_HEIGHT = 0x0018;
	public static final int ID_INPUT_PREV_ALIGN = 0x0019;
	
	private EditText mLeftW = null;
	private EditText mLeftH = null; 
	private EditText mRightW = null;
	private EditText mRightH = null; 
	private EditText mPrevW = null;
	private EditText mPrevH = null;
	private CheckBox mALign = null;
	private EditText mBlur = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate in");

        // Hide the window title.
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
    	initScreenSize();
    	
    	RelativeLayout rootLayout = new RelativeLayout(this);
        rootLayout.setBackgroundColor(Color.BLACK) ;
        setContentView(rootLayout);
        
        LayoutParams param = null;
        Button mImageRefocus = new Button(this);
        mImageRefocus.setId(ID_BTN_IMGREFOCUS);
        mImageRefocus.setText("Image Refocus");
        mImageRefocus.setOnClickListener(onClickButtion);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.CENTER_IN_PARENT);
		param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rootLayout.addView(mImageRefocus, param);
		
        Button mImageRefocusPost = new Button(this);
        mImageRefocusPost.setId(ID_BTN_IMGREFOCUS_POST);
        mImageRefocusPost.setText("Image Refocus Post");
        mImageRefocusPost.setOnClickListener(onClickButtion);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.CENTER_IN_PARENT);
		param.addRule(RelativeLayout.BELOW, ID_BTN_IMGREFOCUS);
		rootLayout.addView(mImageRefocusPost, param);
		
        Button mPreviewRefocus = new Button(this);
        mPreviewRefocus.setId(ID_BTN_VIDREFOCUS);
        mPreviewRefocus.setText("Preview Refocus");
        mPreviewRefocus.setOnClickListener(onClickButtion);
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.CENTER_IN_PARENT);
		param.addRule(RelativeLayout.BELOW, ID_BTN_IMGREFOCUS_POST);
		rootLayout.addView(mPreviewRefocus, param);
        Log.i(TAG, "onCreate out");
    }

	private OnClickListener onClickButtion = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case ID_BTN_IMGREFOCUS:
				goImageRefocus();
				break;

			case ID_BTN_IMGREFOCUS_POST:
				goImageRefocus_Post();
				break;

			case ID_BTN_VIDREFOCUS:
				goVideoRefocus();
				break;
			}
		}
	};
    
    private void goImageRefocus() {
    	//前处理
    	Intent intent = new Intent();
    	intent.setAction("arcsoft.action.Refocus");
    	intent.putExtra("REFOCUS_MODE", 1);
//    	if(mLeftW.getText().toString().length() > 0 && mLeftH.getText().toString().length() > 0) {
//    		intent.putExtra("LEFT_WIDTH",  Integer.parseInt(mLeftW.getText().toString()));
//    		intent.putExtra("LEFT_HEIGHT", Integer.parseInt(mLeftH.getText().toString()));
//    	}
//    	if(mRightW.getText().toString().length() > 0 && mRightH.getText().toString().length() > 0) {
//    		intent.putExtra("RIGHT_WIDTH",  Integer.parseInt(mRightW.getText().toString()));
//    		intent.putExtra("RIGHT_HEIGHT", Integer.parseInt(mRightH.getText().toString()));
//    	}
//    	
//    	if(mBlur.getText().toString().length() > 0) {
//    		intent.putExtra("BLUR_INTENSITY",  Integer.parseInt(mBlur.getText().toString()));
//    	}
    	
		startActivity(intent);
    }
    
    private void goImageRefocus_Post() {
    	//后处理
    	Intent intent = new Intent();
    	intent.setAction("arcsoft.action.PostRefocus");    	
//    	Intent intent = new Intent();
//    	intent.setAction("arcsoft.action.Refocus");
//    	intent.putExtra("REFOCUS_MODE", 2);
////    	if(mLeftW.getText().toString().length() > 0 && mLeftH.getText().toString().length() > 0) {
////    		intent.putExtra("LEFT_WIDTH",  Integer.parseInt(mLeftW.getText().toString()));
////    		intent.putExtra("LEFT_HEIGHT", Integer.parseInt(mLeftH.getText().toString()));
////    	}
////    	
////    	if(mBlur.getText().toString().length() > 0) {
////    		intent.putExtra("BLUR_INTENSITY",  Integer.parseInt(mBlur.getText().toString()));
////    	}
		startActivity(intent);
    }
    
    private void goVideoRefocus() {
    	//预览
    	Intent intent = new Intent();
    	intent.setAction("arcsoft.action.PreviewRefocus"); 
    	startActivity(intent);
    }
    
    private void  initScreenSize() {
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();

		Point p = new Point();
		display.getSize(p);

		if (p.x > p.y) {
			int tmp = p.x;
			p.x = p.y;
			p.y = tmp;
		
		}
		UIGlobalDef.APP_SCREEN_WIDTH = p.x;		//1440
		UIGlobalDef.APP_SCREEN_HEIGHT = p.y;	//2560
	}

}
