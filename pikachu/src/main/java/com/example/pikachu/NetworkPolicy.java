package com.example.pikachu;

public enum NetworkPolicy {
    NO_CACHE(1),

    /**
     * Skips storing the result into the disk cache.
     * <p>
     * <em>Note</em>: At this time this is only supported if you are using OkHttp.
     */
    NO_STORE(1 << 1),

    /**
     * Forces the request through the disk cache only, skipping network.
     */
    OFFLINE(1 << 2);

    final int index;

    NetworkPolicy(int index) {
        this.index = index;
    }
}
