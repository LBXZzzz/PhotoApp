package com.example.pikachu;

import android.os.Looper;

import java.util.concurrent.ThreadFactory;

public class Utils {


    static final StringBuilder MAIN_THREAD_KEY_BUILDER = new StringBuilder();
    private static final int KEY_PADDING = 50;
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
}
