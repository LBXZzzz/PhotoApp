package com.example.pikachu;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class Stats {

    final Handler handler;
    final HandlerThread statsThread;
    final Cache cache;
    private static final String STATS_THREAD_NAME = "Stats";
    private static final int CACHE_HIT = 0;
    private static final int CACHE_MISS = 1;

    Stats(Cache cache) {
        this.cache = cache;
        this.statsThread = new HandlerThread(STATS_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        this.statsThread.start();
        this.handler = new StatsHandler(statsThread.getLooper(), this);
    }

    void dispatchCacheHit() {
        handler.sendEmptyMessage(CACHE_HIT);
    }

    void dispatchCacheMiss() {
        handler.sendEmptyMessage(CACHE_MISS);
    }

    private static class StatsHandler extends Handler {

        private final Stats stats;

        public StatsHandler(Looper looper, Stats stats) {
            super(looper);
            this.stats = stats;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case CACHE_HIT:

                    break;
                case CACHE_MISS:

                    break;
                default:
                    Pikachu.HANDLER.post(() -> {
                        throw new AssertionError("Unhandled stats message." + msg.what);
                    });
            }
        }
    }
}
