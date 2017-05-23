package com.jiaozhu.ahibernate.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface Combine {
    /**
     * 主表的字段名
     *
     * @return
     */
    String target() default "";
}