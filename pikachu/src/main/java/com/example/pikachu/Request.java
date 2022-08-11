package com.example.pikachu;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class Request {
    /**
     * Request封装了有关一次图片请求的所有信息
     */

    public final Uri uri;
    int id;
    long started;
    public final int targetWidth;
    public final int targetHeight;
    public final Bitmap.Config config;
    public final boolean centerInside;
    //优先级
    public final Pikachu.Priority priority;
    public final boolean centerCrop;
    public final boolean onlyScaleDown;

    private Request(Uri uri, int targetWidth, int targetHeight, Bitmap.Config config,boolean centerCorp,boolean onlyScaleDown ,boolean centerInside,Pikachu.Priority priority) {
        this.uri = uri;
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
        this.config = config;
        this.centerInside = centerInside;
        this.priority=priority;
        this.centerCrop=centerCorp;
        this.onlyScaleDown = onlyScaleDown;
    }

    public boolean hasSize() {
        return targetWidth != 0 || targetHeight != 0;
    }

    public static final class Builder {
        private Uri uri;
        private int targetWidth;
        private int targetHeight;
        private Bitmap.Config config;
        private boolean centerInside;
        private boolean centerCrop;
        private boolean onlyScaleDown;
        private Pikachu.Priority priority;

        Builder(Uri uri,Bitmap.Config bitmapConfig) {
            this.uri = uri;
            this.config=bitmapConfig;
        }


        private Builder(Request request) {
            uri = request.uri;
            targetWidth = request.targetWidth;
            targetHeight = request.targetHeight;
            centerCrop = request.centerCrop;
            centerInside = request.centerInside;
            onlyScaleDown = request.onlyScaleDown;
            config = request.config;
            priority = request.priority;
        }

        public Builder resize(int targetWidth, int targetHeight) {
            if (targetWidth < 0) {
                throw new IllegalArgumentException("Width must be positive number or 0.");
            }
            if (targetHeight < 0) {
                throw new IllegalArgumentException("Height must be positive number or 0.");
            }
            if (targetHeight == 0 && targetWidth == 0) {
                throw new IllegalArgumentException("At least one dimension has to be positive number.");
            }
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            return this;
        }

        public Builder centerCrop() {
            if (centerInside) {
                throw new IllegalStateException("Center crop can not be used after calling centerInside");
            }
            centerCrop = true;
            return this;
        }

        public Builder setUri(Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("Image URI may not be null.");
            }
            this.uri = uri;
            return this;
        }

        public Request build() {
            if (priority == null) {
                priority = Pikachu.Priority.NORMAL;
            }

            return new Request(uri, targetWidth, targetHeight, config,centerCrop,onlyScaleDown, centerInside,priority);
        }

        public Builder onlyScaleDown() {
            if (targetHeight == 0 && targetWidth == 0) {
                throw new IllegalStateException("onlyScaleDown can not be applied without resize");
            }
            onlyScaleDown = true;
            return this;
        }
    }
    boolean needsTransformation() {
        return needsMatrixTransform() ;
    }
    boolean needsMatrixTransform() {
        return hasSize() ;
    }
    boolean hasImage() {
        return uri != null;
    }
    String getName() {
        return String.valueOf(uri.getPath());
    }
}

