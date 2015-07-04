package com.jlxc.app.base.manager;

import java.util.Stack;

import android.app.Activity;
import android.app.ProgressDialog;

public class ActivityManager {
	private static Stack<Activity> activityStack;
	private static ActivityManager instance;

	private ActivityManager() {
	}

	public static ActivityManager getInstence() {
		if (instance == null) {
			instance = new ActivityManager();
		}
		
		return instance;
	}

	// ï¿????ºæ??é¡?Activity
	public void popActivity(Activity activity) {
		if (activity != null) {
			// ??¨ä?????å®?ä¹???????ä¸??????ºå?????Activity??¶ï??ä¹?è¿?è¡?äº?Activity?????³é?????ï¿??
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}

	// ??·å??å½???????é¡?Activity
	public Activity currentActivity() {
		Activity activity = null;
		if (!activityStack.empty())
			activity = activityStack.lastElement();
		return activity;
	}

	// å°?å½????Activity??¨å?¥æ??ä¸?
	public void pushActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	// ï¿????ºæ??ä¸???????Activity
	@SuppressWarnings("rawtypes")
	public void popAllActivityExceptOne(Class cls) {
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			if (activity.getClass().equals(cls)) {
				break;
			}
			popActivity(activity);
		}
	}

	/**
	 * ï¿??ï¿??
	 */
	public void exitApplication() {
		System.out.println("ï¿????ºå??ï¿??");
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			popActivity(activity);
		}
	}
}
