package com.example.pikachu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Result;

public class BitmapHunter implements Runnable {
    private final static String TAG = "BitmapHunter";
    private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();
    private static final Object DECODE_LOCK = new Object();
    Bitmap result;
    final int sequence;
    final Pikachu pikachu;
    final Dispatcher dispatcher;
    final Cache cache;
    final Stats stats;
    final String key;
    final Request data;
    final RequestHandler requestHandler;
    final int memoryPolicy;
    int networkPolicy;
    Exception exception;
    Pikachu.Priority priority;
    Pikachu.LoadedFrom loadedFrom;
    Action<?> action;
    List<Action<?>> actions;
    int exifRotation; // Determined during decoding of original resource.
    Future<?> future;

    private static final ThreadLocal<StringBuilder> NAME_BUILDER = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(Utils.THREAD_PREFIX);
        }
    };

    private static final RequestHandler ERRORING_HANDLER = new RequestHandler() {
        @Override
        public boolean canHandleRequest(Request data) {
            return true;
        }

        @Override
        public Result load(Request request, int networkPolicy) {
            throw new IllegalStateException("Unrecognized type of request: " + request);
        }
    };

    BitmapHunter(Pikachu pikachu, Dispatcher dispatcher, Cache cache, Stats stats, Action<?> action,
                 RequestHandler requestHandler) {
        this.sequence = SEQUENCE_GENERATOR.incrementAndGet();
        this.pikachu = pikachu;
        this.dispatcher = dispatcher;
        this.cache = cache;
        this.stats = stats;
        this.action = action;
        this.key = action.getKey();
        this.data = action.getRequest();
        this.priority = action.getPriority();
        this.networkPolicy = action.getNetworkPolicy();
        this.memoryPolicy = action.getMemoryPolicy();
        this.requestHandler = requestHandler;
    }

    static BitmapHunter forRequest(Pikachu pikachu, Dispatcher dispatcher, Cache cache, Stats stats,
                                   Action<?> action) {
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
        //更新线程名称
        updateThreadName(data);
        //获取Bitmap对象
        try {
            result = hunt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) {
            Log.d(TAG, "bitmap获取失败");
            dispatcher.dispatchFailed(this);
        } else {
            // bitmap 获取成功
            Log.d(TAG, "bitmap获取成功");
            dispatcher.dispatchComplete(this);
        }
    }

    Bitmap hunt() throws IOException {
        Bitmap bitmap = null;
        //从内存缓存读取bitmap，如果命中则添加计数
        if (MemoryPolicy.shouldReadFromMemoryCache(memoryPolicy)) {
            // 从内存缓存中获取到了图片,直接返回 bitmap 。
            bitmap = cache.get(key);
            if (bitmap != null) {
                stats.dispatchCacheHit();
                loadedFrom = Pikachu.LoadedFrom.MEMORY;
                return bitmap;
            }
        }

        //重点:执行网络请求或者从磁盘加载 Bitmap
        RequestHandler.Result result = requestHandler.load(data, networkPolicy);
        if (result != null) {
            loadedFrom = result.getLoadedFrom();
            exifRotation = result.getExifOrientation();

            bitmap = result.getBitmap();
            if (bitmap == null) {
                InputStream is = result.getStream();
                try {
                    bitmap = decodeStream(is, data);
                } finally {
                    Utils.closeQuietly(is);
                }
            }

        }

        if (bitmap != null) {
            if (data.needsTransformation() || exifRotation != 0) {
                synchronized (DECODE_LOCK) {
                    if (data.needsMatrixTransform() || exifRotation != 0) {
                        bitmap = transformResult(data, bitmap, exifRotation);
                    }
                }
            }
        }
        return bitmap;
    }

    static Bitmap decodeStream(InputStream stream, Request request) throws IOException {
        MarkableInputStream markStream = new MarkableInputStream(stream);
        stream = markStream;

        long mark = markStream.savePosition(65536); // TODO fix this crap.

        final BitmapFactory.Options options = RequestHandler.createBitmapOptions(request);
        final boolean calculateSize = RequestHandler.requiresInSampleSize(options);

        boolean isWebPFile = Utils.isWebPFile(stream);
        markStream.reset(mark);
        // When decode WebP network stream, BitmapFactory throw JNI Exception and make app crash.
        // Decode byte array instead
        if (isWebPFile) {
            byte[] bytes = Utils.toByteArray(stream);
            if (calculateSize) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                RequestHandler.calculateInSampleSize(request.targetWidth, request.targetHeight, options,
                        request);
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } else {
            if (calculateSize) {
                BitmapFactory.decodeStream(stream, null, options);
                RequestHandler.calculateInSampleSize(request.targetWidth, request.targetHeight, options,
                        request);

                markStream.reset(mark);
            }
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
            if (bitmap == null) {
                // Treat null as an IO exception, we will eventually retry.
                throw new IOException("Failed to decode stream.");
            }
            return bitmap;
        }
    }

    static Bitmap transformResult(Request data, Bitmap result, int exifRotation) {
        int inWidth = result.getWidth();
        int inHeight = result.getHeight();
        boolean onlyScaleDown = data.onlyScaleDown;
        int drawX = 0;
        int drawY = 0;
        int drawWidth = inWidth;
        int drawHeight = inHeight;

        Matrix matrix = new Matrix();

        if (data.needsMatrixTransform()) {
            int targetWidth = data.targetWidth;
            int targetHeight = data.targetHeight;

            if (data.centerCrop) {
                float widthRatio = targetWidth / (float) inWidth;
                float heightRatio = targetHeight / (float) inHeight;
                float scaleX, scaleY;
                if (widthRatio > heightRatio) {
                    int newSize = (int) Math.ceil(inHeight * (heightRatio / widthRatio));
                    drawY = (inHeight - newSize) / 2;
                    drawHeight = newSize;
                    scaleX = widthRatio;
                    scaleY = targetHeight / (float) drawHeight;
                } else {
                    int newSize = (int) Math.ceil(inWidth * (widthRatio / heightRatio));
                    drawX = (inWidth - newSize) / 2;
                    drawWidth = newSize;
                    scaleX = targetWidth / (float) drawWidth;
                    scaleY = heightRatio;
                }
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(scaleX, scaleY);
                }
            } else if (data.centerInside) {
                float widthRatio = targetWidth / (float) inWidth;
                float heightRatio = targetHeight / (float) inHeight;
                float scale = Math.min(widthRatio, heightRatio);
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(scale, scale);
                }
            } else if ((targetWidth != 0 || targetHeight != 0) //
                    && (targetWidth != inWidth || targetHeight != inHeight)) {
                // If an explicit target size has been specified and they do not match the results bounds,
                // pre-scale the existing matrix appropriately.
                // Keep aspect ratio if one dimension is set to 0.
                float sx =
                        targetWidth != 0 ? targetWidth / (float) inWidth : targetHeight / (float) inHeight;
                float sy =
                        targetHeight != 0 ? targetHeight / (float) inHeight : targetWidth / (float) inWidth;
                if (shouldResize(onlyScaleDown, inWidth, inHeight, targetWidth, targetHeight)) {
                    matrix.preScale(sx, sy);
                }
            }
        }


        if (exifRotation != 0) {
            matrix.preRotate(exifRotation);
        }

        Bitmap newResult =
                Bitmap.createBitmap(result, drawX, drawY, drawWidth, drawHeight, matrix, true);
        if (newResult != result) {
            result.recycle();
            result = newResult;
        }

        return result;
    }

    private static boolean shouldResize(boolean onlyScaleDown, int inWidth, int inHeight,
                                        int targetWidth, int targetHeight) {
        return !onlyScaleDown || inWidth > targetWidth || inHeight > targetHeight;
    }

    static void updateThreadName(Request data) {
        String name = data.getName();

        StringBuilder builder = NAME_BUILDER.get();
        builder.ensureCapacity(Utils.THREAD_PREFIX.length() + name.length());
        builder.replace(Utils.THREAD_PREFIX.length(), builder.length(), name);

        Thread.currentThread().setName(builder.toString());
    }

    String getKey() {
        return key;
    }

    boolean isCancelled() {
        return future != null && future.isCancelled();
    }

    Request getData() {
        return data;
    }

    Action<?> getAction() {
        return action;
    }

    Pikachu getPikachu() {
        return pikachu;
    }

    Bitmap getResult() {
        return result;
    }


    int getMemoryPolicy() {
        return memoryPolicy;
    }

    List<Action<?>> getActions() {
        return actions;
    }

    Exception getException() {
        return exception;
    }

    Pikachu.LoadedFrom getLoadedFrom() {
        return loadedFrom;
    }

    Pikachu.Priority getPriority() {
        return priority;
    }


}
