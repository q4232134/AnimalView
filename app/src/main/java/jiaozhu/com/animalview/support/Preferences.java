package jiaozhu.com.animalview.support;

/**
 * Created by Administrator on 2015/1/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jiaozhu.com.animalview.model.FileModel;

/**
 * 软件设置
 */
public class Preferences implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final byte SPLIT_AUTO = 3;//自动分页
    public static final byte SPLIT_NONE = 4;//不分页
    public static final byte SPLIT_FORCE = 5;//强制分页


    public static final byte DIRECTION_LR = 6;//分页方向从左到右
    public static final byte DIRECTION_RL = 7;//分页方向从右到左

    private static Preferences preferences;
    /**
     * 保存的文件名称
     */
    public static final String SHAREDPREFERENCES_NAME = "Setting";
    /**
     * 历史记录
     */
    private static final String HISTORY_FILE = "history-file";
    private static final String HISTORY_PAGE = "history-page";
    /**
     * 设定
     */
    private static final String SETTING_SPLIT = "setting-split";//分页状态
    private static final String SETTING_ROTATION = "setting-rotation";//旋转状态
    private static final String SETTING_DIRECTION = "setting-direction";//分页方向

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static List<FileModel> list = new ArrayList<>();//当前文件列表

    private File historyFile;//最后阅读文件
    private int historyPage;//最后阅读页数
    private int sRotation;//旋转状态
    private byte sSplit;//分页状态
    private byte sDirection;//分页方向

    public static void init(Context context) {
        preferences = new Preferences(context);
    }

    public static Preferences getInstance() {
        if (preferences == null)
            throw new NoSuchElementException("必须先完成初始化");
        else
            return preferences;
    }

    private Preferences(Context context) {
        super();
        sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCES_NAME,
                Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initDate(sharedPreferences, HISTORY_FILE, HISTORY_PAGE, SETTING_ROTATION, SETTING_SPLIT, SETTING_DIRECTION);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * @param keys 初始化指定数据
     */
    private void initDate(SharedPreferences sharedPreferences, String... keys) {
        for (String temp : keys) {
            onSharedPreferenceChanged(sharedPreferences, temp);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case HISTORY_FILE:
                historyFile = new File(sharedPreferences.getString(HISTORY_FILE, ""));
                break;
            case HISTORY_PAGE:
                historyPage = sharedPreferences.getInt(HISTORY_PAGE, 0);
                break;
            case SETTING_ROTATION:
                sRotation = sharedPreferences.getInt(SETTING_ROTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case SETTING_SPLIT:
                sSplit = (byte) sharedPreferences.getInt(SETTING_SPLIT, SPLIT_AUTO);
                break;
            case SETTING_DIRECTION:
                sDirection = (byte) sharedPreferences.getInt(SETTING_DIRECTION, DIRECTION_LR);
                break;
        }
    }

    /**
     * 获取翻页方向
     *
     * @return
     */
    public byte getsDirection() {
        return sDirection;
    }

    /**
     * 设定翻页方向
     *
     * @return
     */
    public void setsDirection(byte sDirection) {
        if (sDirection < DIRECTION_LR || sDirection > DIRECTION_RL) return;
        editor.putInt(SETTING_DIRECTION, sDirection);
        editor.commit();
    }

    /**
     * 获取旋转方向
     *
     * @return
     */
    public int getsRotation() {
        return sRotation;
    }

    /**
     * 设定旋转方向
     *
     * @param sRotation
     */
    public void setsRotation(int sRotation) {
        editor.putInt(SETTING_ROTATION, sRotation);
        editor.commit();
    }

    /**
     * 获取分页方式
     *
     * @return
     */
    public byte getsSplit() {
        return sSplit;
    }

    /**
     * 设定分页方式
     *
     * @param sSplit
     */
    public void setsSplit(byte sSplit) {
        if (sSplit < SPLIT_AUTO || sSplit > SPLIT_FORCE) return;
        editor.putInt(SETTING_SPLIT, sSplit);
        editor.commit();
    }

    /**
     * 保存历史记录
     *
     * @param file
     * @param pageNum
     */
    public void saveHistory(File file, int pageNum) {
        editor.putString(HISTORY_FILE, file.getPath());
        editor.putInt(HISTORY_PAGE, pageNum);
        editor.commit();
    }

    /**
     * 读取最后阅读页数
     *
     * @return
     */
    public int getHistoryPage() {
        return historyPage;
    }

    /**
     * 读取最后阅读文件
     *
     * @return
     */
    public File getHistoryFile() {
        return historyFile;
    }
}
