package jiaozhu.com.animalview.control;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jiaozhu.com.animalview.dao.FileModelDao;
import jiaozhu.com.animalview.model.FileModel;

/**
 * Created by jiaozhu on 16/5/9.
 */
public class FileModelOpe extends BaseOpe<FileModel> {
    private static FileModelOpe ope;

    @Override
    protected AbstractDao<FileModel, Long> getDao() {
        return DbManager.getInstance().getDaoSession().getFileModelDao();
    }

    public static FileModelOpe getInstance() {
        if (ope == null)
            ope = new FileModelOpe();
        return ope;
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
            list.add(temp.getName());
        }
        return getModelsByNames(list);
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
            list.add(temp.getName());
        }
        return getModelsByNames(list);
    }

    /**
     * 获取路径对应的model(如果存在的话)
     *
     * @param names
     * @return
     */
    public Map<String, FileModel> getModelsByNames(Collection<String> names) {
        Map<String, FileModel> map = new Hashtable<>();
        if (names.isEmpty()) return map;
        StringBuffer sb = new StringBuffer();
        QueryBuilder queryBuilder = getDao().queryBuilder();
        for (String temp : names) {
            //对单引号进行特殊处理
            sb.append("name = '").append(temp.replace("'", "''")).append("' or ");
        }
        String temp = sb.substring(0, sb.length() - 3);
        List<FileModel> list = queryBuilder.where(new WhereCondition.StringCondition(temp)).list();
        for (FileModel model : list) {
            map.put(model.getName(), model);
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
        QueryBuilder queryBuilder = getDao().queryBuilder();
        return queryBuilder.where(FileModelDao.Properties.CreateTime.lt(date)).list();
    }

}
