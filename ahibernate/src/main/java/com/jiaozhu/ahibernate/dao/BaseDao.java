package com.jiaozhu.ahibernate.dao;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

public interface BaseDao<T> {

    public SQLiteOpenHelper getDbHelper();

    /**
     * 默认主键自增,调用insert(T,true);
     *
     * @param entity
     * @return
     */
    public abstract long insert(T entity);

    /**
     * 插入实体类
     *
     * @param entity
     * @param flag   flag为true是自动生成主键,flag为false时需手工指定主键.
     * @return
     */
    public abstract long insert(T entity, boolean flag);

    public boolean replace(List<T> list, ProgressListener listener);

    public boolean replace(List<T> list);

    public abstract int delete(String id);

    public abstract boolean delete(String... ids);

    public abstract boolean update(T entity);

    public boolean update(List<T> list);

    public abstract T get(String id);

    public abstract List<T> rawQuery(String sql, String[] selectionArgs);

    public abstract List<T> find();

    public abstract List<T> find(String[] columns, String selection,
                                 String[] selectionArgs, String groupBy, String having,
                                 String orderBy, String limit);

    public abstract boolean isExist(String sql, String[] selectionArgs);

    /**
     * 将查询的结果保存为名值对map.
     *
     * @param sql           查询sql
     * @param selectionArgs 参数值
     * @return 返回的Map中的key全部是小写形式.
     */
    public List<Map<String, String>> query2MapList(String sql,
                                                   String[] selectionArgs);

    /**
     * 封装执行sql代码.
     *  @param sql
     * @param selectionArgs
     */
    public boolean execSql(String sql, Object[] selectionArgs);

    /**
     * 删除当前表
     */
    public void dropTable();

}