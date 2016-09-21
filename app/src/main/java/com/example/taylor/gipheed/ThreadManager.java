package com.example.taylor.gipheed;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Taylor on 9/11/2016.
 */
public class ThreadManager {

    static final ThreadManager sInstance;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static long KEEP_ALIVE_TIME = 2;

    private static BlockingQueue<Runnable> mDecodeWorkQueue;

    static private ThreadPoolExecutor threadPoolExecutor;
    static private Handler handlerUI;

    private static ArrayList<WeakReference<Future>> runningTasks;

    static {
        sInstance = new ThreadManager();
    }

    private ThreadManager() {
        mDecodeWorkQueue = new LinkedBlockingQueue<>();

        threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, TimeUnit.MINUTES, mDecodeWorkQueue);
        handlerUI = new Handler(Looper.getMainLooper());

        runningTasks = new ArrayList<>();
    }

    public static synchronized void RunUI(Runnable task) {
        handlerUI.post(task);
    }

    public static synchronized void Run(Runnable task) {
        RemoveFinishedTasks();
        Future taskFuture = threadPoolExecutor.submit(task);
        runningTasks.add(new WeakReference<Future>(taskFuture));
    }

    public static synchronized void Cancel(Thread thread) {
        synchronized (sInstance) {
            if(thread != null) {
                try {
                    thread.interrupt();
                } catch (Exception e) {

                }
            }
        }
    }

    public static synchronized void CancelAll() {
        synchronized (sInstance) {
            for(WeakReference<Future> task : runningTasks) {
                Future futureTask = task.get();
                if(futureTask != null) {
                    futureTask.cancel(true);
                }
            }
            runningTasks.clear();
        }
    }

    private static synchronized void RemoveFinishedTasks() {
        synchronized (sInstance) {
            for(int i = 0; i < runningTasks.size(); i++) {
                Future task = runningTasks.get(i).get();
                if(task == null || task.isDone() || task.isCancelled()) {
                    runningTasks.remove(i);
                    i--;
                }
            }
        }
    }
}
