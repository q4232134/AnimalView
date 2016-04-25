package jiaozhu.com.animalview.pannel;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.HackyViewPager;
import jiaozhu.com.animalview.model.FileModel;
import jiaozhu.com.animalview.support.CApplication;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Tools;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AnimalActivity extends AppCompatActivity implements PhotoViewAttacher.OnViewTapListener, ViewPager.OnPageChangeListener {
    private FrameLayout mLayout;
    private View mToolBar;
    private SeekBar mSeekBar;
    private TextView mPageNum;
    public static final String INDEX = "index";
    public static final String PAGE_NUM = "page-num";
    private FileModel currentModel;
    private HackyViewPager mViewPager;
    private List<File> list = new ArrayList<>();
    private ImagePagerAdapter adapter;
    public static final byte TOUCH_CENTER = 0;
    public static final byte TOUCH_FRONT = 1;
    public static final byte TOUCH_AFTER = 2;
    private boolean uiShowed = false;
    private List<FileModel> commList;

    private static int HIDE_UI = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    private static int SHOW_UI = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLayout = (FrameLayout) findViewById(R.id.layout);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mToolBar = findViewById(R.id.toolsbar);

        commList = ((CApplication) getApplication()).list;
        currentModel = commList.get(getIntent().getIntExtra(INDEX, 0));
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mPageNum = (TextView) findViewById(R.id.page_num);
        adapter = new ImagePagerAdapter(list);
        adapter.setOnViewTapListener(this);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        showUI();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = -1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    this.progress = progress;
                    freshPageNum();
                    delayedRun(Constants.HIDE_UI_DELAY);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress >= 0)
                    mViewPager.setCurrentItem(progress, false);
            }
        });
        fresh(getIntent().getIntExtra(PAGE_NUM, 0));
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 显示删除对话框
     *
     * @param file     需要删除的目录
     * @param runnable 删除完成后的动作
     */
    private void showDialog(final File file, final Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除");
        builder.setMessage("是否删除此目录:" + file.getName());
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Tools.deleteAnimal(currentModel.getFile());
                if (runnable != null) runnable.run();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }


    /**
     * 刷新,刷新之后显示的当前页
     */
    private void fresh(int pageNum) {
        list.clear();
        File[] tempList = currentModel.getFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String tempName = filename.toLowerCase();
                if (tempName.endsWith(".jpg")) return true;
                if (tempName.endsWith(".png")) return true;
                if (tempName.endsWith(".bmp")) return true;
                if (tempName.endsWith(".gif")) return true;
                return false;
            }
        });
        if (tempList != null)
            for (File temp : tempList) {
                list.add(temp);
            }
        setTitle(currentModel.getFile().getName());
        mSeekBar.setMax(list.size() - 1);
        mSeekBar.setProgress(0);
        freshPageNum();
        adapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(pageNum, false);
    }

    private void freshPageNum() {
        mPageNum.setText((mSeekBar.getProgress() + 1) + "\n" + (mSeekBar.getMax() + 1));
    }


    @Override
    public void onViewTap(View view, float x, float y) {
        switch (getTouchType(x, y)) {
            case TOUCH_CENTER:
                changeUi();
                break;
            case TOUCH_FRONT:
                toNextPage();
                break;
            case TOUCH_AFTER:
                toPreviousPage();
                break;

        }
    }

    /**
     * 显示工具栏方法
     */
    Runnable showToolbar = new Runnable() {
        @Override
        public void run() {
            mToolBar.setVisibility(View.VISIBLE);
        }
    };

    /**
     * 自动隐藏工具栏方法
     */
    Runnable autoHideToolbar = new Runnable() {
        @Override
        public void run() {
            hideUI();
        }
    };

    /**
     * 改变工具栏状态
     */
    private void changeUi() {
        if (uiShowed) {
            hideUI();
        } else {
            showUI();
        }
    }

    private void showUI() {
        mLayout.setSystemUiVisibility(SHOW_UI);
        handler.postDelayed(showToolbar, 300);
        uiShowed = true;
        delayedRun(Constants.HIDE_UI_DELAY);
    }

    private void hideUI() {
        mLayout.setSystemUiVisibility(HIDE_UI);
        mToolBar.setVisibility(View.INVISIBLE);
        uiShowed = false;
    }

    /**
     * 到下一页
     */
    private void toNextPage() {
        if (mViewPager.getCurrentItem() >= adapter.getCount() - 1) {
            setAnimal(commList.indexOf(currentModel) + 1);
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, false);
        }
    }

    /**
     * 到上一页
     */
    private void toPreviousPage() {
        if (mViewPager.getCurrentItem() <= 0) {
            setAnimal(commList.indexOf(currentModel) - 1);
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, false);
        }
    }

    /**
     * 设置当前显示的目录
     *
     * @param index
     */
    private void setAnimal(int index) {
        if (index < 0) {
            Toast.makeText(this, "已经是第一篇了哦", Toast.LENGTH_SHORT).show();
            return;
        }
        if (index > commList.size() - 1) {
            Toast.makeText(this, "已经是最后一篇了哦", Toast.LENGTH_SHORT).show();
            return;
        }
        currentModel = commList.get(index);
        Snackbar.make(mViewPager, currentModel.getFile().getName(), Snackbar.LENGTH_SHORT).show();
        fresh(0);
    }

    /**
     * 点击删除按钮
     *
     * @param view
     */
    public void onDeleteClick(View view) {
        delayedRun(Constants.HIDE_UI_DELAY);
        showDialog(currentModel.getFile(), new Runnable() {
            @Override
            public void run() {
                int position = commList.indexOf(currentModel);
                commList.remove(currentModel);
                setAnimal(position);
            }
        });
    }

    @Override
    protected void onPause() {
        ((CApplication) getApplication()).saveAnimal(currentModel.getFile(), mViewPager.getCurrentItem());
        super.onPause();
    }


    /**
     * 点击
     *
     * @param view
     */
    public void onNextClick(View view) {
        setAnimal(commList.indexOf(currentModel) + 1);
        delayedRun(Constants.HIDE_UI_DELAY);
    }

    /**
     * 获取触摸的方式
     *
     * @param x
     * @param y
     * @return
     */
    byte getTouchType(float x, float y) {
        float minY, minX, maxY, maxX;
        //计算边界
        minY = mLayout.getHeight() * (1 - Constants.CENTER_HEIGHT) * 0.5f;
        maxY = mLayout.getHeight() * (1 + Constants.CENTER_HEIGHT) * 0.5f;
        minX = mLayout.getWidth() * (1 - Constants.CENTER_WIDTH) * 0.5f;
        maxX = mLayout.getWidth() * (1 + Constants.CENTER_WIDTH) * 0.5f;

        if (x > minX && x < maxX && y > minY && y < maxY) return TOUCH_CENTER;
        if (x < minX || (x < maxX && y > maxY)) return TOUCH_FRONT;
        return TOUCH_AFTER;
    }


    private void delayedRun(long time) {
        handler.removeCallbacks(autoHideToolbar);
        handler.postDelayed(autoHideToolbar, time);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSeekBar.setProgress(position);
        freshPageNum();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class ImagePagerAdapter extends PagerAdapter {
        List<File> list;
        PhotoViewAttacher.OnViewTapListener onViewTapListener;

        public PhotoViewAttacher.OnViewTapListener getOnViewTapListener() {
            return onViewTapListener;
        }

        public void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener onViewTapListener) {
            this.onViewTapListener = onViewTapListener;
        }


        public ImagePagerAdapter(List<File> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setOnViewTapListener(onViewTapListener);

            BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
                Bitmap bm;

                @Override
                public void runnable() {
                    bm = Tools.getBitmap(list.get(position).getPath());
                }

                @Override
                public void onBackgroundFinished() {
                    photoView.setImageBitmap(bm);
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
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

}
