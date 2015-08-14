package com.jlxc.app.news.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.jlxc.app.base.ui.view.CustomListViewDialog;
import com.jlxc.app.base.ui.view.CustomListViewDialog.ClickCallBack;
import com.jlxc.app.base.utils.ToastUtil;
import com.lidroid.xutils.view.annotation.event.OnTouch;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.widget.TextView;

public class TextViewHandel {

	private static Context mContext;
	private static String mContent;
	private long mLastActionDownTime = -1;

	public TextViewHandel(Context context, String str) {
		mContext = context;
		mContent = str;
	}

	private class NoLineClickSpan extends ClickableSpan {

		private String mUrl;

		public NoLineClickSpan(String text) {
			super();
			this.mUrl = text;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			// 设置链接的文本颜色
			ds.setColor(ds.linkColor);
			// 去掉下划线
			ds.setUnderlineText(false);
		}

		@Override
		public void onClick(View widget) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(mUrl);
			intent.setData(content_url);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}

	//
	public static OnLongClickListener getLongClickListener(
			final Context context, final String str) {
		return new OnLongClickListener() {

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@SuppressLint("NewApi")
			@Override
			public boolean onLongClick(View arg0) {
				List<String> menuList = new ArrayList<String>();
				menuList.add("复制内容");
				final CustomListViewDialog downDialog = new CustomListViewDialog(
						context, menuList);
				downDialog.setClickCallBack(new ClickCallBack() {

					@Override
					public void Onclick(View view, int which) {
						//
						ClipboardManager clipboard = (ClipboardManager) context
								.getSystemService(Context.CLIPBOARD_SERVICE);
						clipboard.setPrimaryClip(ClipData.newPlainText(null,
								str));
						downDialog.dismiss();
						ToastUtil.show(context, "已复制");
					}
				});
				if (null != downDialog && !downDialog.isShowing()) {
					downDialog.show();
				}

				return false;
			}
		};
	}

	public void setTextContent(TextView tv) {
		Spannable spannable = new SpannableString(mContent);
		Pattern b = Pattern
				.compile("((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnprwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eosuw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agksyz]|v[aceginu]|w[fs]|(?:xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-80akhbyknj4f|xn\\-\\-9t4b11yi5a|xn\\-\\-deba0ad|xn\\-\\-g6w251d|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-zckzah)|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9 -퟿豈-﷏ﷰ-￯\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?");
		tv.setText(spannable);
		Linkify.addLinks(tv, b, "http://");
		tv.setLinksClickable(true);

		tv.setMovementMethod(LinkMovementMethod.getInstance());
		CharSequence text = tv.getText();
		if (text instanceof Spannable) {
			int end = text.length();
			Spannable sp = (Spannable) tv.getText();
			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
			SpannableStringBuilder style = new SpannableStringBuilder(text);
			style.clearSpans();
			for (URLSpan url : urls) {
				NoLineClickSpan myURLSpan = new NoLineClickSpan(url.getURL());
				style.setSpan(myURLSpan, sp.getSpanStart(url),
						sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			tv.setText(style);
		}

		tv.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				CharSequence text = mContent;
				if (text != null && text instanceof Spannable) {
					handleLinkMovementMethod((TextView) view, (Spannable) text,
							event);
				}
				return false;
			}
		});
	}

	private boolean handleLinkMovementMethod(TextView widget, Spannable buffer,
			MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			ClickableSpan[] link = buffer.getSpans(off, off,
					ClickableSpan.class);

			if (link.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					long actionUpTime = System.currentTimeMillis();
					if (actionUpTime - mLastActionDownTime > ViewConfiguration
							.getLongPressTimeout()) {
						// 长按事件，取消LinkMovementMethod处理，即不处理ClickableSpan点击事件
						return false;
					}
					link[0].onClick(widget);
					Selection.removeSelection(buffer);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(link[0]),
							buffer.getSpanEnd(link[0]));
					mLastActionDownTime = System.currentTimeMillis();
				}
			}
		}

		return false;
	}
}