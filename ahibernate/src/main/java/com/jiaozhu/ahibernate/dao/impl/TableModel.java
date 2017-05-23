package com.jiaozhu.ahibernate.dao.impl;

import com.jiaozhu.ahibernate.annotation.Column;
import com.jiaozhu.ahibernate.annotation.Combine;
import com.jiaozhu.ahibernate.annotation.Table;
import com.jiaozhu.ahibernate.util.TableHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by jiaozhu on 2017/3/30.
 */

public class TableModel {
    private Class clazz;
    private String name;
    private boolean isCreateTable;
    private String parentTableName;
    private List<ColumnModel> columns = new ArrayList<>();
    private Map<String, ColumnModel> map = new Hashtable<>();
    private List<CombineModel> combines = new ArrayList<>();
    private ColumnModel idColumn;

    public TableModel(Class clazz) {
        if (!clazz.isAnnotationPresent(Table.class))
            throw new NoSuchFieldError("此class中并没有包含@Table注释");
        Table table = (Table) clazz.getAnnotation(Table.class);
        name = TableHelper.getTableNameByClass(clazz);
        this.isCreateTable = table.createTable();
        if (table.combine() != Object.class) {
            parentTableName = TableHelper.getTableNameByClass(table.combine());
        }
        getColumns(clazz);
        this.clazz = clazz;
        //是否包含关联
    }

    private void getColumns(Class clazz) {
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                // 过滤掉非Column定义的字段
                if (field.isAnnotationPresent(Column.class)) {
                    ColumnModel temp = new ColumnModel(field);
                    map.put(temp.getName(), temp);
                }
                //有关联表
                if (parentTableName != null && field.isAnnotationPresent(Combine.class)) {
                    combines.add(new CombineModel(field));
                }
            }
            clazz = clazz.getSuperclass();
        }
        for (String key : map.keySet()) {
            ColumnModel temp = map.get(key);
            // 如果是Id则放在首位置.
            if (temp.isPrimary()) {
                this.idColumn = temp;
                columns.add(0, temp);
            } else {
                columns.add(temp);
            }
        }
    }

    /**
     * 根据字段名获取column
     *
     * @param name
     * @return
     */
    public ColumnModel getColumnByName(String name) {
        return map.get(name);
    }


    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCreateTable() {
        return isCreateTable;
    }

    public void setCreateTable(boolean createTable) {
        isCreateTable = createTable;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnModel> columns) {
        this.columns = columns;
    }

    public ColumnModel getIdColumn() {
        return idColumn;
    }

    public List<CombineModel> getCombines() {
        return combines;
    }

    public String getParentTableName() {
        return parentTableName;
    }
}
