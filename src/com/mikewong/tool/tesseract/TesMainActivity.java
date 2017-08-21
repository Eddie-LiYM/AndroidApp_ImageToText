package com.mikewong.tool.tesseract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class TesMainActivity extends Activity {

	private static final int PHOTO_CAPTURE = 0x11;// ����
	private static final int PHOTO_RESULT = 0x12;// ���
	private static final int PHOTO_REQUEST_GALLERY = 0x13;// ���

	private static String LANGUAGE = "eng";
	private static String IMG_PATH = getSDPath() + java.io.File.separator
			+ "ocrtest";

	private static EditText tvResult;
	private static TextView tvResult1;
	private static ImageView ivSelected;
	private static ImageView ivTreated;
	private static Button btnCamera;
	private static Button btnSelect;
	private static Button btnCapy;
	private static CheckBox chPreTreat;
	private static RadioGroup radioGroup;
	private static String textResult;
	private static Bitmap bitmapSelected;
	private static Bitmap bitmapTreated;
	private static final int SHOWRESULT = 0x101;
	private static final int SHOWTREATEDIMG = 0x102;

	// ��handler���ڴ����޸Ľ��������
	public static Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			tvResult.setText("");
			switch (msg.what) {
			case SHOWRESULT:
				if (textResult.equals(""))
					tvResult1.setText("ʶ��ʧ��");
				else
					{
						tvResult.setText(textResult);
						tvResult1.setText("ʶ�����");
					}
				break;
			case SHOWTREATEDIMG:
				tvResult1.setText("ʶ����......");
				showPicture(ivTreated, bitmapTreated);
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tesmain);

		// ���ļ��в����� ���ȴ����ļ���
		File path = new File(IMG_PATH);
		if (!path.exists()) {
			path.mkdirs();
		}

		tvResult = (EditText) findViewById(R.id.tv_result);
		tvResult1 = (TextView) findViewById(R.id.tv_result1);
		ivSelected = (ImageView) findViewById(R.id.iv_selected);
		ivTreated = (ImageView) findViewById(R.id.iv_treated);
		btnCamera = (Button) findViewById(R.id.btn_camera);
		btnSelect = (Button) findViewById(R.id.btn_select);
		btnCapy = (Button) findViewById(R.id.btn_capy);
		chPreTreat = (CheckBox) findViewById(R.id.ch_pretreat);
		radioGroup = (RadioGroup) findViewById(R.id.radiogroup);

		btnCamera.setOnClickListener(new cameraButtonListener());
		btnSelect.setOnClickListener(new selectButtonListener());
		btnCapy.setOnClickListener(new capyButtonListener());
		
    	if(!isDirExist("tessdata")){
    		Toast.makeText(getApplicationContext(), "SD��ȱ�����԰��������С�����",Toast.LENGTH_LONG).show();
    		new SaveFile_Thread().start();
    	}
		// �������ý�������
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_en:
					LANGUAGE = "eng";
					break;
				case R.id.rb_ch:
					LANGUAGE = "chi_sim";
					break;
				}
			}

		});

	}


	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {  //����༭������ĵط��˳����뷨
	    if (ev.getAction() == MotionEvent.ACTION_DOWN) {  
	        View v = getCurrentFocus();  
//	        OnTouch = v.getId();
	        if (isShouldHideInput(v, ev)) {  
	  
	            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
	            if (imm != null) {  
	                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
	            }  
	        }  
	        return super.dispatchTouchEvent(ev);  
	    }  
	    // �ز����٣��������е������������TouchEvent��  
	    if (getWindow().superDispatchTouchEvent(ev)) {  
	        return true;  
	    }  
	    return onTouchEvent(ev);  
	}
	public  boolean isShouldHideInput(View v, MotionEvent event) {  
	    if (v != null && (v instanceof EditText)) {  
	        int[] leftTop = { 0, 0 };  
	        //��ȡ�����ǰ��locationλ��  
	        
	        v.getLocationInWindow(leftTop);  
	        
	        int left = leftTop[0];  
	        int top = leftTop[1];  
	        int bottom = top + v.getHeight();  
	        int right = left + v.getWidth();  
	        if (event.getX() > left && event.getX() < right  
	                && event.getY() > top && event.getY() < bottom) {  
	            // ���������������򣬱������EditText���¼�  
	            return false;  
	        } else {  
	            return true;  
	        }  
	    }  
	    return false;  
	}  
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_CANCELED)
			return;
		

		if (requestCode == PHOTO_CAPTURE) {
			tvResult1.setText("abc");
			startPhotoCrop(Uri.fromFile(new File(IMG_PATH, "temp.jpg")));
		}

		if (requestCode == PHOTO_REQUEST_GALLERY) {
			startPhotoCrop(data.getData());
		}
		
		// ������
		if (requestCode == PHOTO_RESULT) {
			bitmapSelected = decodeUriAsBitmap(Uri.fromFile(new File(IMG_PATH,
					"temp_cropped.jpg")));
			if (chPreTreat.isChecked())
				tvResult1.setText("Ԥ������......");
			else
				tvResult1.setText("ʶ����......");
			// ��ʾѡ���ͼƬ
			showPicture(ivSelected, bitmapSelected);
			
			// ���߳�������ʶ��
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (chPreTreat.isChecked()) {
						bitmapTreated = ImgPretreatment
								.doPretreatment(bitmapSelected);
						Message msg = new Message();
						msg.what = SHOWTREATEDIMG;
						myHandler.sendMessage(msg);
						textResult = doOcr(bitmapTreated, LANGUAGE);
					} else {
						bitmapTreated = ImgPretreatment
								.converyToGrayImg(bitmapSelected);
						Message msg = new Message();
						msg.what = SHOWTREATEDIMG;
						myHandler.sendMessage(msg);
						textResult = doOcr(bitmapTreated, LANGUAGE);
					}
					Message msg2 = new Message();
					msg2.what = SHOWRESULT;
					myHandler.sendMessage(msg2);
				}

			}).start();

		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// ����ʶ��
	class cameraButtonListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(IMG_PATH, "temp.jpg")));
			startActivityForResult(intent, PHOTO_CAPTURE);
		}
	};

	
	// �������ݵ����а�
	class capyButtonListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
	        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	        // ���ı����ݷŵ�ϵͳ�������
	        if(tvResult.length() == 0){
	        	Toast.makeText(getApplicationContext(), "������", Toast.LENGTH_SHORT).show();
	        	return;
	        }
	        cm.setText(tvResult.getText());
	        Toast.makeText(getApplicationContext(), "���Ƴɹ�", Toast.LENGTH_SHORT).show();
		}
	};
	
	// �����ѡȡ��Ƭ���ü�
	class selectButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
//			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//			intent.setType("image/*");
//			intent.putExtra("crop", "true");
//			intent.putExtra("scale", true);
//			intent.putExtra("return-data", false);
//			intent.putExtra(MediaStore.EXTRA_OUTPUT,
//					Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
//			intent.putExtra("outputFormat",
//					Bitmap.CompressFormat.JPEG.toString());
//			intent.putExtra("noFaceDetection", true); // no face detection
//			startActivityForResult(intent, PHOTO_RESULT);
			
			 // ����ϵͳͼ�⣬ѡ��һ��ͼƬ
	        Intent intent = new Intent(Intent.ACTION_PICK);
	        intent.setType("image/*");
	        // ����һ�����з���ֵ��Activity��������ΪPHOTO_REQUEST_GALLERY
	        boolean dele= delete(new File(IMG_PATH));
	        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
		}

	}
	
	// ��ͼƬ��ʾ��view��
	public static void showPicture(ImageView iv, Bitmap bmp){
		iv.setImageBitmap(bmp);
	}
	
	/**
	 * ����ͼƬʶ��
	 * 
	 * @param bitmap
	 *            ��ʶ��ͼƬ
	 * @param language
	 *            ʶ������
	 * @return ʶ�����ַ���
	 */
 	public String doOcr(Bitmap bitmap, String language) {
		TessBaseAPI baseApi = new TessBaseAPI();

		baseApi.init(getSDPath(), language);

		// ����Ӵ��У�tess-twoҪ��BMP����Ϊ������
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		baseApi.setImage(bitmap);

		String text = baseApi.getUTF8Text();

		baseApi.clear();
		baseApi.end();

		return text;
	}

	/**
	 * ��ȡsd����·��
	 * 
	 * @return ·�����ַ���
	 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// ��ȡ���Ŀ¼
		}
		return sdDir.toString();
	}

	/**
	 * ����ϵͳͼƬ�༭���вü�
	 */
	public void startPhotoCrop(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(IMG_PATH, "temp_cropped.jpg")));
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		startActivityForResult(intent, PHOTO_RESULT);
	}

	/**
	 * ����URI��ȡλͼ
	 * 
	 * @param uri
	 * @return ��Ӧ��λͼ
	 */
	private Bitmap decodeUriAsBitmap(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(getContentResolver()
					.openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}
	public static boolean delete(File file){
		
//		File file = new File(path);
        if (file.exists()) { //ָ���ļ��Ƿ����  
            if (file.isFile()) { //��·������ʾ���ļ��Ƿ���һ����׼�ļ�  
                file.delete(); //ɾ�����ļ�  
            } else if (file.isDirectory()) { //��·������ʾ���ļ��Ƿ���һ��Ŀ¼���ļ��У�  
                File[] files = file.listFiles(); //�г���ǰ�ļ����µ������ļ�  
                for (File f : files) {  
                	delete(f); //�ݹ�ɾ��  
                    //Log.d("fileName", f.getName()); //��ӡ�ļ���  
                }  
            }  
            //file.delete(); //ɾ���ļ��У�song,art,lyric��  
        }
        return true;  
	}
	
	
    /* 
     * �ж�SD��dirĿ¼�Ƿ���� 
     */  
    public boolean isDirExist(String dir){  
    		
    	//��õ�ǰ�ⲿ�����豸��Ŀ¼  
    	String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    	   
        File file = new File(SDCardRoot + dir + File.separator);  
        if(!file.exists())  
            return false;  //�������false
        else
        	return true;
    } 
    
    public boolean SaveFileToSDCard(){
    	
    	SDUtils sdutils_Chinese = new SDUtils("tessdata","chi_sim.traineddata",this,R.raw.chi_sim);
    	SDUtils sdutils_English = new SDUtils("tessdata","eng.traineddata",this,R.raw.eng);
    	try {
    		sdutils_Chinese.getSQLiteDatabase();
    		sdutils_English.getSQLiteDatabase();
		} catch (IOException e) {
			return false;
		}
    	return true;
    }
    public class SaveFile_Thread extends Thread {
  		
  		public SaveFile_Thread(){
  		}
  		
  		public void run(){
  			synchronized (this) {
  				boolean iret;
  				do {
  					iret = SaveFileToSDCard();
  				} while (false);
  				if(iret){
  					ShowMsg(1);
  				}else
  					ShowMsg(2);
  			}
  		}
  	}
      public void ShowMsg(int what) {
  		mLoadKeyHandler.sendEmptyMessage(what);
  	}
  	
  	public Handler mLoadKeyHandler = new Handler() {
  		@Override
  		public void handleMessage(Message msg) {
  			if(msg.what==1){
  				Toast.makeText(getApplicationContext(), "���Ƴɹ�",Toast.LENGTH_LONG).show();
  				}
  			else if(msg.what==2)
  				Toast.makeText(getApplicationContext(), "����ʧ��",Toast.LENGTH_LONG).show();
  		}
  	};
}
