package jiaozhu.com.animalview.control;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.NoSuchElementException;

import jiaozhu.com.animalview.dao.DaoMaster;
import jiaozhu.com.animalview.dao.DaoSession;

public class DbManager {

    // 是否加密
    public static final boolean ENCRYPTED = true;

    private static DbManager mDbManager;
    private static DaoMaster.DevOpenHelper mDevOpenHelper;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private Context mContext;

    private DbManager(Context context, String DBName) {
        this.mContext = context;
        // 初始化数据库信息
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, DBName);
        mDaoMaster = new DaoMaster(getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    public static void init(Context context, String DBName) {
        mDbManager = new DbManager(context, DBName);
    }

    public static DbManager getInstance() {
        if (mDbManager == null)
            throw new NoSuchElementException("必须先完成初始化");
        else
            return mDbManager;
    }

    /**
     * @desc 获取可读数据库
     **/
    public SQLiteDatabase getReadableDatabase() {
        return mDevOpenHelper.getReadableDatabase();
    }

    /**
     * @desc 获取可写数据库
     **/
    public SQLiteDatabase getWritableDatabase() {
        return mDevOpenHelper.getWritableDatabase();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public DaoMaster getDaoMaster() {
        return mDaoMaster;
    }
}