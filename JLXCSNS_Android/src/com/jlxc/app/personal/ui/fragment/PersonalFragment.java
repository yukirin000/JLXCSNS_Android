package com.jlxc.app.personal.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.mapcore2d.ar;
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
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class PersonalFragment extends BaseFragment {

	//背景图
	@ViewInject(R.id.back_image_View)
	private ImageView backImageView;
	//头像
	@ViewInject(R.id.head_image_view)
	private ImageView headImageView;	
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
	private TextView myFriendsCountTextView;
	
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
	
	//用户模型
	private UserModel userModel;
	//我的相片adapter
	private HelloHaAdapter<String> myImageAdapter;
	//最近来访adapter
	private HelloHaAdapter<String> visitAdapter;
	//好友adapter
	private HelloHaAdapter<String> friendsAdapter;	
	
	@OnClick(value={R.id.name_layout,R.id.sign_layout,R.id.birth_layout,R.id.sex_layout,
			R.id.school_layout,R.id.city_layout})
	private void clickEvent(View view){
		switch (view.getId()) {
		//姓名
		case R.id.name_layout:
			nameClick();
			break;
		//签名
		case R.id.sign_layout:
			
			break;
		//生日
		case R.id.birth_layout:
			
			break;
		//性别
		case R.id.sex_layout:
			
			break;
		//学校
		case R.id.school_layout:
			
			break;
		//城市
		case R.id.city_layout:
			
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
		myImageAdapter = initAdapter();
		visitAdapter = initAdapter();
		friendsAdapter = initAdapter();
		
		//设置adapter
		myImageGridView.setAdapter(myImageAdapter);
		visitGridView.setAdapter(visitAdapter);
		friendsGridView.setAdapter(friendsAdapter);
		//不能点击
		myImageGridView.setEnabled(false);
		visitGridView.setEnabled(false);
		friendsGridView.setEnabled(false);
		
	}

	@Override
	public void loadLayout(View rootView) {
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		userModel = UserManager.getInstance().getUser();
		userModel.setUid(19);
		userModel.setHead_image("2015-07-07/01436273216_sub.jpg");
		userModel.setBackground_image("2015-07-02/191435808476.png");
		
	    //获取当前最近的三张状态图片
		getNewsImages();
		getVisitImages();
		
		//http://192.168.1.100/jlxc_php/Uploads/2015-07-02/191435808476.png
		//设置照片和背景图
		BitmapUtils bitmapUtils = BitmapManager.getInstance().getBitmapUtils(getActivity(), true, true);
		//头像 2015-07-07/01436273216_sub.jpg
		bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+userModel.getHead_image());
		//背景 2015-07-02/191435808476.png
		bitmapUtils.display(backImageView, JLXCConst.ATTACHMENT_ADDR+userModel.getBackground_image());
		//姓名
		if (null == userModel.getName() || "".equals(userModel.getName())) {
			nameTextView.setText("暂无");
		}else {
			nameTextView.setText(userModel.getName());
		}
		//签名
		if (null == userModel.getSign() || "".equals(userModel.getSign())) {
			signTextView.setText("暂无");
		}else {
			signTextView.setText(userModel.getSign());
		}		
		//生日
		if (null == userModel.getBirthday() || "".equals(userModel.getBirthday())) {
			birthTextView.setText("暂无");
		}else {
			birthTextView.setText(userModel.getBirthday());
		}
		//性别
		if (userModel.getSex() == 0) {
			sexTextView.setText("男孩纸");
		}else {
			sexTextView.setText("女孩纸");
		}
		//学校
		if (null == userModel.getSchool() || "".equals(userModel.getSchool())) {
			schoolTextView.setText("暂无");
		}else {
			schoolTextView.setText(userModel.getSchool());
		}
		//城市
		if (null == userModel.getCity() || "".equals(userModel.getCity())) {
			cityTextView.setText("暂无");
		}else {
			cityTextView.setText(userModel.getCity());
		}		
	}
	
	
	//////////////////////private method////////////////////////
	//姓名点击
	private void nameClick() {
		
		//dialog
	 	Builder nameAlertDialog = new AlertDialog.Builder(getActivity()).setNegativeButton("取消", null).setTitle("修改昵称");

	 	LinearLayout textViewLayout = (LinearLayout) View.inflate(getActivity(), R.layout.dialog_text_view, null);
	 	nameAlertDialog.setView(textViewLayout);
	 	EditText et_search = (EditText)textViewLayout.findViewById(R.id.searchC);
	 	et_search.setText(userModel.getName());
	 	//设置确定
	 	nameAlertDialog.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
	 	
	 	nameAlertDialog.show();
		
	}
	
	//初始化adapter
	private HelloHaAdapter<String> initAdapter(){
		
		HelloHaAdapter<String> adapter = new HelloHaAdapter<String>(getActivity(), R.layout.attrament_image) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, String item) {
				ImageView imageView = helper.getView(R.id.image_attrament);
				BitmapManager.getInstance().getBitmapUtils(getActivity(), true, true)
				.display(imageView, JLXCConst.ATTACHMENT_ADDR+item);
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
							List<String> imageList = new ArrayList<String>();
							for (int i = 0; i < array.size(); i++) {
								JSONObject object = (JSONObject) array.get(i);
								imageList.add(object.getString("sub_url"));
							}
							myImageAdapter.replaceAll(imageList);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(getActivity(), jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
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
							for (int i = 0; i < array.size(); i++) {
								JSONObject object = (JSONObject) array.get(i);
								headImageList.add(object.getString("head_sub_image"));
							}
							visitAdapter.replaceAll(headImageList);
							//人数
							int visitCount = jResult.getIntValue("visit_count");
							if (visitCount > 0) {
								visitCountTextView.setText(visitCount+"");
							}else {
								visitCountTextView.setText("");
							}
						}

						if (status == JLXCConst.STATUS_FAIL) {
							ToastUtil.show(getActivity(), jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
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

	public HelloHaAdapter<String> getFriendsAdapter() {
		return friendsAdapter;
	}

	public void setFriendsAdapter(HelloHaAdapter<String> friendsAdapter) {
		this.friendsAdapter = friendsAdapter;
	}
}
