package com.jlxc.app.login.ui.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;

import com.alibaba.fastjson.JSONObject;
import com.jlxc.app.R;
import com.jlxc.app.base.helper.JsonRequestCallBack;
import com.jlxc.app.base.helper.LoadDataHandler;
import com.jlxc.app.base.manager.HttpManager;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.base.utils.Md5Utils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends BaseActivityWithTopBar {

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
	// 标题
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
	@OnClick({ R.id.base_tv_back, R.id.next_button, R.id.revalidated_textview })
	public void viewCickListener(View view) {
		switch (view.getId()) {
		case R.id.base_tv_back:
			backClick();
			break;
		case R.id.next_button:
			nextClick();
			break;
		case R.id.revalidated_textview:
			getVerificationCode();
			break;
		default:
			break;
		}
	}

	// 初始化数据
	private void init() {
		Intent intent = getIntent();
		userPhoneNumber = intent.getStringExtra("username");
	}

	// 点击返回
	private void backClick() {
		if (0 != countdownValue) {
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
									RegisterActivity.this.finish();
								}
							}).show();
		} else {
			RegisterActivity.this.finish();
		}
	}

	// 点击下一步按钮
	private void nextClick() {
		verifyCodeEditTextValue = verifycodeEditText.getText().toString();
		password = passwdeEditText.getText().toString();
		// 判断输入值是否正确
		if (verifyCodeEditTextValue.length() == 0) {
			Toast.makeText(RegisterActivity.this, "验证码未输入", Toast.LENGTH_SHORT)
					.show();
		} else if (verifyCodeEditTextValue.length() < 6) {
			Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT)
					.show();
		} else if (password.length() < 6) {
			Toast.makeText(RegisterActivity.this, "密码最少得6位啦",
					Toast.LENGTH_SHORT).show();
		} else {
			startRegister();
		}
	}

	// 开始注册
	private void startRegister() {
		RegisterActivity.this.showLoading("正在注册", false);
		RequestParams params = new RequestParams();
		params.addBodyParameter("username", userPhoneNumber);
		params.addBodyParameter("password", Md5Utils.encode(password));
		params.addBodyParameter("verify_code",
				String.valueOf(verifyCodeEditTextValue));

		HttpManager.post(JLXCConst.REGISTER_USER, params,
				new JsonRequestCallBack<String>(new LoadDataHandler<String>() {

					@Override
					public void onSuccess(JSONObject jsonResponse, String flag) {
						super.onSuccess(jsonResponse, flag);
						int status = jsonResponse
								.getInteger(JLXCConst.HTTP_STATUS);
						if (status == JLXCConst.STATUS_SUCCESS) {
							hideLoading();
							JSONObject object = jsonResponse
									.getJSONObject(JLXCConst.HTTP_RESULT);
							Toast.makeText(RegisterActivity.this, "注册成功",
									Toast.LENGTH_SHORT).show();
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
						showConfirmAlert("提示", "注册失败，请检查网络连接!");
					}

				}, null));
	}

	@Override
	public int setLayoutId() {
		return R.layout.register_layout;
	}

	@Override
	protected void setUpView() {
		init();
		backTextView.setText("返回");
		titletTextView.setText("注册");
		phonePromptTextView.setText("验证码已发送至：" + userPhoneNumber);
		revalidatedTextView.setEnabled(false);
		verifyCountdownTimer = new CountDownTimer(60000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				countdownValue = (int) millisUntilFinished / 1000;
				revalidatedTextView.setText(countdownValue + "s后重发");
			}

			@Override
			public void onFinish() {
				revalidatedTextView.setEnabled(true);
				revalidatedTextView.setText("获取验证码");
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
							Toast.makeText(RegisterActivity.this, "验证码已发送",
									Toast.LENGTH_SHORT).show();
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
}
