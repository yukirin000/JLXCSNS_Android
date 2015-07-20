package com.jlxc.app.message.ui.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.personal.ui.activity.OtherPeopleFriendsActivity;
import com.lidroid.xutils.BitmapUtils;
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
	}
	//////////////////////////////private method////////////////////////////////
	private void initListView() {
		//初始化adapter
		newFriendAdapter = new HelloHaAdapter<IMModel>(this, R.layout.new_friend_adapter) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, IMModel item) {
				//头像
				ImageView imageView = helper.getView(R.id.head_image_view);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item.getAvatarPath());
				//姓名
				helper.setText(R.id.name_text_view, item.getTitle());
			}
		};
		newFriendListView.setAdapter(newFriendAdapter);
		
		//点击进入详情
		newFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				IMModel imModel = newFriendAdapter.getItem(position);
				Intent intent = new Intent(NewFriendsActivity.this, OtherPeopleFriendsActivity.class);
				intent.putExtra(OtherPeopleFriendsActivity.INTENT_KEY, JLXCUtils.stringToInt(imModel.getTargetId().replace(JLXCConst.JLXC, "")));
				startActivityWithRight(intent);
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

}
