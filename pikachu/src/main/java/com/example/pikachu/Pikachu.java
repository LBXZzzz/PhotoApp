package com.example.pikachu;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Pikachu {

    static final String TAG = "Pikachu";

    static volatile Pikachu singPikachu = null;

    private Context context;
    private ExecutorService service;
    private Cache cache;
    private Listener listener;
    private RequestTransformer transformer;
    private Dispatcher dispatcher;
    final ReferenceQueue<Object> referenceQueue;
    private final List<RequestHandler> requestHandlers;

    public interface RequestTransformer {
        Request transformRequest(Request request);

        RequestTransformer IDENTITY = new RequestTransformer() {
            @Override
            public Request transformRequest(Request request) {
                return request;
            }
        };
    }

    static void checkMain() {
        if (!Utils.isMain()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }
    }

    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public Pikachu(Context context, Dispatcher dispatcher, List<RequestHandler> extraRequestHandlers, ExecutorService service,
                   Cache cache, Listener listener, RequestTransformer transformer) {
        this.context = context;
        this.service = service;
        this.cache = cache;
        this.listener = listener;
        this.transformer = transformer;
        this.dispatcher = dispatcher;
        this.referenceQueue = new ReferenceQueue<Object>();
        int builtInHandlers = 7; // Adjust this as internal handlers are added or removed.
        int extraCount = (extraRequestHandlers != null ? extraRequestHandlers.size() : 0);
        List<RequestHandler> allRequestHandlers =
                new ArrayList<RequestHandler>(builtInHandlers + extraCount);
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

        private Context context;
        private ExecutorService service;
        private Cache cache;
        private Listener listener;
        private RequestTransformer transformer;
        private List<RequestHandler> requestHandlers;

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
                service = new PicassoExecutorService();
            }
            if (transformer == null) {
                transformer = RequestTransformer.IDENTITY;
            }
            Stats stats = new Stats(cache);
            Dispatcher dispatcher = new Dispatcher(context, service, HANDLER, cache, stats);
            return new Pikachu(context, dispatcher, requestHandlers, service, cache, listener, transformer);
        }
    }

    public RequestCreator load(Uri uri) {
        return new RequestCreator(this, uri);
    }

    //取消请求
    public void cancelRequest(Target target) {
        cancelExistingRequest(target);
    }

    private void cancelExistingRequest(Object target) {
        checkMain();
       /* Action action = targetToAction.remove(target);
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
        }*/
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

        private LoadedFrom(int debugColor) {
            this.debugColor = debugColor;
        }
    }

    void enqueueAndSubmit(Action action) {
        Object object = action.getTarget();
        dispatcher.dispatchSubmit(action);
    }

    List<RequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

}
