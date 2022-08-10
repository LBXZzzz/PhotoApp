package com.example.pikachu;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

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

    private Request(Uri uri, int targetWidth, int targetHeight, Bitmap.Config config, boolean centerInside,Pikachu.Priority priority) {
        this.uri = uri;
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
        this.config = config;
        this.centerInside = centerInside;
        this.priority=priority;
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
        private Pikachu.Priority priority;

        Builder(Uri uri,Bitmap.Config bitmapConfig) {
            this.uri = uri;
            this.config=bitmapConfig;
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
            return new Request(uri, targetWidth, targetHeight, config, centerInside,priority);
        }
    }


    boolean hasImage() {
        return uri != null;
    }
    String getName() {
        return String.valueOf(uri.getPath());
    }
}

