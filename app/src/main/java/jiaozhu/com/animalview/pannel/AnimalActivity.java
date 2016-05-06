package jiaozhu.com.animalview.pannel;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import jiaozhu.com.animalview.pannel.Adapter.BasePagerAdapter;
import jiaozhu.com.animalview.pannel.Interface.OnViewClickListener;
import jiaozhu.com.animalview.support.CApplication;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Tools;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class AnimalActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,
        OnViewClickListener {
    private FrameLayout mLayout;
    private View mToolBar;
    private SeekBar mSeekBar;
    private TextView mPageNum;
    private Button mRotation, mSplit;
    public static final String INDEX = "index";
    public static final String PAGE_NUM = "page-num";
    private FileModel currentModel;
    private HackyViewPager mViewPager;
    private List<File> list = new ArrayList<>();
    private ImagePagerAdapter adapter;

    private byte splitStatus = SPLIT_AUTO;
    private boolean uiShowed = false;
    private List<FileModel> commList;
    private boolean showLastPageByChild = false;//双页图片载入时是否应当默认先显示后一页(从后向前翻页时需要先显示后一页)

    private static final boolean autoHide = false;

    public static final byte TOUCH_CENTER = 0;
    public static final byte TOUCH_FRONT = 1;
    public static final byte TOUCH_AFTER = 2;


    public static final byte SPLIT_AUTO = 3;//自动分页
    public static final byte SPLIT_NONE = 4;//不分页
    public static final byte SPLIT_FORCE = 5;//强制分页


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
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mPageNum = (TextView) findViewById(R.id.page_num);
        mRotation = (Button) findViewById(R.id.rotation_btn);
        mSplit = (Button) findViewById(R.id.split_btn);

        commList = ((CApplication) getApplication()).list;
        adapter = new ImagePagerAdapter(list, mViewPager);
        adapter.setOnViewClickListener(this);
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
        currentModel = commList.get(getIntent().getIntExtra(INDEX, 0));
        fresh(getIntent().getIntExtra(PAGE_NUM, 0));
    }

    public void onOrientationClick(View view) {
        switch (getRequestedOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mRotation.setText("横屏");
                break;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mRotation.setText("竖屏");
                break;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                mRotation.setText("自动");
                break;
        }
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
     * 刷新
     *
     * @param pageNum 刷新之后显示的页码,-1代表最后一页
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
        mSeekBar.setMax(adapter.getCount() - 1);
        mSeekBar.setProgress(0);
        freshPageNum();
        adapter.notifyDataSetChanged();
        if (pageNum == -1)
            pageNum = adapter.getCount() - 1;
        mViewPager.setCurrentItem(pageNum, false);
    }

    private void freshPageNum() {
        mPageNum.setText((mSeekBar.getProgress() + 1) + "\n" + (mSeekBar.getMax() + 1));
    }


    @Override
    public void onViewTap(View view, float x, float y, BasePagerAdapter adapter) {
        switch (getTouchType(x, y)) {
            case TOUCH_CENTER:
                changeUi();
                break;
            case TOUCH_FRONT:
                toNextPage(adapter);
                break;
            case TOUCH_AFTER:
                toPreviousPage(adapter);
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
    private void toNextPage(BasePagerAdapter adapter) {
        showLastPageByChild = false;
        if (!adapter.toNextPage()) {
            setAnimal(commList.indexOf(currentModel) + 1, 0);
        }
    }

    /**
     * 到上一页
     */
    private void toPreviousPage(BasePagerAdapter adapter) {
        showLastPageByChild = true;
        if (!adapter.toPreviousPage()) {
            setAnimal(commList.indexOf(currentModel) - 1, -1);
        }
    }

    /**
     * 设置当前显示的目录
     *
     * @param index   需要显示的目录
     * @param pageNum 刷新之后显示的页码,-1代表最后一页
     */
    private void setAnimal(int index, int pageNum) {
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
        fresh(pageNum);
    }

    /**
     * 点击删除按钮
     *
     * @param view
     */
    public void onDeleteClick(View view) {
        showLastPageByChild = false;
        delayedRun(Constants.HIDE_UI_DELAY);
        showDialog(currentModel.getFile(), new Runnable() {
            @Override
            public void run() {
                int position = commList.indexOf(currentModel);
                commList.remove(currentModel);
                setAnimal(position, 0);
            }
        });
    }

    /**
     * 点击分页按钮
     *
     * @param view
     */
    public void onSplitClick(View view) {
        switch (splitStatus) {
            case SPLIT_AUTO:
                splitStatus = SPLIT_FORCE;
                mSplit.setText("强制分页");
                break;
            case SPLIT_FORCE:
                splitStatus = SPLIT_NONE;
                mSplit.setText("不分页");
                break;
            case SPLIT_NONE:
                splitStatus = SPLIT_AUTO;
                mSplit.setText("自动分页");
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        ((CApplication) getApplication()).saveAnimal(currentModel.getFile(), mViewPager.getCurrentItem());
        super.onPause();
    }


    /**
     * 点击下一篇
     *
     * @param view
     */
    public void onNextClick(View view) {
        showLastPageByChild = false;
        setAnimal(commList.indexOf(currentModel) + 1, 0);
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
        if (autoHide) {
            handler.removeCallbacks(autoHideToolbar);
            handler.postDelayed(autoHideToolbar, time);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    int lastPosition = 0;

    @Override
    public void onPageSelected(int position) {
        if (position > lastPosition) {
            showLastPageByChild = false;
        } else {
            showLastPageByChild = true;
        }
        lastPosition = position;
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

    /**
     * 子适配器
     */
    class ContentPagerAdapter extends BasePagerAdapter implements PhotoViewAttacher.OnViewTapListener {
        List<Bitmap> list;
        OnViewClickListener onViewClickListener;

        public ContentPagerAdapter(List<Bitmap> list, ViewPager viewPager, BasePagerAdapter parentAdapter) {
            this.list = list;
            setViewPager(viewPager);
            setParentAdapter(parentAdapter);
        }

        public OnViewClickListener getOnViewClickListener() {
            return onViewClickListener;
        }

        public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
            this.onViewClickListener = onViewClickListener;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageBitmap(list.get(position));
            photoView.setOnViewTapListener(this);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            if (onViewClickListener != null)
                onViewClickListener.onViewTap(view, x, y, this);
        }
    }


    /**
     * 主要适配器
     */
    class ImagePagerAdapter extends BasePagerAdapter {
        List<File> list;
        OnViewClickListener onViewClickListener;

        public ImagePagerAdapter(List<File> list, ViewPager viewPager) {
            this.list = list;
            setViewPager(viewPager);
        }

        public OnViewClickListener getOnViewClickListener() {
            return onViewClickListener;
        }

        public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
            this.onViewClickListener = onViewClickListener;
        }

        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            final List<Bitmap> bms = new ArrayList<>();
            final HackyViewPager viewPager = new HackyViewPager(container.getContext());


//            Bitmap bm;
//            bm = Tools.getBitmap(list.get(position).getPath());
//            switch (splitStatus) {
//                case SPLIT_AUTO:
//                    if (bm.getHeight() < bm.getWidth() * 3 / 4) {
//                        bms.add(Tools.splitBitmap(bm, 0));
//                        bms.add(Tools.splitBitmap(bm, 1));
//                    } else {
//                        bms.add(bm);
//                    }
//                    break;
//                case SPLIT_NONE:
//                    bms.add(bm);
//                    break;
//                case SPLIT_FORCE:
//                    bms.add(Tools.splitBitmap(bm, 0));
//                    bms.add(Tools.splitBitmap(bm, 1));
//                    break;
//                default:
//                    bms.add(bm);
//            }

            final ContentPagerAdapter contentPagerAdapter = new ContentPagerAdapter(bms, viewPager, this);
            contentPagerAdapter.setOnViewClickListener(onViewClickListener);
            viewPager.setAdapter(contentPagerAdapter);

            BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
                //不能再非主线程更新bms
                List<Bitmap> tempList = new ArrayList<>();

                @Override
                public void runnable() {
                    Bitmap bm;
                    bm = Tools.getBitmap(list.get(position).getPath());
                    switch (splitStatus) {
                        case SPLIT_AUTO:
                            if (bm.getHeight() < bm.getWidth() * 3 / 4) {
                                tempList.add(Tools.splitBitmap(bm, 0));
                                tempList.add(Tools.splitBitmap(bm, 1));
                            } else {
                                tempList.add(bm);
                            }
                            break;
                        case SPLIT_NONE:
                            tempList.add(bm);
                            break;
                        case SPLIT_FORCE:
                            tempList.add(Tools.splitBitmap(bm, 0));
                            tempList.add(Tools.splitBitmap(bm, 1));
                            break;
                        default:
                            tempList.add(bm);
                    }
                }

                @Override
                public void onBackgroundFinished() {
                    bms.addAll(tempList);
                    contentPagerAdapter.notifyDataSetChanged();
                    if (showLastPageByChild)
                        contentPagerAdapter.getViewPager().setCurrentItem(contentPagerAdapter.getCount() - 1, false);
                }
            });
            container.addView(viewPager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return viewPager;
        }
    }

}
