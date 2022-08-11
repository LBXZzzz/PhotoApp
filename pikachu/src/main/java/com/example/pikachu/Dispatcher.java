package com.example.pikachu;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    final List<BitmapHunter> batch;
    final Map<String, BitmapHunter> hunterMap;
    //HandlerThread的名字
    private static final String HANDLER_ThREAD = "dispatcherThread";
    //Handler的submit msg
    //提交请求
    static final int REQUEST_SUBMIT = 1;
    //取消请求
    static final int REQUEST_CANCEL = 2;
    static final int HUNTER_COMPLETE = 4;

    static final int HUNTER_DECODE_FAILED = 6;
    static final int HUNTER_DELAY_NEXT_BATCH = 7;
    static final int HUNTER_BATCH_COMPLETE = 8;

    //Picasso 并不是将每个请求的回调,立即切换到主线程,而是每 200 ms 处理一次
    private static final int BATCH_DELAY = 200;
    Dispatcher(Context context, ExecutorService service, Handler mainThreadHandler,
               Cache cache, Stats stats) {
        this.context = context;
        this.dispatcherThread = new HandlerThread(HANDLER_ThREAD);
        dispatcherThread.start();
        this.service = service;
        this.mainThreadHandler = mainThreadHandler;
        this.cache = cache;
        this.stats = stats;
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
        this.hunterMap = new LinkedHashMap<String, BitmapHunter>();
        this.batch = new ArrayList<BitmapHunter>(4);
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
                case HUNTER_COMPLETE: {
                    BitmapHunter hunter = (BitmapHunter) msg.obj;
                    dispatcher.performComplete(hunter);
                    break;
                }
                case HUNTER_DELAY_NEXT_BATCH: {
                    dispatcher.performBatchComplete();
                    break;
                }
            }
        }
    }

    void dispatchFailed(BitmapHunter hunter) {
        handler.sendMessage(handler.obtainMessage(HUNTER_DECODE_FAILED, hunter));
    }

    void dispatchComplete(BitmapHunter hunter) {
        handler.sendMessage(handler.obtainMessage(HUNTER_COMPLETE, hunter));
    }

    //提交了action
    void dispatchSubmit(Action action) {
        handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, action));
    }
    //取消请求
    void dispatchCancel(Action action) {
        handler.sendMessage(handler.obtainMessage(REQUEST_CANCEL, action));
    }
    void performSubmit(Action action) {
        performSubmit(action, true);
    }

    void performSubmit(Action action, boolean dismissFailed) {

        BitmapHunter hunter = hunterMap.get(action.getKey());
        // 根据 Action 对象创建 BitmapHunter 对象, BitmapHunter 实现了 Runnable 接口。
        hunter = BitmapHunter.forRequest(action.getPicasso(), this, cache, stats, action);
        // 将任务提交到线程池。这里有个赋值操作,拿到 Future 对象。目的是为了提供取消任务的功能
        hunter.future = service.submit(hunter);
        hunterMap.put(action.getKey(), hunter);
    }

    void performComplete(BitmapHunter hunter) {
        // 是否存储 Bitmap 到内存当中
        if (MemoryPolicy.shouldWriteToMemoryCache(hunter.getMemoryPolicy())) {
            cache.set(hunter.getKey(), hunter.getResult());
        }
        hunterMap.remove(hunter.getKey());
        batch(hunter);
    }

    void performBatchComplete() {
        // 200 ms 内需要分发的 BitmapHunter
        List<BitmapHunter> copy = new ArrayList<>(batch);
        batch.clear();
        //切换到主线程显示图片
        mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(HUNTER_BATCH_COMPLETE, copy));
        //logBatch(copy);
    }

    private void batch(BitmapHunter hunter) {
        if (hunter.isCancelled()) {
            return;
        }
        batch.add(hunter);
        if (!handler.hasMessages(HUNTER_DELAY_NEXT_BATCH)) {
            handler.sendEmptyMessageDelayed(HUNTER_DELAY_NEXT_BATCH, BATCH_DELAY);
        }
    }

  /*  private void logBatch(List<BitmapHunter> copy) {
        if (copy == null || copy.isEmpty()) return;
        BitmapHunter hunter = copy.get(0);
        Pikachu pikachu= hunter.getPikachu();
        if (pikachu.loggingEnabled) {
            StringBuilder builder = new StringBuilder();
            for (BitmapHunter bitmapHunter : copy) {
                if (builder.length() > 0) builder.append(", ");
                builder.append(Utils.getLogIdsForHunter(bitmapHunter));
            }
            log(OWNER_DISPATCHER, VERB_DELIVERED, builder.toString());
        }
    }*/
}
