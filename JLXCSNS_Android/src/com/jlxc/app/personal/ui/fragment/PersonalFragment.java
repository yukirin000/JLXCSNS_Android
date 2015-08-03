package com.jlxc.app.personal.ui.fragment;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.TuSdkResult;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.impl.activity.TuFragment;
import org.lasque.tusdk.impl.components.TuEditComponent;
import org.lasque.tusdk.impl.components.base.TuSdkComponent.TuSdkComponentDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.FileUtil;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.discovery.ui.avtivity.QRCodeScanActivity;
import com.jlxc.app.login.ui.activity.SelectSchoolActivity;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.personal.model.CityModel;
import com.jlxc.app.personal.model.ProvinceModel;
import com.jlxc.app.personal.ui.activity.AccountSettingActivity;
import com.jlxc.app.personal.ui.activity.ContactFriendListActivity;
import com.jlxc.app.personal.ui.activity.FriendListActivity;
import com.jlxc.app.personal.ui.activity.MyCardActivity;
import com.jlxc.app.personal.ui.activity.MyFriendListActivity;
import com.jlxc.app.personal.ui.activity.MyNewsListActivity;
import com.jlxc.app.personal.ui.activity.PersonalSettingActivity;
import com.jlxc.app.personal.ui.activity.PersonalSignActivity;
import com.jlxc.app.personal.ui.activity.VisitListActivity;
import com.jlxc.app.personal.ui.view.cityView.OnWheelChangedListener;
import com.jlxc.app.personal.ui.view.cityView.WheelView;
import com.jlxc.app.personal.ui.view.cityView.adapters.ArrayWheelAdapter;
import com.jlxc.app.personal.utils.XmlParserHandler;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.core.BitmapCache;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonalFragment extends BaseFragment {

	public static final int TAKE_PHOTO = 1;// 拍照
	public static final int ALBUM_SELECT = 2;// 相册选取
	public static final int PHOTO_ZOOM = 3;// 缩放
	public static final int PHOTO_RESOULT = 4;// 结果
	public static final int SIGN_RESOULT = 100;// 签名添加返回
	public static final String IMAGE_UNSPECIFIED = "image/*";
	private String tmpImageName = "";//临时文件名
	
	public static final int HEAD_IMAGE = 1;// 头像
	public static final int BACK_IMAGE = 2;// 背景
	private int imageType;//点击的图片类型
	
	//省份数组
	protected String[] mProvinceDatas;
	//城市map
	protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	//当前省份名
	protected String mCurrentProviceName;
	// 当前城市名
	protected String mCurrentCityName;
	private WheelView mViewProvince;
	private WheelView mViewCity;
	private Builder cityBuilder;
	private LinearLayout linearLayout;
	
	//背景图
	@ViewInject(R.id.back_image_View)
	private ImageView backImageView;
	//头像
	@ViewInject(R.id.head_image_view)
	private ImageView headImageView;	
	//顶部姓名
	@ViewInject(R.id.top_name_text_view)
	private TextView topNameTextView;	
	//顶部学校
	@ViewInject(R.id.top_school_text_view)
	private TextView topSchoolTextView;	
	//我的相片grid
	@ViewInject(R.id.my_image_grid_view)
	private GridView myImageGridView;
	//我的相片数量
	@ViewInject(R.id.my_image_count_text_view)
	private TextView myImageCountTextView;
	//最近来访grid
	@ViewInject(R.id.visit_grid_view)
	private GridView visitGridView;
	//最近来访数量
	@ViewInject(R.id.visit_count_text_view)
	private TextView visitCountTextView;	
	//我的好友grid
	@ViewInject(R.id.friend_grid_view)
	private GridView friendsGridView;
	//我的好友数量
	@ViewInject(R.id.friend_count_text_view)
	private TextView friendsCountTextView;
	//姓名
	@ViewInject(R.id.name_text_view)
	private TextView nameTextView;
	//签名
	@ViewInject(R.id.sign_text_view)
	private TextView signTextView;
	//学校
	@ViewInject(R.id.school_text_view)
	private TextView schoolTextView;
	//性别
	@ViewInject(R.id.sex_text_view)
	private TextView sexTextView;
	//性别图片
	@ViewInject(R.id.sex_image_view)
	private ImageView sexImageView;	
	//生日
	@ViewInject(R.id.birth_text_view)
	private TextView birthTextView;
	//城市
	@ViewInject(R.id.city_text_view)
	private TextView cityTextView;
	
	//用户模型
	private UserModel userModel;
	//单例bitmapUtils的引用
	private BitmapUtils bitmapUtils;
	//无缓存的
//	private BitmapUtils noCacheBitmapUtils;
	//我的相片adapter
	private HelloHaAdapter<String> myImageAdapter;
	//最近来访adapter
	private HelloHaAdapter<String> visitAdapter;
	//好友adapter
	private HelloHaAdapter<IMModel> friendsAdapter;	
	
    @OnClick(value={R.id.name_layout,R.id.sign_layout,R.id.birth_layout,R.id.sex_layout,
			R.id.school_layout,R.id.city_layout, R.id.head_image_view, R.id.back_click_layout,R.id.my_image_layout
			,R.id.visit_layout, R.id.friend_layout, R.id.setting_Button, R.id.card_Button})
	private void clickEvent(View view){
		switch (view.getId()) {
		//姓名
		case R.id.name_layout:
			nameClick();
			break;
		//签名
		case R.id.sign_layout:
			Intent intent = new Intent(getActivity(), PersonalSignActivity.class);
			startActivityForResult(intent, 1);
			getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			break;
		//生日
		case R.id.birth_layout:
			birthClick();
			break;
		//性别
		case R.id.sex_layout:
			sexClick();
			break;
		//学校
		case R.id.school_layout:
			Intent schoolIntent = new Intent(getActivity(), SelectSchoolActivity.class);
			schoolIntent.putExtra("notRegister", true);
			startActivity(schoolIntent);
			break;
		//城市
		case R.id.city_layout:
			linearLayout = (LinearLayout) View.inflate(getActivity(), R.layout.wheel, null);
			mViewProvince = (WheelView) linearLayout.findViewById(R.id.id_province);
			mViewCity = (WheelView) linearLayout.findViewById(R.id.id_city);
			cityBuilder.setView(linearLayout);
			setUpListener();
			setUpData();
			cityBuilder.show();
			break;
		//头像点击
		case R.id.head_image_view:
			//拍照
			//dialog
		 	Builder headAlertDialog = new AlertDialog.Builder(getActivity()).setNegativeButton("取消", null).setTitle("修改头像");
		 	String[] headStrings = new String[]{"拍照","相册"}; 
		 	headAlertDialog.setItems(headStrings, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//设置为头像
					imageType = HEAD_IMAGE;
					if (which == 0) {
						//相机
						Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						File tmpFile = new File(FileUtil.TEMP_PATH+tmpImageName);
						intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
				                Uri.fromFile(tmpFile));
						startActivityForResult(intentCamera, TAKE_PHOTO);
					}else {
						//相册
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						Intent intentAlbum = new Intent(Intent.ACTION_GET_CONTENT);
						intentAlbum.setType(IMAGE_UNSPECIFIED);
						startActivityForResult(intentAlbum, ALBUM_SELECT);
						
					}
				}
			});
		 	headAlertDialog.show();
			break;
			//背景点击
		case R.id.back_click_layout:
			//拍照
			//dialog
		 	Builder backAlertDialog = new AlertDialog.Builder(getActivity()).setNegativeButton("取消", null).setTitle("修改背景");
		 	String[] backStrings = new String[]{"拍照","相册"}; 
		 	backAlertDialog.setItems(backStrings, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//设置为头像
					imageType = BACK_IMAGE;
					if (which == 0) {
						//相机
						Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						File tmpFile = new File(FileUtil.TEMP_PATH+tmpImageName);
						intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
				                Uri.fromFile(tmpFile));
						startActivityForResult(intentCamera, TAKE_PHOTO);
					}else {
						//相册
						tmpImageName = JLXCUtils.getPhotoFileName()+"";
						Intent intentAlbum = new Intent(Intent.ACTION_GET_CONTENT);
						intentAlbum.setType(IMAGE_UNSPECIFIED);
						startActivityForResult(intentAlbum, ALBUM_SELECT);
					}
				}
			});
		 	backAlertDialog.show();
			break;	
		case R.id.my_image_layout:
			//图片点击
			Intent myImageIntent = new Intent(getActivity(), MyNewsListActivity.class);
			myImageIntent.putExtra(MyNewsListActivity.INTNET_KEY_UID, userModel.getUid()+"");
			startActivityWithRight(myImageIntent);
			break;
		case R.id.visit_layout:
			//最近来访点击
			Intent visitIntent = new Intent(getActivity(), VisitListActivity.class);
			visitIntent.putExtra(VisitListActivity.INTENT_KEY, userModel.getUid());
			startActivityWithRight(visitIntent);
			break;
		case R.id.friend_layout:
			//我的好友列表
			Intent friendIntent = new Intent(getActivity(), MyFriendListActivity.class);
			startActivityWithRight(friendIntent);
			break;
		case R.id.setting_Button:
			//设置
			Intent setIntent = new Intent(getActivity(), PersonalSettingActivity.class);
			startActivityWithRight(setIntent);
			break;
		case R.id.card_Button:
			//名片点击
			Intent cardIntent = new Intent(getActivity(), MyCardActivity.class);
			startActivityWithRight(cardIntent);
			break;
		default:
			break;
		}
	}
	
	//////////////////////////////////life cycle/////////////////////////////////
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.fragment_personal;
	}

	@Override
	public void setUpViews(View rootView) {
		
		//初始化adapter
		myImageAdapter = initAdapter(R.layout.my_image_adapter);
		visitAdapter = initAdapter(R.layout.attrament_image);
		friendsAdapter = new HelloHaAdapter<IMModel>(getActivity(), R.layout.attrament_image) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, IMModel item) {
				ImageView imageView = helper.getView(R.id.image_attrament);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item.getAvatarPath());
			}
		};
		
		//设置adapter
		myImageGridView.setAdapter(myImageAdapter);
		visitGridView.setAdapter(visitAdapter);
		friendsGridView.setAdapter(friendsAdapter);
		//不能点击
		myImageGridView.setEnabled(false);
		visitGridView.setEnabled(false);
		friendsGridView.setEnabled(false);
		
		userModel = UserManager.getInstance().getUser(); 
		
		//签名因为要跳到领一个页面 所以在只初始化一次
		if (null == userModel.getSign() || "".equals(userModel.getSign())) {
			signTextView.setText("暂无");
		}else {
			signTextView.setText(userModel.getSign());  
		}
		
		//设置照片和背景图
		bitmapUtils = BitmapManager.getInstance().getHeadPicBitmapUtils(getActivity(), R.drawable.ic_launcher, true, true);
		//无缓存bitmap
//		noCacheBitmapUtils = BitmapManager.getInstance().getHeadPicBitmapUtils(getActivity(), R.drawable.ic_launcher, false, false);
		//解析省份城市xml
		initProvinceDatas();
		cityBuilder = new AlertDialog.Builder(getActivity());
		cityBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cityTextView.setText(mCurrentProviceName+mCurrentCityName);
				
				uploadInformation("city", mCurrentProviceName+","+mCurrentCityName);
			}
		});
		cityBuilder.setNegativeButton("取消", null);
		
	}

	@Override
	public void loadLayout(View rootView) {
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//头像 2015-07-07/01436273216_sub.jpg
		bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+userModel.getHead_image());
		//背景 2015-07-02/191435808476.png
		bitmapUtils.display(backImageView, JLXCConst.ATTACHMENT_ADDR+userModel.getBackground_image());		
		//姓名
		if (null == userModel.getName() || "".equals(userModel.getName())) {
			nameTextView.setText("暂无");
		}else {
			nameTextView.setText(userModel.getName());
			//顶部姓名
			topNameTextView.setText(userModel.getName());
		}
//		//签名 签名不能放在这里更新
//		if (null == userModel.getSign() || "".equals(userModel.getSign())) {
//			signTextView.setText("暂无");
//		}else {
//			signTextView.setText(userModel.getSign());
//		}		
		//生日
		if (null == userModel.getBirthday() || "".equals(userModel.getBirthday())) {
			birthTextView.setText("暂无");
		}else {
			birthTextView.setText(userModel.getBirthday());
		}
		//性别
		if (userModel.getSex() == 0) {
			sexTextView.setText("男孩纸");
			sexImageView.setImageResource(R.drawable.sex_boy);
		}else {
			sexTextView.setText("女孩纸");
			sexImageView.setImageResource(R.drawable.sex_girl);
		}
		//学校字符串
		String schoolString = "";
		//城市字符串
//		String cityString = "";		
		//学校
		if (null == userModel.getSchool() || "".equals(userModel.getSchool())) {
			schoolTextView.setText("暂无");
		}else {
			schoolTextView.setText(userModel.getSchool());
			schoolString = userModel.getSchool();
		}
		//城市
		if (null == userModel.getCity() || "".equals(userModel.getCity())) {
			cityTextView.setText("暂无");
		}else {
			cityTextView.setText(userModel.getCity());
//			cityString = userModel.getCity();
		}
		
		//顶部学校
//		String topSchoolString = "";
//		if (cityString.length()>0) {
//			topSchoolString = cityString+","+schoolString;
//		}else {
//			topSchoolString = schoolString;
//		}
		
		//顶部学校tv 暂时只有学校
		topSchoolTextView.setText(schoolString);
		
	    //获取当前最近的三张状态图片
		getNewsImages();
		getVisitImages();
		getFriendsImages();
//		//好友
//		List<IMModel> listModels = IMModel.findHasAddAll();
//		List<IMModel> userModels = new ArrayList<IMModel>();
//		//取前三个
//		for (int i = 0; i < listModels.size(); i++) {
//			if (i==3) {
//				break;
//			}
//			userModels.add(listModels.get(i));
//		}
//		friendsAdapter.replaceAll(userModels);
//		if (listModels.size() > 0) {
//			friendsCountTextView.setText(""+listModels.size());
//		}else {
//			friendsCountTextView.setText("");
//		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		LogUtils.i("test11"+" "+ requestCode, 1);
		//图片返回值
		if (resultCode == Activity.RESULT_OK) {  
            String sdStatus = Environment.getExternalStorageState();  
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            	LogUtils.i("SD card is not avaiable/writeable right now.", 1);
                return;  
            }  
        	//头像需要缩放	            
            switch (requestCode) {
            case TAKE_PHOTO:// 当选择拍照时调用   
            	//头像 获取剪切
            	if (imageType == HEAD_IMAGE) {
            		File tmpFile = new File(FileUtil.TEMP_PATH+tmpImageName);
            		startPhotoZoom(Uri.fromFile(tmpFile));	
				}else {
						
					// 图片压缩 
					int[] screenSize = getScreenSize();
					if (FileUtil.tempToLocalPath(tmpImageName, screenSize[0], screenSize[1])) {
						filterImage(FileUtil.BIG_IMAGE_PATH+tmpImageName);
					}	 
				}
            	
                break;
            case ALBUM_SELECT:// 当选择从本地获取图片时
                // 做非空判断，当我们觉得不满意想重新剪裁的时候便不会报异常，下同
                if (data != null) {
                	//头像 获取剪切
                	if (imageType == HEAD_IMAGE) {
                		startPhotoZoom(data.getData());	
                	}else {
                		String path = getRealPathFromURI(data.getData());
                		// 图片压缩
    					int[] screenSize1 = getScreenSize();
    					if (FileUtil.tempToLocalPath(path, tmpImageName, screenSize1[0], screenSize1[1])) {
//    						bitmapUtils.display(backImageView, FileUtil.BIG_IMAGE_PATH + tmpImageName);
    						filterImage(FileUtil.BIG_IMAGE_PATH+tmpImageName);
						}

					}
                }
                break;
            case PHOTO_RESOULT:// 返回的结果
                if (data != null){
                	
                	if (null != tmpImageName) {
        				bitmapUtils.display(headImageView, FileUtil.HEAD_PIC_PATH+tmpImageName);
	    				//删除临时文件
	        			File file = new File(FileUtil.TEMP_PATH+tmpImageName);
	    				if (file.exists()) {
	    					file.delete(); 
	    				}	
	    				filterImage(FileUtil.HEAD_PIC_PATH+tmpImageName);
        			}
                	
//                	Bundle bundle = data.getExtras();
//        			Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
//        			if (null != bitmap && tmpImageName != null) {
//        				FileUtil.savePic(Bitmap.createBitmap(bitmap), FileUtil.HEAD_PIC_PATH, tmpImageName, 100);
//        				//删除临时文件
//	        			File file = new File(FileUtil.TEMP_PATH+tmpImageName);
//	    				if (file.exists()) {
//	    					file.delete(); 
//	    				}	
//        				filterImage(FileUtil.HEAD_PIC_PATH+tmpImageName);
//        			}else {
//            			if (null != tmpImageName) {
//            				bitmapUtils.display(headImageView, FileUtil.HEAD_PIC_PATH+tmpImageName);
//    	    				//删除临时文件
//    	        			File file = new File(FileUtil.TEMP_PATH+tmpImageName);
//    	    				if (file.exists()) {
//    	    					file.delete(); 
//    	    				}	
//    	    				filterImage(FileUtil.HEAD_PIC_PATH+tmpImageName);
//            			}
//					}
                	// 图片裁切后的结果
                	//FileUtil.HEAD_PIC_PATH+tmpImageName
//                	Bitmap bitmap = BitmapFactory.decodeFile(FileUtil.HEAD_PIC_PATH+tmpImageName);
//        			headImageView.setImageBitmap(bitmap);// 将图片显示在ImageView里
                }
                break;
            }
        }else if (resultCode == SIGN_RESOULT) {
        	//签名返回
        	String signString = data.getStringExtra("sign");
			signTextView.setText(signString);
			uploadInformation("sign", signString);
		}else {
			File file = new File(FileUtil.TEMP_PATH+tmpImageName);
			if (file.exists()) {
				file.delete(); 
			}	
			tmpImageName = "";
		}  
		
	}
	
	
	//////////////////////private method////////////////////////
	//开启缩放
	 public void startPhotoZoom(Uri uri) {
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
			intent.putExtra("crop", "true");
			// aspectX aspectY 是宽高的比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			
			intent.putExtra("outputX", 960);
			intent.putExtra("outputY", 960);
			
			intent.putExtra("scaleUpIfNeeded", true);//黑边
			intent.putExtra("scale", true);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			intent.putExtra("noFaceDetection", true); // no face detection
//			intent.putExtra("return-data", true);
			
			File headDir = new File(FileUtil.HEAD_PIC_PATH);
			if (!headDir.exists()) {
				headDir.mkdir();
			}
			File tmpFile = new File(FileUtil.HEAD_PIC_PATH+tmpImageName);
			//地址
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
			startActivityForResult(intent, PHOTO_RESOULT);
	 }
	
	//姓名点击
	private void nameClick() {
		
		//dialog
	 	Builder nameAlertDialog = new AlertDialog.Builder(getActivity()).setNegativeButton("取消", null).setTitle("修改昵称");
	 	LinearLayout textViewLayout = (LinearLayout) View.inflate(getActivity(), R.layout.dialog_text_view, null);
	 	nameAlertDialog.setView(textViewLayout);
	 	final EditText et_search = (EditText)textViewLayout.findViewById(R.id.searchC);
	 	et_search.setText(userModel.getName());
	 	//设置确定
	 	nameAlertDialog.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = et_search.getText().toString();
				if (name.length()<1) {
					ToastUtil.show(getActivity(), "昵称不能为空");
		            return ;
		        }
				if (name.length()>8) {
					ToastUtil.show(getActivity(), "昵称不能超过八个字");
		            return ;
		        }
				uploadInformation("name", et_search.getText().toString());
				nameTextView.setText(name);
			}
		});
	 	
	 	nameAlertDialog.show();
	}
	
	@SuppressLint("NewApi") private void birthClick() {
		DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				
			}
		}, 2000, 0, 0);
		//取消按钮
		datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",new DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            	LogUtils.i("cancel~~cancel~~",1);  
            }  
        });
		//确定按钮
		final DatePicker picker = datePickerDialog.getDatePicker();
		datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定",new DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {
            	
            	LogUtils.i("ok~~ok~~",1);
            	String date = picker.getYear()+"-"+picker.getMonth()+"-"+picker.getDayOfMonth();
            	LogUtils.i(date, 1);
            	birthTextView.setText(date);
            	uploadInformation("birthday", date);
            }  
        });
		datePickerDialog.show();	
	}
	
	//姓名点击
	private void sexClick() {
		
		//dialog
	 	Builder nameAlertDialog = new AlertDialog.Builder(getActivity()).setNegativeButton("取消", null).setTitle("选择性别");
	 	String[] sexStrings = new String[]{"男孩纸","女孩纸"}; 
	 	nameAlertDialog.setItems(sexStrings, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					sexTextView.setText("男孩纸");
					sexImageView.setImageResource(R.drawable.sex_boy);
				}else {
					sexTextView.setText("女孩纸");
					sexImageView.setImageResource(R.drawable.sex_girl);
				}
				uploadInformation("sex", ""+which);
			}
		});
	 	nameAlertDialog.show();
	}
	
	
	//初始化adapter
	private HelloHaAdapter<String> initAdapter(int id){
		
		HelloHaAdapter<String> adapter = new HelloHaAdapter<String>(getActivity(), id) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, String item) {
				ImageView imageView = helper.getView(R.id.image_attrament);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item);
			}
		};
		return adapter;
	}
	
    //获取当前最近的三张状态图片
	private void getNewsImages(){
		
		String path = JLXCConst.GET_NEWS_IMAGES+"?"+"uid="+UserManager.getInstance().getUser().getUid();
		
		HttpManager.get(path,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							//数据处理
							JSONArray array = jResult.getJSONArray(JLXCConst.HTTP_LIST);
							if (null !=array && array.size() < 1) {
								myImageAdapter.clear();	
							}
							List<String> imageList = new ArrayList<String>();
							for (int i = 0; i < array.size(); i++) {
								JSONObject object = (JSONObject) array.get(i);
								imageList.add(object.getString("sub_url"));
							}
							myImageAdapter.replaceAll(imageList);
						}

						if (status == JLXCConst.STATUS_FAIL) {
//							ToastUtil.show(getActivity(), jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
							myImageAdapter.clear();
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(getActivity(), "网络有毒=_=");
					}

				}, null));
		
	}
	
    //获取当前最近的三张最近来访人的头像	
	private void getVisitImages(){
		
		String path = JLXCConst.GET_VISIT_IMAGES+"?"+"uid="+UserManager.getInstance().getUser().getUid();
		HttpManager.get(path,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							//数据处理
							JSONArray array = jResult.getJSONArray(JLXCConst.HTTP_LIST);
							List<String> headImageList = new ArrayList<String>();
							if (null !=array && array.size() < 1) {
								visitAdapter.clear();	
							}
							for (int i = 0; i < array.size(); i++) {
								JSONObject object = (JSONObject) array.get(i);
								headImageList.add(object.getString("head_sub_image"));
							}
							visitAdapter.replaceAll(headImageList);
							//人数
							int visitCount = jResult.getIntValue("visit_count");
							if (visitCount > 0) {
								visitCountTextView.setText(visitCount+"人");
							}else {
								visitCountTextView.setText("");
							}
						}

						if (status == JLXCConst.STATUS_FAIL) {
//							ToastUtil.show(getActivity(), jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
							visitAdapter.clear();
							visitCountTextView.setText("");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(getActivity(), "网络有毒=_=");
					}

				}, null));
	}
	
	//获取最近关注的三个人的头像	
	private void getFriendsImages(){
		
		String path = JLXCConst.GET_FRIENDS_IMAGE+"?"+"user_id="+UserManager.getInstance().getUser().getUid();
		HttpManager.get(path, new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							JSONObject jResult = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							//数据处理
							JSONArray array = jResult.getJSONArray(JLXCConst.HTTP_LIST);
							List<IMModel> headImageList = new ArrayList<IMModel>();
							friendsAdapter.clear();
							for (int i = 0; i < array.size(); i++) {
								JSONObject object = (JSONObject) array.get(i);
								IMModel imModel = new IMModel();
								imModel.setAvatarPath(object.getString("head_sub_image"));
								headImageList.add(imModel);
							}
							friendsAdapter.replaceAll(headImageList);
							//人数
							int friendCount = jResult.getIntValue("friend_count");
							if (friendCount > 0) {
								friendsCountTextView.setText(friendCount+"人");
							}else {
								friendsCountTextView.setText("");
							}
							
						}

						if (status == JLXCConst.STATUS_FAIL) {
							friendsAdapter.clear();
							friendsCountTextView.setText("");
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(getActivity(), "网络有毒=_=");
					}

				}, null));
	}
	
	
	private void uploadInformation(final String field,final String value) {
		
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", userModel.getUid() + "");
		params.addBodyParameter("field", field);
		params.addBodyParameter("value", value);
		
		HttpManager.post(JLXCConst.CHANGE_PERSONAL_INFORMATION, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							//设置数据 
							if ("name".equals(field)) {
								//姓名
								userModel.setName(value);
							}else if ("sign".equals(field)) {
								//签名
								userModel.setSign(value);	
								signTextView.setText(value);
							}else if ("birthday".equals(field)) {
								//生日
								userModel.setBirthday(value);
							}else if ("sex".equals(field)) {
								//性别
								userModel.setSex(Integer.parseInt(value));			
							}else if ("city".equals(field)) {
								//城市
								userModel.setCity(value);			
							}
							//本地持久化
							UserManager.getInstance().saveAndUpdate();
						}
						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(getActivity(),
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(getActivity(),
								"网络异常");
					}
				}, null));
		
//		 Class clazz = userModel.getClass(); 不使用反射 可原始处理 
//	     Method m = clazz.getDeclaredMethod("setMsg", String.class);
//	     m.invoke(userModel, "重新设置msg信息！"); 
	}
	
	//图片滤镜
	private void filterImage(final String path) {
		File filterFile = new File(path);
		// 组件委托
		TuSdkComponentDelegate delegate = new TuSdkComponentDelegate()
		{
			@Override
			public void onComponentFinished(TuSdkResult result, Error error,
					TuFragment lastFragment)
			{
				File oriFile = result.imageFile;
				File newFile = new File(path);
				boolean filterOK = oriFile.renameTo(newFile);
				if (filterOK) {
					uploadImage(path);
				}else {
					ToastUtil.show(getActivity(), "图片处理失败T_T");
				}
			}
		};
		
		TuEditComponent component = TuSdk.editCommponent(getActivity(),delegate);
		component.componentOption().editEntryOption().setEnableCuter(false);
		component.componentOption().editEntryOption().setEnableSticker(false);
		component.componentOption().editEntryOption().setSaveToAlbum(false);
		component.componentOption().editEntryOption().setAutoRemoveTemp(false);
		component.componentOption().editEntryOption().setSaveToTemp(true);
		component.componentOption().editEntryOption().setOutputCompress(80);
		
		TuSdkResult result = new TuSdkResult();
		result.imageFile = filterFile;
		
		// 设置图片
		component.setImage(result.image)
		// 设置系统照片
				.setImageSqlInfo(result.imageSqlInfo)
				// 设置临时文件
				.setTempFilePath(result.imageFile)
				// 在组件执行完成后自动关闭组件
				.setAutoDismissWhenCompleted(true)
				// 开启组件
				.showComponent();
		
	}
	
	//上传头像
	private void uploadImage(final String path) {
		
		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("uid", userModel.getUid() + "");
		File uplodaFile = new File(path);
		if (!uplodaFile.exists()) {
			return;
		}
		params.addBodyParameter("image", uplodaFile);
		//类型
		if (imageType == HEAD_IMAGE) {
			params.addBodyParameter("field", "head_image");
		}else {
			params.addBodyParameter("field", "background_image");
		}
		
		showLoading(getActivity(), "上传中^_^");
		
		HttpManager.post(JLXCConst.CHANGE_INFORMATION_IMAGE, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						hideLoading();
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							String serverPath = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT).getString("image");
							if (imageType == HEAD_IMAGE) {
								//头像有缩略图
								String subPath = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT).getString("subimage");
								userModel.setHead_image(serverPath);
								userModel.setHead_sub_image(subPath);
								bitmapUtils.display(headImageView, FileUtil.HEAD_PIC_PATH+tmpImageName);
								bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+serverPath);
								//刷新信息								
								UserInfo userInfo = new UserInfo(JLXCConst.JLXC+userModel.getUid(), userModel.getName(), Uri.parse(JLXCConst.ATTACHMENT_ADDR+serverPath));
								RongIM.getInstance().refreshUserInfoCache(userInfo);
							}else {
								userModel.setBackground_image(serverPath);
								bitmapUtils.display(backImageView, FileUtil.BIG_IMAGE_PATH+tmpImageName);
								bitmapUtils.display(backImageView, JLXCConst.ATTACHMENT_ADDR+serverPath);
							}
							ToastUtil.show(getActivity(),jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
							//本地持久化
							UserManager.getInstance().saveAndUpdate();
							//删除临时文件
							File tmpFile =new File(path);
							if (tmpFile.exists()) {
								tmpFile.delete();
							}
							tmpImageName = "";
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(getActivity(),
									jsonResponse
											.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						hideLoading();
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(getActivity(),
								"网络异常");
					}
				}, null));
		
		
//		 Class clazz = userModel.getClass(); 不使用反射 可原始处理 
//	     Method m = clazz.getDeclaredMethod("setMsg", String.class);
//	     m.invoke(userModel, "重新设置msg信息！"); 
		
	}
	
	
	private String getRealPathFromURI(Uri contentURI) {
		String result;
		Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
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
	
	
	/**
	 * XML解析
	 */
	
    protected void initProvinceDatas()
	{
		List<ProvinceModel> provinceList = null;
    	AssetManager asset = getActivity().getAssets();
        try {
        	//解析
            InputStream input = asset.open("province_data.xml");
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser = spf.newSAXParser();
			XmlParserHandler handler = new XmlParserHandler();
			parser.parse(input, handler);
			input.close();
			provinceList = handler.getDataList();
			if (provinceList!= null && !provinceList.isEmpty()) {
				mCurrentProviceName = provinceList.get(0).getName();
				List<CityModel> cityList = provinceList.get(0).getCityList();
				if (cityList!= null && !cityList.isEmpty()) {
					mCurrentCityName = cityList.get(0).getName();
				}
			}
			mProvinceDatas = new String[provinceList.size()];
        	for (int i=0; i< provinceList.size(); i++) {
        		mProvinceDatas[i] = provinceList.get(i).getName();
        		List<CityModel> cityList = provinceList.get(i).getCityList();
        		String[] cityNames = new String[cityList.size()];
        		for (int j=0; j< cityList.size(); j++) {
        			cityNames[j] = cityList.get(j).getName();
        		}
        		mCitisDatasMap.put(provinceList.get(i).getName(), cityNames);
        	}
        	
        } catch (Throwable e) {  
            e.printStackTrace();  
        } finally {
        	
        } 
	}
    
    private void setUpListener() {
    	// 省份改变
    	mViewProvince.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				updateCities();
			}
		});
    	// 城市改变
    	mViewCity.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				updateAreas();
			}
		});
    }
	
	private void setUpData() {
		initProvinceDatas();
		mViewProvince.setViewAdapter(new ArrayWheelAdapter<String>(getActivity(), mProvinceDatas));
		mViewProvince.setVisibleItems(7);
		mViewCity.setVisibleItems(7);
		updateCities();
		updateAreas();
	}

	/**
	 * WheelView滚动省份变化
	 */
	private void updateAreas() {
		int pCurrent = mViewCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
	}

	/**
	 * WheelView滚动城市变化
	 */
	private void updateCities() {
		int pCurrent = mViewProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[pCurrent];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null) {
			cities = new String[] { "" }; 
		}
		mViewCity.setViewAdapter(new ArrayWheelAdapter<String>(getActivity(), cities));
		mViewCity.setCurrentItem(0);
		updateAreas(); 
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub 
		super.onSaveInstanceState(outState);
//		outState.putSerializable("user", userModel);
		outState.putSerializable("imageType", imageType);
		outState.putSerializable("tmpImageName", tmpImageName);  
	}
	
	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewStateRestored(savedInstanceState);
		
		if (null != savedInstanceState) {
			imageType = savedInstanceState.getInt("imageType");
			tmpImageName = savedInstanceState.getString("tmpImageName");
		}
	}
    
	////////////////////////getter setter/////////////////////
	public HelloHaAdapter<String> getMyImageAdapter() {
		return myImageAdapter;
	}

	public void setMyImageAdapter(HelloHaAdapter<String> myImageAdapter) {
		this.myImageAdapter = myImageAdapter;
	}

	public HelloHaAdapter<String> getVisitAdapter() {
		return visitAdapter;
	}

	public void setVisitAdapter(HelloHaAdapter<String> visitAdapter) {
		this.visitAdapter = visitAdapter;
	}

	public HelloHaAdapter<IMModel> getFriendsAdapter() {
		return friendsAdapter;
	}

	public void setFriendsAdapter(HelloHaAdapter<IMModel> friendsAdapter) {
		this.friendsAdapter = friendsAdapter;
	}

	public String getTmpImageName() {
		return tmpImageName;
	}

	public void setTmpImageName(String tmpImageName) {
		this.tmpImageName = tmpImageName;
	}
}
