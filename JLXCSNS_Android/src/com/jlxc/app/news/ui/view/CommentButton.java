package com.jlxc.app.news.ui.view;

import com.jlxc.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentButton extends FrameLayout {

	// 背景图片
	private ImageView imageView;
	// 文字
	private TextView textView;

	public CommentButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CommentButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 将自定义组合控件的布局渲染成View
		View view = View.inflate(context,
				R.layout.custom_view_comment_btn_layout, this);
		imageView = (ImageView) view.findViewById(R.id.iv_comment_background);
		textView = (TextView) view.findViewById(R.id.tv_comment_content);
	}

	/** 设置点赞的状态 */
	public void setContent(int content) {
		if (content >= 10000) {
			content = content / 10000;
			textView.setText("" + content + "W+");
			return;
		}
		if (content >= 1000) {
			content = content / 1000;
			textView.setText("" + content + "K+");
			return;
		}
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textView
				.getLayoutParams();
		params.rightMargin = 30;
		textView.setLayoutParams(params);
		textView.setText("" + content);
	}
}
