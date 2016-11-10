package jiaozhu.com.animalview.commonTools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jiaozhu on 16/3/16.
 * 错误日志记录
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private String logPath;
    private Context context;
    private static Thread.UncaughtExceptionHandler defaultHandler;
    private static CrashHandler crashHandler;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private CrashHandler(Context context, String logPath) {
        this.context = context;
        this.logPath = logPath;
    }

    public static void init(Context context, String logPath) {
        if (crashHandler == null) {
            crashHandler = new CrashHandler(context, logPath);
        }
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(crashHandler);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringBuffer content = new StringBuffer();
        content.append(getPhoneInfo()).append(ex.toString()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            content.append(element).append("\n");
        }
        content.append(format.format(new Date()) + "---------------------------------------------------------------\n");
        writeFile(new File(logPath), content.toString());
        if (defaultHandler == null) {
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            defaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * 获取手机信息
     *
     * @return
     */
    private StringBuffer getPhoneInfo() {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        StringBuffer sb = new StringBuffer();
        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            sb.append("AppName:").append(context.getPackageName()).append("App Version:")
                    .append(pi.versionName).append("_").append(pi.versionCode).append("\n");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //版本号
        sb.append("OS Version:").append(Build.VERSION.RELEASE).append("_")
                .append(Build.VERSION.SDK_INT).append("\n");
        //手机制造商
        sb.append("Vendor:").append(Build.MANUFACTURER).append("\nModel").append(Build.MODEL)
                .append("\nCPU ABI:").append(Build.CPU_ABI).append("\n");
        return sb;
    }

    /**
     * 写入文件
     *
     * @param file 需要写入的文件
     * @param str  需要写入的内容
     * @return 写入是否成功
     */
    private static boolean writeFile(File file, String str) {
        boolean flag = false;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            byte[] bytes = str.getBytes();
            fos.write(bytes);
            fos.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
