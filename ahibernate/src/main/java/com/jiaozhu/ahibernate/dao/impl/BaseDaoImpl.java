package com.jiaozhu.ahibernate.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.jiaozhu.ahibernate.dao.BaseDao;
import com.jiaozhu.ahibernate.dao.ProgressListener;
import com.jiaozhu.ahibernate.dao.Task;
import com.jiaozhu.ahibernate.type.Type;
import com.jiaozhu.ahibernate.util.DaoManager;
import com.jiaozhu.ahibernate.util.Log;
import com.jiaozhu.ahibernate.util.TableHelper;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AHibernate
 * 框架原作者：lk_blog 博客:http://blog.csdn.net/lk_blog
 */
public class BaseDaoImpl<T> implements BaseDao<T> {
    private String TAG = "AHibernate";
    private SQLiteOpenHelper dbHelper;
    private String tableName;
    private String idColumn;
    private TableModel table;
    private static final int METHOD_INSERT = 0;
    private static final int METHOD_UPDATE = 1;
    private static final int TYPE_NOT_INCREMENT = 0;
    private static final int TYPE_INCREMENT = 1;

    protected BaseDaoImpl(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
        table = new TableModel(this.getSupportModel());
        this.tableName = table.getName();
        if (table.getIdColumn() == null) throw new NullPointerException(tableName + "未找到对应主键!");
        this.idColumn = table.getIdColumn().getName();
    }

    /**
     * 获取对应model的的class
     *
     * @param <T>
     * @return
     */
    public <T> Class<T> getSupportModel() {
        return ((Class<T>) ((java.lang.reflect.ParameterizedType) this.getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public TableModel getTable() {
        return table;
    }

    public String getTableName() {
        return tableName;
    }

    public void close() {
        getDbHelper().getReadableDatabase().close();
        getDbHelper().getWritableDatabase().close();
    }

    public SQLiteOpenHelper getDbHelper() {
        return dbHelper;
    }

    public T get(String id) {
        String selection = this.idColumn + " = ?";
        String[] selectionArgs = {id};
        Log.d(TAG, "[get]: select * from " + this.tableName + " where " + this.idColumn
                + " = '" + id + "'");
        List<T> list = find(null, selection, selectionArgs, null, null, null, null);
        if ((list != null) && (list.size() > 0)) {
            return list.get(0);
        }
        return null;
    }

    public List<T> rawQuery(String sql, String[] selectionArgs) {
        Log.d(TAG, "[rawQuery]: " + getLogSql(sql, selectionArgs));
        List<T> list = new ArrayList<T>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.dbHelper.getReadableDatabase();
            db.beginTransaction();
            cursor = db.rawQuery(sql, selectionArgs);
            getListFromCursor(list, cursor);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(this.TAG, "[rawQuery] from DB Exception.");
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null)
                db.endTransaction();
        }
        return list;
    }

    public boolean isExist(String sql, String[] selectionArgs) {
        Log.d(TAG, "[isExist]: " + getLogSql(sql, selectionArgs));
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.dbHelper.getReadableDatabase();
            db.beginTransaction();
            cursor = db.rawQuery(sql, selectionArgs);
            db.setTransactionSuccessful();
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(this.TAG, "[isExist] from DB Exception.");
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.endTransaction();
        }
        return false;
    }

    public List<T> find() {
        return find(null, null, null, null, null, null, null);
    }

    public List<T> find(String[] columns, String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {
        Log.d(TAG, "[find]");
        List<T> list = new ArrayList<T>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.dbHelper.getReadableDatabase();
            db.beginTransaction();
            cursor = db.query(this.tableName, columns, selection, selectionArgs, groupBy, having,
                    orderBy, limit);
            getListFromCursor(list, cursor);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(this.TAG, "[find] from DB Exception");
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.endTransaction();
        }
        return list;
    }

    public long insert(T entity) {
        return insert(entity, true);
    }

    public long insert(T entity, boolean flag) {
        String sql = "";
        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            if (flag) {
                sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);// id自增
            } else {
                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);// id需指定
            }
            Log.d(TAG, "[insert]: insert into " + this.tableName + " " + sql);
            long row = db.insert(this.tableName, null, cv);
            db.setTransactionSuccessful();
            return row;
        } catch (Exception e) {
            Log.d(this.TAG, "[insert] into DB Exception.");
            e.printStackTrace();
        } finally {
            if (db != null)
                db.endTransaction();
        }
        return 0L;
    }

    /**
     * 批量替换（采用事务方式提交操作）
     *
     * @param list 需要提交的列表
     * @return 提交是否成功
     */
    public boolean replace(List<T> list, @Nullable ProgressListener listener) {
        SQLiteDatabase db = null;
        String sql = "";
        long finishNum = 0;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            for (T entity : list) {
                ContentValues cv = new ContentValues();
                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);// id需指定
                Log.d(TAG, "[replace]: replace into " + this.tableName + " " + sql);
                db.replace(this.tableName, null, cv);
                if (listener != null)
                    listener.onProgress(++finishNum, list.size());
            }
            db.setTransactionSuccessful();
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null)
                db.endTransaction();
        }
    }

    /**
     * 批量替换（采用事务方式提交操作）
     *
     * @param list 需要提交的列表
     * @return 提交是否成功
     */
    public boolean replace(List<T> list) {
        return replace(list, null);
    }

    public long replace(T entity) {
        String sql = "";
        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);// id需指定
            Log.d(TAG, "[replace]: replace into " + this.tableName + " " + sql);
            long row = db.replace(this.tableName, null, cv);
            db.setTransactionSuccessful();
            return row;
        } catch (Exception e) {
            Log.d(this.TAG, "[replace] into DB Exception.");
            e.printStackTrace();
        } finally {
            if (db != null)
                db.endTransaction();
        }
        return 0L;
    }

    public void delete(Integer id) {
        this.delete("" + id);
    }

    public void delete(Integer... ids) {
        String[] temps = new String[ids.length];
        int j = 0;
        for (int i : ids) {
            temps[j++] = String.valueOf(i);
        }
        this.delete(temps);
    }

    public int delete(String id) {
        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            String where = this.idColumn + " = ?";
            String[] whereValue = {id};
            Log.d(TAG,
                    "[delete]: delete from " + this.tableName + " where "
                            + where.replace("?", String.valueOf(id)));
            db.setTransactionSuccessful();
            return db.delete(this.tableName, where, whereValue);
        } catch (Exception e) {
            Log.e(TAG, "[delete] from DB exception");
            e.printStackTrace();
        } finally {
            if (db != null)
                db.endTransaction();
        }
        return -1;
    }

    /**
     * 根据ID进行批量删除
     *
     * @param ids
     * @return
     */
    public boolean delete(String... ids) {
        StringBuffer sb = new StringBuffer();
        for (String id : ids) {
            sb = sb.append(idColumn).append(" = ").append(id).append(" or ");
        }
        if (sb.length() > 3)
            sb.delete(sb.length() - 4, sb.length() - 1);
        String sql = "delete from " + this.tableName + " where " + sb;
        return execSql(sql, null);
    }


    public boolean update(T entity) {
        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            updateEntity(db, entity);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.d(this.TAG, "[update] DB Exception.");
            e.printStackTrace();
            return false;
        } finally {
            if (db != null)
                db.endTransaction();
        }
    }

    @Override
    public boolean update(List<T> list) {
        return update(list, null);
    }

    public boolean update(List<T> list, @Nullable ProgressListener listener) {
        SQLiteDatabase db = null;
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            int i = 0;
            for (T entity : list) {
                updateEntity(db, entity);
                if (listener != null)
                    listener.onProgress(++i, list.size());
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.d(this.TAG, "[update] DB Exception.");
            e.printStackTrace();
            return false;
        } finally {
            if (db != null)
                db.endTransaction();
        }
    }

    /**
     * 修改数据库
     *
     * @param db
     * @param entity
     * @throws IllegalAccessException
     */
    private void updateEntity(SQLiteDatabase db, T entity) throws IllegalAccessException {
        ContentValues cv = new ContentValues();
        String sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_UPDATE);
        String where = this.idColumn + " = ?";
        // int id = Integer.parseInt(cv.get(this.idColumn).toString());
        String id = cv.get(this.idColumn).toString();
        cv.remove(this.idColumn);
        Log.d(TAG,
                "[update]: update " + this.tableName + " set " + sql + " where "
                        + where.replace("?", String.valueOf(id)));

        String[] whereValue = {id};
        db.update(this.tableName, cv, where, whereValue);
    }

    /**
     * 从游标中提取对应model的list
     *
     * @param list
     * @param cursor
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void getListFromCursor(List<T> list, Cursor cursor) throws IllegalAccessException,
            InstantiationException {
        while (cursor.moveToNext()) {
            T entity = (T) this.table.getClazz().newInstance();
            for (ColumnModel model : table.getColumns()) {
                Class<?> fieldType = model.getField().getType();
                int c = cursor.getColumnIndex(model.getName());
                //是否存在对应字段
                if (c < 0) {
                    continue; // 如果不存则循环下个属性值
                } else if (cursor.isNull(c)) {//是否为null
                    model.getField().set(entity, null);
                } else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {//是否为integer
                    model.getField().set(entity, cursor.getInt(c));
                } else if (String.class == fieldType) {//是否为String
                    model.getField().set(entity, cursor.getString(c));
                } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {//是否为long
                    model.getField().set(entity, Long.valueOf(cursor.getLong(c)));
                } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {//是否为float
                    model.getField().set(entity, Float.valueOf(cursor.getFloat(c)));
                } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {//是否为short
                    model.getField().set(entity, Short.valueOf(cursor.getShort(c)));
                } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {//是否为double
                    model.getField().set(entity, Double.valueOf(cursor.getDouble(c)));
                } else if (Date.class == fieldType) {//是否为date
                    Date date = new Date();
                    date.setTime(cursor.getLong(c));
                    model.getField().set(entity, date);
                } else if (Blob.class == fieldType) {//是否为blob二进制类型
                    model.getField().set(entity, cursor.getBlob(c));
                } else if (Character.TYPE == fieldType) {//是否为char
                    String fieldValue = cursor.getString(c);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        model.getField().set(entity, Character.valueOf(fieldValue.charAt(0)));
                    }
                } else if (Boolean.TYPE == fieldType || (Boolean.class == fieldType)) {//是否为boolean
                    String temp = cursor.getString(c);
                    if (temp.equals("1") || temp.equals("true") || temp.equals("Y") || temp.equals("y")) {
                        model.getField().set(entity, true);
                    } else {
                        model.getField().set(entity, false);
                    }
                }
            }
            list.add((T) entity);
        }
    }


    private String setContentValues(T entity, ContentValues cv, int type, int method)
            throws IllegalAccessException {
        //TODO
        StringBuffer strField = new StringBuffer("(");
        StringBuffer strValue = new StringBuffer(" values(");
        StringBuffer strUpdate = new StringBuffer(" ");
        for (ColumnModel model : table.getColumns()) {
            Object fieldValue = model.getField().get(entity);
            if (fieldValue == null) {
                String s = null;
                cv.put(model.getName(), s);
                continue;
            }
            if ((type == TYPE_INCREMENT) && (model.isPrimary())) {
                continue;
            }
            Class fieldType = model.getField().getType();
            if (Date.class == fieldType) {//处理时间类型
                cv.put(model.getName(), ((Date) fieldValue).getTime());
            } else if (Boolean.class == fieldType||boolean.class == fieldType) {//处理布尔类型
                if (model.getType().equals(Type.TYPE_STRING)) {//是否以字符串形式保存
                    cv.put(model.getName(), (boolean) fieldValue);
                } else {
                    if ((boolean) fieldValue) {
                        cv.put(model.getName(), 1);
                    } else {
                        cv.put(model.getName(), 0);
                    }
                }
            } else if (Blob.class == fieldType) {
                cv.put(model.getName(), (byte[]) fieldValue);
            } else {
                cv.put(model.getName(), String.valueOf(fieldValue));
            }

            String value = String.valueOf(fieldValue);
            if (method == METHOD_INSERT) {
                strField.append(model.getName()).append(",");
                strValue.append("'").append(value).append("',");
            } else {
                strUpdate.append(model.getName()).append("=").append("'").append(value).append("',");
            }
        }
        if (method == METHOD_INSERT) {
            strField.deleteCharAt(strField.length() - 1).append(")");
            strValue.deleteCharAt(strValue.length() - 1).append(")");
            return strField.toString() + strValue.toString();
        } else {
            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
        }
    }

    /**
     * 将查询的结果保存为名值对map.
     *
     * @param sql           查询sql
     * @param selectionArgs 参数值
     * @return 返回的Map中的key全部是小写形式.
     */
    public List<Map<String, String>> query2MapList(String sql, String[] selectionArgs) {
        Log.d(TAG, "[query2MapList]: " + getLogSql(sql, selectionArgs));
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            db = this.dbHelper.getReadableDatabase();
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                for (String columnName : cursor.getColumnNames()) {
                    int c = cursor.getColumnIndex(columnName);
                    if (c < 0) {
                        continue; // 如果不存在循环下个属性值
                    } else {
                        map.put(columnName.toLowerCase(), cursor.getString(c));
                    }
                }
                retList.add(map);
            }
        } catch (Exception e) {
            Log.e(TAG, "[query2MapList] from DB exception");
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retList;
    }

    /**
     * 封装执行sql代码.
     *
     * @param sql
     * @param selectionArgs
     */
    public boolean execSql(String sql, Object[] selectionArgs) {
        SQLiteDatabase db = null;
        Log.d(TAG, "[execSql]: " + getLogSql(sql, selectionArgs));
        try {
            db = this.dbHelper.getWritableDatabase();
            db.beginTransaction();
            if (selectionArgs == null) {
                db.execSQL(sql);
            } else {
                db.execSQL(sql, selectionArgs);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "[execSql] DB exception.");
            e.printStackTrace();
            return false;
        } finally {
            if (db != null)
                db.endTransaction();
        }
    }

    @Override
    public void dropTable() {
        TableHelper.dropTable(dbHelper.getWritableDatabase(), tableName);
    }

    /**
     * 获取能够被打印的sql语句
     *
     * @param sql
     * @param args
     * @return
     */
    private static String getLogSql(String sql, Object[] args) {
        if (args == null || args.length == 0) {
            return sql;
        }
        for (int i = 0; i < args.length; i++) {
            sql = sql.replaceFirst("\\?", "'" + String.valueOf(args[i]) + "'");
        }
        return sql;
    }

    public T get(Integer id) {
        return this.get("" + id);
    }

    /**
     * 在事务中运行指定任务
     *
     * @param task
     */
    public void runInTransaction(Task task) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "[execTransaction] exception.");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取相关联的子表
     *
     * @param entity 主表对象
     * @param <G>
     * @return
     */
    public <G> List<T> findCombines(G entity) {
        String tableName = TableHelper.getTableNameByClass(entity.getClass());
        if (!tableName.equals(table.getParentTableName())) {
            Log.e(TAG, "传入model与父表类型不符合");
            return new ArrayList<>();
        }
        StringBuffer sb = new StringBuffer();
        for (CombineModel temp : table.getCombines()) {
            String target = temp.getTarget();
            ColumnModel columnModel = null;
            //获取主表对应的值
            if (target.equals("")) {
                columnModel = DaoManager.getInstance().getTable(tableName).getIdColumn();
            } else {
                columnModel = DaoManager.getInstance().getTable(tableName)
                        .getColumnByName(temp.getTarget());
            }
            String value = getStringValueFromColumn(columnModel, entity);
            sb.append(temp.getColumn()).append(" = '").append(value).append("' and");
        }
        if (sb.length() > 4)
            sb.delete(sb.lastIndexOf(" and"), sb.length());
        return rawQuery("select * from " + this.tableName + " where " + sb, null);
    }

    /**
     * 根据ColumnModel获取值
     *
     * @param columnModel
     * @param entity
     * @return
     */
    private String getStringValueFromColumn(ColumnModel columnModel, Object entity) {
        String value = null;
        Object fieldValue = null;
        try {
            fieldValue = columnModel.getField().get(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
        //为date类型
        if (Date.class == columnModel.getField().getType()) {
            value = "" + ((Date) fieldValue).getTime();
        }
        //为boolean类型
        else if (columnModel.getField().getType() == Boolean.class
                ||columnModel.getField().getType() == boolean.class) {
            if (columnModel.getType().equals(Type.TYPE_STRING)) {
                value = String.valueOf(fieldValue);
            } else {
                if ((boolean) fieldValue) {
                    value = "1";
                } else {
                    value = "0";
                }
            }
        } else {
            value = String.valueOf(fieldValue);
        }
        return value;
    }

}