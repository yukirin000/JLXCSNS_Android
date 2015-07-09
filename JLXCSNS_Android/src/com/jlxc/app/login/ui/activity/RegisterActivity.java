package com.jlxc.app.login.ui.activity;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.R.bool;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.Md5Utils;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends BaseActivityWithTopBar {

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
		userPhoneNumber = intent.getStringExtra("username");
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
		} else if (verifyCodeEditTextValue.length() < 6) {
			ToastUtil.show(RegisterActivity.this, "验证码错误");
		} else if (password.length() < 6) {
			ToastUtil.show(RegisterActivity.this, "密码最少得6位啦");
		} else {
			// 忘记密码
			if (isFindPwd) {
				findPwd();
			} else {
				// 注册
				startRegister();
				// 跳转到选择学校
				Intent intent = new Intent(RegisterActivity.this,
						SelectSchoolActivity.class);
				startActivityWithRight(intent);
			}
		}
	}

	// 找回密码
	private void findPwd() {
		showLoading("数据上传中^_^", false);
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
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", userPhoneNumber);
		params.addBodyParameter("password", Md5Utils.encode(password));
		params.addBodyParameter("verify_code", verifyCodeEditTextValue);

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

							ToastUtil.show(RegisterActivity.this, "注册成功");
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

	@Override
	public int setLayoutId() {
		return R.layout.activity_register_layout;
	}

	@Override
	protected void setUpView() {
		init();
		backTextView.setText("返回");
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
	protected void loadLayout(View v) {

	}

	// 获取验证码
	private void getVerificationCode() {
		// 设置字体颜色
		revalidatedTextView.setTextColor(Color.GRAY);
		// 网络请求
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone_num", userPhoneNumber);

		HttpManager.post(JLXCConst.GET_MOBILE_VERIFY, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							ToastUtil.show(RegisterActivity.this, "验证码已发送");
							verifyCountdownTimer.start();
						}

						if (status == JLXCConst.STATUS_FAIL) {
							hideLoading();
							showConfirmAlert("提示", jsonResponse
									.getString(JLXCConst.HTTP_MESSAGE));
						}
					}

					@Override
					public void onFailure(HttpException arg0, String arg1,
							String flag) {
						super.onFailure(arg0, arg1, flag);
						hideLoading();
						showConfirmAlert("提示", "获取失败，请检查网络连接!");
					}
				}, null));
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
}
