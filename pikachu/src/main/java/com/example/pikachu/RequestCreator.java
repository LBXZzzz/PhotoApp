package com.example.pikachu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.widget.ImageView;

public class RequestCreator {
    private final Pikachu pikachu;
    private boolean deferred;
    private final Request.Builder data;
    private int memoryPolicy;
    private boolean noFade;
    private int networkPolicy;
    private boolean setPlaceholder = true;
    private Drawable placeholderDrawable;
    private Drawable errorDrawable;
    private int errorResId;
    private int placeholderResId;
    private Object tag;

    RequestCreator(Pikachu pikachu, Uri uri) {
        this.pikachu = pikachu;
        this.data = new Request.Builder(uri,pikachu.defaultBitmapConfig);
    }



    public void into(ImageView target) {
        into(target,null);
    }

    public void into(ImageView target,Callback callback){
        long started = System.nanoTime();
        checkMain();
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }
        if(deferred){
            int width = target.getWidth();
            int height = target.getHeight();
            data.resize(width, height);
        }
        Request request = createRequest(started);
        String requestKey = Utils.createKey(request);
        // 根据缓存策略,判断是否从内存缓存中获取图片。
        if (MemoryPolicy.shouldReadFromMemoryCache(memoryPolicy)) {
            //从内存缓存中获取 Bitmap 对象
            Bitmap bitmap = pikachu.quickMemoryCacheCheck(requestKey);
            if (bitmap != null) {
                //首先取消掉请求,防止异步请求,重新加载图片。
                pikachu.cancelRequest(target);
                PikachuDrawable.setBitmap(target, pikachu.context, bitmap, Pikachu.LoadedFrom.MEMORY, noFade, pikachu.indicatorsEnabled);
                if (callback != null) {
                    callback.onSuccess();
                }
                return;
            }
        }

        Action action = new ImageViewAction(pikachu, target, request, memoryPolicy, networkPolicy, errorResId,
                errorDrawable, requestKey, tag, callback, noFade);
        pikachu.enqueueAndSubmit(action);
    }

    public RequestCreator tag(Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag invalid.");
        }
        if (this.tag != null) {
            throw new IllegalStateException("Tag already set.");
        }
        this.tag = tag;
        return this;
    }

    public RequestCreator fit() {
        deferred = true;
        return this;
    }

    RequestCreator unfit() {
        deferred = false;
        return this;
    }

    public RequestCreator noFade() {
        noFade = true;
        return this;
    }

    public RequestCreator resize(int targetWidth, int targetHeight) {
        data.resize(targetWidth, targetHeight);
        return this;
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

    public RequestCreator error(int errorResId) {
        if (errorResId == 0) {
            throw new IllegalArgumentException("Error image resource invalid.");
        }
        if (errorDrawable != null) {
            throw new IllegalStateException("Error image already set.");
        }
        this.errorResId = errorResId;
        return this;
    }

    public RequestCreator placeholder(Drawable placeholderDrawable) {
        if (!setPlaceholder) {
            throw new IllegalStateException("Already explicitly declared as no placeholder.");
        }
        if (placeholderResId != 0) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    public RequestCreator noPlaceholder() {
        if (placeholderResId != 0) {
            throw new IllegalStateException("Placeholder resource already set.");
        }
        if (placeholderDrawable != null) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        setPlaceholder = false;
        return this;
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

    public RequestCreator networkPolicy(NetworkPolicy policy, NetworkPolicy... additional) {
        if (policy == null) {
            throw new IllegalArgumentException("Network policy cannot be null.");
        }
        this.networkPolicy |= policy.index;
        if (additional == null) {
            throw new IllegalArgumentException("Network policy cannot be null.");
        }
        if (additional.length > 0) {
            for (NetworkPolicy networkPolicy : additional) {
                if (networkPolicy == null) {
                    throw new IllegalArgumentException("Network policy cannot be null.");
                }
                this.networkPolicy |= networkPolicy.index;
            }
        }
        return this;
    }
}
