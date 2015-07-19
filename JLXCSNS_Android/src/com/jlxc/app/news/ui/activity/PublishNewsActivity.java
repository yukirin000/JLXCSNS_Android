package com.jlxc.app.news.ui.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.utils.FileUtil;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PublishNewsActivity extends BaseActivityWithTopBar {

	public static final int TAKE_PHOTO = 1;// 拍照
	public static final int ALBUM_SELECT = 2;// 相册选取
	public static final int PHOTO_ZOOM = 3; // 缩放
	public static final int PHOTO_RESOULT = 4;// 结果
	
	public static final int LOCATION_SELECT = 100;// 地理位置返回
	public static final String IMAGE_UNSPECIFIED = "image/*";
		
	//添加附件的layout
	@ViewInject(R.id.addImageLayout)
	private RelativeLayout addImageLayout;
	//添加附件的imageView
	@ViewInject(R.id.addImageView)
	private ImageView addImageView;
	@ViewInject(R.id.contentEt)
	private EditText contentEditText;
	@ViewInject(R.id.choiceLocationBtn)
	private Button choiceLocationBtn;
	
	//起始左边间距
	private int oriMarginLeft;
	//间隔
	private int space;
	//点击加号的image弹窗 
	AlertDialog imageDialog;
	//临时文件名
	String tmpImageName;
	//地点
	String locationString;

	@OnClick(value={R.id.addImageView, R.id.choiceLocationBtn, R.id.base_ll_right_btns})
	private void clickEvent(View view){
		switch (view.getId()) {
		//添加图片点击
		case R.id.addImageView:
			showChoiceImageAlert();
			break;
		//选择地理位置点击
		case R.id.choiceLocationBtn:
			Intent intent = new Intent(this, ChoiceLocationActivity.class);
			startActivityForResult(intent, 1);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			break;
		//发布
		case R.id.base_ll_right_btns:
			publishNews();
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
	        
	        //地理位置
	        if (resultCode == LOCATION_SELECT) {
            	String location = data.getStringExtra("location");
            	if (null != location && !"".equals(location)) {
            		locationString = location;
            		choiceLocationBtn.setText(location);	
				}
			}
	 }
	
	
	private void addNewsImageView(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			Toast.makeText(this, "文件异常", Toast.LENGTH_SHORT).show();
			return;
		}
		
		View imageViewBack = View.inflate(this, R.layout.attrament_image, null);
		LinearLayout layout = (LinearLayout) imageViewBack.findViewById(R.id.attrament_image_layout);
		ImageView imageView = (ImageView) imageViewBack.findViewById(R.id.image_attrament);
		layout.removeAllViews();
		int imageCount   = addImageLayout.getChildCount();
		//移动位置
		moveImageView(imageView, imageCount);
		//添加
		addImageLayout.addView(imageView);
		//设置tag
		imageView.setTag(filePath);
		//设置照片
		BitmapUtils utils = BitmapManager.getInstance().getHeadPicBitmapUtils(this, R.drawable.abc_ab_bottom_solid_light_holo, false, false);
		utils.display(imageView, filePath);
		
		//设置点击查看大图事件
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//相册
				final String tmpFilePath = (String) v.getTag();
				new AlertDialog.Builder(PublishNewsActivity.this).setTitle("操作").setItems(new String[]{"删除","查看大图"}, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						switch (which) {
						case 0:
							//删除
							deleteNewsImageView(tmpFilePath);
							break;
						case 1:
							//查看大图				
							Intent intent = new Intent(PublishNewsActivity.this, BigImgLookActivity.class);
							intent.putExtra(BigImgLookActivity.INTENT_KEY, tmpFilePath);
							startActivityWithBottom(intent);
							break;
						default: 
							break;
						}					
					}
				}).setNegativeButton("取消", null).show();
			}
		});
		
	}
	
	//删除状态图片
	private void deleteNewsImageView(String tag) {
		//删除
		int subviewsCount = addImageLayout.getChildCount();
		for (int i = 0; i < subviewsCount; i++) {
			View view = addImageLayout.getChildAt(i);
			if (null != view.getTag() && view.getTag().equals(tag)) {
				addImageLayout.removeViewAt(i);
				break;
			}
		}
		
		//添加按钮位置重置
		MarginLayoutParams addlp = (MarginLayoutParams) addImageView.getLayoutParams();
		addlp.setMargins(oriMarginLeft, 0, 0, 0);
		//删除之后重新排序
		subviewsCount = addImageLayout.getChildCount();
		for (int i = 1; i < subviewsCount; i++) {
			View view = addImageLayout.getChildAt(i);
			moveImageView((ImageView)view, i);
		}
		
	}
	
	//移动位置
	private void moveImageView(ImageView imageView, int imageCount) {
		
		int columnNum    = imageCount%4;
		int lineNum      = imageCount/4;
		//添加按钮位置
		MarginLayoutParams addlp = (MarginLayoutParams) addImageView.getLayoutParams();
		//布局位置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(addImageView.getWidth(), addImageView.getHeight());
		lp.setMargins(addlp.leftMargin, addlp.topMargin, 0, 0);
		imageView.setLayoutParams(lp);
		addlp.setMargins(oriMarginLeft+(addImageView.getWidth()+space)*columnNum, (addImageView.getHeight()+10)*lineNum, 0, 0);
		if (imageCount>8) {
			addImageView.setVisibility(View.GONE);
		}else {
			addImageView.setVisibility(View.VISIBLE);
		}
	}
	
	//发布动态
	private void publishNews() {
		
		if ("".equals(contentEditText.getText().toString()) && addImageLayout.getChildCount()==1) {
			ToastUtil.show(this, "内容和图片至少有一个不能为空=_=");
			return;
		}
		
		if (contentEditText.getText().toString().length() > 140) {
			ToastUtil.show(this, "内容不能超过140字=_=");
			return;
		}
		
		final UserModel userModel = UserManager.getInstance().getUser();
		showLoading("发布中，请稍候...", false);
		RequestParams params = new RequestParams();		
		//用户id
		params.addBodyParameter("uid", userModel.getUid()+"");
//		params.addBodyParameter("uid", "1");
		//内容
		params.addBodyParameter("content_text", contentEditText.getText().toString());
		//location
		params.addBodyParameter("location", locationString);
		
		//图片
		for (int i = 0; i < addImageLayout.getChildCount(); i++) {
			View view = addImageLayout.getChildAt(i);
			//如果不是添加按钮
			if (view != addImageView) {
				//图片
				File file = new File((String) view.getTag());
				if (file.exists()) {
					params.addBodyParameter("image"+i, file);	
				}
			}
		}
				
		//姓名
		HttpManager.post(JLXCConst.PUBLISH_NEWS, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
					
					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						hideLoading();
						int status = jsonResponse.getIntValue("status");
						switch (status) {
						case JLXCConst.STATUS_SUCCESS:							
							//toast
							ToastUtil.show(PublishNewsActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
							hideLoading();
							finishWithRight();
							break;
						case JLXCConst.STATUS_FAIL:
							hideLoading();
							Toast.makeText(PublishNewsActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE), Toast.LENGTH_SHORT).show();
							break;
						}
					}
					
					@Override
					public void onFailure(HttpException arg0, String arg1, String flag) {
						LogUtils.i(arg0.getMessage(), 1);
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						Toast.makeText(PublishNewsActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
					}
		 }, null));
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

	@SuppressWarnings("deprecation")
	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		//设置初始间隔
		MarginLayoutParams oriLp = (MarginLayoutParams) addImageView.getLayoutParams();
		oriMarginLeft = oriLp.leftMargin; 
		space = (getWindowManager().getDefaultDisplay().getWidth()-oriLp.width*4-oriMarginLeft*2)/3;
		addRightBtn("发布");
		locationString = "";
	}

}
