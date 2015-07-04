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

	// �????��??�?Activity
	public void popActivity(Activity activity) {
		if (activity != null) {
			// ??��?????�?�???????�??????��?????Activity??��??�?�?�?�?Activity?????��?????�??
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}

	// ??��??�???????�?Activity
	public Activity currentActivity() {
		Activity activity = null;
		if (!activityStack.empty())
			activity = activityStack.lastElement();
		return activity;
	}

	// �?�????Activity??��?��??�?
	public void pushActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	// �????��??�???????Activity
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
	 * �??�??
	 */
	public void exitApplication() {
		System.out.println("�????��??�??");
		while (true) {
			Activity activity = currentActivity();
			if (activity == null) {
				break;
			}
			popActivity(activity);
		}
	}
}
