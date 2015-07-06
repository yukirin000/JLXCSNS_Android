package com.jlxc.app.login.ui.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager.Request;
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

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.JLXCUtils;
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
	@ViewInject(R.id.loginRegister)
	private Button loginRegisterBtn;
	//布局文件
	@ViewInject(R.id.login_activity)
	private LinearLayout loginLayout;
	
	
	@OnClick(value={R.id.loginRegister,R.id.login_activity})
	public void loginOrRegisterClick(View v) {
		
		switch (v.getId()) {
		case R.id.loginRegister:
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
		String username = usernameEt.getText().toString();
		if (!username.matches(JLXCConst.PHONENUMBER_PATTERN)) {
			showConfirmAlert(getResources().getString(R.string.alert_title), "请输入正确的手机号码");
			return; 
		}
		//网络请求
		showLoading("正在登录，请稍后", true);
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", username);
		HttpManager.post(JLXCConst.IS_USER, params, new JsonRequestCallBack<String>(new LoadDataHandler<String>(){
			
			@Override
			public void onSuccess(JSONObject jsonResponse, String flag) {
				// TODO Auto-generated method stub
				super.onSuccess(jsonResponse, flag);
				
				int status = jsonResponse.getInteger("status");
				switch (status) {
				case JLXCConst.STATUS_SUCCESS:
					LogUtils.i(jsonResponse.toJSONString(), 1);
					JSONObject result = jsonResponse.getJSONObject("result");
					//登录
			        int loginDirection    = 1;
			        //注册
			        int registerDirection = 2;
			        int direction = result.getIntValue("direction");
		            if (direction == loginDirection) {
		            	hideLoading();
		            	LogUtils.i("跳转到登录", 1);
		            }
		            
		            if (direction == registerDirection) {
		            	hideLoading();
		            	LogUtils.i("跳转到注册", 1);
		            	showConfirmAlert("注册", "");
		            }
					
					break;
				case JLXCConst.STATUS_FAIL:
					showConfirmAlert("提示", jsonResponse.getString("msg"));
				}
			}
			@Override
			public void onFailure(HttpException arg0, String arg1, String flag) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1, flag);
				hideLoading();
				showConfirmAlert("提示", "登录失败，请检查网络连接!");
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
