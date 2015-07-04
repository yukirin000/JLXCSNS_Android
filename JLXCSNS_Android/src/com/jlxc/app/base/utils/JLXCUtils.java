package com.jlxc.app.base.utils;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.util.Base64;

@SuppressLint("SimpleDateFormat")
public class JLXCUtils {

	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat mqttSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat chattingSdf = new SimpleDateFormat("MM-dd HH:mm");
	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat chatHistorySdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static SimpleDateFormat YMD_HMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat YM = new SimpleDateFormat("yyyy-MM");

	public static SimpleDateFormat MD = new SimpleDateFormat("MM-dd");

	public static Date parseDateTime(String timeStr) {
		try {
			return mqttSdf.parse(timeStr);
		} catch (ParseException e) {
			return new Date();
		}
	}

	public static String formatDateStr(String dateStr) {
		try {
			return chattingSdf.format(mqttSdf.parse(dateStr));
		} catch (Exception e) {
			return dateStr;
		}
	}

	public static String formatDataChatHistoryList(String dateStr) {
		try {
			return chatHistorySdf.format(mqttSdf.parse(dateStr));
		} catch (Exception e) {
			try {
				return chatHistorySdf.format(YMD_HMS.parse(dateStr));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				return chatHistorySdf.format(new Date(Long.parseLong(dateStr) * 1000L));
			}
		}
	}

	public static String toDateString(Date date) {
		return mqttSdf.format(date);
	}

	/**
	 * url 加密 先Base64再url编码
	 * 
	 * @param url
	 * @return
	 */
	public static String urlEncoding(String url) {
		try {
			return URLEncoder.encode(Base64.encodeToString(url.getBytes(), Base64.DEFAULT), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return url;
		}
	}
}
