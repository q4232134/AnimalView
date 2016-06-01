package jiaozhu.com.animalview.pannel.Adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jiaozhu on 16/5/5.
 * 嵌套viewPager，实现翻页方法，实现notify重绘
 */

abstract public class BasePagerAdapter extends PagerAdapter {
    private static final String TAG = "BasePagerAdapter";
    ViewPager mViewPager;
    BasePagerAdapter parentAdapter;

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setViewPager(ViewPager mViewPager) {
        this.mViewPager = mViewPager;
    }

    public BasePagerAdapter getParentAdapter() {
        return parentAdapter;
    }

    public void setParentAdapter(BasePagerAdapter parentAdapter) {
        this.parentAdapter = parentAdapter;
    }

    /**
     * 到下一页
     *
     * @return 翻页是否成功
     */
    public boolean toNextPage() {
        int targetItem = mViewPager.getCurrentItem() + 1;
        if (mViewPager == null) {
            Log.e(TAG, "没有设定对应的ViewPager");
            return false;
        }
        if (targetItem > getCount() - 1) {
            if (parentAdapter != null) {
                return parentAdapter.toNextPage();
            } else {
                return false;
            }
        }
        mViewPager.setCurrentItem(targetItem, false);
        return true;
    }

    /**
     * 到上一页
     *
     * @return 翻页是否成功
     */
    public boolean toPreviousPage() {
        int targetItem = mViewPager.getCurrentItem() - 1;
        if (mViewPager == null) {
            Log.e(TAG, "没有设定对应的ViewPager");
            return false;
        }
        if (targetItem < 0) {
            if (parentAdapter != null) {
                return parentAdapter.toPreviousPage();
            } else {
                return false;
            }
        }
        mViewPager.setCurrentItem(targetItem, false);
        return true;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    //用于在notify时强行重绘画面
    private int mChildCount = 0;

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }


}