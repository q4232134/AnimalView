package com.jiaozhu.ahibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
public @interface Table {
    /**
     * 表名
     *
     * @return
     */
    String name() default "";

    /**
     * 是否创建实体表
     *
     * @return
     */
    boolean createTable() default true;

    /**
     * 是否创建实体表
     *
     * @return
     */
    Class combine() default Object.class;
}