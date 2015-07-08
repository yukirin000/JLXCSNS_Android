package com.jlxc.app.login.ui.activity;

import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
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
					Toast.makeText(SecondLoginActivity.this, "登录成功",
							Toast.LENGTH_SHORT).show();
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
		//网络请求
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone_num", username);
		HttpManager.post(JLXCConst.GET_MOBILE_VERIFY, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
			
			@Override
			public void onSuccess(JSONObject jsonResponse, String flag) {
				// TODO Auto-generated method stub
				super.onSuccess(jsonResponse, flag);
				LogUtils.i(jsonResponse.toJSONString(), 1);
				int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
				switch (status) {
				case JLXCConst.STATUS_SUCCESS:
					hideLoading();
					//跳转
	            	Intent intent = new Intent(SecondLoginActivity.this, RegisterActivity.class);
	            	intent.putExtra("username", username);
	            	intent.putExtra("isFindPwd", true);
	            	startActivityWithRight(intent);	
					
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
		
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
