package com.jiaozhu.ahibernate.dao;

/**
 * Created by jiaozhu on 16/6/30.
 */
public interface ProgressListener {
    /**
     * 进度条
     *
     * @param finished 已完成数量
     * @param total    总共数量
     */
    void onProgress(long finished, long total);
}
