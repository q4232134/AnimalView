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
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jiaozhu.com.animalview.R;
import jiaozhu.com.animalview.commonTools.BackgroundExecutor;
import jiaozhu.com.animalview.commonTools.DoubleTapListener;
import jiaozhu.com.animalview.commonTools.HackyViewPager;
import jiaozhu.com.animalview.pannel.Adapter.BasePagerAdapter;
import jiaozhu.com.animalview.pannel.Interface.OnViewClickListener;
import jiaozhu.com.animalview.support.Constants;
import jiaozhu.com.animalview.support.Preferences;
import jiaozhu.com.animalview.support.Tools;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressWarnings("WrongConstant")
public abstract class BaseAnimalActivity<T> extends AppCompatActivity implements ViewPager.OnPageChangeListener,
        OnViewClickListener {
    protected static final int LAST_PAGE = -2;
    protected FrameLayout mLayout;
    protected View mToolBar;
    protected SeekBar mSeekBar;
    protected TextView mPageNum;
    protected Button mRotation, mSplit, mDirection;
    public static final String PARAM_PATH = "param_path";
    protected T currentFile;
    protected HackyViewPager mViewPager;
    protected List<T> list = new ArrayList<>();
    protected ImagePagerAdapter<T> adapter;


    protected byte splitStatus = Preferences.SPLIT_AUTO;
    protected byte directionStatus = Preferences.DIRECTION_LR;
    protected boolean uiShowed = false;
    protected boolean showLastPageByChild = false;//双页图片载入时是否应当默认先显示后一页(从后向前翻页时需要先显示后一页)

    protected static final boolean autoHide = true;

    public static final byte TOUCH_CENTER = 0;
    public static final byte TOUCH_FRONT = 1;
    public static final byte TOUCH_AFTER = 2;

    protected Runnable doubleClickAction = null;
    protected Runnable longClickAction = null;

    protected Runnable noneAction = null;

    protected Runnable exitAction = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    protected Runnable nextAction = new Runnable() {
        @Override
        public void run() {
            onNextClick(null);
        }
    };

    protected Runnable deleteAction = new Runnable() {
        @Override
        public void run() {
            onDeleteClick(null);
        }
    };


    protected static int HIDE_UI = View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    protected static int SHOW_UI = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    protected Handler handler = new Handler();


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
        mDirection = (Button) findViewById(R.id.direction_btn);

        initData();
    }

    protected void initData() {
        adapter = new ImagePagerAdapter<T>(list, mViewPager) {
            @Override
            Bitmap getBitmap(T t) {
                return getBitmapByFile(t);
            }
        };
        adapter.setOnViewClickListener(this);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);

        /**
         * 初始化设置
         */
        setRequestedOrientation(Preferences.getInstance().getsRotation());
        splitStatus = Preferences.getInstance().getsSplit();
        directionStatus = Preferences.getInstance().getsDirection();
        mRotation.setText(getRotationName());
        mSplit.setText(getSplitName());
        mDirection.setText(getDirectName());
        switch (Preferences.getInstance().getDoubleClickAction()) {
            case Preferences.ACTION_NONE:
                doubleClickAction = noneAction;
                break;
            case Preferences.ACTION_DELETE:
                doubleClickAction = deleteAction;
                break;
            case Preferences.ACTION_EXIT:
                doubleClickAction = exitAction;
                break;
            case Preferences.ACTION_NEXT:
                doubleClickAction = nextAction;
                break;
            default:
                doubleClickAction = noneAction;
        }
        switch (Preferences.getInstance().getLongClickAction()) {
            case Preferences.ACTION_NONE:
                longClickAction = noneAction;
                break;
            case Preferences.ACTION_DELETE:
                longClickAction = deleteAction;
                break;
            case Preferences.ACTION_EXIT:
                longClickAction = exitAction;
                break;
            case Preferences.ACTION_NEXT:
                longClickAction = nextAction;
                break;
            default:
                longClickAction = noneAction;
        }


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
                if (progress >= 0) {
                    //拖动进度条后初始化最后位置
                    lastPosition = 0;
                    showLastPageByChild = false;
                    mViewPager.setCurrentItem(progress, false);
                }
            }
        });
        setAnimal(getFileByPath(getIntent().getStringExtra(PARAM_PATH)));
    }

    /**
     * 单击旋转按钮
     *
     * @param view
     */
    public void onOrientationClick(View view) {
        switch (getRequestedOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
        }
        mRotation.setText(getRotationName());
        Preferences.getInstance().setsRotation(getRequestedOrientation());
    }

    /**
     * 获取旋转状态名称
     *
     * @return
     */
    protected String getRotationName() {
        switch (getRequestedOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                return "横屏";
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                return "竖屏";
            case ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED:
                return "自动";
            default:
                return "";
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 显示删除对话框
     *
     * @param file 需要删除的目录
     */
    protected void showDeleteDialog(final T file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除");
        builder.setMessage("是否删除此目录:" + getName(file));
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setAnimal(deleteFile(currentFile));
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
    protected void fresh(int pageNum) {
        list.clear();
        List<T> tempList = listFiles(currentFile);
        if (tempList != null)
            for (T temp : tempList) {
                list.add(temp);
            }
        setTitle(getName(currentFile));
        mSeekBar.setMax(adapter.getCount() - 1);
        mSeekBar.setProgress(0);
        freshPageNum();
        adapter.notifyDataSetChanged();
        if (pageNum == LAST_PAGE)
            pageNum = adapter.getCount() - 1;
        mViewPager.setCurrentItem(pageNum, false);
    }

    protected void freshPageNum() {
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
    protected void changeUi() {
        if (uiShowed) {
            hideUI();
        } else {
            showUI();
        }
    }

    protected void showUI() {
        mLayout.setSystemUiVisibility(SHOW_UI);
        handler.postDelayed(showToolbar, 300);
        uiShowed = true;
        delayedRun(Constants.HIDE_UI_DELAY);
    }

    protected void hideUI() {
        mLayout.setSystemUiVisibility(HIDE_UI);
        mToolBar.setVisibility(View.INVISIBLE);
        uiShowed = false;
    }

    /**
     * 到下一页
     */
    protected void toNextPage(BasePagerAdapter adapter) {
        showLastPageByChild = false;
        if (!adapter.toNextPage()) {
            T temp = getNextFile(currentFile);
            if (temp == null) {
                Toast.makeText(this, "已经是最后一篇了哦", Toast.LENGTH_SHORT).show();
            } else {
                setAnimal(temp);
            }
        }
    }

    /**
     * 到上一页
     */
    protected void toPreviousPage(BasePagerAdapter adapter) {
        showLastPageByChild = true;
        if (!adapter.toPreviousPage()) {
            T temp = getPreviousFile(currentFile);
            if (temp == null) {
                Toast.makeText(this, "已经是第一篇了哦", Toast.LENGTH_SHORT).show();
            } else {
                setAnimal(temp);
            }
        }
    }

    /**
     * 设置当前显示的目录
     *
     * @param t
     */
    protected void setAnimal(T t) {
        if (t == null) {
            Toast.makeText(this, "已经是最后一篇了哦", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentFile != null) {
            saveLastPage();
        }
        currentFile = t;
        Snackbar.make(mViewPager, getName(currentFile), Snackbar.LENGTH_SHORT).show();
        fresh(getLastPage(currentFile));
    }

    /**
     * 点击删除按钮
     *
     * @param view
     */
    public void onDeleteClick(View view) {
        showLastPageByChild = false;
        delayedRun(Constants.HIDE_UI_DELAY);
        showDeleteDialog(currentFile);
    }

    /**
     * 点击分页按钮
     *
     * @param view
     */
    public void onSplitClick(View view) {
        switch (splitStatus) {
            case Preferences.SPLIT_AUTO:
                splitStatus = Preferences.SPLIT_FORCE;
                break;
            case Preferences.SPLIT_FORCE:
                splitStatus = Preferences.SPLIT_NONE;
                break;
            case Preferences.SPLIT_NONE:
                splitStatus = Preferences.SPLIT_AUTO;
                break;
        }
        adapter.notifyDataSetChanged();
        mSplit.setText(getSplitName());
        Preferences.getInstance().setsSplit(splitStatus);
    }

    /**
     * 点击方向按钮
     *
     * @param view
     */
    public void onDirectionClick(View view) {
        switch (directionStatus) {
            case Preferences.DIRECTION_LR:
                directionStatus = Preferences.DIRECTION_RL;
                break;
            case Preferences.DIRECTION_RL:
                directionStatus = Preferences.DIRECTION_LR;
                break;
        }
        adapter.notifyDataSetChanged();
        mDirection.setText(getDirectName());
        Preferences.getInstance().setsDirection(directionStatus);
    }


    /**
     * 获取分页方式名称
     *
     * @return
     */
    protected String getSplitName() {
        switch (splitStatus) {
            case Preferences.SPLIT_AUTO:
                return "自动分页";
            case Preferences.SPLIT_FORCE:
                return "强制分页";
            case Preferences.SPLIT_NONE:
                return "不分页";
            default:
                return "";
        }
    }

    @Override
    protected void onPause() {
        saveLastPage();
        super.onPause();
    }

    /**
     * 保存最后访问页
     */
    protected void saveLastPage() {
        int lastPage = mViewPager.getCurrentItem();
        //如果为最后一页则保存第一页(从头阅读)
        if (lastPage == adapter.getCount() - 1)
            lastPage = 0;
        saveLastPage(currentFile, lastPage);
    }


    /**
     * 点击下一篇
     *
     * @param view
     */
    public void onNextClick(View view) {
        showLastPageByChild = false;
        T temp = getNextFile(currentFile);
        if (temp == null) {
            Toast.makeText(this, "已经是最后一篇了哦", Toast.LENGTH_SHORT).show();
        } else {
            setAnimal(temp);
        }
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


    protected void delayedRun(long time) {
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
        }
        if (position < lastPosition) {
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
     * 获取方向按钮名称
     *
     * @return
     */
    public String getDirectName() {
        switch (directionStatus) {
            case Preferences.DIRECTION_LR:
                return "左→右";
            case Preferences.DIRECTION_RL:
                return "右→左";
            default:
                return "";
        }
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
            final PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageBitmap(list.get(position));
            photoView.setOnViewTapListener(this);
            if (doubleClickAction != null) {
                final GestureDetector.OnDoubleTapListener doubleTapListener
                        = new DoubleTapListener((PhotoViewAttacher) photoView.getIPhotoViewImplementation()) {
                    @Override
                    public void onDoubleCLick() {
                        doubleClickAction.run();
                    }
                };
                photoView.setOnDoubleTapListener(doubleTapListener);
            }
            if (longClickAction != null) {
                photoView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickAction.run();
                        return true;
                    }
                });
            }
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
    abstract class ImagePagerAdapter<T> extends BasePagerAdapter {
        List<T> list;
        OnViewClickListener onViewClickListener;

        public ImagePagerAdapter(List<T> list, ViewPager viewPager) {
            this.list = list;
            setViewPager(viewPager);
        }

        public OnViewClickListener getOnViewClickListener() {
            return onViewClickListener;
        }

        public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
            this.onViewClickListener = onViewClickListener;
        }

        abstract Bitmap getBitmap(T t);

        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            final List<Bitmap> bms = new ArrayList<>();
            final HackyViewPager viewPager = new HackyViewPager(container.getContext());
            final ContentPagerAdapter contentPagerAdapter = new ContentPagerAdapter(bms, viewPager, this);
            contentPagerAdapter.setOnViewClickListener(onViewClickListener);
            viewPager.setAdapter(contentPagerAdapter);

            BackgroundExecutor.getInstance().runInBackground(new BackgroundExecutor.Task() {
                //不能再非主线程更新bms
                List<Bitmap> tempList = new ArrayList<>();

                @Override
                public void runnable() {
                    Bitmap bm;
                    bm = getBitmap(list.get(position));
                    switch (splitStatus) {
                        case Preferences.SPLIT_AUTO:
                            if (bm != null && bm.getHeight() < bm.getWidth() * 3 / 4) {
                                getBitmapList(bm);
                            } else {
                                tempList.add(bm);
                            }
                            break;
                        case Preferences.SPLIT_NONE:
                            tempList.add(bm);
                            break;
                        case Preferences.SPLIT_FORCE:
                            getBitmapList(bm);
                            break;
                        default:
                            tempList.add(bm);
                    }
                }

                /**
                 * 根据方向获取bitmap列表
                 * @param bm
                 */
                protected void getBitmapList(Bitmap bm) {
                    if (directionStatus == Preferences.DIRECTION_LR) {
                        tempList.add(Tools.splitBitmap(bm, 0));
                        tempList.add(Tools.splitBitmap(bm, 1));
                    }
                    if (directionStatus == Preferences.DIRECTION_RL) {
                        tempList.add(Tools.splitBitmap(bm, 1));
                        tempList.add(Tools.splitBitmap(bm, 0));
                    }
                }

                @Override
                public void onBackgroundFinished() {
                    bms.addAll(tempList);
                    contentPagerAdapter.notifyDataSetChanged();
                    if (showLastPageByChild)
                        contentPagerAdapter.getViewPager().setCurrentItem(contentPagerAdapter.getCount() - 1, false);
                }
            }, false);
            container.addView(viewPager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return viewPager;
        }
    }

    /**
     * 根据路径获取对象
     *
     * @param path
     * @return
     */
    abstract T getFileByPath(String path);

    /**
     * 获取下一篇漫画
     *
     * @param t 当前漫画
     * @return
     */
    abstract T getNextFile(T t);

    /**
     * 获取下一篇漫画
     *
     * @param t
     * @return
     */
    abstract T getPreviousFile(T t);

    /**
     * 列出所有图形文件
     *
     * @param t
     * @return
     */
    abstract List<T> listFiles(T t);

    /**
     * 根据URL得到Bitmap
     *
     * @param t
     * @return
     */
    abstract Bitmap getBitmapByFile(T t);

    /**
     * 获取文件名
     *
     * @param t
     * @return
     */
    abstract String getName(T t);

    /**
     * 删除文件
     *
     * @param t 需要删除的文件
     * @return 之后需要显示的文件
     */
    abstract T deleteFile(T t);

    /**
     * 获取阅读历史记录
     *
     * @param t
     * @return
     */
    abstract int getLastPage(T t);

    /**
     * 保存最后阅读页
     *
     * @param t
     * @param lastPage
     * @return
     */
    abstract boolean saveLastPage(T t, int lastPage);

}
