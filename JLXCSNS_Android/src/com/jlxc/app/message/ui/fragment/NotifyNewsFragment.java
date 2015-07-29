package com.jlxc.app.message.ui.fragment;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jlxc.app.R;
import com.jlxc.app.base.adapter.HelloHaAdapter;
import com.jlxc.app.base.adapter.HelloHaBaseAdapterHelper;
import com.jlxc.app.base.manager.BitmapManager;
import com.jlxc.app.base.model.NewsPushModel;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.TimeHandle;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.message.ui.activity.NewFriendsActivity;
import com.jlxc.app.news.model.NewsOperateModel;
import com.jlxc.app.news.receiver.ui.NewMessageReceiver;
import com.jlxc.app.news.ui.activity.NewsDetailActivity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class NotifyNewsFragment extends BaseFragment {

	//列表
	@ViewInject(R.id.notify_list_view)
	ListView notifyListView;
	//adapter
	HelloHaAdapter<NewsPushModel> newsAdapter;
	HelloHaAdapter<IMModel> imAdapter;
	BitmapUtils bitmapUtils;
	//头部layout
	LinearLayout headLayout;
	//顶部新朋友头像
	GridView newFriendGridView;
	//顶部未读新朋友
	TextView unreadTextView;
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.fragment_new_notify;
	}

	@Override
	public void loadLayout(View rootView) {

	}

	@Override
	public void setUpViews(View rootView) {
		
		bitmapUtils = BitmapManager.getInstance().getHeadPicBitmapUtils(getActivity(), R.drawable.ic_launcher, true, true);
		//注册通知
		registerNotify();
//		//初始化list
//		multiItemTypeSet();
		setListView();
		//刷新列表
		refreshList();
	}
	
	////////////////////////////private method/////////////////////////////
	//设置listView
	private void setListView() {
		//头部
		headLayout = (LinearLayout) View.inflate(getActivity(), R.layout.new_friend_cover_adapter, null);
		//点击事件
		headLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newFriendIntent = new Intent(getActivity(), NewFriendsActivity.class);
				startActivityWithRight(newFriendIntent);
			}
		});
		//内容
		newFriendGridView = (GridView) headLayout.findViewById(R.id.new_friends_grid_view);
		unreadTextView = (TextView) headLayout.findViewById(R.id.unread_text_view);
		imAdapter = new HelloHaAdapter<IMModel>(getActivity(), R.layout.attrament_image) {
			@Override
			protected void convert(HelloHaBaseAdapterHelper helper, IMModel item) {
				//图片显示
				ImageView imageView = helper.getView(R.id.image_attrament);
				bitmapUtils.display(imageView, JLXCConst.ATTACHMENT_ADDR+item.getAvatarPath());
			}
		};
		newFriendGridView.setAdapter(imAdapter);
		notifyListView.addHeaderView(headLayout);
		
		newsAdapter = new HelloHaAdapter<NewsPushModel>(getActivity(), R.layout.new_notify_adapter) {

			@Override
			protected void convert(HelloHaBaseAdapterHelper helper,
					NewsPushModel item) {
				// TODO Auto-generated method stub
				//姓名
				helper.setText(R.id.name_text_view, item.getName());
				//时间
				helper.setText(R.id.time_text_view, TimeHandle.getShowTimeFormat(item.getPush_time()));
				//内容
				if (item.getType() == NewsPushModel.PushLikeNews) {
					helper.setText(R.id.content_text_view, "为你点了赞");
				}else {
					helper.setText(R.id.content_text_view, item.getComment_content());
				}
				
				String headString = item.getHead_image();
				if (item.getHead_image().equals("null")) {
					headString = "";
				}
				//头像
				ImageView headImageView = helper.getView(R.id.head_image_view);
				bitmapUtils.display(headImageView, JLXCConst.ATTACHMENT_ADDR+headString);					
				//有图片显示图片 没图片显示内容
				ImageView newsImageView = helper.getView(R.id.news_image_view);
				TextView newsTextView = helper.getView(R.id.news_text_view);
				if (null != item.getNews_image() && !"".equals(item.getNews_image())) {
					newsImageView.setVisibility(View.VISIBLE);
					newsTextView.setVisibility(View.GONE);
					bitmapUtils.display(newsImageView, JLXCConst.ATTACHMENT_ADDR+item.getNews_image());
				}else {
					newsImageView.setVisibility(View.GONE);
					newsTextView.setVisibility(View.VISIBLE);
					newsTextView.setText(item.getNews_content());
				}
			}
		};
		
		notifyListView.setAdapter(newsAdapter);
		//点击进入详情
		notifyListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				NewsPushModel newsPushModel = newsAdapter.getItem(position-1);
				Intent detailIntent = new Intent(getActivity(), NewsDetailActivity.class);
				detailIntent.putExtra(NewsOperateModel.INTENT_KEY_NEWS_ID, ""+newsPushModel.getNews_id());
				startActivityWithRight(detailIntent);
			}
		});
		
		//长按删除
		notifyListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				
				new AlertDialog.Builder(getActivity()).setTitle("确定要删除吗").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						NewsPushModel newsPushModel = newsAdapter.getItem(position-1);
						newsPushModel.remove();
						refreshList(); 
					}
				}).setNegativeButton("取消", null).show();
				
				return true;
			}
		});
		
	}
	
	//刷新页面
	private void refreshList() {
		
		List<NewsPushModel> pushModels = NewsPushModel.findAll();
		newsAdapter.replaceAll(pushModels);
		
		//刷新上端
		List<IMModel> imModels = IMModel.findThreeNewFriends();
		imAdapter.replaceAll(imModels);
		
		//未读
		int unreadNum = IMModel.unReadNewFriendsCount();
		if (unreadNum > 0) {
			unreadTextView.setText(""+unreadNum);
		}else {
			unreadTextView.setText("");
		}
		
	}
	
	private NewMessageReceiver messageReceiver;
	//注册通知
	private void registerNotify() {
		
		messageReceiver = new NewMessageReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				refreshList();
			}
		};
		//回复或者点赞或新朋友到达
		IntentFilter newsfilter = new IntentFilter(JLXCConst.BROADCAST_NEW_MESSAGE_PUSH);
		getActivity().registerReceiver(messageReceiver, newsfilter);
	}
	

}
