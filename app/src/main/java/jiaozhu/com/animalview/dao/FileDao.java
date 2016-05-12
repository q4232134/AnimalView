package jiaozhu.com.animalview.dao;

import android.database.sqlite.SQLiteOpenHelper;

import com.tgb.lk.ahibernate.dao.impl.BaseDaoImpl;

import java.io.File;
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
    public Map<String, FileModel> getModelsByFiles(List<File> files) {
        Map<String, FileModel> map = new Hashtable<>();
        if (files.isEmpty()) return map;
        StringBuffer sb = new StringBuffer();
        for (File temp : files) {
            sb.append(" '").append(temp.getPath()).append("' ,");
        }
        String temp = sb.substring(0, sb.length() - 2);
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
