package jiaozhu.com.animalview.support;

import android.app.Application;

import java.io.File;
import java.io.IOException;

import jiaozhu.com.animalview.commonTools.CrashHandler;
import jiaozhu.com.animalview.control.DbManager;

/**
 * Created by apple on 15/10/30.
 */
public class CApplication extends Application {

    @Override
    public void onCreate() {
        //TODO 在此初始化所有单例,注意初始化顺序
        super.onCreate();
        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        Preferences.init(this);
        CrashHandler.init(this, Constants.ROOT_DIR_PATH + File.separator + "crash.log");

        Constants.CACHE_DIR = getCacheDir();
        initPath(Constants.ROOT_DIR, Constants.CACHE_DIR);
        DbManager.init(this, Constants.DB_NAME);
        if (!Constants.NO_MEDIA.exists()) {
            try {
                Constants.NO_MEDIA.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initPath(File... files) {
        for (File temp : files) {
            if (!temp.exists()) {
                temp.mkdirs();
            }
        }
    }


}