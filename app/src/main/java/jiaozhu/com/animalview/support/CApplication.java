package jiaozhu.com.animalview.support;

import android.app.Application;

import java.io.File;
import java.io.IOException;

/**
 * Created by apple on 15/10/30.
 */
public class CApplication extends Application {

    @Override
    public void onCreate() {
        //TODO 在此初始化所有单例,注意初始化顺序
        super.onCreate();

        Preferences.init(this);

        initPath(Constants.ROOT_DIR, Constants.CACHE_DIR);
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