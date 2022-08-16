package com.example.pikachu;

public enum MemoryPolicy {

    //直接跳过检查内存是否有缓存该图片
    NO_CACHE(1),
    //图片下载之后不在内存中进行缓存
    NO_STORE(1 << 1);

    static boolean shouldReadFromMemoryCache(int memoryPolicy) {
        return (memoryPolicy & MemoryPolicy.NO_CACHE.index) == 0;
    }

    static boolean shouldWriteToMemoryCache(int memoryPolicy) {
        return (memoryPolicy & MemoryPolicy.NO_STORE.index) == 0;
    }

    final int index;

    MemoryPolicy(int index) {
        this.index = index;
    }
}
