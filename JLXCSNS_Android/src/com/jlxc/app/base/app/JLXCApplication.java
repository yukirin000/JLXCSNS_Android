package com.jlxc.app.base.app;

import android.app.Application;

/**
 *Application
 * 
 * @author Direct.Hao
 * 
 */
public class JLXCApplication extends Application {
	// ��ȡapplication
	public static JLXCApplication application;
	public static boolean isDebug;

	public static JLXCApplication getInstance() {
        return application;
    }
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = (JLXCApplication) getApplicationContext();
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
