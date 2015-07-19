package com.jlxc.app.personal.ui.activity;

import io.rong.imkit.RongIM;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Process;
import android.view.View;

import com.jlxc.app.R;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.model.UserModel;
import com.jlxc.app.base.ui.activity.BaseActivityWithTopBar;
import com.jlxc.app.login.ui.activity.LoginActivity;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class AccountSettingActivity extends BaseActivityWithTopBar{

	@OnClick({R.id.logout_button})
	private void clickMethod(View view) {
		switch (view.getId()) {
		case R.id.logout_button:
			logout();
			break;
		default:
			break;
		}
	}
	
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_account_setting;
	}

	@Override
	protected void setUpView() {
		// TODO Auto-generated method stub
		
	}
	
	//退出
	private void logout() {
		final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
        alterDialog.setMessage("确定退出该账号吗？");
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (RongIM.getInstance() != null)
                    RongIM.getInstance().logout();
                //清空数据
                UserManager.getInstance().clear();
                UserManager.getInstance().setUser(new UserModel());
                com.jlxc.app.base.manager.ActivityManager.getInstence().popAllActivityExceptOne(LoginActivity.class);
//                killThisPackageIfRunning(AccountSettingActivity.this, "com.jlxc.app");
//                Process.killProcess(Process.myPid());

                Intent exit = new Intent(AccountSettingActivity.this, LoginActivity.class);
				startActivity(exit);
				finish();
            }
        });
        alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alterDialog.show();
	}
	
	public static void killThisPackageIfRunning(final Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
    }
}