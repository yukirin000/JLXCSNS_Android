package com.jlxc.app.base.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.lidroid.xutils.ViewUtils;
import com.jlxc.app.base.manager.ActivityManager;
import com.jlxc.app.R;

/**
 * 
 * 
 */
public abstract class BaseActivity extends FragmentActivity {
	private ProgressDialog ubabyProgressDialog;

	/**
	 */
	public boolean hasActiveNetwork(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isConnected();
		}

		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ActivityManager.getInstence().pushActivity(this);
		int id = setLayoutId();
		View v = null;
		if (0 == id) {
			new Exception(
					"Please return the layout id in setLayoutId method,as simple as R.layout.cr_news_fragment_layout")
					.printStackTrace();
		} else {
			// 娉ㄥ��view���浜�浠�
			v = LayoutInflater.from(this).inflate(setLayoutId(), null);
			setContentView(v);
			ViewUtils.inject(this);
			loadLayout(v);
		}
		setUpView();
	}

	/**
	 * ��峰��layout���id
	 * 
	 * @return
	 */
	public abstract int setLayoutId();

	protected abstract void loadLayout(View v);

	protected abstract void setUpView();

	/**
	 * ��充晶杩����
	 * 
	 * @param intent
	 */
	public void startActivityWithRight(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	}

	/**
	 * ��充晶������
	 */
	public void finishWithRight() {
		ActivityManager.getInstence().popActivity(this);
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	/**
	 * 涓���硅�����
	 * 
	 * @param intent
	 */
	public void startActivityWithBottom(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.push_bottom_in, R.anim.push_top_out);
	}

	/**
	 * 涓���归�����
	 */
	public void finishWithBottom() {
		ActivityManager.getInstence().popActivity(this);
		finish();
		overridePendingTransition(R.anim.push_top_in, R.anim.push_bottom_out);
	}

	/**
	 * ���杞�progressdialog
	 */
	public void showLoading(String message, boolean cancleable) {
		if (ubabyProgressDialog == null) {
			ubabyProgressDialog = new ProgressDialog(this);
			ubabyProgressDialog.setCancelable(cancleable);
			ubabyProgressDialog.setMessage(message + "");
		}
		if (!ubabyProgressDialog.isShowing()) {
			ubabyProgressDialog.show();
		}
	}

	/**
	 * ��抽��progressdialog
	 */
	public void hideLoading() {
		if (ubabyProgressDialog != null && ubabyProgressDialog.isShowing()) {
			ubabyProgressDialog.cancel();
			ubabyProgressDialog = null;
		}
	}

	/**
	 * ��剧ずAlertDialog涓�������纭�璁ゆ�����
	 */
	public void showConfirmAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("纭�璁�", null).show();
	}

	public void onResume() {
		super.onResume();
//		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
//		MobclickAgent.onPause(this);
	}

	/**
	 * ������杩�������浣�
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finishWithRight();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}

	public int[] getScreenSize() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return new int[] { dm.widthPixels, dm.heightPixels };
	}
}
