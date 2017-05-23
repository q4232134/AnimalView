package com.jiaozhu.ahibernate.dao.impl;

import com.jiaozhu.ahibernate.annotation.Column;
import com.jiaozhu.ahibernate.annotation.Id;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.Date;

import static android.text.style.TtsSpan.TYPE_TEXT;
import static com.jiaozhu.ahibernate.type.Type.*;

/**
 * Created by jiaozhu on 2017/3/30.
 */

public class ColumnModel {
    private String name = "";
    private String type = "";
    private boolean notNull = false;
    private boolean uniQue = false;
    private String def = "";
    private int length = 0;
    private Field field;
    private boolean isPrimary = false;

    public ColumnModel(Field field) {
        Column column = field.getAnnotation(Column.class);
        this.name = getColumnName(column.name(), field);
        this.type = getColumnType(column.type(), field.getType());
        this.notNull = column.notNull();
        this.uniQue = column.uniQue();
        this.length = column.length();
        this.field = field;
        this.field.setAccessible(true);
        if (field.isAnnotationPresent(Id.class)) {
            isPrimary = true;
        }
    }

    /**
     * 获取列名
     *
     * @param name  输入列名
     * @param field
     * @return
     */
    private static String getColumnName(String name, Field field) {
        if (name != null && !name.equals("")) return name;
        return field.getName();
    }

    /**
     * 获取列的类型
     *
     * @param type      输入类型
     * @param fieldType 域类型
     * @return
     */
    private static String getColumnType(String type, Class<?> fieldType) {
        if (type != null && !type.equals("")) return type;
        if (String.class == fieldType) {
            return TYPE_STRING;
        }
        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType) || (int.class == fieldType)) {
            return TYPE_INTEGER;
        }
        if ((Long.TYPE == fieldType) || (Long.class == fieldType) || (long.class == fieldType)) {
            return TYPE_INTEGER;
        }
        if ((Float.TYPE == fieldType) || (Float.class == fieldType) || (float.class == fieldType)) {
            return TYPE_FLOAT;
        }
        if ((Short.TYPE == fieldType) || (Short.class == fieldType) || (short.class == fieldType)) {
            return TYPE_INTEGER;
        }
        if ((Double.TYPE == fieldType) || (Double.class == fieldType) || (double.class == fieldType)) {
            return TYPE_FLOAT;
        }
        if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType) || (boolean.class == fieldType)) {
            return TYPE_INTEGER;
        }
        if (Date.class == fieldType) {
            return TYPE_INTEGER;
        }
        if (Blob.class == fieldType) {
            return TYPE_BLOB;
        }
        return TYPE_TEXT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isUniQue() {
        return uniQue;
    }

    public void setUniQue(boolean uniQue) {
        this.uniQue = uniQue;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
}
