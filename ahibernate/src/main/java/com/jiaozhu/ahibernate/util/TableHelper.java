package com.jiaozhu.ahibernate.util;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.jiaozhu.ahibernate.annotation.Table;
import com.jiaozhu.ahibernate.dao.impl.ColumnModel;
import com.jiaozhu.ahibernate.dao.impl.TableModel;
import com.jiaozhu.ahibernate.type.Type;

public class TableHelper {
    private static final String TAG = "AHibernate";

    public static void createTable(SQLiteDatabase db, Class... clazzs) {
        for (Class clazz : clazzs)
            createTablesByClass(db, clazz);
    }

    public static void dropTable(SQLiteDatabase db, Class... clazzs) {
        for (Class<?> clazz : clazzs)
            dropTable(db, clazz);
    }


    public static void createTable(SQLiteDatabase db, String... names) {
        for (String name : names)
            createTable(db, name);
    }

    public static void dropTable(SQLiteDatabase db, String... tableNames) {
        for (String name : tableNames)
            dropTable(db, name);
    }

    /**
     * 根据model创建表
     *
     * @param db
     * @param clazz
     */
    public static void createTablesByClass(SQLiteDatabase db, Class clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            createTable(db, getTableNameByClass(clazz));
        }
    }

    /**
     * 根据表名创建表
     *
     * @param db
     * @param name
     * @return 创建是否成功
     */
    public static boolean createTable(SQLiteDatabase db, String name) {
        TableModel table = DaoManager.getInstance().getTable(name);
        if (table == null || !table.isCreateTable()) return false;
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(table.getName()).append(" (");
        for (ColumnModel temp : table.getColumns()) {
            sb.append(temp.getName() + " " + temp.getType());
            if (temp.getLength() != 0) {
                sb.append("(" + temp.getLength() + ")");
            }
            //主键是否为整数类型
            if (temp.isPrimary() && ((temp.getType() == Type.TYPE_INTEGER)))
                sb.append(" primary key autoincrement");
            else if (temp.isPrimary()) {
                sb.append(" primary key");
            }
            if (temp.isNotNull() == true) {
                sb.append(" not null");
            }
            if (temp.isUniQue() == true) {
                sb.append(" unique");
            }
            if (!temp.getDef().equals("")) {
                sb.append(" default " + temp.getDef() + "");
            }

            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length() - 1);
        sb.append(")");

        String sql = sb.toString();
        Log.d(TAG, "crate table [" + table.getName() + "]: " + sql);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据model删除表
     *
     * @param db
     * @param clazz
     */
    public static void dropTable(SQLiteDatabase db, Class clazz) {
        dropTable(db, getTableNameByClass(clazz));
    }

    /**
     * 根据表名删除表
     *
     * @param db
     * @param tableName
     */
    public static void dropTable(SQLiteDatabase db, String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        Log.d(TAG, "dropTable[" + tableName + "]:" + sql);
        db.execSQL(sql);
    }

    /**
     * 根据class获取表名
     *
     * @param clazz
     * @return
     */
    public static String getTableNameByClass(Class clazz) {
        if (!clazz.isAnnotationPresent(Table.class))
            throw new NoSuchFieldError("此class中并没有包含@Table注释");
        Table table = (Table) clazz.getAnnotation(Table.class);
        if (table.name().equals("")) {
            return clazz.getSimpleName();
        } else {
            return table.name();
        }
    }
}