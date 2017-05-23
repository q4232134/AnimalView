package com.jiaozhu.ahibernate.dao.impl;

import com.jiaozhu.ahibernate.annotation.Combine;

import java.lang.reflect.Field;

/**
 * Created by jiaozhu on 2017/3/30.
 */

public class CombineModel {
    private Field field;
    private String column = "";
    private String target = "";

    public CombineModel(Field field) {
        this.field = field;
        Combine combine = field.getAnnotation(Combine.class);
        this.column = field.getName();
        this.target = combine.target();
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getColumn() {
        return column;
    }

    public String getTarget() {
        return target;
    }
}
