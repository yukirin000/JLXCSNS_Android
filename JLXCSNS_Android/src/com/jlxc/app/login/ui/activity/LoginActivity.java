package com.jlxc.app.login.ui.activity;

import java.util.Map;

import android.annotation.SuppressLint;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.support.v7.appcompat.R.string;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

@SuppressLint("ResourceAsColor") 
public class LoginActivity extends BaseActivity {

	//用户名输入框
	@ViewInject(R.id.usernameEt)
	private EditText usernameEt;
	//登录注册按钮
	@ViewInject(R.id.loginRegisterBtn)
	private Button loginRegisterBtn;
	//布局文件
	@ViewInject(R.id.login_activity)
	private LinearLayout loginLayout;
	
	
	@OnClick(value={R.id.loginRegisterBtn,R.id.login_activity})
	public void loginOrRegisterClick(View v) {
		
		switch (v.getId()) {
		//登录或者注册判断
		case R.id.loginRegisterBtn:
			loginOrRegister();
			break;
		case R.id.login_activity:
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);  
	        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	        break;
		default:
			break;
		}
	}
	
	/**
	 * 登录或者注册跳转
	 */
	public void loginOrRegister() {
		final String username = usernameEt.getText().toString();
		if (!username.matches(JLXCConst.PHONENUMBER_PATTERN)) {
			Toast.makeText(LoginActivity.this, "请输入正确的手机号码",
					Toast.LENGTH_SHORT).show();
			return; 
		}
		//网络请求
		showLoading("正在验证，请稍后", true);
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", username);
				
		HttpManager.post(JLXCConst.IS_USER, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
			
			@Override
			public void onSuccess(JSONObject jsonResponse, String flag) {
				// TODO Auto-generated method stub
				super.onSuccess(jsonResponse, flag);
				
				int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
				switch (status) {
				case JLXCConst.STATUS_SUCCESS:
//					LogUtils.i(jsonResponse.toJSONString(), 1);
					JSONObject result = jsonResponse.getJSONObject(JLXCConst.HTTP_RESULT);
					//登录
			        int loginDirection    = 1;
			        //注册
			        int registerDirection = 2;
			        int direction = result.getIntValue("direction");
		            if (direction == loginDirection) {
		            	hideLoading();
//		            	LogUtils.i("跳转到登录", 1); 
		            	//跳转
		            	Intent intent = new Intent(LoginActivity.this, SecondLoginActivity.class);
		            	intent.putExtra("username", username);
		            	startActivityWithRight(intent);
		            }
		            
		            if (direction == registerDirection) {
//		            	LogUtils.i("跳转到注册", 1);
		            	//发送验证码
		            	sendVerify(username);
		            }
					
					break;
				case JLXCConst.STATUS_FAIL:
					hideLoading();
					Toast.makeText(LoginActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE),
							Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onFailure(HttpException arg0, String arg1, String flag) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1, flag);
				hideLoading();
				Toast.makeText(LoginActivity.this, "网络异常",
						Toast.LENGTH_SHORT).show();
			}
			
		}, null)); 
	}
	
	//发送验证码
	public void sendVerify(final String username) {
		//网络请求
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone_num", username);
		HttpManager.post(JLXCConst.GET_MOBILE_VERIFY, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
			
			@Override
			public void onSuccess(JSONObject jsonResponse, String flag) {
				// TODO Auto-generated method stub
				super.onSuccess(jsonResponse, flag);
				int status = jsonResponse.getInteger(JLXCConst.HTTP_STATUS);
				switch (status) {
				case JLXCConst.STATUS_SUCCESS:
					hideLoading();
					//跳转
	            	Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
	            	intent.putExtra("username", username);
	            	startActivityWithRight(intent);	
					
					break;
				case JLXCConst.STATUS_FAIL:
					hideLoading();
					Toast.makeText(LoginActivity.this, jsonResponse.getString(JLXCConst.HTTP_MESSAGE),
							Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onFailure(HttpException arg0, String arg1, String flag) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1, flag);
				hideLoading();
				Toast.makeText(LoginActivity.this, "网络异常",
						Toast.LENGTH_SHORT).show();
			}
			
		}, null)); 
	}
	
	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			ActivityManager.getInstence().exitApplication();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.login_activity;
	}

	@Override
	protected void loadLayout(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setUpView() {
	}

}
