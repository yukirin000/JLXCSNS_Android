package com.jlxc.app.login.ui.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.activity.BaseActivity;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class RegisterActivity extends BaseActivity {
	private int countdownValue = 0;
	private CountDownTimer verifyCountdown = null;
	private int sysVerificationCode;// 系统发送的验证码
	private String password = "";// 密码

	// 返回按钮
	@ViewInject(R.id.back_button)
	private Button backButton;

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

	// 点击返回按钮
	@OnClick(R.id.back_button)
	public void registerClick(View view) {
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
									RegisterActivity.this.finish();
								}
							}).show();
		} else {
			RegisterActivity.this.finish();
		}
	}

	// 点击下一步按钮
	@OnClick(R.id.next_button)
	public void nextClick(View view) {
		String verifyCodeEditTextValue = verifycodeEditText.getText()
				.toString();

		if (verifyCodeEditTextValue.length() == 0) {
			Toast.makeText(RegisterActivity.this, "验证码未输入", Toast.LENGTH_SHORT)
					.show();
		} else if (password.length() == 0) {
			Toast.makeText(RegisterActivity.this, "密码未输入", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(RegisterActivity.this,
					"验证码为：" + verifyCodeEditTextValue + " 密码为：" + password,
					Toast.LENGTH_SHORT).show();
		}
	}

	// 重新发送验证码
	@OnClick(R.id.revalidated_textview)
	public void revalidatedClick(View view) {
		// 重新获取新的验证码....

		verifyCountdown.start();
		Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public int setLayoutId() {
		return R.layout.register_layout;
	}

	@Override
	protected void setUpView() {
		phonePromptTextView.setText("验证码已发送至：" + "12351236215");
		revalidatedTextView.setEnabled(false);
		verifyCountdown = new CountDownTimer(60000, 1000) {

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
		verifyCountdown.start();

		// 监听密码框文本的变化
		passwdeEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				password = passwdeEditText.getText().toString();
				// 密码过滤
				String legalPasword = stringFilter(password);
				if (!password.equals(legalPasword)) {
					passwdeEditText.setText(legalPasword);
					// 设置新的光标所在位置
					passwdeEditText.setSelection(legalPasword.length());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	protected void loadLayout(View v) {

	}

	// 密码字符的限制
	public static String stringFilter(String str) throws PatternSyntaxException {
		// 只允许字母、数字
		String regEx = "[^a-zA-Z0-9]";
		Pattern strPattern = Pattern.compile(regEx);
		Matcher match = strPattern.matcher(str);
		return match.replaceAll("").trim();
	}

	// 获取网络上的数据
	private void initNetData() {
		sysVerificationCode = 0;
	}
}
