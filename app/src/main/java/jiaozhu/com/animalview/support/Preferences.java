package jiaozhu.com.animalview.support;

/**
 * Created by Administrator on 2015/1/17.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.NoSuchElementException;

/**
 * 软件设置
 */
public class Preferences {
    private static Preferences preferences;
    /**
     * 保存的文件名称
     */
    private static final String SHAREDPREFERENCES_NAME = "Setting";
    /**
     * 历史记录
     */
    private static final String SAVE_HISTORY_FILE = "history-file";
    private static final String SAVE_HISTORY_PAGE = "history-file-page";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

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
     * 保存历史记录
     *
     * @param file 用户名
     * @return 是否设置成功
     */
    public boolean setHistory(File file, int pageNum) {
        editor.putInt(SAVE_HISTORY_PAGE, pageNum);
        editor.putString(SAVE_HISTORY_FILE, file.getPath());
        return editor.commit();
    }

    /**
     * 获取历史记录文件
     *
     * @return file
     */
    public File getHistoryFile() {
        return new File(get(SAVE_HISTORY_FILE));
    }

    /**
     * 获取历史记录页数
     *
     * @return
     */
    public int getHistoryPage() {
        return sharedPreferences.getInt(SAVE_HISTORY_PAGE, 0);
    }

    /**
     * 设置数据
     *
     * @param key
     * @param value
     * @return 是否成功
     */
    private boolean set(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * 获取数据
     *
     * @param key
     * @param defaultValue 默认值
     * @return
     */
    private String get(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * 获取数据
     *
     * @param key
     * @return
     */
    private String get(String key) {
        return sharedPreferences.getString(key, "");
    }
}
