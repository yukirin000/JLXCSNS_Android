package com.jlxc.app.news.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jlxc.app.R;

public class LikeButton extends FrameLayout {

	// 背景图片
	private ImageView imageView;
	// 文字
	private TextView textView;

	public LikeButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public LikeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 将自定义组合控件的布局渲染成View
		View view = View.inflate(context, R.layout.custom_view_like_btn_layout,
				this);
		imageView = (ImageView) view
				.findViewById(R.id.iv_custom_btn_like_background);
		textView = (TextView) view.findViewById(R.id.tv_custom_btn_like);
	}

	/** 设置点赞的状态 */
	public void setStatue(boolean statue) {
		if (statue) {
			textView.setText("已赞 ");
			imageView.setImageResource(R.drawable.like_have);
		} else {
			textView.setText("点赞 ");
			imageView.setImageResource(R.drawable.like_no);
		}
	}

	/** 设置点赞的状态 */
	public void setStatue(boolean statue, int count) {
		if (statue) {
			imageView.setImageResource(R.drawable.like_have);
		} else {
			imageView.setImageResource(R.drawable.like_no);
		}

		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textView
				.getLayoutParams();
		params.rightMargin = 30;
		textView.setLayoutParams(params);

		if (count >= 10000) {
			count = count / 10000;
			textView.setText("" + count + "W+");
			return;
		} else if (count >= 1000) {
			count = count / 1000;
			textView.setText("" + count + "K+");
			return;
		} else {
			textView.setText("" + count + "");
		}
	}
}
