package jiaozhu.com.animalview.control;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Created by jiaozhu on 16/8/30.
 */
public abstract class BaseOpe<T> {
    abstract AbstractDao<T, Long> getDao();

    public long insert(T t) {
        return getDao().insert(t);
    }

    public long replace(T t) {
        return getDao().insertOrReplace(t);
    }

    public void replace(Iterable<T> entities) {
        getDao().insertOrReplaceInTx(entities);
    }

    public void replace(T... entities) {
        getDao().insertOrReplaceInTx(entities);
    }


    public void delete(Long key) {
        getDao().deleteByKey(key);
    }

    public void delete(Long... keys) {
        getDao().deleteByKeyInTx(keys);
    }


    public void delete(T t) {
        getDao().delete(t);
    }

    public void delete(Iterable<T> t) {
        getDao().deleteInTx(t);
    }


    public void delete(T... t) {
        getDao().deleteInTx(t);
    }

    public void update(T t) {
        getDao().update(t);
    }

    public void update(Iterable<T> entities) {
        getDao().updateInTx(entities);
    }

    public void update(T... entities) {
        getDao().updateInTx(entities);
    }

    public T get(Long key) {
        return getDao().load(key);
    }

    public List<T> rawQuery(String s, String[] strings) {
        return getDao().queryRaw(s, strings);
    }

    public List<T> find() {
        return getDao().loadAll();
    }

    public void execSql(String s, Object[] objects) {
        getDao().getDatabase().execSQL(s, objects);
    }
}
