package com.example.pikachu;

import android.graphics.Bitmap;
import android.net.Uri;

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

    private Request(Uri uri, int targetWidth, int targetHeight, Bitmap.Config config, boolean centerInside) {
        this.uri = uri;
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
        this.config = config;
        this.centerInside = centerInside;
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

        Builder(Uri uri) {
            this.uri = uri;
        }

        public Builder setUri(Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("Image URI may not be null.");
            }
            this.uri = uri;
            return this;
        }

        public Request build() {
            return new Request(uri, targetWidth, targetHeight, config, centerInside);
        }
    }


    boolean hasImage() {
        return uri != null;
    }
}

