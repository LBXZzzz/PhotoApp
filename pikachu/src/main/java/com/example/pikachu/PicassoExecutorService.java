package com.example.pikachu;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PicassoExecutorService extends ThreadPoolExecutor {
    private static final int DEFAULT_THREAD_COUNT=3;
    public PicassoExecutorService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), new Utils.PicassoThreadFactory());
    }

}
