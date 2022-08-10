package com.example.pikachu;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PicassoExecutorService extends ThreadPoolExecutor {
    //默认线程数为3
    private static final int DEFAULT_THREAD_COUNT = 3;

    public PicassoExecutorService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), new Utils.PikachuThreadFactory());
    }

}
