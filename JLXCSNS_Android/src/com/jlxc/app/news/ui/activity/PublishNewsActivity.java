package com.jlxc.app.news.ui.activity;

import java.io.File;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.utils.FileUtil;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PublishNewsActivity extends BaseActivityWithTopBar {

	public static final int TAKE_PHOTO = 1;// 拍照
	public static final int ALBUM_SELECT = 2;// 相册选取
	public static final String IMAGE_UNSPECIFIED = "image/*";
	
	//添加附件的layout
	@ViewInject(R.id.addImageLayout)
	private RelativeLayout addImageLayout;
	//添加附件的imageView
	@ViewInject(R.id.addImageView)
	private ImageView addImageView;
	@ViewInject(R.id.contentEt)
	private EditText contentEditText;
	//起始左边间距
	private int oriMarginLeft;
	//间隔
	private int space;
	//点击加号的image弹窗
	AlertDialog imageDialog;
	//临时文件名
	String tmpImageName;

	@OnClick(value={R.id.addImageView})
	private void clickEvent(View view){
		switch (view.getId()) {
		case R.id.addImageView:
			showChoiceImageAlert();
			break;
		default:
			break;
		}
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
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						LogUtils.i(tmpImageName, 1);
						File tmpFile = new File(FileUtil.TEMP_PATH+tmpImageName);
						intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(tmpFile));
						startActivityForResult(intentCamera, TAKE_PHOTO);
						break;
					case 1:
						//相册
						Intent intentAlbum = new Intent(Intent.ACTION_GET_CONTENT);
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						intentAlbum.setType(IMAGE_UNSPECIFIED);
						startActivityForResult(intentAlbum, ALBUM_SELECT);
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
	            
	            switch (requestCode) {
	            case TAKE_PHOTO:// 当选择拍照时调用   
	            	// 图片压缩
					int[] screenSize = getScreenSize();
					if (FileUtil.tempToLocalPath(tmpImageName, screenSize[0], screenSize[1])) {
						addNewsImageView(FileUtil.BIG_IMAGE_PATH + tmpImageName);
					}
	                break;
	            case ALBUM_SELECT:// 当选择从本地获取图片时
	                // 做非空判断
	                if (data != null) {
	                	Uri uri = data.getData();
						try {
							ContentResolver cr = this.getContentResolver();
							if (uri.toString().endsWith(".png") || uri.toString().endsWith(".jpg")
									|| "image/jpeg".equals(cr.getType(uri)) || "image/png".equals(cr.getType(uri))) {
								// 压缩存储
								int[] screenSize1 = getScreenSize();
								String fileRealPath = getRealPathFromURI(uri);
								if (fileRealPath != null
										&& FileUtil
												.tempToLocalPath(fileRealPath, tmpImageName, screenSize1[0], screenSize1[1])) {
									addNewsImageView(FileUtil.BIG_IMAGE_PATH + tmpImageName);
								}
							} else {
								
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
	                	
	                }
	                break;
	            }
	        }  
	 }
	
	
	private void addNewsImageView(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			Toast.makeText(this, "文件异常", Toast.LENGTH_SHORT).show();
			return;
		}
		
		int imageCount   = addImageLayout.getChildCount();
		int columnNum    = imageCount%4;
		int lineNum      = imageCount/4;
		View imageViewBack = View.inflate(this, R.layout.attrament_image, null);
		LinearLayout layout = (LinearLayout) imageViewBack.findViewById(R.id.attrament_image_layout);
		ImageView imageView = (ImageView) imageViewBack.findViewById(R.id.image_attrament);
		imageView.setTag(filePath);
		
		//添加按钮
		MarginLayoutParams addlp = (MarginLayoutParams) addImageView.getLayoutParams();
		//布局位置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(addImageView.getWidth(), addImageView.getHeight());
		lp.setMargins(addlp.leftMargin, addlp.topMargin, 0, 0);
		imageView.setLayoutParams(lp);
		//设置照片
		BitmapUtils utils = BitmapManager.getInstance().getHeadPicBitmapUtils(this, R.drawable.abc_ab_bottom_solid_light_holo, false, false);
		utils.display(imageView, filePath);
		layout.removeAllViews();
		addImageLayout.addView(imageView);
		addlp.setMargins(oriMarginLeft+(addImageView.getWidth()+space)*columnNum, (addImageView.getHeight()+10)*lineNum, 0, 0);
		
		//设置点击查看大图事件
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String tmpFilePath = (String) v.getTag();				
				Intent intent = new Intent(PublishNewsActivity.this, BigImgLookActivity.class);
				intent.putExtra(BigImgLookActivity.INTENT_KEY, tmpFilePath);
				startActivityWithBottom(intent);
			}
		});
		
		if (imageCount>9) {
			addImageView.setVisibility(View.GONE);
		}
	}
	
	private String getRealPathFromURI(Uri contentURI) {
		String result;
		Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			result = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_publish_news;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		//设置初始间隔
		MarginLayoutParams oriLp = (MarginLayoutParams) addImageView.getLayoutParams();
		oriMarginLeft = oriLp.leftMargin; 
		space = (getWindowManager().getDefaultDisplay().getWidth()-oriLp.width*4-oriMarginLeft*2)/3;
		LogUtils.i(oriLp.width+" "+oriLp.leftMargin, 1);  
	}

}
