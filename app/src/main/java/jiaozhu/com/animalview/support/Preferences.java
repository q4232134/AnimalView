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

import jcifs.smb.SmbFile;
import jiaozhu.com.animalview.model.FileModel;

/**
 * 软件设置
 */
public class Preferences {

    public static final byte SPLIT_AUTO = 3;//自动分页
    public static final byte SPLIT_NONE = 4;//不分页
    public static final byte SPLIT_FORCE = 5;//强制分页


    public static final byte DIRECTION_LR = 6;//分页方向从左到右
    public static final byte DIRECTION_RL = 7;//分页方向从右到左


    public static final String ACTION_NONE = "none";//无
    public static final String ACTION_NEXT = "next";//下一篇
    public static final String ACTION_DELETE = "delete";//删除
    public static final String ACTION_EXIT = "exit";//退出

    private static Preferences preferences;
    /**
     * 保存的文件名称
     */
    public static final String SHAREDPREFERENCES_NAME = "Setting";
    /**
     * 历史记录
     */
    private static final String HISTORY_FILE = "history-file";
    /**
     * 设定
     */
    private static final String SETTING_SPLIT = "setting-split";//分页状态
    private static final String SETTING_ROTATION = "setting-rotation";//旋转状态
    private static final String SETTING_DIRECTION = "setting-direction";//分页方向

    public static final String SETTING_LONG_CLICK = "setting-long-click";//长按
    public static final String SETTING_DOUBLE_CLICK = "setting-double-click";//双击动作

    public static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static List<FileModel> list = new ArrayList<>();//当前文件列表
    public static List<SmbFile> smbList = new ArrayList<>();//远程当前文件列表

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
    }

    /**
     * 获取双击动作
     *
     * @return
     */
    public String getDoubleClickAction() {
        return sharedPreferences.getString(SETTING_DOUBLE_CLICK, ACTION_NONE);
    }


    /**
     * 获取长按动作
     *
     * @return
     */
    public String getLongClickAction() {
        return sharedPreferences.getString(SETTING_LONG_CLICK, ACTION_NONE);
    }

    /**
     * 获取翻页方向
     *
     * @return
     */
    public byte getsDirection() {
        return (byte) sharedPreferences.getInt(SETTING_DIRECTION, DIRECTION_LR);
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
        return sharedPreferences.getInt(SETTING_ROTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
        return (byte) sharedPreferences.getInt(SETTING_SPLIT, SPLIT_AUTO);
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
     */
    public void saveHistory(File file) {
        editor.putString(HISTORY_FILE, file.getPath());
        editor.commit();
    }

    /**
     * 读取最后阅读文件
     *
     * @return
     */
    public File getHistoryFile() {
        return new File(sharedPreferences.getString(HISTORY_FILE, ""));
    }
}
