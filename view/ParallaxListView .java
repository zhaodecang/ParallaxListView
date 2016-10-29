package com.zdc.parallaxlayout.view;

import java.util.ArrayList;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zdc.parallaxlayout.R;
import com.zdc.parallaxlayout.bean.IndexBean;
import com.zdc.parallaxlayout.global.TestData;
import com.zdc.parallaxlayout.utils.IndexBeanUtil;
import com.zdc.parallaxlayout.view.SwipLayout.OnControllerStateChangedLintener;

/**
 * 描述：用于展示通过拼音首字母大写分组的names集合的listview
 * <p>
 * Created by zhaodecang on 2016-10-24下午4:01:32
 * <p>
 * 邮箱：zhaodecang@gmail.comParallaxListView
 */
public class ParallaxListView  extends ListView {
	/** 默认展示的数据 **/
	private String[] listContents = TestData.NAMES;
	private ArrayList<IndexBean> indexBeans;
	/** 视察效果的顶部view **/
	private ImageView mParallaxView;
	private int mOriHeight, mMaxHeight;
	public ParallaxListView (Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public ParallaxListView (Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ParallaxListView (Context context) {
		super(context);
	}
	/** 在listview中展示默认的的数据集合,返回的集合中每一个bean对象已经包含拼音 **/
	public ArrayList<IndexBean> setAdapter() {
		return setAdapter(listContents);
	}
	/** 在listview中展示指定的的数据集合,返回的集合中每一个bean对象已经包含拼音 **/
	public ArrayList<IndexBean> setAdapter(ArrayList<String> listContents) {
		String[] contents = (String[]) listContents.toArray();
		return setAdapter(contents);
	}
	/** 在listview中展示指定的的数据集合,返回的集合中每一个bean对象已经包含拼音 **/
	public ArrayList<IndexBean> setAdapter(String[] listContents) {
		this.listContents = listContents;
		indexBeans = IndexBeanUtil.convertArray2Bean(listContents);
		super.setAdapter(new QilvAdapter());
		return indexBeans;
	}
	// ----------------------↓处理视察效果↓----------------------
	public void setParallaxView(ImageView parallaxView) {
		this.mParallaxView = parallaxView;
		if (mParallaxView != null) {
			OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					mParallaxView.getViewTreeObserver()
							.removeOnGlobalLayoutListener(this);
					mOriHeight = mParallaxView.getHeight();
				}
			};
			mParallaxView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
			mMaxHeight = mParallaxView.getDrawable().getIntrinsicHeight();
		}
	}
	@Override
	protected boolean overScrollBy(int dx, int dy, int sx, int sy, int srx, int sry,
			int msx, int msy, boolean isTouch) {
		if (dy < 0 && isTouch) {
			int newHeight = mParallaxView.getHeight() + Math.abs(dy) / 2;
			if (newHeight > mMaxHeight) {
				newHeight = mMaxHeight;
			}
			changeParallaxHeight(newHeight);
		}
		return super.overScrollBy(dx, dy, sx, sy, srx, sry, msx, msy, isTouch);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_UP:
			ValueAnimator animator = ValueAnimator.ofInt(mParallaxView.getHeight(),
					mOriHeight);
			animator.setDuration(200);
			animator.setInterpolator(new OvershootInterpolator(4));
			animator.addUpdateListener(listener);
			animator.start();
			break;
		}
		return super.onTouchEvent(ev);
	}
	AnimatorUpdateListener listener = new AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			changeParallaxHeight((Integer) animation.getAnimatedValue());
		}
	};
	private void changeParallaxHeight(int newHeight) {
		ViewGroup.LayoutParams params = mParallaxView.getLayoutParams();
		params.height = newHeight;
		mParallaxView.setLayoutParams(params);
	}
	// ----------------------↑处理视察效果↑----------------------
	// ----------------------↓数据适配器↓----------------------
	/** ParallaxListView 的数据适配器 **/
	private class QilvAdapter extends BaseAdapter implements OnClickListener,
			OnControllerStateChangedLintener {
		private ArrayList<SwipLayout> swipLayouts = new ArrayList<SwipLayout>();
		@Override
		public int getCount() {
			return indexBeans.size();
		}
		@Override
		public IndexBean getItem(int position) {
			return indexBeans.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(parent.getContext(), R.layout.item_man, null);
				holder.tvIndex = (TextView) convertView.findViewById(R.id.tv_index);
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 显示每一个名字及其姓名的首字母大写
			holder.tvIndex.setText(String.valueOf(getItem(position).getFirstLetter()));
			holder.tvName.setText(getItem(position).getName());
			// 同一组首字母只显示一个
			if (position > 0) {
				char preLetter = getItem(position - 1).getFirstLetter();
				char CurrentLetter = getItem(position).getFirstLetter();
				if (preLetter == CurrentLetter) {
					holder.tvIndex.setVisibility(View.GONE);
				} else {
					holder.tvIndex.setVisibility(View.VISIBLE);
				}
			} else {
				holder.tvIndex.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
	}
	private static class ViewHolder {
		TextView tvIndex, tvName;
	}
}
