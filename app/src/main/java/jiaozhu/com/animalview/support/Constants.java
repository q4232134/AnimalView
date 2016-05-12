package jiaozhu.com.animalview.support;

import android.os.Environment;

import java.io.File;

/**
 * Created by apple on 15/10/30.
 */
public class Constants {
    public static String ROOT_DIR_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "AnimalView";
    public static File ROOT_DIR = new File(ROOT_DIR_PATH);
    public static File CACHE_DIR = new File(ROOT_DIR_PATH + File.separator + ".cache");
    public static File NO_MEDIA = new File(ROOT_DIR_PATH + File.separator + ".nomedia");
    public static File DELETE_LOG = new File(ROOT_DIR_PATH + File.separator + "delete.log");

    /**
     * 触摸中间部分所占比例
     */
    public static final float CENTER_WIDTH = 0.4f;
    public static final float CENTER_HEIGHT = 0.4f;

    /**
     * 历史记录保存长度
     */
//    public static final long HISTORY_DURATION = 30 * 24 * 60 * 60 * 1000;//历史记录保存一个月
    public static final long HISTORY_DURATION = 0;

    /**
     * 隐藏界面延迟时间
     */
    public static final long HIDE_UI_DELAY = 3500;
    /**
     * 数据库名称
     */
    public static final String DB_NAME = "animalView.db";
    /**
     * 数据库版本
     */
    public static final int DB_VERSION = 1;
    /**
     * 设备标识
     */
    public static String ANDROID_ID;
    /**
     * 历史数据显示条数
     */
    public static final int HISTORY_LENGTH = 20;

    /**
     * IP地址验证的正则表达式
     */
    public static final String IP_CHECK_STRING = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    /**
     * 缓存文件高度
     */
    public static final int CACHE_HEIGHT = 512;
    /**
     * 缓存文件宽度
     */
    public static final int CACHE_WIDTH = 512;

    /**
     * 可识别的图片类型
     */
    public static final String[] IMAGE_TYPE = {".jpg", ".png", ".bmp", ".gif"};
}
