package com.example.pikachu;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static android.os.Build.VERSION_CODES.HONEYCOMB_MR1;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadFactory;

public class Utils {

    static final String THREAD_PREFIX = "Picasso-";
    static final StringBuilder MAIN_THREAD_KEY_BUILDER = new StringBuilder();
    private static final int KEY_PADDING = 50;
    private static final int WEBP_FILE_HEADER_SIZE = 12;
    private static final String WEBP_FILE_HEADER_RIFF = "RIFF";
    private static final String WEBP_FILE_HEADER_WEBP = "WEBP";
    static final char KEY_SEPARATOR = '\n';

    static class PikachuThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new PikachuThread(r);
        }
    }


    private static class PikachuThread extends Thread {
        public PikachuThread(Runnable r) {
            super(r);
        }
    }

    static String createKey(Request data) {
        String result = createKey(data, MAIN_THREAD_KEY_BUILDER);
        MAIN_THREAD_KEY_BUILDER.setLength(0);
        return result;
    }

    static String createKey(Request data, StringBuilder builder) {
        String path = data.uri.toString();
        builder.ensureCapacity(path.length() + KEY_PADDING);
        builder.append(path);
        builder.append(KEY_SEPARATOR);
        builder.append("resize:").append(data.targetWidth).append('x').append(data.targetHeight);
        builder.append(KEY_SEPARATOR);
        builder.append("centerCrop").append(KEY_SEPARATOR);
        return builder.toString();
    }

    static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while (-1 != (n = input.read(buffer))) {
            byteArrayOutputStream.write(buffer, 0, n);
        }
        return byteArrayOutputStream.toByteArray();
    }

    static boolean isWebPFile(InputStream stream) throws IOException {
        byte[] fileHeaderBytes = new byte[WEBP_FILE_HEADER_SIZE];
        boolean isWebPFile = false;
        if (stream.read(fileHeaderBytes, 0, WEBP_FILE_HEADER_SIZE) == WEBP_FILE_HEADER_SIZE) {
            // If a file's header starts with RIFF and end with WEBP, the file is a WebP file
            isWebPFile = WEBP_FILE_HEADER_RIFF.equals(new String(fileHeaderBytes, 0, 4, "US-ASCII"))
                    && WEBP_FILE_HEADER_WEBP.equals(new String(fileHeaderBytes, 8, 4, "US-ASCII"));
        }
        return isWebPFile;
    }
    static void closeQuietly(InputStream is) {
        if (is == null) return;
        try {
            is.close();
        } catch (IOException ignored) {
        }
    }

    static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = getService(context, ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && SDK_INT >= HONEYCOMB) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        // Target ~15% of the available heap.
        return 1024 * 1024 * memoryClass / 7;
    }

    static <T> T getService(Context context, String service) {
        return (T) context.getSystemService(service);
    }


    @TargetApi(HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    static int getBitmapBytes(Bitmap bitmap) {
        int result;
        if (SDK_INT >= HONEYCOMB_MR1) {
            result = BitmapHoneycombMR1.getByteCount(bitmap);
        } else {
            result = bitmap.getRowBytes() * bitmap.getHeight();
        }
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + bitmap);
        }
        return result;
    }

    @TargetApi(HONEYCOMB_MR1)
    private static class BitmapHoneycombMR1 {
        static int getByteCount(Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    }
}
