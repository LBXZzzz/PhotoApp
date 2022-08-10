package com.example.pikachu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public abstract class RequestHandler {
    //是否能够处理给定的 Request
    public abstract boolean canHandleRequest(Request data);

    public static final class Result {
        private final Pikachu.LoadedFrom loadedFrom;
        private final Bitmap bitmap;
        private final InputStream stream;
        private final int exifOrientation;

        public Result(Bitmap bitmap, Pikachu.LoadedFrom loadedFrom) {
            this(Utils.checkNotNull(bitmap, "bitmap == null"), null, loadedFrom, 0);
        }

        public Result(InputStream stream, Pikachu.LoadedFrom loadedFrom) {
            this(null, Utils.checkNotNull(stream, "stream == null"), loadedFrom, 0);
        }

        Result(Bitmap bitmap, InputStream stream, Pikachu.LoadedFrom loadedFrom, int exifOrientation) {
            if (!(bitmap != null ^ stream != null)) {
                throw new AssertionError();
            }
            this.bitmap = bitmap;
            this.stream = stream;
            this.loadedFrom = Utils.checkNotNull(loadedFrom, "loadedFrom == null");
            this.exifOrientation = exifOrientation;
        }

        public InputStream getStream() {
            return stream;
        }

        public Pikachu.LoadedFrom getLoadedFrom() {
            return loadedFrom;
        }

        int getExifOrientation() {
            return exifOrientation;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
    //如果 canHandleRequest() 返回值为 true ,那么之后就会执行 load () 方法
    public abstract Result load(Request request, int networkPolicy) throws IOException;

    static void calculateInSampleSize(int reqWidth, int reqHeight, int width, int height,
                                      BitmapFactory.Options options, Request request) {
        int sampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio;
            final int widthRatio;
            if (reqHeight == 0) {
                sampleSize = (int) Math.floor((float) width / (float) reqWidth);
            } else if (reqWidth == 0) {
                sampleSize = (int) Math.floor((float) height / (float) reqHeight);
            } else {
                heightRatio = (int) Math.floor((float) height / (float) reqHeight);
                widthRatio = (int) Math.floor((float) width / (float) reqWidth);
                sampleSize = request.centerInside
                        ? Math.max(heightRatio, widthRatio)
                        : Math.min(heightRatio, widthRatio);
            }
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
    }

    static boolean requiresInSampleSize(BitmapFactory.Options options) {
        return options != null && options.inJustDecodeBounds;
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

    static void calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options,
                                      Request request) {
        calculateInSampleSize(reqWidth, reqHeight, options.outWidth, options.outHeight, options,
                request);
    }
    int getRetryCount() {
        return 0;
    }
}
