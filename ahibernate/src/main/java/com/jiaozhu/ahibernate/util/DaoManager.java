package com.jiaozhu.ahibernate.util;

import android.database.sqlite.SQLiteOpenHelper;

import com.jiaozhu.ahibernate.dao.impl.BaseDaoImpl;
import com.jiaozhu.ahibernate.dao.impl.TableModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by jiaozhu on 2017/3/30.
 */

public class DaoManager {
    private static final String TAG = "DaoManager";
    private static DaoManager daoManager;
    private Map<String, TableModel> tables = new Hashtable<>();
    private Map<String, BaseDaoImpl> daoMap = new Hashtable<>();
    private SQLiteOpenHelper dbHelper;

    private DaoManager(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }


    public static DaoManager init(SQLiteOpenHelper dbHelper) {
        if (daoManager == null) {
            daoManager = new DaoManager(dbHelper);
        }
        return daoManager;
    }

    public static DaoManager getInstance() {
        if (daoManager == null) throw new NoSuchElementException("DaoManager未被初始化，请调用init方法");
        return daoManager;
    }

    /**
     * 获取dao
     *
     * @param name
     * @return
     */
    public BaseDaoImpl getDao(String name) {
        if (!tables.containsKey(name))
            throw new NoSuchElementException("指定表'" + name + "'并未注册到DaoManager!");
        return daoMap.get(name);
    }

    /**
     * 根据model获取dao
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> BaseDaoImpl<T> getDao(Class<T> clazz) {
        return getDao(TableHelper.getTableNameByClass(clazz));
    }

    /**
     * 获取表结构
     *
     * @param name
     * @return
     */
    public TableModel getTable(String name) {
        if (!tables.containsKey(name))
            throw new NoSuchElementException("指定表'" + name + "'并未注册到DaoManager!");
        return tables.get(name);
    }

    /**
     * 注册dao
     *
     * @param daoClasses
     */
    public void registerDao(Class... daoClasses) {
        for (Class temp : daoClasses) {
            registerDao(temp);
        }
    }

    private void registerDao(Class clazz) {
        if (!BaseDaoImpl.class.isAssignableFrom(clazz)) {
            throw new NoSuchElementException("指定类'" + clazz.getName() +
                    "'并非继承于'BaseDaoImpl'，无法注册dao!");
        }
        try {
            Constructor c = clazz.getDeclaredConstructor(new Class[]{SQLiteOpenHelper.class});
            BaseDaoImpl dao = (BaseDaoImpl) c.newInstance(dbHelper);
            TableModel model = dao.getTable();
            tables.put(model.getName(), model);
            daoMap.put(model.getName(), dao);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库表名列表
     *
     * @return
     */
    public List<String> getTableNameList() {
        return new ArrayList<>(tables.keySet());
    }

    /**
     * 设置LOG打印等级
     *
     * @param level
     */
    public static void setLogLevel(int level) {
        Log.setLevel(level);
    }

}
