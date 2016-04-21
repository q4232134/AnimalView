package jiaozhu.com.animalview.commonTools;

/**
 * Created by Administrator on 2015/5/28.
 */
public class Log {
    //这是一个测试
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    private static int level = 0;

    public static int getLevel() {
        return level;
    }

    public static void setLevel(int level) {
        Log.level = level;
    }

    public static int v(String tag, String msg) {
        if (level <= VERBOSE)
            return android.util.Log.v(tag, msg);
        return -1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (level <= VERBOSE)
            return android.util.Log.v(tag, msg, tr);
        return -1;
    }

    public static int d(String tag, String msg) {
        if (level <= DEBUG)
            return android.util.Log.d(tag, msg);
        return -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (level <= DEBUG)
            return android.util.Log.d(tag, msg, tr);
        return -1;
    }

    public static int i(String tag, String msg) {
        if (level <= INFO)
            return android.util.Log.i(tag, msg);
        return -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (level <= INFO)
            return android.util.Log.i(tag, msg, tr);
        return -1;
    }

    public static int w(String tag, String msg) {
        if (level <= WARN)
            return android.util.Log.w(tag, msg);
        return -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (level <= WARN)
            return android.util.Log.w(tag, msg, tr);
        return -1;
    }

    public static boolean isLoggable(String var0, int var1) {
        return android.util.Log.isLoggable(var0, var1);
    }

    public static int w(String tag, Throwable tr) {
        if (level <= WARN)
            return android.util.Log.w(tag, tr);
        return -1;
    }

    public static int e(String tag, String msg) {
        if (level <= ERROR)
            return android.util.Log.e(tag, msg);
        return -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (level <= ERROR)
            return android.util.Log.e(tag, msg, tr);
        return -1;
    }

    public static int wtf(String tag, String msg) {
        return android.util.Log.wtf(tag, msg);
    }

    public static int wtf(String tag, Throwable tr) {
        return android.util.Log.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return android.util.Log.wtf(tag, msg, tr);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }
}
