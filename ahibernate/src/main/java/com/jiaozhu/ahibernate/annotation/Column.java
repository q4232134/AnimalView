package com.jiaozhu.ahibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface Column {
    /**
     * 列名
     *
     * @return
     */
    String name() default "";

    /**
     * 类型
     *
     * @return
     */
    String type() default "";

    /**
     * 非空列
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * 不可重复
     *
     * @return
     */
    boolean uniQue() default false;

    /**
     * 长度
     *
     * @return
     */
    int length() default 0;
}