package com.example.pikachu;

import android.net.Uri;

public interface Listener {
    void onImageLoadFailed(Pikachu pikachu, Uri uri, Exception exception);
}
