package jiaozhu.com.animalview.support;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by apple on 15/10/30.
 */
public class CApplication extends Application {
    public List<FileModel> list = new ArrayList<>();

    @Override
    public void onCreate() {
        //TODO 在此初始化所有单例,注意初始化顺序
        super.onCreate();

        initPath(Constants.ROOT_DIR, Constants.CACHE_DIR);
    }

    private void initPath(File... files) {
        for (File temp : files) {
            if (!temp.exists()) {
                temp.mkdirs();
            }
        }
    }

}