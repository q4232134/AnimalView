package com.jiaozhu.ahibernate.util;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 线程池控制类
 *
 * @author huangxizhou
 */
public class BackgroundExecutor {
    private static ExecutorService executeService;// 线程池
    private static Handler handler = new Handler();

    static {
        executeService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.setDaemon(false);
                return thread;
            }
        });
    }

    /**
     * 在后台运行线程
     *
     * @param callback
     */
    public static void runInBackground(final Task callback) {
        executeService.execute(new Runnable() {
            @Override
            public void run() {
                callback.execute();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onBackgroundFinished();
                    }
                });
            }
        });
    }

    /**
     * 任务
     */
    public interface Task {
        /**
         * 运行任务
         */
        void execute();

        /**
         * 运行完成回调
         */
        void onBackgroundFinished();
    }

}
