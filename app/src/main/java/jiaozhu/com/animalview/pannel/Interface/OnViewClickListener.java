package jiaozhu.com.animalview.pannel.Interface;

import android.view.View;

import jiaozhu.com.animalview.pannel.Adapter.BasePagerAdapter;

/**
 * Created by jiaozhu on 16/5/5.
 */

/**
 * 单击接口
 */
public interface OnViewClickListener {
    void onViewTap(View view, float x, float y, BasePagerAdapter adapter);
}
