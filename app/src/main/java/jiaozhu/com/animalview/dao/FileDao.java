package jiaozhu.com.animalview.dao;

import android.database.sqlite.SQLiteOpenHelper;

import com.tgb.lk.ahibernate.dao.impl.BaseDaoImpl;

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
}
