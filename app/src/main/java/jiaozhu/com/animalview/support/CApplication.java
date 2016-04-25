package jiaozhu.com.animalview.support;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by apple on 15/10/30.
 */
public class CApplication extends Application {
    public List<FileModel> list = new ArrayList<>();
    public File markFile;//最后阅读的文件
    public int markPage;//最后阅读的页数

    @Override
    public void onCreate() {
        //TODO 在此初始化所有单例,注意初始化顺序
        super.onCreate();

        Preferences.init(this);
        markFile = Preferences.getInstance().getHistoryFile();
        markPage = Preferences.getInstance().getHistoryPage();

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

    public void saveAnimal(File file, int pageNum) {
        Preferences.getInstance().setHistory(file, pageNum);
        this.markFile = file;
        this.markPage = pageNum;
    }

}