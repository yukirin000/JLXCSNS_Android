package com.jlxc.app.base.utils;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageTools {
	/**
	 * 获取本地图片并指定高度和宽度
	 */
	public static Bitmap getNativeImage(String imagePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		Bitmap myBitmap = BitmapFactory.decodeFile(imagePath, options); // 此时返回myBitmap为空
		// 计算缩放比
		int be = (int) (options.outHeight / (float) 200);
		int ys = options.outHeight % 200;// 求余数
		float fe = ys / (float) 200;
		if (fe >= 0.5)
			be = be + 1;
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
		options.inJustDecodeBounds = false;
		myBitmap = BitmapFactory.decodeFile(imagePath, options);
		return myBitmap;
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/**
	 * 以最省内存的方式读取本地资源的图片 或者SDCard中的图片
	 * 
	 * @param imagePath
	 *            图片在SDCard中的路径
	 * @return
	 */
	public static Bitmap getSDCardImg(String imagePath) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		return BitmapFactory.decodeFile(imagePath, opt);
	}
}
