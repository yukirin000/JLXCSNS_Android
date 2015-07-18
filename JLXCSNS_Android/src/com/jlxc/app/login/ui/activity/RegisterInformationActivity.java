package com.jlxc.app.login.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.FileUtil;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.demo.ui.activity.MainActivity;
import com.jlxc.app.demo.ui.activity.NextActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterInformationActivity extends BaseActivityWithTopBar {

	public static final int PHOTOHRAPH = 1;// 拍照
	public static final int PHOTOZOOM = 2; // 缩放
	public static final int PHOTORESOULT = 3;// 结果
	public static final int CAMERA_SELECT = 4;// 相册选取
	public static final int GET_DEPARTMENT_REQUEST_CODE = 5;
	public static final String IMAGE_UNSPECIFIED = "image/*";
	
	//图片名字
	private String headImageName;
	
	//头像
	@ViewInject(R.id.headImageView)
	private ImageView headImageView;
	//性别
	@ViewInject(R.id.radioGroup)
	private RadioGroup radioGroup;
	//姓名
	@ViewInject(R.id.nameEt)
	private EditText nameEditText;
	//确定按钮
	@ViewInject(R.id.confirmBtn)
	private Button confirmButton;
	//点击头像的image弹窗
	AlertDialog imageDialog;
	
	//统计处理点击
	@OnClick({R.id.headImageView,R.id.register_information_activity,R.id.confirmBtn,R.id.base_ll_right_btns})
	private void clickEvent(View view) {
		switch (view.getId()) {
		//头像点击
		case R.id.headImageView:
			showChoiceImageAlert();
			break;
		//确认
		case R.id.confirmBtn:
			//提交信息
			uploadInformation();
			break;
		case R.id.base_ll_right_btns:
			//跳过
			Intent intent = new Intent(this, NextActivity.class);
			startActivityWithRight(intent);
			break;
		case R.id.register_information_activity:
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);  
	        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	        break;
		default:
			break;
		}
	}
	
	
//	public Bitmap convertViewToBitmap(View view){
//		view.setDrawingCacheEnabled(Boolean.TRUE);
//		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//        view.buildDrawingCache();
//        Bitmap bitmap = view.getDrawingCache();
//        
//		return bitmap;
//	}
	
	//上传个人信息
	private void uploadInformation(){
		
		final UserModel userModel = UserManager.getInstance().getUser();
		showLoading("信息上传中，请稍候...", false);
		RequestParams params = new RequestParams();
		//如果选择头像了
		//bitmap获取
//		headImageView.setDrawingCacheEnabled(true);
//        Bitmap bitmap = Bitmap.createBitmap(headImageView.getDrawingCache());
//        headImageView.setDrawingCacheEnabled(false);	
//        String photoName = getPhotoFileName();
//		//图片保存到本地
//		FileUtil.savePic(Bitmap.createBitmap(bitmap), FileUtil.HEAD_PIC_PATH, photoName, 100);
		
		final File imageFile = new File(FileUtil.HEAD_PIC_PATH+headImageName);
		if (imageFile.exists()) {
			//图片
			params.addBodyParameter("image", imageFile);			
		}
			
		params.addBodyParameter("uid", userModel.getUid()+"");
//		params.addBodyParameter("uid", "1");
		String sex = "0";
		//性别
		switch (radioGroup.getCheckedRadioButtonId()) {
			case R.id.radioMale:
				sex = "0";	
				break;
			case R.id.radioFemale:
				sex = "1";
				break;
			default:
				break;
		}
		params.addBodyParameter("sex", sex);
		//姓名
		params.addBodyParameter("name", nameEditText.getText().toString());
		final int sexValue = Integer.parseInt(sex);
		HttpManager.post(JLXCConst.SAVE_PERSONAL_INFO, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						hideLoading();
						int status = jsonResponse.getIntValue("status");
						switch (status) {
						case JLXCConst.STATUS_SUCCESS:							
							//设置修改内容
							userModel.setName(nameEditText.getText().toString());
							userModel.setSex(sexValue);
							userModel.setHead_image(jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT).getString("head_image"));
							userModel.setHead_image(jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT).getString("head_sub_image"));
							
							//数据持久化
							UserManager.getInstance().saveAndUpdate();
							
							//toast
							Toast.makeText(RegisterInformationActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE), Toast.LENGTH_SHORT).show();
							hideLoading();
							//跳到主页
							Intent intent = new Intent(RegisterInformationActivity.this, NextActivity.class);
							startActivityWithRight(intent);
							
							//删除缓存
							if (imageFile.exists()) {
								imageFile.delete();
							}
							
							break;
						case JLXCConst.STATUS_FAIL:
							hideLoading();
							Toast.makeText(RegisterInformationActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE), Toast.LENGTH_SHORT).show();
							break;
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1, String flag) {
						LogUtils.i(arg0.getMessage(), 1);
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						Toast.makeText(RegisterInformationActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
					}
		 }, null));
	}
	
	private void showChoiceImageAlert() {
		if (imageDialog == null) {
			imageDialog = new AlertDialog.Builder(this).setTitle("选择照片").setItems(new String[]{"拍照","相册"}, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					switch (which) {
					case 0:
						//拍照
						Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						headImageName = getPhotoFileName()+"";
						LogUtils.i(headImageName, 1);
						File tmpFile = new File(FileUtil.TEMP_PATH+headImageName);
						intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(tmpFile));
						startActivityForResult(intentCamera, PHOTOHRAPH);
						break;
					case 1:
						//相册
						Intent intentAlbum = new Intent(Intent.ACTION_GET_CONTENT);
						intentAlbum.setType(IMAGE_UNSPECIFIED);
						startActivityForResult(intentAlbum, CAMERA_SELECT);
						break;
					default: 
						break;
					}					
				}
			}).setNegativeButton("取消", null).create();
		}
		
		imageDialog.show();
		
	}
	
	 @Override  
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	        // TODO Auto-generated method stub  
	        super.onActivityResult(requestCode, resultCode, data);  
	        if (resultCode == Activity.RESULT_OK) {  
	            String sdStatus = Environment.getExternalStorageState();  
	            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
	            	LogUtils.i("SD card is not avaiable/writeable right now.", 1);
	                return;  
	            }  
	        	//头像需要缩放	            
	            switch (requestCode) {
	            case PHOTOHRAPH:// 当选择拍照时调用   
	            	File tmpFile = new File(FileUtil.TEMP_PATH+headImageName);
	                startPhotoZoom(Uri.fromFile(tmpFile));
	                break;
	            case CAMERA_SELECT:// 当选择从本地获取图片时
	                // 做非空判断，当我们觉得不满意想重新剪裁的时候便不会报异常，下同
	                if (data != null) {
	                    startPhotoZoom(data.getData());
	                }
	                break;
	            case PHOTORESOULT:// 返回的结果
	                if (data != null){
//	                	// 设置为有图片
//	                	// 图片裁切后的结果
//	        			Bundle bundle = data.getExtras();
//	        			Bitmap bitmap = (Bitmap) bundle.get("data");
//	        			headImageView.setImageBitmap(bitmap);// 将图片显示在ImageView里
//	        			if (null != bitmap && null != headImageName) {
//		    				//删除临时文件
//		        			File file = new File(FileUtil.TEMP_PATH+headImageName);
//		    				if (file.exists()) {
//		    					file.delete();
//		    				}	
//	        			}
	        			
	        			if (null != headImageName) {
	        				BitmapManager.getInstance().getBitmapUtils(this, false, false)
	        				.display(headImageView, FileUtil.HEAD_PIC_PATH+headImageName);
		    				//删除临时文件
		        			File file = new File(FileUtil.TEMP_PATH+headImageName);
		    				if (file.exists()) {
		    					file.delete();
		    				}	
		    				uploadInformation();
	        			}	 
	                }
	                break;
	            }
	        }  
	 }
	//开启缩放
	 public void startPhotoZoom(Uri uri) {
		 
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
			intent.putExtra("crop", "true");
			// aspectX aspectY 是宽高的比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// outputX outputY 是裁剪图片宽高
			intent.putExtra("outputX", 960);
			intent.putExtra("outputY", 960);
			intent.putExtra("scale", true);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("noFaceDetection", true); // no face detection
			intent.putExtra("return-data", true);
			File tmpFile = new File(FileUtil.HEAD_PIC_PATH+headImageName);
			//地址
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
			startActivityForResult(intent, PHOTORESOULT);
	 }

	// 使用系统当前日期加以调整作为照片的名称
    @SuppressLint("SimpleDateFormat") 
    private String getPhotoFileName() {
    	//用户id+时间戳
    	String fileName = UserManager.getInstance().getUser().getUid()+""+System.currentTimeMillis()/1000;
        return fileName + ".jpg";
    }
	 
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_register_information;
	}

	@Override
	protected void setUpView() {
//		radioGroup.getCheckedRadioButtonId();
		radioGroup.check(R.id.radioMale);
		addRightBtn("跳过");
		
	}

}
