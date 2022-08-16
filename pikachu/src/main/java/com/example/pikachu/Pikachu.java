package com.example.pikachu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

public class Pikachu {

    static final String TAG = "Pikachu";

    static volatile Pikachu singPikachu = null;

    final Context context;
    final ExecutorService service;
    final Cache cache;
    final Listener listener;
    final RequestTransformer transformer;
    final Dispatcher dispatcher;
    final ReferenceQueue<Object> referenceQueue;
    private final List<RequestHandler> requestHandlers;
    final Bitmap.Config defaultBitmapConfig;
    final Stats stats;
    final Map<Object, Action<?>> targetToAction;
    final Map<ImageView, DeferredRequestCreator> targetToDeferredRequestCreator;
    boolean indicatorsEnabled;

    public interface RequestTransformer {
        Request transformRequest(Request request);

        RequestTransformer IDENTITY = request -> request;
    }

    static void checkMain() {
        if (!Utils.isMain()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }
    }

    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == Dispatcher.HUNTER_BATCH_COMPLETE) {
                @SuppressWarnings("unchecked") List<BitmapHunter> batch = (List<BitmapHunter>) msg.obj;
                for (int i = 0, n = batch.size(); i < n; i++) {
                    BitmapHunter hunter = batch.get(i);
                    hunter.pikachu.complete(hunter);
                }
            }
        }
    };

    void complete(BitmapHunter hunter) {
        Action<?> single = hunter.getAction();
        List<Action<?>> joined = hunter.getActions();

        boolean hasMultiple = joined != null && !joined.isEmpty();
        boolean shouldDeliver = single != null || hasMultiple;

        if (!shouldDeliver) {
            return;
        }

        Uri uri = hunter.getData().uri;
        Exception exception = hunter.getException();
        Bitmap result = hunter.getResult();
        LoadedFrom from = hunter.getLoadedFrom();

        if (single != null) {
            deliverAction(result, from, single);
        }

        if (hasMultiple) {
            for (int i = 0, n = joined.size(); i < n; i++) {
                Action<?> join = joined.get(i);
                deliverAction(result, from, join);
            }
        }

        if (listener != null && exception != null) {
            listener.onImageLoadFailed(this, uri, exception);
        }
    }

    private void deliverAction(Bitmap result, LoadedFrom from, Action<?> action) {
        if (action.isCancelled()) {
            return;
        }
        if (!action.willReplay()) {
            targetToAction.remove(action.getTarget());
        }
        if (result != null) {
            if (from == null) {
                throw new AssertionError("LoadedFrom cannot be null.");
            }
            action.complete(result, from);
        } else {
            action.error();
        }
    }

    public Pikachu(Context context, Dispatcher dispatcher, Stats stats, List<RequestHandler> extraRequestHandlers, ExecutorService service,
                   Cache cache, Listener listener, RequestTransformer transformer, Bitmap.Config defaultBitmapConfig,boolean indicatorsEnabled) {
        this.context = context;
        this.service = service;
        this.cache = cache;
        this.listener = listener;
        this.transformer = transformer;
        this.dispatcher = dispatcher;
        this.referenceQueue = new ReferenceQueue<>();
        this.defaultBitmapConfig = defaultBitmapConfig;
        this.stats = stats;
        this.targetToAction = new WeakHashMap<>();
        this.targetToDeferredRequestCreator = new WeakHashMap<>();
        this.indicatorsEnabled=indicatorsEnabled;
        int builtInHandlers = 7; // Adjust this as internal handlers are added or removed.
        int extraCount = (extraRequestHandlers != null ? extraRequestHandlers.size() : 0);
        List<RequestHandler> allRequestHandlers =
                new ArrayList<>(builtInHandlers + extraCount);
        allRequestHandlers.add(new MediaStoreRequestHandler(context));
        requestHandlers = Collections.unmodifiableList(allRequestHandlers);
    }

    //调用这个方法获取到Pikachu实例
    public static Pikachu with(Context context) {
        if (singPikachu == null) {
            synchronized (Pikachu.class) {
                if (singPikachu == null) {
                    singPikachu = new Builder(context).build();
                }
            }
        }
        return singPikachu;
    }


    public static class Builder {

        final Context context;
        private ExecutorService service;
        private Cache cache;
        private Listener listener;
        private RequestTransformer transformer;
        private List<RequestHandler> requestHandlers;
        private Bitmap.Config defaultBitmapConfig;
        private boolean indicatorsEnabled;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

        public Pikachu build() {
            Context context = this.context;
            if (cache == null) {
                cache = new LruCache(context);
            }
            if (service == null) {
                service = new PikachuExecutorService();
            }
            if (transformer == null) {
                transformer = RequestTransformer.IDENTITY;
            }
            Stats stats = new Stats(cache);
            Dispatcher dispatcher = new Dispatcher(context, service, HANDLER, cache, stats);
            return new Pikachu(context, dispatcher, stats, requestHandlers, service, cache, listener, transformer, defaultBitmapConfig,indicatorsEnabled);
        }

        @Deprecated public Builder debugging(boolean debugging) {
            return indicatorsEnabled(debugging);
        }

        /** Toggle whether to display debug indicators on images. */
        public Builder indicatorsEnabled(boolean enabled) {
            this.indicatorsEnabled = enabled;
            return this;
        }
    }

    public RequestCreator load(Uri uri) {
        return new RequestCreator(this, uri);
    }

    //取消请求
    public void cancelRequest(ImageView target) {
        cancelExistingRequest(target);
    }

    void submit(Action<?> action) {
        dispatcher.dispatchSubmit(action);
    }

    Bitmap quickMemoryCacheCheck(String key) {
        Bitmap cached = cache.get(key);
        if (cached != null) {
            stats.dispatchCacheHit();
        } else {
            stats.dispatchCacheMiss();
        }
        return cached;
    }

    void enqueueAndSubmit(Action<?> action) {
        Object target = action.getTarget();
        //下方代码做了一个判断, Target 已经存在,取消掉对应的请求。
        // 这个判断很重要, 解决了 ListView/RecyclerView 快速滑动, ImageView 复用导致图片错位的问题。
        if (target != null && targetToAction.get(target) != action) {
            cancelExistingRequest(target);
            targetToAction.put(target, action);
        }
        submit(action);
    }

    //取消已经存在的请求
    private void cancelExistingRequest(Object target) {
        checkMain();
        Action<?> action = targetToAction.remove(target);
        if (action != null) {
            action.cancel();
            dispatcher.dispatchCancel(action);
        }
        if (target instanceof ImageView) {
            ImageView targetImageView = (ImageView) target;
            DeferredRequestCreator deferredRequestCreator =
                    targetToDeferredRequestCreator.remove(targetImageView);
            if (deferredRequestCreator != null) {
                deferredRequestCreator.cancel();
            }
        }
    }

    Request transformRequest(Request request) {
        Request transformed = transformer.transformRequest(request);
        if (transformed == null) {
            throw new IllegalStateException("Request transformer "
                    + transformer.getClass().getCanonicalName()
                    + " returned null for "
                    + request);
        }
        return transformed;
    }

    //描述图片是从哪加载的
    public enum LoadedFrom {
        MEMORY(Color.GREEN),
        DISK(Color.BLUE),
        NETWORK(Color.RED);

        final int debugColor;

        LoadedFrom(int debugColor) {
            this.debugColor = debugColor;
        }
    }
    public enum Priority {
        LOW,
        NORMAL,
        HIGH
    }


    List<RequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

}
