package com.jlxc.app.base.ui.view.gallery.imageloader;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.jlxc.app.R;
import com.jlxc.app.base.ui.view.gallery.utils.CommonAdapter;
import com.jlxc.app.base.ui.view.gallery.utils.ViewHolder;

public class GalleyAdapter extends CommonAdapter<String> {

	private final static int MAX_SELECT_COUNT = 9;
	// 用户选择的图片，存储为图片的完整路径
	private static List<String> mSelectedImage = new LinkedList<String>();
	// 已经选中的数量
	private int haveSelectCount = 0;
	// 回调接口
	private OnItemClickClass onItemClickClass;
	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public GalleyAdapter(Context context, List<String> mDatas,
			int itemLayoutId, String dirPath, int count) {
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.haveSelectCount = count;
	}

	@Override
	public void convert(final ViewHolder helper, final String item) {
		// 设置no_pic
		helper.setImageResource(R.id.btn_galley_item_select,
				R.drawable.pictures_no);
		// 设置no_selected
		helper.setImageResource(R.id.btn_galley_item_select,
				R.drawable.picture_unselected);
		// 设置图片
		helper.setImageByUrl(R.id.img_galley_item, mDirPath + "/" + item);

		final ImageView mImageView = helper.getView(R.id.img_galley_item);
		final ImageView mSelect = helper.getView(R.id.btn_galley_item_select);

		mImageView.setColorFilter(null);
		// 设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener() {
			// 选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v) {
				// 已经选择过该图片
				if (mSelectedImage.contains(mDirPath + "/" + item)) {
					mSelectedImage.remove(mDirPath + "/" + item);
					mSelect.setImageResource(R.drawable.picture_unselected);
					mImageView.setColorFilter(null);
				} else if (mSelectedImage.size() + haveSelectCount < MAX_SELECT_COUNT) {
					mSelectedImage.add(mDirPath + "/" + item);
					mSelect.setImageResource(R.drawable.pictures_selected);
					mImageView.setColorFilter(Color.parseColor("#77000000"));
				}
				onItemClickClass.OnItemClick(mSelectedImage.size()
						+ haveSelectCount);
			}
		});

		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (mSelectedImage.contains(mDirPath + "/" + item)) {
			mSelect.setImageResource(R.drawable.pictures_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}

	}

	// 得到返回的数据
	public List<String> getSelectedImageList() {
		return mSelectedImage;
	}

	// 清空选中的值
	public static void clearSelectedImageList() {
		mSelectedImage.clear();
	}

	public void setClickCallBack(OnItemClickClass clickCallBack) {
		onItemClickClass = clickCallBack;
	}

	// 选则事件回调
	public interface OnItemClickClass {
		public void OnItemClick(int count);
	}
}
