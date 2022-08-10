package com.example.pikachu;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Looper;
import android.widget.ImageView;

public class RequestCreator {
    private final Pikachu pikachu;
    private boolean deferred;
    private final Request.Builder data;

    RequestCreator(Pikachu pikachu, Uri uri) {
        this.pikachu = pikachu;
        this.data = new Request.Builder(uri);
    }

    public RequestCreator fit() {
        deferred = true;
        return this;
    }

    public void into(Target target) {
        long started = System.nanoTime();
        checkMain();
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }
        Request request = createRequest(started);
        String requestKey = Utils.createKey(request);
        Action action = new TargetAction(pikachu, request, target, requestKey);
    }

    private Request createRequest(long started) {
        Request request = data.build();
        request.started = started;
        Request transformed = Pikachu.singPikachu.transformRequest(request);
        return transformed;
    }

    //检查是否在主线程
    static void checkMain() {
        if (!isMain()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }
    }

    static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    static BitmapFactory.Options createBitmapOptions(Request data) {
        final boolean justBounds = data.hasSize();
        final boolean hasConfig = data.config != null;
        BitmapFactory.Options options = null;
        if (justBounds || hasConfig) {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = justBounds;
            if (hasConfig) {
                options.inPreferredConfig = data.config;
            }
        }
        return options;
    }
}
