package com.jlxc.app.base.app;

import io.rong.imkit.RongIM;

import com.jlxc.app.base.helper.RongCloudEvent;
import com.jlxc.app.base.manager.DBManager;
import com.jlxc.app.base.manager.UserManager;
import com.jlxc.app.base.utils.FileUtil;
import com.jlxc.app.base.utils.LogUtils;

import android.app.Application;

/**
 *Application
 * 
 * @author Direct.Hao
 * 
 */
public class JLXCApplication extends Application {
	// application
	public static JLXCApplication application;
	public static boolean isDebug;

	public static JLXCApplication getInstance() {
        return application;
    }
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = (JLXCApplication) getApplicationContext();
		//数据库初始化
		DBManager.getInstance(); 
		//FileUtils初始化
		FileUtil.makeDirs(); 
		//融云初始化
		RongIM.init(this);
		// 融云SDK事件监听处理
        RongCloudEvent.init(this);
        //初始化用户模型
        UserManager.getInstance().getUser();
	}  

////	private void init() {
////		new Thread(new Runnable() {
////			@Override
////			public void run() {
////			}
////		}).start();
////	}
//
//	@Override
//	public void onTerminate() {
//		super.onTerminate();
//	}
}
