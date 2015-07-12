package com.jlxc.app.personal.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
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
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.ui.activity.BigImgLookActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class OtherPersonalActivity extends BaseActivity{

	public final static String INTENT_KEY = "uid";
	
	//背景图
	@ViewInject(R.id.back_image_View)
	private ImageView backImageView;
	//头像
	@ViewInject(R.id.head_image_view)
	private ImageView headImageView;	
	//TA的相片grid
	@ViewInject(R.id.his_image_grid_view)
	private GridView hisImageGridView;
	//TA的好友grid
	@ViewInject(R.id.his_friend_grid_view)
	private GridView hisFriendsGridView;
	//TA的好友数量
	@ViewInject(R.id.his_friend_count_text_view)
	private TextView hisFriendsCountTextView;
	
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
	//生日
	@ViewInject(R.id.birth_text_view)
	private TextView birthTextView;
	//城市
	@ViewInject(R.id.city_text_view)
	private TextView cityTextView;
	
	//发消息layout
	@ViewInject(R.id.send_message_layout)
	private LinearLayout sendMessageLayout;
	//发消息btn
	@ViewInject(R.id.send_message_btn)
	private Button sendMessageButton;
	//添加好友layout
	@ViewInject(R.id.add_friend_layout)
	private LinearLayout addFriendLayout;
	//添加好友btn 
	@ViewInject(R.id.add_friend_button)
	private Button addFriendButton;	
	
	//共同好友
	@ViewInject(R.id.common_friend_button)
	private Button commonFriendButton;	
	//访客
	@ViewInject(R.id.visit_button)
	private Button visitButton;	
	
	//单例bitmapUtils的引用
	BitmapUtils bitmapUtils;
	//TA的相片adapter
	private HelloHaAdapter<String> hisImageAdapter;
	//TA的来访adapter
	private HelloHaAdapter<String> hisFriendAdapter;
	//查看的用户id
	private int uid;
	//查看的用户模型
	private UserModel otherUserModel;
	
	@OnClick({R.id.head_image_view, R.id.visit_button, R.id.common_friend_button})
	private void clickEvent(View view){
		switch (view.getId()) {
		case R.id.head_image_view:
			//头像
			Intent headIntent = new Intent(this, BigImgLookActivity.class);
			headIntent.putExtra(BigImgLookActivity.INTENT_KEY, JLXCConst.ATTACHMENT_ADDR+otherUserModel.getHead_image());
			startActivityWithBottom(headIntent);
			break;
		case R.id.visit_button:
			//来访
			Intent visitIntent = new Intent(this, VisitListActivity.class);
			visitIntent.putExtra(VisitListActivity.INTENT_KEY, otherUserModel.getUid());
			startActivityWithRight(visitIntent);
			break;			
		case R.id.common_friend_button:
			//共同好友
			Intent commonIntent = new Intent(this, CommonFriendsActivity.class);
			commonIntent.putExtra(CommonFriendsActivity.INTENT_KEY, otherUserModel.getUid());
			startActivityWithRight(commonIntent);
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_other_personal;
	}

	@Override
	protected void setUpView() {
		Intent intent = getIntent();
		uid = intent.getIntExtra(INTENT_KEY, 0);
		
		/////////////////////////测试数据//////////////////////
		uid = 19;
		UserManager.getInstance().getUser().setUid(19);
		/////////////////////////测试数据//////////////////////
		
		//初始化adapter
		hisImageAdapter = initAdapter();
		hisFriendAdapter = initAdapter();
		//设置adapter
		hisImageGridView.setAdapter(hisImageAdapter);
		hisFriendsGridView.setAdapter(hisFriendAdapter);
		//不能点击
		hisImageGridView.setEnabled(false);
		hisFriendsGridView.setEnabled(false);
		//设置照片和背景图
		bitmapUtils = new BitmapUtils(this);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.ARGB_8888);
		bitmapUtils.configMemoryCacheEnabled(true);
		bitmapUtils.configDiskCacheEnabled(true);		
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.ic_launcher);
		
		//获取数据
		getPersonalInformation();
	}

	@Override
	protected void loadLayout(View v) {
		
	}
	
	//////////////////////private method////////////////////////
	//初始化adapter
	private HelloHaAdapter<String> initAdapter(){
		HelloHaAdapter<String> adapter = new HelloHaAdapter<String>(this, R.layout.attrament_image) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, String item) {
				ImageView imageView = helper.getView(R.id.image_attrament);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item);
			}
		};
		return adapter;
	}
	
	 //获取用户信息	
	private void getPersonalInformation(){
		
		String path = JLXCConst.PERSONAL_INFORMATION+"?"+"uid="+uid+"&current_id="+UserManager.getInstance().getUser().getUid();
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
							handleData(jResult);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(OtherPersonalActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						ToastUtil.show(OtherPersonalActivity.this, "网络有毒=_=");
					}

				}, null));
	}
		
	//处理数据
	private void handleData(JSONObject jsonObject) {
		
		otherUserModel = new UserModel();		
		otherUserModel.setContentWithJson(jsonObject);
		
		//姓名
		if (null == otherUserModel.getName() || "".equals(otherUserModel.getName())) {
			nameTextView.setText("暂无");
		}else {
			nameTextView.setText(otherUserModel.getName());
		}
		//签名
		if (null == otherUserModel.getSign() || "".equals(otherUserModel.getSign())) {
			signTextView.setText("暂无");
		}else {
			signTextView.setText(otherUserModel.getSign());
		}		
		//生日
		if (null == otherUserModel.getBirthday() || "".equals(otherUserModel.getBirthday())) {
			birthTextView.setText("暂无");
		}else {
			birthTextView.setText(otherUserModel.getBirthday());
		}
		//性别
		if (otherUserModel.getSex() == 0) {
			sexTextView.setText("男孩纸");
		}else {
			sexTextView.setText("女孩纸");
		}
		//学校
		if (null == otherUserModel.getSchool() || "".equals(otherUserModel.getSchool())) {
			schoolTextView.setText("暂无");
		}else {
			schoolTextView.setText(otherUserModel.getSchool());
		}
		//城市
		if (null == otherUserModel.getCity() || "".equals(otherUserModel.getCity())) {
			cityTextView.setText("暂无");
		}else {
			cityTextView.setText(otherUserModel.getCity());
		}	
		//头像
		bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+otherUserModel.getHead_sub_image());
		//背景
		bitmapUtils.display(backImageView, JLXCConst.ATTACHMENT_ADDR+otherUserModel.getBackground_image());
		
		//他的朋友
		JSONArray friendArray = jsonObject.getJSONArray("friend_list");
		List<String> friendImageList = new ArrayList<String>();
		for (int i = 0; i < friendArray.size(); i++) {
			JSONObject object = (JSONObject) friendArray.get(i);
			friendImageList.add(object.getString("head_sub_image"));
		}
		hisFriendAdapter.replaceAll(friendImageList);
		
		//他的动态
		JSONArray imagesArray = jsonObject.getJSONArray("image_list");
		List<String> imagesList = new ArrayList<String>();
		for (int i = 0; i < imagesArray.size(); i++) {
			JSONObject object = (JSONObject) imagesArray.get(i);
			imagesList.add(object.getString("sub_url"));
		}
		hisImageAdapter.replaceAll(imagesList);
		
		//访客数量
		if (jsonObject.getIntValue("visit_count")>0) {
			visitButton.setText("访客"+jsonObject.getIntValue("visit_count"));	
		}else {
			visitButton.setText("访客");
		}
		//好友数量
		if (jsonObject.getIntValue("friend_count")>0) {
			hisFriendsCountTextView.setText(""+jsonObject.getIntValue("friend_count"));	
		}else {
			hisFriendsCountTextView.setText("");
		}
		//共同好友数量
		if (jsonObject.getIntValue("common_friend_count")>0) {
			commonFriendButton.setText("共同好友"+jsonObject.getIntValue("common_friend_count"));	
		}else {
			commonFriendButton.setText("共同好友");
		}
		
		//如果是自己
		if (UserManager.getInstance().getUser().getUid() == uid) {
			addFriendButton.setEnabled(false);
			sendMessageButton.setEnabled(false);
		}else {
			boolean isFriend = jsonObject.getBoolean("isFriend");
			if (isFriend) {
				addFriendLayout.setVisibility(View.GONE);
			}
		}
		
	}

}
