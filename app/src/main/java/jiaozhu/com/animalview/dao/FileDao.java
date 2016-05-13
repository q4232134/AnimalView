package jiaozhu.com.animalview.dao;

import android.database.sqlite.SQLiteOpenHelper;

import com.tgb.lk.ahibernate.dao.impl.BaseDaoImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by jiaozhu on 16/5/9.
 */
public class FileDao extends BaseDaoImpl<FileModel> {
    private static FileDao dao;

    private FileDao(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }


    public static FileDao getInstance() {
        if (dao == null)
            throw new NoSuchElementException("必须先完成初始化");
        else
            return dao;
    }

    public static void init(SQLiteOpenHelper dbHelper) {
        dao = new FileDao(dbHelper);
    }

    /**
     * 获取文件对应的model(如果存在的话)
     *
     * @param files
     * @return
     */
    public Map<String, FileModel> getModelsByFiles(Collection<File> files) {
        List<String> list = new ArrayList<>();
        for (File temp : files) {
            list.add(temp.getPath());
        }
        return getModelsByPaths(list);
    }

    /**
     * 获取文件对应的model(如果存在的话)
     *
     * @param models
     * @return
     */
    public Map<String, FileModel> getModelsByModels(Collection<FileModel> models) {
        List<String> list = new ArrayList<>();
        for (FileModel temp : models) {
            list.add(temp.getPath());
        }
        return getModelsByPaths(list);
    }

    /**
     * 获取路径对应的model(如果存在的话)
     *
     * @param paths
     * @return
     */
    public Map<String, FileModel> getModelsByPaths(Collection<String> paths) {
        Map<String, FileModel> map = new Hashtable<>();
        if (paths.isEmpty()) return map;
        StringBuffer sb = new StringBuffer();
        for (String temp : paths) {
            sb.append("'").append(temp).append("',");
        }
        String temp = sb.substring(0, sb.length() - 1);
        List<FileModel> list = rawQuery("select * from " + FileModel.TABLE_NAME + " where path in ( " + temp + " )",
                null);
        for (FileModel model : list) {
            map.put(model.getPath(), model);
        }
        return map;
    }

    /**
     * 查询指定时间之前的model
     *
     * @param date
     * @return
     */
    public List<FileModel> getModelByTime(long date) {
        return rawQuery("select * from " + FileModel.TABLE_NAME + " where createTime < ?",
                new String[]{date + ""});
    }
}
