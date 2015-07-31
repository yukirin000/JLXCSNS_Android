package com.jlxc.app.login.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.MainTabActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class SecondLoginActivity extends BaseActivityWithTopBar {

	//用户名输入框
	@ViewInject(R.id.passwordEt)
	private EditText passwordEt;
	//登录注册按钮
	@ViewInject(R.id.loginBtn)
	private Button loginBtn;
	//布局文件
	@ViewInject(R.id.second_login_activity)
	private LinearLayout secondLoginLayout;
	
	private String username;
	
	@OnClick(value={R.id.loginBtn,R.id.second_login_activity,R.id.findPwdBtn})
	public void viewClick(View v) {
		
		switch (v.getId()) {
		case R.id.loginBtn:
			//登录
			login();
			break;
		case R.id.findPwdBtn:
			//找回密码
			findPwd();
			break;	
		case R.id.second_login_activity:
			//收键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	        break;
		default:
			break;
		}
	}
	
	public void login() {
		final String password = passwordEt.getText().toString().trim();
		if (null==password || "".equals(password)) {
			Toast.makeText(SecondLoginActivity.this, "密码不能为空",
					Toast.LENGTH_SHORT).show();
			return; 
		}
		//网络请求
		showLoading("正在登录，请稍后", true);
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", username);
		params.addBodyParameter("password", Md5Utils.encode(password));
		
		HttpManager.post(JLXCConst.LOGIN_USER, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
			
			@Override
			public void onSuccess(JSONObject jsonResponse, String flag) {
				// TODO Auto-generated method stub
				super.onSuccess(jsonResponse, flag);
				int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
				switch (status) {
				case JLXCConst.STATUS_SUCCESS:
					hideLoading();
					//登录成功用户信息注入
					JSONObject result = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT);
					UserManager.getInstance().getUser().setContentWithJson(result);
					//数据持久化
					UserManager.getInstance().saveAndUpdate();
					
					Toast.makeText(SecondLoginActivity.this, "登录成功",
							Toast.LENGTH_SHORT).show();
					//跳转主页
					Intent intent = new Intent(SecondLoginActivity.this, MainTabActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
					break; 
				case JLXCConst.STATUS_FAIL:
					hideLoading();
					Toast.makeText(SecondLoginActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE),
							Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onFailure(HttpException arg0, String arg1, String flag) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1, flag);
				hideLoading();
				Toast.makeText(SecondLoginActivity.this, "网络异常",
						Toast.LENGTH_SHORT).show();
			}
			
		}, null)); 
	}
	
	//找回密码
	public void findPwd() {
		//发送验证码
		SMSSDK.getVerificationCode("86",username);
//		//网络请求
//		RequestParams params = new RequestParams();
//		params.addBodyParameter("phone_num", username);
//		HttpManager.post(JLXCConst.GET_MOBILE_VERIFY, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
//			
//			@Override
//			public void onSuccess(JSONObject jsonResponse, String flag) {
//				// TODO Auto-generated method stub
//				super.onSuccess(jsonResponse, flag);
//				LogUtils.i(jsonResponse.toJSONString(), 1);
//				int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
//				switch (status) {
//				case JLXCConst.STATUS_SUCCESS:
//					hideLoading();
//					//跳转
//	            	Intent intent = new Intent(SecondLoginActivity.this, RegisterActivity.class);
//	            	intent.putExtra("username", username);
//	            	intent.putExtra("isFindPwd", true);
//	            	startActivityWithRight(intent);	
//					
//					break;
//				case JLXCConst.STATUS_FAIL:
//					hideLoading();
//					Toast.makeText(SecondLoginActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE),
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//			@Override
//			public void onFailure(HttpException arg0, String arg1, String flag) {
//				// TODO Auto-generated method stub
//				super.onFailure(arg0, arg1, flag);
//				hideLoading();
//				Toast.makeText(SecondLoginActivity.this, "网络异常",
//						Toast.LENGTH_SHORT).show();
//			}
//			
//		}, null)); 
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_second_login;
	}

	@Override
	protected void setUpView() {
		//设置用户名
		Intent intent =	getIntent();
		setUsername(intent.getStringExtra("username"));
//		passwordEt.setText("123456");
//		EventHandler eh=new EventHandler(){
//			@Override
//			public void afterEvent(int event, int result, Object data) {
//				Message msg = new Message();
//				msg.arg1 = event;
//				msg.arg2 = result;
//				msg.obj = data;
//				handler.sendMessage(msg);
//			}
//		};
//		SMSSDK.registerEventHandler(eh);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		EventHandler eh=new EventHandler(){
			@Override
			public void afterEvent(int event, int result, Object data) {
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				handler.sendMessage(msg);
			}
		};
		SMSSDK.registerEventHandler(eh);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SMSSDK.unregisterAllEventHandler();
	}
	
	@SuppressLint("HandlerLeak") 
	Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			hideLoading();
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int event = msg.arg1;
			int result = msg.arg2;
			Object data = msg.obj;
			Log.e("event", "event="+event);
			if (result == SMSSDK.RESULT_COMPLETE) {
				//短信注册成功后，返回MainActivity,然后提示新好友
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
//					Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
	            	Intent intent = new Intent(SecondLoginActivity.this, RegisterActivity.class);
	            	intent.putExtra("username", username);
	            	startActivityWithRight(intent);	
	            	
//	            	SMSSDK.unregisterAllEventHandler();
				}else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
//					Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
					
				}
			} else {
				((Throwable) data).printStackTrace();
				ToastUtil.show(SecondLoginActivity.this, "网络有点小问题");
			}
			
		}
		
	};

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
