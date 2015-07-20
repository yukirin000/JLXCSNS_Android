package com.jlxc.app.message.ui.fragment;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.jlxc.app.R;
import com.jlxc.app.base.model.NewsPushModel;
import com.jlxc.app.base.ui.fragment.BaseFragment;
import com.jlxc.app.base.utils.JLXCConst;
import com.jlxc.app.demo.ui.fragment.FragmentPage1;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

public class MessageMainFragment extends BaseFragment {

	@ViewInject(R.id.vPager)
	private ViewPager mPager;//页卡内容
	//会话tab
	@ViewInject(R.id.conversation_text_view)
	private TextView conversationTextView;
	//通知tab
	@ViewInject(R.id.notify_text_view)
    private TextView notifyTextView;
	@ViewInject(R.id.img_cursor)
    private ImageView cursor;// 动画图片
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
	// 横线图片宽度
	private int cursorWidth;
	// 屏幕的尺寸
	private int screenWidth = 0, screenHeight = 0;
	
	@OnClick({R.id.conversation_text_view,R.id.notify_text_view})
    private void clickEvent(View view) {
		switch (view.getId()) {
		case R.id.conversation_text_view:
			mPager.setCurrentItem(0);
			break;
		case R.id.notify_text_view:
			mPager.setCurrentItem(1);				
			break;
		default:
			break;
		}
		
	}
    
	@Override
	public int setLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.fragment_message_main;
	}

	@Override
	public void loadLayout(View rootView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUpViews(View rootView) {
		// TODO Auto-generated method stub
		initImage();
		initViewPager();
	}
	
	/*
	 * 初始化图片的位移像素
	 */
	public void initImage() {
		
		// 获取屏幕尺寸
		DisplayMetrics displayMet = getResources().getDisplayMetrics();
		screenWidth = displayMet.widthPixels;
		screenHeight = displayMet.heightPixels;
		
		cursorWidth = 60;
		int cursorheight = 10;

		offset = (screenWidth / 2 - cursorWidth) / 2;
		// 设置游标的尺寸与位置
		LayoutParams cursorParams = (LayoutParams) cursor
				.getLayoutParams();
		cursorParams.width = cursorWidth;
		cursorParams.height = cursorheight;
		cursorParams.leftMargin = offset;
		cursor.setLayoutParams(cursorParams);
		cursor.setScaleType(ImageView.ScaleType.FIT_XY);
	}
	
	/**
     * 初始化ViewPager
     */
    @SuppressLint("InflateParams") 
    private void initViewPager() {
    	
        mPager.setAdapter(new MessageFragmentPagerAdapter(getChildFragmentManager()));;
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }
    
    /**
     * ViewPager适配器
*/
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;
        
        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }
    
    
    private class MessageFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public MessageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                        ConversationListFragment listFragment = ConversationListFragment.getInstance();
                        Uri uri = Uri.parse("rong://" + getActivity().getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//讨论组
                                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//应用公众服务。
                                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//系统
                                .build();
                        listFragment.setUri(uri);
                        fragment = listFragment;
                    break;
                case 1:
                	fragment = new NotifyNewsFragment();
                    break;

            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
    
    
    /**
     * 页卡切换监听
*/
    public class MyOnPageChangeListener implements OnPageChangeListener {

    	int one = offset * 2 + cursorWidth;// 页卡1 -> 页卡2 偏移量
		float lastpostion = 0;

		public void onPageScrollStateChanged(int index) {
			currIndex = index;

			if (index == 1) {
				//设置已读
				NewsPushModel.setIsRead();
			}
			
			//通知刷新
			Intent tabIntent = new Intent(JLXCConst.BROADCAST_TAB_BADGE);
			getActivity().sendBroadcast(tabIntent);
			Intent messageIntent = new Intent(JLXCConst.BROADCAST_NEW_MESSAGE_PUSH);
			getActivity().sendBroadcast(messageIntent);
			
		}

		// CurrentTab:当前页面序号
		// OffsetPercent:当前页面偏移的百分比
		// offsetPixel:当前页面偏移的像素位置
		public void onPageScrolled(int CurrentTab, float OffsetPercent,
				int offsetPixel) {
			// 下标的移动动画
			Animation animation = new TranslateAnimation(one * lastpostion, one
					* (CurrentTab + OffsetPercent), 0, 0);

			lastpostion = OffsetPercent + CurrentTab;
			// True:图片停在动画结束位置
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

		public void onPageSelected(int arg0) {

		}
    }

}
