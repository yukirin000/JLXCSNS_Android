package com.jlxc.app.login.ui.activity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.R.bool;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.ui.activity.MainTabActivity;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.LogUtils;
import com.jlxc.app.base.utils.Md5Utils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends BaseActivityWithTopBar {

	private final static String INTENT_KEY = "username";

	// 是否是忘记密码
	private Boolean isFindPwd;
	// 当前倒计时的值
	private int countdownValue = 0;
	// 倒计时对象
	private CountDownTimer verifyCountdownTimer = null;
	// 用户 的电话号码
	private String userPhoneNumber;
	// 用户输入的验证码
	private String verifyCodeEditTextValue;
	// 密码
	private String password = "";
	// 返回按钮
	@ViewInject(R.id.base_tv_back)
	private TextView backTextView;
	// 页面标头
	@ViewInject(R.id.base_tv_title)
	private TextView titletTextView;
	// 提示电话的textview
	@ViewInject(R.id.phone_prompt_textview)
	private TextView phonePromptTextView;
	// 验证码输入框
	@ViewInject(R.id.verificationcode_edittext)
	private EditText verifycodeEditText;
	// 下一步按钮
	@ViewInject(R.id.next_button)
	private Button nextButton;
	// 重新验证
	@ViewInject(R.id.revalidated_textview)
	private TextView revalidatedTextView;
	// 密码框
	@ViewInject(R.id.passwd_edittext)
	private EditText passwdeEditText;

	// 点击事件绑定
	@OnClick({ R.id.base_tv_back, R.id.next_button, R.id.revalidated_textview,
			R.id.register_activity })
	public void viewCickListener(View view) {
		switch (view.getId()) {
		case R.id.base_tv_back:
			backClick();
			break;
		case R.id.next_button:
			// 点击下一步
			nextClick();
			break;
		case R.id.revalidated_textview:
			getVerificationCode();
			break;
		case R.id.register_activity:
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			break;
		default:
			break;
		}
	}

	// 初始化数据
	private void init() {
		Intent intent = getIntent();
		userPhoneNumber = intent.getStringExtra(INTENT_KEY);
		isFindPwd = intent.getBooleanExtra("isFindPwd", false);
	}

	// 点击返回
	private void backClick() {
		if (countdownValue > 0) {
			new AlertDialog.Builder(RegisterActivity.this)
					.setTitle("提示")
					.setMessage("已经发送验证码了，再等会儿")
					.setPositiveButton("好的", null)
					.setNegativeButton("不了",
							new DialogInterface.OnClickListener() {// 添加返回按钮
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									verifyCountdownTimer.cancel();
									finishWithRight();
								}
							}).show();
		} else {
			finishWithRight();
		}
	}

	// 点击下一步按钮
	private void nextClick() {
		verifyCodeEditTextValue = verifycodeEditText.getText().toString();
		password = passwdeEditText.getText().toString();
		// 判断输入值是否正确
		if (verifyCodeEditTextValue.length() == 0) {
			ToastUtil.show(RegisterActivity.this, "验证码未输入");
		} else if (password.length() < 6) {
			ToastUtil.show(RegisterActivity.this, "密码最少得6位啦");
		} else {
			// 忘记密码
			if (isFindPwd) {
				findPwd();
			} else {
				// 注册
				startRegister();
			}
		}
	}

	// 找回密码
	private void findPwd() {
		showLoading("数据上传中^_^", false);
		//先验证验证码
//		SMSSDK.submitVerificationCode("86", userPhoneNumber, verifycodeEditText.getText().toString().trim());
		finishPwd();
		
//		RequestParams params = new RequestParams();
//		params.addBodyParameter("username", userPhoneNumber);
//		params.addBodyParameter("password", Md5Utils.encode(password));
//		params.addBodyParameter("verify_code",
//				String.valueOf(verifyCodeEditTextValue));
//
//		HttpManager.post(JLXCConst.FIND_PWD, params,
//				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
//
//					@Override
//					public void onSuccess(JSONObject jsonResponse, String flag) {
//						super.onSuccess(jsonResponse, flag);
//						int status = jsonResponse
//								.getInteger(JLXCConst.HTTP_STATUS);
//						if (status == JLXCConst.STATUS_SUCCESS) {
//							hideLoading();
//							JSONObject result = jsonResponse
//									.getJSONObject(JLXCConst.HTTP_RESULT);
//							UserModel userMd = new UserModel();
//							userMd.setContentWithJson(result);
//							UserManager.getInstance().setUser(userMd);
//							ToastUtil.show(RegisterActivity.this, "修改成功");
//							// 数据持久化
//							UserManager.getInstance().saveAndUpdate();
//							// 跳转主页
//							Intent intent = new Intent(RegisterActivity.this,
//									MainTabActivity.class);
//							startActivity(intent);
//
//						}
//
//						if (status == JLXCConst.STATUS_FAIL) {
//							hideLoading();
//							ToastUtil.show(RegisterActivity.this, jsonResponse
//									.getString(JLXCConst.HTTP_MESSAGE));
//						}
//					}
//
//					@Override
//					public void onFailure(HttpException arg0, String arg1,
//							String flag) {
//						super.onFailure(arg0, arg1, flag);
//						hideLoading();
//						showConfirmAlert("提示", "注册失败，请检查网络连接!");
//					}
//				}, null));
	}
	
	// 找回密码
	private void finishPwd() {
//		showLoading("数据上传中^_^", false);
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", userPhoneNumber);
		params.addBodyParameter("password", Md5Utils.encode(password));
		params.addBodyParameter("verify_code",
				String.valueOf(verifyCodeEditTextValue));

		HttpManager.post(JLXCConst.FIND_PWD, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							hideLoading();
							JSONObject result = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							UserModel userMd = new UserModel();
							userMd.setContentWithJson(result);
							UserManager.getInstance().setUser(userMd);
							ToastUtil.show(RegisterActivity.this, "修改成功");
							// 数据持久化
							UserManager.getInstance().saveAndUpdate();
							// 跳转主页
							Intent intent = new Intent(RegisterActivity.this,
									MainTabActivity.class);
							startActivity(intent);

						}

						if (status == JLXCConst.STATUS_FAIL) {
							hideLoading();
							ToastUtil.show(RegisterActivity.this, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						showConfirmAlert("提示", "注册失败，请检查网络连接!");
					}
				}, null));
	}

	// 开始注册
	private void startRegister() {
		RegisterActivity.this.showLoading("正在注册", false);
		//先验证验证码 测试注释掉
//		SMSSDK.submitVerificationCode("86", userPhoneNumber, verifycodeEditText.getText().toString().trim());
		
		finishRegister();
		
//		RegisterActivity.this.showLoading("正在注册", false);
//		RequestParams params = new RequestParams();
//		params.addBodyParameter("username", userPhoneNumber);
//		params.addBodyParameter("password", Md5Utils.encode(password));
//		params.addBodyParameter("verify_code", verifyCodeEditTextValue);
//
//		HttpManager.post(JLXCConst.REGISTER_USER, params,
//				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
//
//					@Override
//					public void onSuccess(JSONObject jsonResponse, String flag) {
//						super.onSuccess(jsonResponse, flag);
//						int status = jsonResponse
//								.getInteger(JLXCConst.HTTP_STATUS);
//						if (status == JLXCConst.STATUS_SUCCESS) {
//							hideLoading();
//							JSONObject result = jsonResponse
//									.getJSONObject(JLXCConst.HTTP_RESULT);
//							// 设置用户实例
//							UserModel userMd = new UserModel();
//							userMd.setContentWithJson(result);
//							UserManager.getInstance().setUser(userMd);
//							// 数据持久化
//							UserManager.getInstance().saveAndUpdate();
//							ToastUtil.show(RegisterActivity.this, "注册成功");
//							// 跳转至选择学校页面
//							Intent intent = new Intent(RegisterActivity.this,
//									SelectSchoolActivity.class);
//							startActivity(intent);
//						}
//
//						if (status == JLXCConst.STATUS_FAIL) {
//							hideLoading();
//							ToastUtil.show(RegisterActivity.this, jsonResponse
//									.getString(JLXCConst.HTTP_MESSAGE));
//						}
//					}
//
//					@Override
//					public void onFailure(HttpException arg0, String arg1,
//							String flag) {
//						super.onFailure(arg0, arg1, flag);
//						hideLoading();
//						ToastUtil.show(RegisterActivity.this, "注册失败，你网络太垃圾了!");
//					}
//
//				}, null));
	}
	
	//验证成功 完成注册
	private void finishRegister() {
		
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", userPhoneNumber);
		params.addBodyParameter("password", Md5Utils.encode(password));
//		params.addBodyParameter("verify_code", verifyCodeEditTextValue);

		HttpManager.post(JLXCConst.REGISTER_USER, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							hideLoading();
							JSONObject result = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							// 设置用户实例
							UserModel userMd = new UserModel();
							userMd.setContentWithJson(result);
							UserManager.getInstance().setUser(userMd);
							// 数据持久化
							UserManager.getInstance().saveAndUpdate();
							ToastUtil.show(RegisterActivity.this, "注册成功");
							// 跳转至选择学校页面
							Intent intent = new Intent(RegisterActivity.this,
									SelectSchoolActivity.class);
							startActivity(intent);
						}

						if (status == JLXCConst.STATUS_FAIL) {
							hideLoading();
							ToastUtil.show(RegisterActivity.this, jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						ToastUtil.show(RegisterActivity.this, "注册失败，你网络太垃圾了!");
					}

				}, null));
	}

	@Override
	public int setLayoutId() {
		return R.layout.activity_register_layout;
	}

	@Override
	protected void setUpView() {
		
		init();
		titletTextView.setText("注册");
		phonePromptTextView.setText("验证码已发送至：" + userPhoneNumber);
		revalidatedTextView.setEnabled(false);
		revalidatedTextView.setTextColor(Color.GRAY);
		verifyCountdownTimer = new CountDownTimer(60000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				countdownValue = (int) millisUntilFinished / 1000;
				revalidatedTextView.setText(countdownValue + "s 后重发");
			}

			@Override
			public void onFinish() {
				countdownValue = 0;
				revalidatedTextView.setEnabled(true);
				revalidatedTextView.setText("获取验证码");
				revalidatedTextView.setTextColor(Color.BLUE);
			}
		};
		// 开始倒计时
		verifyCountdownTimer.start();
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			//验证码接收器
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
		} catch (Exception e) {
			System.out.println("没初始化SMSSDK 因为这个短信sdk对DEBUG有影响 所以不是RELEASE不初始化");
		}
		
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try{
			SMSSDK.unregisterAllEventHandler();
		} catch (Exception e) {
			System.out.println("没初始化SMSSDK 因为这个短信sdk对DEBUG有影响 所以不是RELEASE不初始化");
		}		
	}

	@Override
	protected void loadLayout(View v) {

	}

	// 获取验证码
	private void getVerificationCode() {
		
		try {
			//发送验证码
			SMSSDK.getVerificationCode("86",userPhoneNumber);			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
//		// 设置字体颜色
//		revalidatedTextView.setTextColor(Color.GRAY);
//		// 网络请求
//		RequestParams params = new RequestParams();
//		params.addBodyParameter("phone_num", userPhoneNumber);
//
//		HttpManager.post(JLXCConst.GET_MOBILE_VERIFY, params,
//				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {
//
//					@Override
//					public void onSuccess(JSONObject jsonResponse, String flag) {
//						super.onSuccess(jsonResponse, flag);
//						int status = jsonResponse
//								.getInteger(JLXCConst.HTTP_STATUS);
//						if (status == JLXCConst.STATUS_SUCCESS) {
//							ToastUtil.show(RegisterActivity.this, "验证码已发送");
//							verifyCountdownTimer.start();
//						}
//
//						if (status == JLXCConst.STATUS_FAIL) {
//							hideLoading();
//							showConfirmAlert("提示", jsonResponse
//									.getString(JLXCConst.HTTP_MESSAGE));
//						}
//					}
//
//					@Override
//					public void onFailure(HttpException arg0, String arg1,
//							String flag) {
//						super.onFailure(arg0, arg1, flag);
//						hideLoading();
//						showConfirmAlert("提示", "获取失败，请检查网络连接!");
//					}
//				}, null));
	}

	/**
	 * 重写返回操作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			backClick();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
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
					//完成注册或者找回密码
					if (isFindPwd) {
						finishPwd();
					} else {
						// 注册
						finishRegister();
					}
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
					ToastUtil.show(RegisterActivity.this, "验证码已发送至您的手机");	
					
				}else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
//					Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();
					
				}
			} else {
				((Throwable) data).printStackTrace();
				ToastUtil.show(RegisterActivity.this, "验证码错误");
				System.out.println(((Throwable) data).toString());
//				int resId = getStringRes(RegisterActivity.this, "smssdk_network_error");
//				Toast.makeText(MainActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
//				if (resId > 0) {
//					Toast.makeText(MainActivity.this, resId, Toast.LENGTH_SHORT).show();
//				}
			}
			
		}
		
	};
}
