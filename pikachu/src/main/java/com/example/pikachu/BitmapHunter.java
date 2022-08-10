package com.example.pikachu;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Result;

public class BitmapHunter implements Runnable {
    private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();
    Bitmap result;
    final int sequence;
    final Pikachu pikachu;
    final Dispatcher dispatcher;
    final Cache cache;
    final Stats stats;
    final String key;
    final Request data;
    final RequestHandler requestHandler;
    Action action;
    Future<?> future;

    private static final RequestHandler ERRORING_HANDLER = new RequestHandler() {
        @Override
        public boolean canHandleRequest(Request data) {
            return true;
        }

        @Override
        public Result load(Request request, int networkPolicy) throws IOException {
            throw new IllegalStateException("Unrecognized type of request: " + request);
        }
    };

    BitmapHunter(Pikachu pikachu, Dispatcher dispatcher, Cache cache, Stats stats, Action action,
                 RequestHandler requestHandler) {
        this.sequence = SEQUENCE_GENERATOR.incrementAndGet();
        this.pikachu = pikachu;
        this.dispatcher = dispatcher;
        this.cache = cache;
        this.stats = stats;
        this.action = action;
        this.key = action.getKey();
        this.data = action.getRequest();
        this.requestHandler = requestHandler;
    }

    static BitmapHunter forRequest(Pikachu pikachu, Dispatcher dispatcher, Cache cache, Stats stats,
                                   Action action) {
        Request request = action.getRequest();
        List<RequestHandler> requestHandlers = pikachu.getRequestHandlers();

        for (int i = 0, count = requestHandlers.size(); i < count; i++) {
            RequestHandler requestHandler = requestHandlers.get(i);
            if (requestHandler.canHandleRequest(request)) {
                return new BitmapHunter(pikachu, dispatcher, cache, stats, action, requestHandler);
            }
        }

        return new BitmapHunter(pikachu, dispatcher, cache, stats, action, ERRORING_HANDLER);
    }

    @Override
    public void run() {
        result = hunt();
        if (result == null) {
            dispatcher.dispatchFailed(this);
        } else {
            dispatcher.dispatchComplete(this);
        }
    }

    Bitmap hunt() {
        Bitmap bitmap = null;
        //从内存缓存读取bitmap，如果命中则添加计数
        bitmap = cache.get(key);
        if (bitmap != null) {
            stats.dispatchCacheHit();
            return bitmap;
        }
        return bitmap;
    }
}
