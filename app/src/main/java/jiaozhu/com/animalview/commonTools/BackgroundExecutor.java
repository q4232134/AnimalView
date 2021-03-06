package jiaozhu.com.animalview.commonTools;

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
    private static ExecutorService singleExecuteService;// 线性线程池
    private static ExecutorService multiExecuteService;// 同时线程池
    private static BackgroundExecutor backgroundExecutor;
    private static Handler handler = new Handler();

    private BackgroundExecutor() {
        singleExecuteService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setPriority(5);
                thread.setDaemon(false);
                return thread;
            }
        });
        multiExecuteService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(10);
                thread.setDaemon(false);
                return thread;
            }
        });
    }

    public static BackgroundExecutor getInstance() {
        if (backgroundExecutor == null) {
            backgroundExecutor = new BackgroundExecutor();
        }
        return backgroundExecutor;
    }

    /**
     * 在后台运行线程
     *
     * @param callback
     */
    public void runInBackground(final Task callback) {
        runInBackground(callback, true);
    }


    /**
     * 在后台运行线程
     *
     * @param callback
     * @param inSingle 是否运行在同一线程
     */
    public void runInBackground(final Task callback, boolean inSingle) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                callback.runnable();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onBackgroundFinished();
                    }
                });
            }
        };
        if (inSingle) {
            singleExecuteService.execute(r);
        } else {
            multiExecuteService.execute(r);
        }
    }


    /**
     * 任务
     */
    public interface Task {
        /**
         * 运行任务
         */
        void runnable();

        /**
         * 运行完成回调
         */
        void onBackgroundFinished();
    }

}
