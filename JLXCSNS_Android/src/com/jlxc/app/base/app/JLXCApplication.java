package com.jlxc.app.base.app;

import com.jlxc.app.base.manager.DBManager;
import com.jlxc.app.base.utils.FileUtil;

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
