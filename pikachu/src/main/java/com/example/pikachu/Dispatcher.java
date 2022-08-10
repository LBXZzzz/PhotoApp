package com.example.pikachu;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class Dispatcher {
    final Context context;
    final HandlerThread dispatcherThread;
    final ExecutorService service;
    final Handler mainThreadHandler;
    final Handler handler;
    final Cache cache;
    final Stats stats;
    final Map<String, BitmapHunter> hunterMap;
    //HandlerThread的名字
    private static final String HANDLER_ThREAD = "dispatcherThread";
    //Handler的submit msg
    static final int REQUEST_SUBMIT = 1;
    static final int HUNTER_COMPLETE = 4;
    static final int HUNTER_DECODE_FAILED = 6;

    Dispatcher(Context context, ExecutorService service, Handler mainThreadHandler,
               Cache cache, Stats stats) {
        this.context = context;
        this.dispatcherThread = new HandlerThread(HANDLER_ThREAD);
        this.service = service;
        this.mainThreadHandler = mainThreadHandler;
        this.cache = cache;
        this.stats = stats;
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.hunterMap = new LinkedHashMap<String, BitmapHunter>();
    }

    private static class DispatcherHandler extends Handler {
        private final Dispatcher dispatcher;

        public DispatcherHandler(Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REQUEST_SUBMIT:
                    Action action = (Action) msg.obj;
                    dispatcher.performSubmit(action);
                    break;
            }
        }
    }

    void dispatchFailed(BitmapHunter hunter) {
        handler.sendMessage(handler.obtainMessage(HUNTER_DECODE_FAILED, hunter));
    }

    void dispatchComplete(BitmapHunter hunter) {
        handler.sendMessage(handler.obtainMessage(HUNTER_COMPLETE, hunter));
    }

    void dispatchSubmit(Action action) {
        handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, action));
    }

    void performSubmit(Action action) {
        performSubmit(action, true);
    }

    void performSubmit(Action action, boolean dismissFailed) {
        BitmapHunter hunter = hunterMap.get(action.getKey());
        hunter = BitmapHunter.forRequest(action.getPicasso(), this, cache, stats, action);
        hunter.future = service.submit(hunter);
        hunterMap.put(action.getKey(), hunter);
    }
}
