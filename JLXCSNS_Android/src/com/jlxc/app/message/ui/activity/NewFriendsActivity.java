package com.jlxc.app.message.ui.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;

//新好友
public class NewFriendsActivity extends BaseActivityWithTopBar {

	@ViewInject(R.id.new_friend_list_view)
	ListView newFriendListView;
	//adapter
	HelloHaAdapter<IMModel> newFriendAdapter;
	BitmapUtils bitmapUtils;
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_new_friend_list;
	}

	@Override
	protected void setUpView() {
		
		bitmapUtils = BitmapManager.getInstance().getHeadPicBitmapUtils(this, R.drawable.ic_launcher, true, true);
		
		initListView();
		refreshListView();
		//发送通知
		sendNotify();
	}
	//////////////////////////////private method////////////////////////////////
	private void initListView() {
		//初始化adapter
		newFriendAdapter = new HelloHaAdapter<IMModel>(this, R.layout.new_friend_adapter) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, final IMModel item) {
				//头像
				ImageView imageView = helper.getView(R.id.head_image_view);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item.getAvatarPath());
				//姓名
				helper.setText(R.id.name_text_view, item.getTitle());
				TextView addTextView = helper.getView(R.id.add_text_view);
				//点击添加
				addTextView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						addFriend(item);
					}
				});
				//按钮
				if (item.getCurrentState() == IMModel.GroupHasAdd) {
					addTextView.setText("已添加");
					addTextView.setEnabled(false);
				}else {
					addTextView.setText("添加");
					addTextView.setEnabled(true);
				}
			}
		};
		newFriendListView.setAdapter(newFriendAdapter);
		
		//点击进入详情
		newFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				IMModel imModel = newFriendAdapter.getItem(position);
				Intent intent = new Intent(NewFriendsActivity.this, OtherPersonalActivity.class);
				intent.putExtra(OtherPersonalActivity.INTENT_KEY, JLXCUtils.stringToInt(imModel.getTargetId().replace(JLXCConst.JLXC, "")));
				startActivityWithRight(intent);
			}
		});
		
		//长按删除
		newFriendListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				new AlertDialog.Builder(NewFriendsActivity.this).setTitle("确定要删除吗").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						IMModel imModel = newFriendAdapter.getItem(position);
						imModel.setIsNew(0);
						imModel.update();
						refreshListView(); 
					}
				}).setNegativeButton("取消", null).show();
				
				return false;
			}
		});
	}
	
	private void refreshListView() {
		//设置已读
		IMModel.hasRead();
		List<IMModel> newFriendList = IMModel.findAllNewFriends();
		newFriendAdapter.replaceAll(newFriendList);
		
		//通知
		Intent notifyIntent = new Intent(JLXCConst.BROADCAST_TAB_BADGE);
		sendBroadcast(notifyIntent);
		
	}

	//添加好友
	private void addFriend(final IMModel imModel) {

		// 参数设置
		RequestParams params = new RequestParams();
		params.addBodyParameter("user_id", UserManager.getInstance().getUser().getUid()+"");
		params.addBodyParameter("friend_id", imModel.getTargetId().replace(JLXCConst.JLXC, "")+"");
		
		showLoading("添加中^_^", false);
		HttpManager.post(JLXCConst.Add_FRIEND, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						
						hideLoading();
						int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
						ToastUtil.show(NewFriendsActivity.this,jsonResponse.getString(JLXCConst.HTTP_MESSAGE));
						
						if (status == JLXCConst.STATUS_SUCCESS) {
							//本地数据持久化
							IMModel newModel = IMModel.findByGroupId(imModel.getTargetId());
							//如果存在更新
							if (null != newModel) {
								newModel.setTitle(imModel.getTitle());
								newModel.setAvatarPath(imModel.getAvatarPath());
								newModel.setIsNew(1);
								newModel.setIsRead(1);
								newModel.setCurrentState(IMModel.GroupHasAdd);
								newModel.update();
							}else {
								newModel = new IMModel();
								newModel.setType(IMModel.ConversationType_PRIVATE);
								newModel.setTargetId(imModel.getTargetId());
								newModel.setTitle(imModel.getTitle());
								newModel.setAvatarPath(imModel.getAvatarPath());
								newModel.setIsNew(1);
								newModel.setIsRead(1);
								newModel.setCurrentState(IMModel.GroupHasAdd);
								newModel.setOwner(UserManager.getInstance().getUser().getUid());
								newModel.save();
							}
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						ToastUtil.show(NewFriendsActivity.this,
								"网络异常");
					}
				}, null));
	}
	
	//发送通知
	private void sendNotify() {
		//通知刷新
		Intent tabIntent = new Intent(JLXCConst.BROADCAST_TAB_BADGE);
		sendBroadcast(tabIntent);
		//通知页面刷新
		Intent messageIntent = new Intent(JLXCConst.BROADCAST_NEW_MESSAGE_PUSH);
		sendBroadcast(messageIntent);
		//顶部刷新
		Intent messageTopIntent = new Intent(JLXCConst.BROADCAST_MESSAGE_REFRESH);
		sendBroadcast(messageTopIntent);
	}
	
}
