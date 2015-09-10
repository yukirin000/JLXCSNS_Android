package com.jlxc.app.group.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.jlxc.app.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * 左侧group菜单popupwindow
 * */
public class GroupMenuPopWindow extends PopupWindow {

	// 布局
	private View conentView;
	// 屏幕宽度
	private int screenWidth;
	// 屏幕高度
	private int screenHeight;
	// 频道类别list
	private ListView groupTypeListView;
	// 数据
	private ArrayList<HashMap<String, String>> groupTypeData = new ArrayList<HashMap<String, String>>();

	public GroupMenuPopWindow(final Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 获取布局
		conentView = inflater.inflate(R.layout.popup_window_group_menu, null);

		// 获取屏幕尺寸
		DisplayMetrics displayMet = context.getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;

		// 设置PopupWindow的View
		this.setContentView(conentView);
		// 设置窗体的尺寸
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(screenHeight * 1 / 3);
		// 设置窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		// 刷新状态
		this.update();
		// 背景变暗
		this.setBackgroundDrawable(new ColorDrawable(0xff000000));
		// 设置窗体动画效果
		this.setAnimationStyle(R.style.anim_group_menu);
	}

	/**
	 * 显示窗体
	 */
	public void showPopupWindow(View parent) {
		if (!this.isShowing()) {
			this.showAsDropDown(parent, 0, 0);
		} else {
			this.dismiss();
		}
	}
}
