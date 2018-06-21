package com.arcsoft.refocus.refocus;
import java.io.File;  
import java.util.ArrayList;  
import java.util.Collections;
import java.util.Comparator;

import com.arcsoft.refocus.R;

import android.app.*;  
import android.content.Intent;  
import android.os.Bundle;  
import android.util.Log;
import android.view.View;  
import android.widget.AdapterView;  
import android.widget.AdapterView.OnItemClickListener;  
import android.widget.ArrayAdapter;  
import android.widget.EditText;  
import android.widget.ListView;  
import android.widget.TextView;  
import android.widget.Toast;  

public class OpenFileDialogActivity extends Activity{  
      
    String DefaultFilePath;  
    String DefaultFileName;  
    ArrayList<File> FileList = new ArrayList<File>();  
    File FileNow;  
    String Ext;     
      
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.openfiledialog);  
          
        //��ȡ����  
        Intent intent = getIntent();  
        DefaultFilePath = intent.getStringExtra("DefaultFilePath");  
        DefaultFileName = intent.getStringExtra("DefaultFileName");  
        this.Ext = intent.getStringExtra("Ext");  
        
          
        this.FileNow = new File(DefaultFilePath);  
        this.RefreshFileList();  
        //  
        //TextView EditFileName = (TextView)findViewById(R.id.TextFileName);  
        //EditFileName.setText(DefaultFileName);  
        //����ListView�����¼�    
        ListView mListView = (ListView)findViewById(R.id.FileList);
        mListView.setOnItemClickListener(new OnItemClickListener(){  
  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {  
                if (FileList.get(arg2).isDirectory()){  
                    FileNow = FileList.get(arg2);  
                    RefreshFileList();  
                }  
                else{
                	// after click file item
                    //SendAndFinish(FileList.get(arg2).getName());
                }  
            }  
              
        });  
    }  
      
    protected void RefreshFileList(){  
        //����Щ�ļ�������listview  
        this.FileList.clear();  
        File[] TempFiles = this.FileNow.listFiles();  
        if (TempFiles != null){  
            for (int i = 0;i < TempFiles.length;i ++){  
                if (TempFiles[i].isDirectory()){  
                    this.FileList.add(TempFiles[i]);  
                }  
                else{  
                    if (Ext!=null&&TempFiles[i].getName().endsWith(this.Ext)
                    		||Ext == null){  
                        this.FileList.add(TempFiles[i]);  
                    }
                }  
            }
            // ���ļ�������
            Collections.sort(FileList, new Comparator< File>() {  
             @Override  
             	public int compare(File o1, File o2) {
            	 	//Ŀ¼��ǰ���ļ��ں�
     	 			//Log.i("open dialog", o1.getName()+" "+o2.getName());
            	 	if (o1.isDirectory() && o2.isFile())  
            	 		return -1;	//o1 o2
            	 	if (o1.isFile() && o2.isDirectory())  
            	 		return 1;	//o2 o1
            	 	if(o1.getName().length()>=8 && o2.getName().length()>=8 && o1.getName().substring(0, 3).equals(o2.getName().substring(0, 3))){
            	 		//�ļ������ȴ��ڵ���8������.�ͺ�׺����ǰ4���ַ���ͬ��һ������dump�������ز�
            	 		//�������򣬱���
            	 		//aux_640x480_1.nv21
            	 		//aux_640x480_2.nv21
            	 		//aux_640x480_10.nv21
                	 	if(o1.getName().length() == o2.getName().length()){
                	 		return o1.getName().compareTo(o2.getName());
                	 	}
                	 	else{
                	 		return o1.getName().length()-o2.getName().length();
                	 	}
            	 	}
            	 	else{
            	 		//ͨ��Ŀ¼�µ��ļ�
            	 		return o1.getName().compareTo(o2.getName());	
            	 	}
             	}  
            });  
            
            //��ֵ��listView  
            String[] TempStrArr = new String[this.FileList.size()];  
            for (int i = 0;i < TempStrArr.length;i ++){  
                TempStrArr[i] = this.FileList.get(i).isDirectory() ? "[" + this.FileList.get(i).getName() + "]" : this.FileList.get(i).getName();  
            }  
            ListView mListView = (ListView)findViewById(R.id.FileList);  
            mListView.setAdapter(new ArrayAdapter<String>(this,  
                    android.R.layout.simple_list_item_multiple_choice, TempStrArr));  
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);  
        }  
        else{  
            Toast.makeText(this,"Ȩ�޲�����", Toast.LENGTH_LONG).show();  
            if (this.FileNow.getParentFile() != null){  
                this.FileNow = this.FileNow.getParentFile();  
            }  
            else{  
                this.FileNow = new File(DefaultFilePath);  
            }  
            this.RefreshFileList();  
        }  
    }  
      
    public void Return (View srcView){  
        if (this.FileNow.getParentFile() != null){  
            this.FileNow = this.FileNow.getParentFile();  
            this.RefreshFileList();  
        }  
    }  
      
    public void Cancel(View srcView){  
        this.finish();  
    }  
    
    public void Enter(View srcView){  
    	ListView mListView = (ListView)findViewById(R.id.FileList);
    	
    	long[] fileIndexs = getListSelectededItemIds(mListView);
    	
    	int nCheckedItemCount = fileIndexs.length;
    	//long[] fileIndexs = mListView.getCheckedItemIds();
    	String[] filenames = new String[nCheckedItemCount];
    	for(int i=0; i<nCheckedItemCount; i++){
    		filenames[i] = FileNow.getAbsolutePath() + "/" + FileList.get((int) fileIndexs[i]).getName();
    	}
    	Intent intent = new Intent();
    	Bundle b= new Bundle();
    	b.putStringArray("FilePathName", filenames);
    	intent.putExtras(b);
    	setResult(RESULT_OK, intent);
        this.finish();
    } 
    
    // ����ʹ��getCheckItemIds()����
    public long[] getListSelectededItemIds(ListView listView) {
         
        long[] ids = new long[listView.getCount()];//getCount()����ȡ��ListView��������item�ܸ���
        //�����û�ѡ��Item���ܸ���
        int checkedTotal = 0;
        for (int i = 0; i < listView.getCount(); i++) {
            //������Item�Ǳ�ѡ�е�
            if (listView.isItemChecked(i)) {
                ids[checkedTotal++] = i;
            }
        }
 
        if (checkedTotal < listView.getCount()) {
            //����ѡ�е�Item��ID����
            final long[] selectedIds = new long[checkedTotal];
            //���鸴�� ids
            System.arraycopy(ids, 0, selectedIds, 0, checkedTotal);
            return selectedIds;
        } else {
            //�û������е�Item��ѡ��
            return ids;
        }
    }
}  
