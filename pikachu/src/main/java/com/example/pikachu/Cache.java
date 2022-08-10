package com.example.pikachu;

import android.graphics.Bitmap;

public interface Cache {
    /**
     * 用于存储最近使用的图像的内存缓存。
     */
    Bitmap get(String key);
}
