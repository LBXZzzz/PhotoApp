package com.example.pikachu;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

import android.app.ActivityManager;
import android.content.Context;

import java.util.concurrent.ThreadFactory;

public class Utils {

    static class PicassoThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new PicassoThread(r);
        }
    }


    private static class PicassoThread extends Thread {
        public PicassoThread(Runnable r) {
            super(r);
        }
    }

}
