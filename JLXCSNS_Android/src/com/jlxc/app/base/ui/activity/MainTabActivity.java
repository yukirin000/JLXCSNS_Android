package com.jlxc.app.base.ui.activity;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient.ConnectCallback;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.model.Conversation;
import io.yunba.android.manager.YunBaManager;

import com.jlxc.app.R;
import com.jlxc.app.base.helper.RongCloudEvent;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.NewsPushModel;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.ToastUtil;
import com.jlxc.app.discovery.ui.fragment.DiscoveryFragment;
import com.jlxc.app.message.model.IMModel;
import com.jlxc.app.message.ui.fragment.MessageMainFragment;
import com.jlxc.app.news.receiver.ui.NewMessageReceiver;
import com.jlxc.app.news.ui.fragment.MainPageFragment;
import com.jlxc.app.personal.ui.activity.OtherPersonalActivity;
import com.jlxc.app.personal.ui.fragment.PersonalFragment;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.FragmentTabHost;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class MainTabActivity extends BaseActivity {

	private final static int SCANNIN_GREQUEST_CODE = 1;
	
	// FragmentTabHost对象
	@ViewInject(android.R.id.tabhost)
	private FragmentTabHost mTabHost;

	private LayoutInflater layoutInflater; 

	private Class<?> fragmentArray[] = { MainPageFragment.class, MessageMainFragment.class,DiscoveryFragment.class,
			PersonalFragment.class };

	private int mImageViewArray[] = { R.drawable.tab_home_btn,R.drawable.tab_home_btn,R.drawable.tab_home_btn,
			R.drawable.tab_message_btn };

	private String mTextviewArray[] = { "主页", "消息","发现", "我" };
//	//已经连接
//	private boolean isConnect = false;
	
	//im未读数量
	public static int imUnreadCount;
	
	public void initTab() {

		layoutInflater = LayoutInflater.from(this);

		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i])
					.setIndicator(getTabItemView(i));
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}
		
		//注册通知
		registerNotify();
		refreshTab();
	}
	
	
	//初始化融云
	private void initRong(){
		
		String token = "";
		UserModel userModel = UserManager.getInstance().getUser();
		if (null != userModel.getIm_token() && userModel.getIm_token().length()>0) {
			token = userModel.getIm_token();
		}
		RongIM.connect(token, new ConnectCallback() {

			@Override 
			public void onError(ErrorCode arg0) {
				Toast.makeText(MainTabActivity.this, "connect onError", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess(String arg0) {
				Toast.makeText(MainTabActivity.this, "connect onSuccess", Toast.LENGTH_SHORT).show();
				RongCloudEvent.getInstance().setOtherListener();
				
				//设置im未读监听
				final Conversation.ConversationType[] conversationTypes = {Conversation.ConversationType.PRIVATE, Conversation.ConversationType.DISCUSSION,
		                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
		                Conversation.ConversationType.APP_PUBLIC_SERVICE, Conversation.ConversationType.PUBLIC_SERVICE};
				Handler handler = new Handler();
		        handler.postDelayed(new Runnable() {
		            @Override
		            public void run() {
		                RongIM.getInstance().setOnReceiveUnreadCountChangedListener(mCountListener, conversationTypes);
		            }
		        }, 1000);
			}

			@Override
			public void onTokenIncorrect() {
				Toast.makeText(MainTabActivity.this, "token error", Toast.LENGTH_SHORT).show();
			}

		});
		
	}
	//初始化云巴
	private void initYunBa(){
//		registerMessageRecevier();
		final UserModel userModel = UserManager.getInstance().getUser();
		if (userModel.getUid() != 0) {
			LogUtils.i("yunba test", 1);
			YunBaManager.subscribe(this, new String[]{JLXCConst.JLXC+userModel.getUid()}, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken arg0) {
//					LogUtils.i("yunba success"+JLXCConst.JLXC+userModel.getUid(), 1);
					Looper.prepare();
					Toast.makeText(MainTabActivity.this, "yunba success", Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					Looper.prepare();
					Toast.makeText(MainTabActivity.this, "yunba failr", Toast.LENGTH_SHORT).show();
					Looper.loop();
				}
			});
		}
		
	}
	
	
	/**
	 */
	@SuppressLint("InflateParams")
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextviewArray[index]);

		return view;
	}

	@Override
	public int setLayoutId() {
		return R.layout.activity_main;
	}

	@Override
	protected void loadLayout(View v) {

	}

	@Override
	protected void setUpView() {
		
		//初始化tab
		initTab();
		//初始化融云
		initRong();			
		//初始化云巴
		initYunBa();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
	        alterDialog.setMessage("确定退出该账号吗？");
	        alterDialog.setCancelable(true);

	        alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                if (RongIM.getInstance() != null)
	                	RongIM.getInstance().disconnect();
	                if (null != newMessageReceiver) {
	                	unregisterReceiver(newMessageReceiver);
	                	newMessageReceiver = null;
					}
	                ActivityManager.getInstence().exitApplication();
//	                killThisPackageIfRunning(MainTabActivity.this, "com.jlxc.app");
//	                Process.killProcess(Process.myPid());
	                
//	                finish();
	                
	            }
	        });
	        alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.cancel();
	            }
	        });
	        alterDialog.show();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		
		if (null != newMessageReceiver) {
			unregisterReceiver(newMessageReceiver);
			newMessageReceiver = null;
		}
		
		if (RongIM.getInstance() != null)
            RongIM.getInstance().logout();
		
		Process.killProcess(Process.myPid());
		super.onDestroy();  
	}
	
	//友盟集成
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case SCANNIN_GREQUEST_CODE:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				String resultString = bundle.getString("result");
//				mTextView.setText(resultString);
//				mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
				//如果是可以用的
				if (resultString.contains(JLXCConst.JLXC)) {
					String baseUid = resultString.substring(4);
					int uid = JLXCUtils.stringToInt(new String(Base64.decode(baseUid, Base64.DEFAULT)));
					if (uid == UserManager.getInstance().getUser().getUid()) {
						ToastUtil.show(this, "不要没事扫自己玩(ㅎ‸ㅎ)");
					}else {
						Intent intent = new Intent(this, OtherPersonalActivity.class);
						intent.putExtra(OtherPersonalActivity.INTENT_KEY, uid);
						startActivity(intent);
					}
				}
				
				ToastUtil.show(this, "'"+resultString+"'"+"是什么");
			}
			break;
		}
    }
	
	
	////////////////////////////////private method////////////////////////////////
	private NewMessageReceiver newMessageReceiver;
	//注册通知
	private void registerNotify(){
		
		//刷新tab
		newMessageReceiver = new NewMessageReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//刷新tab
				refreshTab();
			}
		};
		IntentFilter intentFilter = new IntentFilter(JLXCConst.BROADCAST_TAB_BADGE);
		registerReceiver(newMessageReceiver, intentFilter);
	}
	
	//刷新tab 未读标志
	private void refreshTab() {
		View messageView = mTabHost.getTabWidget().getChildAt(1);
		TextView unreadTextView = (TextView) messageView.findViewById(R.id.unread_text_view);
	    //聊天页面
	    //新好友请求未读
		int newFriendsCount = IMModel.unReadNewFriendsCount();
	    //徽标 最多显示99
	    //未读推送
		int newsUnreadCount = NewsPushModel.findUnreadCount().size();
		int total = newsUnreadCount+newFriendsCount+imUnreadCount;
	    if (total > 99) {
	        total = 99;
	    }
	    unreadTextView.setText(total+"");
	    if (total == 0) {
			unreadTextView.setVisibility(View.GONE);
		}else {
			unreadTextView.setVisibility(View.VISIBLE);
		} 
	}
	
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		// TODO Auto-generated method stub
//		outState.putBoolean("isConnect", isConnect);
//		LogUtils.i("on save" + " "+ isConnect, 1);
//		
//		super.onSaveInstanceState(outState);
//	}
	
//	@Override
//	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		
//	};
//	
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		
//		isConnect = savedInstanceState.getBoolean("isConnect");
//		LogUtils.i("on restroe" + " "+ isConnect, 1);
//		isConnect = true;
//		
//		super.onRestoreInstanceState(savedInstanceState);
//	}
	
	//im未读监听器
	public RongIM.OnReceiveUnreadCountChangedListener mCountListener = new RongIM.OnReceiveUnreadCountChangedListener() {
        @Override
        public void onMessageIncreased(int count) {
            imUnreadCount = count;
            refreshTab();
        }
    };
	
	//杀死进程
	public static void killThisPackageIfRunning(final Context context, String packageName) {
        android.app.ActivityManager activityManager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
    }
	
}
