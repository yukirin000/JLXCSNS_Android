package com.jlxc.app.base.utils;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;

/***
 * 一天以内按距离现在的时间是多少，eg:4分钟前，16小时前 超过一天，两天以内显示为昨天+时间，例如：昨天 12:22
 * 超过两天，三天以内显示为前天+时间，例如：昨天 12:22 超过三天，同一年以内显示为：月份+日期+时间，eg：5月12日 12:20
 * 超过一年的显示为：年份+月份+日期+时间，eg：2012年5月12日 12:20
 */
@SuppressLint("SimpleDateFormat")
public class TimeHandle {

	// 获取要显示的时间格式
	public static String getShowTimeFormat(String dateStr) {
		String decTime = "";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date dt = new Date();
		Date time = null;

		try {
			time = format.parse(dateStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);

			// 与当前时间差，毫秒ms
			long diff = dt.getTime() - time.getTime();

			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);

			int year = calendar.get(Calendar.YEAR); // 获取年;
			int month = calendar.get(Calendar.MONTH); // 获取月;
			int day = calendar.get(Calendar.DATE); // 获取日;
			int hour = calendar.get(Calendar.HOUR); // 获取小时;
			int min = calendar.get(Calendar.MINUTE); // 获取分钟;

			calendar = Calendar.getInstance();
			int currentYear = calendar.get(Calendar.YEAR); // 获取当前年份

			if (diffDays <= 0 && diffHours <= 0) {
				if (diffMinutes == 0) {
					diffMinutes = 1;
				}
				decTime = diffMinutes + "分钟前";
			} else if (diffDays <= 0 && diffHours > 0) {
				decTime = diffHours + "小时前";
			} else if (1 == diffDays) {
				decTime = "昨天 " + hour + ":" + min;
			} else if (2 == diffDays) {
				decTime = "前天 " + hour + ":" + min;
			} else if (currentYear == year) {
				decTime = month + "月" + day + "日 " + hour + ":" + min;
			} else {
				decTime = year + "年" + month + "月" + day + "日 " + hour + ":"
						+ min;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decTime;
	}

	/**
	 * 当前系统的时间
	 * */

	public static String getCurrentDataStr() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sDateFormat.format(new java.util.Date());
	}
}
